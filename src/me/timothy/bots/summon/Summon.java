package me.timothy.bots.summon;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;

/**
 * Contains information about a summoning. Meant to be initialized
 * once and then reused.
 * 
 * @author Timothy
 */
public abstract class Summon {
	public static final LoanSummon 			LOAN_SUMMON		= new LoanSummon();
	public static final AdvancedLoanSummon 	ADV_LOAN_SUMMON	= new AdvancedLoanSummon();
	public static final PaidSummon			PAID_SUMMON		= new PaidSummon();
	public static final CheckSummon 		CHECK_SUMMON	= new CheckSummon();
	public static final UnpaidSummon		UNPAID_SUMMON	= new UnpaidSummon();
	public static final ConfirmSummon		CONFIRM_SUMMON	= new ConfirmSummon();
	
	public static enum SummonType {
		LOAN("loan"), ADV_LOAN("loan"), PAID("paid"), CHECK("check"), UNPAID("unpaid"), CONFIRM("confirm");
		
		final String summonString;
		
		SummonType(String summonString) {
			this.summonString = summonString;
		}
		
		public String getSummonString() {
			return summonString;
		}
	}
	
	private final SummonType type;
	private Pattern pattern;
	
	public Summon(SummonType type, Pattern pattern) {
		this.type = type;
		this.pattern = pattern;
	}

	public SummonType getType() {
		return type;
	}
	
	protected void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}
	
	protected Pattern getPattern() {
		return pattern;
	}

	/**
	 * Returns the parsable summon text or null
	 * @param text the text
	 * @return the parsable summon text or null if not applicable
	 */
	public String getParseText(String text) {
		Matcher matcher = pattern.matcher(text);
		if(!matcher.find())
			return null;
		return matcher.group();
	}
	
	protected String getUser(String str) {
		return (str.startsWith("/u/") ? str.substring(3) : str).toLowerCase();
	}
	
	protected int getPennies(String number) throws ParseException {
		double amountDollars;
		try {
			amountDollars = Double.valueOf(number);
		}catch(NumberFormatException ex) {
			throw new ParseException(ex.getMessage(), 0);
		}
		return (int) Math.round(amountDollars * 100);
	}
	
	/**
	 * Parses the specified text and sets some valid internal state, or throws
	 * an exception. 
	 * @param doer who did the thing
	 * @param doneTo who the thing might have been done to
	 * @param url the url of the thread where the thing was done
	 * @param text the text to parse
	 * @throws ParseException if the text does not contain a valid summon of this type
	 */
	public abstract void parse(String doer, String doneTo, String url, String text) throws ParseException;
	
	/**
	 * Applies the internal state as changes to the database. If parse was never called
	 * or the last parse failed, this has undefined behavior.
	 * 
	 * @param config the configuration options to use
	 * @param database the database to modify 
	 * @return the response that should be given, if applicable
	 * @throws SQLException if a sql-exception occurs while interacting with the database
	 */
	public abstract String applyChanges(FileConfiguration config, Database database) throws SQLException;
}
