package me.timothy.bots.redflags;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import me.timothy.bots.models.RedFlag;
import me.timothy.bots.models.RedFlagUserHistoryComment;
import me.timothy.bots.models.RedFlagUserHistoryLink;
import me.timothy.bots.models.Username;

/**
 * This was originally going to be based on interquartile range or something along those lines, but
 * even at 97% - 3% * 1.5 + 97% it still had way too many false positives. Instead it's just a flat
 * threshold
 * 
 * @author Timothy
 *
 */
public class RedFlagForActivityGapDetector implements IRedFlagDetector {
	//private static final Logger logger = LogManager.getLogger();
	public final int THRESHOLD_DAYS = 60;
	public final long THRESHOLD = THRESHOLD_DAYS * 86400000l;
	
	private Timestamp lastTimestamp;
	
	@Override
	public void start(Username username) {
	}

	private List<RedFlag> produceRedFlag(Timestamp gapStart, Timestamp gapEnd) {
		DateFormat formatter = SimpleDateFormat.getDateInstance(DateFormat.SHORT);
		return Collections.singletonList(new RedFlag(-1, 1, RedFlag.RedFlagType.ACTIVITY_GAP, 
				String.format("%s - %s", formatter.format(gapStart), formatter.format(gapEnd)), 
				String.format("No activity within this period. Gap exceeded threshold (%d days)", THRESHOLD_DAYS),
				1, new Timestamp(System.currentTimeMillis())));
	}
	
	@Override
	public List<RedFlag> parseComment(RedFlagUserHistoryComment comment) {
		List<RedFlag> result = null;
		if(lastTimestamp != null) {
			long gap = comment.createdAt.getTime() - lastTimestamp.getTime();
			if(gap > THRESHOLD) {
				result = produceRedFlag(lastTimestamp, comment.createdAt);
			}
		}
		
		lastTimestamp = comment.createdAt;
		return result;
	}

	@Override
	public List<RedFlag> parseLink(RedFlagUserHistoryLink link) {
		List<RedFlag> result = null;
		if(lastTimestamp != null) {
			long gap = link.createdAt.getTime() - lastTimestamp.getTime();
			if(gap > THRESHOLD) {
				result = produceRedFlag(lastTimestamp, link.createdAt);
			}
		}
		
		lastTimestamp = link.createdAt;
		return result;
	}

	@Override
	public List<RedFlag> finish() {
		return null;
	}
	
	@Override
	public boolean requiresResweep() {
		return false;
	}

}
