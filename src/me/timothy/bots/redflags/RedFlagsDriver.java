package me.timothy.bots.redflags;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import me.timothy.bots.Bot;
import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.Retryable;
import me.timothy.bots.models.RedFlag;
import me.timothy.bots.models.RedFlagQueueSpot;
import me.timothy.bots.models.RedFlagReport;
import me.timothy.bots.models.Username;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
import me.timothy.jreddit.RedditUtils;
import me.timothy.jreddit.info.Comment;
import me.timothy.jreddit.info.CommentResponse;
import me.timothy.jreddit.info.Link;
import me.timothy.jreddit.info.Listing;
import me.timothy.jreddit.info.Thing;

/**
 * The red flags driver handles red-flag associated logic and is 
 * called from the main driver.
 * 
 * @author Timothy
 */
public class RedFlagsDriver {
	private static final Logger logger = LogManager.getLogger();
	protected final long briefPauseMS;
	
	protected LoansDatabase database;
	protected LoansFileConfiguration config;
	protected Bot bot;
	protected Runnable maybeLoginAgainRunnable;
	
	protected List<IRedFlagDetector> redFlagDetectors;
	
	/**
	 * Initialize a new driver attached to the given information
	 * @param loansDatabase database
	 * @param loansConfig file configuration
	 * @param loansBot bot
	 * @param maybeLoginAgainRunnable the runnable to try relogging in 
	 */
	public RedFlagsDriver(LoansDatabase loansDatabase, LoansFileConfiguration loansConfig, Bot loansBot, Runnable maybeLoginAgainRunnable,
			long briefPauseMS) {
		this.database = loansDatabase;
		this.config = loansConfig;
		this.bot = loansBot;
		this.maybeLoginAgainRunnable = maybeLoginAgainRunnable;
		this.briefPauseMS = briefPauseMS;
		
		redFlagDetectors = new ArrayList<>();
		redFlagDetectors.add(new RedFlagForSubredditDetector(loansDatabase, loansConfig));
	}
	
	/**
	 * Handle the queue
	 * @param numRequests approximately how many requests you want us to do. If 0 then this does nothing
	 * @return how many requests we actually did
	 * @throws IOException should one occur
	 * @throws ParseException should one occur
	 * @throws java.text.ParseException should one occur
	 */
	public int handleQueue(int numRequests) throws IOException, ParseException, java.text.ParseException {
		int requests = 0;
		if(numRequests < 1)
			return requests;
		if(config.getProperty("red_flags.suppress").equals("true"))
			return requests;
		
		while(requests < numRequests) {
			int requestsPerformed = handleTopOfQueue(numRequests - requests);
			if(requestsPerformed == 0)
				break;
			requests += requestsPerformed;
		}
		
		return requests;
	}
	
	/**
	 * Handle the oldest part of the queue
	 * @param numRequests approximately how many requests you want us to do
	 * @return the number of requests performed
	 */
	private int handleTopOfQueue(int numRequests) {
		RedFlagQueueSpot spot = database.getRedFlagQueueSpotMapping().fetchOldestUncompleted();
		
		if(spot == null)
			return 0;
		
		int requests;
		if(spot.startedAt != null) {
			requests = continueQueuedRedFlagReport(spot, numRequests);
		}else {
			requests = startQueuedRedFlagReport(spot, numRequests);
		}
		
		return requests;
	}
	
	/**
	 * Save the specified flags to the database for the given report after de-duplicating.
	 * 
	 * @param report the report
	 * @param flags the flag
	 */
	private void saveFlags(RedFlagReport report, List<RedFlag> flags) {
		for(RedFlag flag : flags) {
			RedFlag similiar = database.getRedFlagMapping().fetchByReportAndTypeAndIden(report.id, flag.type.databaseIdentifier, flag.identifier);
			if(similiar != null) {
				similiar.count++;
				database.getRedFlagMapping().save(similiar);
			}else {
				flag.reportId = report.id;
				database.getRedFlagMapping().save(flag);
			}
		}
	}
	
