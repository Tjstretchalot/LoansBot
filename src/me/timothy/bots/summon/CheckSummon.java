package me.timothy.bots.summon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.Loan;
import me.timothy.bots.models.User;
import me.timothy.bots.models.Username;
import me.timothy.bots.responses.GenericFormattableObject;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;
import me.timothy.bots.summon.patterns.PatternFactory;
import me.timothy.bots.summon.patterns.SummonMatcher;
import me.timothy.bots.summon.patterns.SummonPattern;
import me.timothy.jreddit.info.Comment;
import me.timothy.jreddit.info.Link;

/**
 * A summon for checking all loans made by a user
 * 
 * @author Timothy
 */
public class CheckSummon implements CommentSummon, LinkSummon {
	/**
	 * Matches things like
	 * 
	 * $check /u/John $check /u/Asdf_Jkl
	 */
	private static final SummonPattern CHECK_PATTERN = new PatternFactory().addCaseInsensLiteral("$check").addUsername("user1").build();

	private Logger logger;

	public CheckSummon() {
		logger = LogManager.getLogger();
	}

	@Override
	public boolean mightInteractWith(Link link, Database db, FileConfiguration config) {
		String title = link.title();
		if(title.toUpperCase().startsWith("[META]"))
			return false;
		if(title.toUpperCase().startsWith("[REQ]"))
			return true;
		
		LoansDatabase database = (LoansDatabase)db;
		Username authorUname = database.getUsernameMapping().fetchByUsername(link.author());
		if(authorUname == null)
			return true;
		
		return !database.getResponseOptOutMapping().contains(authorUname.userId);
	}
	
	@Override
	public SummonResponse handleLink(Link submission, Database db, FileConfiguration config) {
		LoansDatabase database = (LoansDatabase) db;

		ResponseInfo respInfo = new ResponseInfo(ResponseInfoFactory.base);
		String checked = submission.author();
		User checkedUser = database.getUserMapping().fetchOrCreateByName(checked);
		if(checkedUser == null) {
			respInfo.addTemporaryString("user1 id", Integer.toString(-1));
		}else {
			respInfo.addTemporaryString("user1 id", Integer.toString(checkedUser.id));
		}
		
		
		respInfo.addTemporaryObject("author", new GenericFormattableObject(checked));
		respInfo.addTemporaryObject("user1", new GenericFormattableObject(checked));
		
		ResponseFormatter formatter = new ResponseFormatter(database.getResponseMapping().fetchByName("check").responseBody, respInfo);

		List<PMResponse> pmResponses = new ArrayList<>();
		List<Loan> loansAsBorrower = database.getLoanMapping().fetchWithBorrowerAndOrLender(checkedUser.id, -1, false);
		Set<Integer> uniqueLenders = new HashSet<>();
		for(Loan loan : loansAsBorrower) {
			if(loan.borrowerId != checkedUser.id)
				continue;
			if(loan.principalCents == loan.principalRepaymentCents)
				continue;
			if(loan.lenderId == checkedUser.id)
				continue;
			if(loan.deleted || loan.unpaid)
				continue;
			
			uniqueLenders.add(loan.lenderId);
		}
		
		if(uniqueLenders.size() > 0) {
			String pmTitleFmt = database.getResponseMapping().fetchByName("borrower_req_pm_title").responseBody;
			String pmBodyFmt = database.getResponseMapping().fetchByName("borrower_req_pm_body").responseBody;
			
			ResponseInfo pmRespInfo = new ResponseInfo(ResponseInfoFactory.base);
			pmRespInfo.addLongtermString("borrower", checked);
			pmRespInfo.addLongtermString("borrower id", Integer.toString(checkedUser.id));
			pmRespInfo.addLongtermString("thread", submission.permalink());
			
			for(Integer lenderId : uniqueLenders) {
				Username lenderUname = database.getUsernameMapping().fetchByUserId(lenderId).get(0);
				if(database.getBorrowerReqPMOptOutMapping().contains(lenderId.intValue())) {
					logger.printf(Level.DEBUG, "Notification for %s due to %s making a request thread suppressed - opt out",
								  lenderUname.username, checked);
					continue;
				}
				
				User lender = database.getUserMapping().fetchById(lenderId);
				
				pmRespInfo.addTemporaryString("lender", lenderUname.username);
				pmRespInfo.addTemporaryString("lender id", Integer.toString(lender.id));
				
				String pmTitle = new ResponseFormatter(pmTitleFmt, pmRespInfo).getFormattedResponse(config, db);
				String pmBody = new ResponseFormatter(pmBodyFmt, pmRespInfo).getFormattedResponse(config, db);
				
				logger.printf(Level.DEBUG, "Notifying %s due to %s making a request thread", lenderUname.username, checked);
				pmResponses.add(new PMResponse(lenderUname.username, pmTitle, pmBody));
				
				pmRespInfo.clearTemporary();
			}
		}
		
		
		logger.printf(Level.DEBUG, "%s posted a non-meta submission and recieved a check", respInfo.getObject("author").toString());
		return new SummonResponse(
				SummonResponse.ResponseType.VALID, 
				formatter.getFormattedResponse(config, (LoansDatabase) db),
				null,
				pmResponses
				);
	}

	@Override
	public boolean mightInteractWith(Comment comment, Database db, FileConfiguration config) {
		return CHECK_PATTERN.matcher(comment.body()).find();
	}
	
	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		if(comment.author().equalsIgnoreCase(config.getProperty("user.username"))) {
			return null;
		}
		
		SummonMatcher matcher = CHECK_PATTERN.matcher(comment.body());
		
		if(matcher.find()) {
			LoansDatabase database = (LoansDatabase) db;
			
			ResponseInfo respInfo = matcher.group();
			String author = comment.author();
			ResponseInfoFactory.addCommentDetails(respInfo, comment);
			String checked = respInfo.getObject("user1").toString();
			logger.printf(Level.INFO, "%s requested a check on %s", author, checked);

			User checkedUser = database.getUserMapping().fetchOrCreateByName(checked);
			if(checkedUser == null) {
				respInfo.addTemporaryString("user1 id", Integer.toString(-1));
			}else {
				respInfo.addTemporaryString("user1 id", Integer.toString(checkedUser.id));
			}
			
			ResponseFormatter respFormatter = new ResponseFormatter(database.getResponseMapping().fetchByName("check").responseBody, respInfo);
			return new SummonResponse(SummonResponse.ResponseType.VALID, respFormatter.getFormattedResponse(config, database));
		}
		
		return null;
	}


}
