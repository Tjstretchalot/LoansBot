package me.timothy.bots.summon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.models.Username;
import me.timothy.bots.redflags.RedFlagsDriver;
import me.timothy.jreddit.info.Link;

/**
 * Post red flags for requests in the subreddit.
 * 
 * @author Timothy
 */
public class RedFlagSummon implements LinkSummon {
	private RedFlagsDriver driver;
	private Logger logger;
	
	public RedFlagSummon() {
		logger = LogManager.getLogger();
	}
	
	/**
	 * Set the red flags driver to use
	 * @param driver the new driver
	 */
	public void setDriver(RedFlagsDriver driver) {
		this.driver = driver;
	}

	@Override
	public boolean mightInteractWith(Link link, Database db, FileConfiguration config) {
		return !link.title().toLowerCase().startsWith("[meta]");
	}

	@Override
	public SummonResponse handleLink(Link link, Database db, FileConfiguration config) {
		LoansDatabase ldb = (LoansDatabase)db;
		String author = link.author();
		if(author == null || author.equals("[deleted]") || author.equalsIgnoreCase(config.getProperty("user.username")))
				return null;
		
		Username uname = ldb.getUsernameMapping().fetchByUsername(author);
		if(uname == null) {
			ldb.getUserMapping().fetchOrCreateByName(author);
			uname = ldb.getUsernameMapping().fetchByUsername(author);
		}
		
		logger.trace("Enqueuing link " + link.title() + " by " + link.author() + " for red flag report");
		driver.enqueue(uname.id);
		return null;
	}

	
}
