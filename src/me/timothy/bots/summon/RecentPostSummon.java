package me.timothy.bots.summon;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.RecentPost;
import me.timothy.bots.summon.SummonResponse.ResponseType;
import me.timothy.jreddit.info.Link;

/**
 * Reports posts by users if they post more than once every 24 hours.
 * 
 * @author Timothy
 */
public class RecentPostSummon implements LinkSummon {

	private Logger logger;
	
	public RecentPostSummon()
	{
		logger = LogManager.getLogger();
	}
	
	@Override
	public SummonResponse handleLink(Link link, Database db, FileConfiguration config) {
		if(!link.title().contains("[REQ]"))
			return null;
		
		LoansDatabase database = (LoansDatabase) db;
		
		long createdUTC = (long)(link.createdUTC() * 1000);
		RecentPost rPost = new RecentPost(-1, link.author().toLowerCase(), link.subreddit().toLowerCase(), new Timestamp(createdUTC), new Timestamp(createdUTC));
		database.getRecentPostMapping().save(rPost);
		
		List<RecentPost> recentPostsByAuthor = database.getRecentPostMapping().fetchByUsername(link.author().toLowerCase());
		
		Date yesterday = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
		int numRecent = 0;
		for(RecentPost rp : recentPostsByAuthor)
		{
			if(rp.createdAt.after(yesterday))
				numRecent++;
		}
		
		if(numRecent > 1)
		{
			logger.printf(Level.INFO, "Reporting link fullname=%s from author=%s (numRecent = %d)", link.fullname(), link.author(), numRecent);
			String repMess = database.getResponseMapping().fetchByName("too_recent_request_report").responseBody;
			return new SummonResponse(ResponseType.SILENT, null, null, null, repMess);
		}
		
		return null;
	}

}