	/**
	 * Continue a queued red flag report (it's already been started)
	 * 
	 * @param spot the queue spot
	 * @param numRequests approximate number of requests we can make
	 * @return the number of requests made
	 */
	private int continueQueuedRedFlagReport(RedFlagQueueSpot spot, int numRequests) {
		// if we got here we have a started_at, report_id, and comment_fullname 
		// we may or may not have an after_fullname, but that's fine since we
		// can pass null to start at the beginning (which is what we want if 
		// we haven't started yet)
		if(numRequests == 0) 
			return 0;
		
		RedFlagReport report = database.getRedFlagReportMapping().fetchByID(spot.reportId);
		Username username = database.getUsernameMapping().fetchById(spot.usernameId);
		if(report.afterFullname == null) {
			for(IRedFlagDetector detector : redFlagDetectors) {
				detector.start(username);
			}
		}else {
			for(IRedFlagDetector detector : redFlagDetectors) {
				detector.resume(username);
			}
		}
		int[] requests = new int[] { 0 };
		while(requests[0] < numRequests) {
			logger.trace("Fetching another page of history about " + username.username);
			Listing history = new Retryable<Listing>("continueQueuedRedFlagReport", maybeLoginAgainRunnable) {

				@Override
				protected Listing runImpl() throws Exception {
					requests[0]++;
					
					return RedditUtils.getUserHistory(username.username, "new", null, report.afterFullname, null, 25, bot.getUser());
				}
				
			}.run();
			sleepFor(briefPauseMS);
			
			String oldestFullname = null;
			double oldestRedditUTC = -1;
			for(int i = 0; i < history.numChildren(); i++) {
				Thing child = history.getChild(i);
				if(child instanceof Comment) {
					Comment comment = (Comment)child;
					if(oldestFullname == null || (comment.createdUTC() < oldestRedditUTC)) {
						oldestFullname = comment.fullname();
						oldestRedditUTC = comment.createdUTC();
					}
					for(IRedFlagDetector detector : redFlagDetectors) {
						saveFlags(report, detector.parseComment(comment));
					}
				}else if(child instanceof Link) {
					Link link = (Link)child;
					if(oldestFullname == null || (link.createdUTC() < oldestRedditUTC)) {
						oldestFullname = link.fullname();
						oldestRedditUTC = link.createdUTC();
					}
					for(IRedFlagDetector detector : redFlagDetectors) {
						saveFlags(report, detector.parseLink(link));
					}
				}
			}
			
			if(oldestFullname == null) {
				// we're at the end
				logger.trace("Reached the end of " + username.username + "'s history");
				
				for(IRedFlagDetector detector : redFlagDetectors) {
					saveFlags(report, detector.finish());
				}
				
				spot.completedAt = new Timestamp(System.currentTimeMillis());
				database.getRedFlagQueueSpotMapping().save(spot);;
				
				report.completedAt = new Timestamp(System.currentTimeMillis());
				database.getRedFlagReportMapping().save(report);
				
				String newMessage = RedFlagFormatUtils.formatReport(database, config, report);
				new Retryable<Boolean>("continueQueuedRedFlagReport#edit", maybeLoginAgainRunnable) {

					@Override
					protected Boolean runImpl() throws Exception {
						requests[0]++;
						RedditUtils.edit(bot.getUser(), spot.commentFullname, newMessage);
						return Boolean.TRUE;
					}
					
				}.run();
				break;
			}else {
				logger.trace("We found more history");
				report.afterFullname = oldestFullname;
				database.getRedFlagReportMapping().save(report);
			}
		}
		
		if(spot.completedAt == null) {
			for(IRedFlagDetector detector : redFlagDetectors) {
				detector.pause();
			}
		}
		
		return requests[0];
	}

	/**
	 * Handle starting a queued red flag report.
	 * 
	 * @param spot the queued spot
	 * @param numRequests the approximate number of requests we can make
	 * @return the number of requests made
	 */
	private int startQueuedRedFlagReport(RedFlagQueueSpot spot, int numRequests) {
		// We should determine if we already have a report for this guy
		List<RedFlagReport> existingReports = database.getRedFlagReportMapping().fetchByUsernameID(spot.usernameId);
		if(existingReports.size() != 0) {
			RedFlagReport latestReport = null;
			for(int i = 0; i < existingReports.size(); i++) {
				RedFlagReport rep = existingReports.get(i);
				if(rep.completedAt != null) {
					if (latestReport == null || rep.completedAt.after(latestReport.completedAt)) {
						latestReport = rep;
					}
				}
			}
			
			if(latestReport != null) {
				// we found an already completed report; let's verify that it is fairly recent (last month)
				Timestamp oneMonthAgo = new Timestamp(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 30);
				if(latestReport.completedAt.after(oneMonthAgo)) {
					// it's recent enough!
					return handleQueuedRedFlagReportWithPreviousReport(spot, latestReport);
				}
			}
		}
		
		// okay we don't have a good report to use; lets post a temporary comment to note that we're thinking about this
		
		return startQueuedRedFlagReportWithNoPreviousReports(spot, numRequests);
	}

