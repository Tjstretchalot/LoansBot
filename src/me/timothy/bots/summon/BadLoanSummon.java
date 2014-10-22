package me.timothy.bots.summon;

import java.util.regex.Pattern;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.jreddit.info.Comment;

/**
 * So many people use $loan /u/asdf $100 format that I feel like
 * a little message telling them why it doesn't work is in order.
 * 
 * @author Timothy
 *
 */
public class BadLoanSummon implements CommentSummon {

	private static final Pattern BAD_LOAN_PATTERN = Pattern.compile("\\$loan\\s/u/\\S+\\s\\$?\\d+\\.?\\d*\\$?");
	
	@Override
	public String applyChanges(FileConfiguration config, Database database) {
		LoansFileConfiguration lfc = (LoansFileConfiguration) config;
		
		return lfc.getBadLoanSummon();
	}

	@Override
	public boolean parse(Comment comment) {
		return BAD_LOAN_PATTERN.matcher(comment.body()).find();
	}

}