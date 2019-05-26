package me.timothy.bots.summon;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.summon.patterns.PatternFactory;
import me.timothy.bots.summon.patterns.SummonPattern;
import me.timothy.jreddit.info.Comment;

/**
 * So many people use $loan /u/asdf $100 format that I feel like
 * a little message telling them why it doesn't work is in order.
 * 
 * @author Timothy
 *
 */
public class BadLoanSummon implements CommentSummon {
	private static final SummonPattern BAD_LOAN_PATTERN = new PatternFactory().addCaseInsensLiteral("$loan").addUsername("user1").addMoney("money1").build();


	@Override
	public boolean mightInteractWith(Comment comment, Database db, FileConfiguration config) {
		return BAD_LOAN_PATTERN.matcher(comment.body()).find();
	}
	
	@Override
	public SummonResponse handleComment(Comment comment, Database db, FileConfiguration config) {
		LoansDatabase database = (LoansDatabase) db;
		return new SummonResponse(SummonResponse.ResponseType.INVALID, database.getResponseMapping().fetchByName("bad_loan_summon").responseBody);
	}

}
