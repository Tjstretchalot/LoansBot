package me.timothy.bots.summon;

import java.util.regex.Pattern;

import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.jreddit.info.Comment;

/**
 * So many people use $loan /u/asdf $100 format that I feel like
 * a little message telling them why it doesn't work is in order.
 * 
 * @author Timothy
 *
 */
public class BadLoanSummon implements CommentSummon {

	private static final Pattern BAD_LOAN_PATTERN = Pattern.compile("\\$loan\\s/u/\\S+\\s" + BotUtils.getDollarAmountPatternString());

	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		if(BAD_LOAN_PATTERN.matcher(comment.body()).find()) {
			LoansDatabase database = (LoansDatabase) db;
			return new SummonResponse(SummonResponse.ResponseType.INVALID, database.getResponseMapping().fetchByName("bad_loan_summon").responseBody);
		}
		return null;
	}

}