	/**
	 * Handles a queued red flag report request by posting a summary of a previous report.
	 * 
	 * @param spot the spot in the queue
	 * @param latestReport the available report
	 * @return the number of requests we made
	 */
	private int handleQueuedRedFlagReportWithPreviousReport(RedFlagQueueSpot spot, RedFlagReport latestReport) {
		int[] requests = new int[] { 0 };
		
		logger.debug("Fetching the replyable for responding to " + spot.respondToFullname);
		Thing replyable = new Retryable<Thing>("handleQueuedRedFlagReportWithPreviousReport#getreplyable", maybeLoginAgainRunnable) {
			@Override
			protected Thing runImpl() throws Exception {
				requests[0]++;
				return RedditUtils.getThing(spot.respondToFullname, bot.getUser());
			}
		}.run();
		sleepFor(briefPauseMS);
		
		String response = RedFlagFormatUtils.formatReport(database, config, latestReport);
		logger.info("Posting old red flag report about " + latestReport.usernameId + " in response to " + spot.respondToFullname);
		new Retryable<Boolean>("handleQueuedRedFlagReportWithPreviousReport#reply", maybeLoginAgainRunnable) {

			@Override
			protected Boolean runImpl() throws Exception {
				requests[0]++;
				bot.respondTo(replyable, response);
				return Boolean.TRUE;
			}
			
		}.run();
		sleepFor(briefPauseMS);
		
		return requests[0];
	}
	
	/**
	 * Starts up a queued red flag report that has no previous report we can use.
	 * 
	 * @param spot the spot in the queue
	 * @param numRequests the approximate number of requests we can make
	 * @return the number of requests we made
	 */
	private int startQueuedRedFlagReportWithNoPreviousReports(RedFlagQueueSpot spot, int numRequests) {
		// we're going to post a comment
		final long now = System.currentTimeMillis();
		
		
		int[] requests = new int[] { 0 };
		ResponseInfo respInfo = new ResponseInfo(ResponseInfoFactory.base);
		respInfo.addTemporaryString("user", database.getUsernameMapping().fetchById(spot.usernameId).username);
		respInfo.addTemporaryString("started_at", LoansBotUtils.formatDate(new Timestamp(now)));
		
		String format = database.getResponseMapping().fetchByName("red_flags_placeholder").responseBody;
		String message = new ResponseFormatter(format, respInfo).getFormattedResponse(config, database);
		logger.info("Posting placeholder red flag report in response to " + spot.respondToFullname);
		CommentResponse cr = new Retryable<CommentResponse>("startQueuedRedFlagReportWithNoPreviousReports#reply", maybeLoginAgainRunnable) {

			@Override
			protected CommentResponse runImpl() throws Exception {
				requests[0]++;
				CommentResponse resp = RedditUtils.comment(bot.getUser(), spot.respondToFullname, message);
				
				if(resp.getErrors() != null && resp.getErrors().size() > 0) {
					List<?> errors = resp.getErrors();
					
					logger.trace("Got errors for reply to " + spot.respondToFullname + ": " + errors.toString());
					
					for(Object o : errors) {
						if(o instanceof List) {
							List<?> error = (List<?>) o;
							for(Object o2 : error) {
								if(o2 instanceof String) {
									String errorMessage = (String) o2;
									
									if(errorMessage.equals("TOO_OLD")) {
										logger.trace("TOO_OLD error => response was a success (for our purposes)");
										return resp;
									}
								}
							}
						}
					}
					return null;
				}
				
				return resp;
			}
			
		}.run();
		
		if(cr.getErrors() != null && cr.getErrors().size() > 0) {
			// we got the TOO_OLD thing; we're just gonna mark this as complete
			spot.startedAt = new Timestamp(now);
			spot.completedAt = new Timestamp(now);
			database.getRedFlagQueueSpotMapping().save(spot);
			return requests[0];
		}
		
		// ok we got a real comment response. we're going to save that comment & start the report
		Comment ourComment = cr.getComment();
		spot.commentFullname = ourComment.fullname();
		spot.startedAt = new Timestamp(now);
		
		RedFlagReport report = new RedFlagReport(-1, spot.usernameId, null, new Timestamp(now), new Timestamp(now), null);
		database.getRedFlagReportMapping().save(report);
		
		spot.reportId = report.id;
		database.getRedFlagQueueSpotMapping().save(spot);
		
		requests[0] += continueQueuedRedFlagReport(spot, numRequests - requests[0]);
		return requests[0];
	}
	
	/**
	 * Respond to the given thing with the red flags associated with the given
	 * username id.
	 * 
	 * @param respondTo the thing to respond to
	 * @param usernameId the id of the username to generate a report on
	 */
	public void enqueue(Thing respondTo, int usernameId) {
		if(config.getProperty("red_flags.suppress").equals("true"))
			return;
		
		database.getRedFlagQueueSpotMapping().save(
				new RedFlagQueueSpot(-1, null, usernameId, respondTo.fullname(), null, 
						new Timestamp(System.currentTimeMillis()), null, null));
	}

	/**
	 * Sleeps for the specified time in milliseconds, as if by
	 * {@code Thread.sleep(ms)}. Logs the exception and terminates
	 * the program on error.
	 * 
	 * @param ms the time in milliseconds to sleep
	 */
	protected void sleepFor(long ms) {
		try {
			logger.trace("Sleeping for " + ms + " milliseconds");
			Thread.sleep(ms);
		} catch (InterruptedException ex) {
			logger.error(ex);
			throw new RuntimeException("interrupted", ex);
		}
	}
}
