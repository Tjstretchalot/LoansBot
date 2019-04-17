package me.timothy.bots.redflags;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import me.timothy.bots.Bot;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.Retryable;
import me.timothy.bots.database.RedFlagUserHistoryCommentMapping;
import me.timothy.bots.database.RedFlagUserHistoryLinkMapping;
import me.timothy.bots.database.RedFlagUserHistorySortMapping;
import me.timothy.bots.models.RedFlag;
import me.timothy.bots.models.RedFlagQueueSpot;
import me.timothy.bots.models.RedFlagReport;
import me.timothy.bots.models.RedFlagUserHistoryComment;
import me.timothy.bots.models.RedFlagUserHistoryLink;
import me.timothy.bots.models.RedFlagUserHistorySort;
import me.timothy.bots.models.Username;
import me.timothy.jreddit.RedditUtils;
import me.timothy.jreddit.info.Comment;
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
		redFlagDetectors.add(new RedFlagForActivityGapDetector());
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
		if(flags == null)
			return;
		
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
		// if we got here we have a started_at and report_id 
		// we may or may not have an after_fullname, but that's fine since we
		// can pass null to start at the beginning (which is what we want if 
		// we haven't started yet)
		if(numRequests == 0) 
			return 0;
		
		RedFlagReport report = database.getRedFlagReportMapping().fetchByID(spot.reportId);
		Username username = database.getUsernameMapping().fetchById(spot.usernameId);
		logger.printf(Level.TRACE, "Continuing queued red flag report on %s (report id = %s)", username.username, report.id);
		int[] requests = new int[] { 0 };
		while(requests[0] < numRequests) {
			logger.trace("Fetching another page of history about " + username.username);
			Listing history = new Retryable<Listing>("continueQueuedRedFlagReport", maybeLoginAgainRunnable) {

				@SuppressWarnings("unchecked")
				@Override
				protected Listing runImpl() throws Exception {
					requests[0]++;
					Listing result;
					try {
						result = RedditUtils.getUserHistory(username.username, "new", null, report.afterFullname, null, 25, bot.getUser());
					}catch(FileNotFoundException exc) {
						result = null;
					}
					if(result == null) {
						logger.trace(username.username + " does not exist!");
						
						JSONObject jObj = new JSONObject();
						JSONObject data = new JSONObject();
						jObj.put("data", data);
						data.put("children", new JSONArray());
						return new Listing(jObj);
					}
					return result;
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
					database.getRedFlagUserHistoryCommentMapping().save(new RedFlagUserHistoryComment(comment, report.id, username.userId));
				}else if(child instanceof Link) {
					Link link = (Link)child;
					if(oldestFullname == null || (link.createdUTC() < oldestRedditUTC)) {
						oldestFullname = link.fullname();
						oldestRedditUTC = link.createdUTC();
					}
					database.getRedFlagUserHistoryLinkMapping().save(new RedFlagUserHistoryLink(link, report.id, username.userId));
				}
			}
			
			if(oldestFullname == null) {
				// we're at the end
				logger.trace("Reached the end of " + username.username + "'s history");
				
				logger.trace("Sorting " + username.username + "'s history for faster processing...");
				long start = System.currentTimeMillis();
				database.getRedFlagUserHistorySortMapping().produceSort(database, report.id);
				long time = System.currentTimeMillis() - start;
				logger.printf(Level.TRACE, "Finished sorting history in %d milliseconds..", time);
				
				logger.trace("Generating red flags...");
				start = System.currentTimeMillis();
				List<IRedFlagDetector> detectors = redFlagDetectors;
				RedFlagUserHistoryCommentMapping cMapping = database.getRedFlagUserHistoryCommentMapping();
				RedFlagUserHistoryLinkMapping lMapping = database.getRedFlagUserHistoryLinkMapping();
				RedFlagUserHistorySortMapping sMapping = database.getRedFlagUserHistorySortMapping();
				
				while(detectors != null) { 
					long sweepStart = System.currentTimeMillis();
					for(IRedFlagDetector detector : detectors) {
						detector.start(username);
					}
					
					RedFlagUserHistorySort next = sMapping.fetchNext(report.id, -1);
					while(next != null) {
						switch(next.table) { 
						case Comment:
							RedFlagUserHistoryComment comment = cMapping.fetchByID(next.foreignId);
							for(IRedFlagDetector detector : detectors) {
								saveFlags(report, detector.parseComment(comment));
							}
							break;
						case Link:
							RedFlagUserHistoryLink link = lMapping.fetchByID(next.foreignId);
							for(IRedFlagDetector detector : detectors) {
								saveFlags(report, detector.parseLink(link));
							}
							break;
						}
						
						next = sMapping.fetchNext(report.id, next.sort);
					}
					
					List<IRedFlagDetector> newDetectors = null;
					for(IRedFlagDetector detector : detectors) {
						saveFlags(report, detector.finish());
						if(detector.requiresResweep()) {
							if(newDetectors == null) {
								newDetectors = new ArrayList<>();
							}
							newDetectors.add(detector);
							
							logger.printf(Level.TRACE, "Finished sweep in %d milliseconds - starting another sweep", System.currentTimeMillis() - sweepStart);
						}
					}
					detectors = newDetectors;
				}
				time = System.currentTimeMillis() - start;
				logger.printf(Level.TRACE, "Finished generating processing red flags in %d milliseconds", time);
				
				logger.trace("Cleaning up red flag temporary table...");
				start = System.currentTimeMillis();
				sMapping.deleteByReport(report.id);
				lMapping.deleteByReportID(report.id);
				cMapping.deleteByReportID(report.id);
				time = System.currentTimeMillis() - start;
				logger.printf(Level.TRACE, "Finished clearing temporary tables in %d milliseconds", time);
				
				
				
				spot.completedAt = new Timestamp(System.currentTimeMillis());
				database.getRedFlagQueueSpotMapping().save(spot);;
				
				report.completedAt = new Timestamp(System.currentTimeMillis());
				database.getRedFlagReportMapping().save(report);
				break;
			}else {
				logger.trace("We found more history (oldest fullname is now " + oldestFullname + " @ " + oldestRedditUTC + ")");
				report.afterFullname = oldestFullname;
				database.getRedFlagReportMapping().save(report);
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
				long msAllowed = Long.valueOf(config.getProperty("red_flags.refresh_ms"));
				Timestamp oneMonthAgo = new Timestamp(System.currentTimeMillis() - msAllowed);
				if(latestReport.completedAt.after(oneMonthAgo)) {
					// it's recent enough!
					spot.startedAt = new Timestamp(System.currentTimeMillis());
					spot.completedAt = new Timestamp(System.currentTimeMillis());
					spot.reportId = latestReport.id;
					database.getRedFlagQueueSpotMapping().save(spot);
					Username username = database.getUsernameMapping().fetchById(spot.usernameId);
					logger.debug("Not generating report on " + username.username + " - have recent enough report");
					return 0;
				}
			}
		}
		
		// no report to use, lets get started
		return startQueuedRedFlagReportWithNoPreviousReports(spot, numRequests);
	}
	
	/**
	 * Starts up a queued red flag report that has no previous report we can use.
	 * 
	 * @param spot the spot in the queue
	 * @param numRequests the approximate number of requests we can make
	 * @return the number of requests we made
	 */
	private int startQueuedRedFlagReportWithNoPreviousReports(RedFlagQueueSpot spot, int numRequests) {
		Username username = database.getUsernameMapping().fetchById(spot.usernameId);
		logger.info("Starting red flag report on " + username.username);
		final long now = System.currentTimeMillis();
		
		spot.startedAt = new Timestamp(now);
		
		RedFlagReport report = new RedFlagReport(-1, spot.usernameId, null, new Timestamp(now), new Timestamp(now), null);
		database.getRedFlagReportMapping().save(report);
		
		spot.reportId = report.id;
		database.getRedFlagQueueSpotMapping().save(spot);
		
		return continueQueuedRedFlagReport(spot, numRequests);
	}
	
	/**
	 * Respond to the given thing with the red flags associated with the given
	 * username id.
	 * 
	 * @param respondTo the thing to respond to
	 * @param usernameId the id of the username to generate a report on
	 */
	public void enqueue(int usernameId) {
		if(config.getProperty("red_flags.suppress").equals("true"))
			return;
		
		database.getRedFlagQueueSpotMapping().save(
				new RedFlagQueueSpot(-1, null, usernameId,
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
