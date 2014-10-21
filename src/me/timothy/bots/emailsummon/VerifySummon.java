package me.timothy.bots.emailsummon;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;

import me.timothy.bots.Applicant;
import me.timothy.bots.BotUtils;
import me.timothy.bots.Database;
import me.timothy.bots.FileConfiguration;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;

/**
 * Called to verify that some personal information matches what
 * is in our database. Never gives out personal information in
 * an exploitable manner
 * 
 * @author Timothy
 */
public class VerifySummon implements EmailSummon {
	/**
	 * Matches things like
	 * 
	 * $unpaid /u/John
	 * $unpaid /u/Asdf_Jkl
	 */
	private static final Pattern UNPAID_PATTERN = Pattern.compile("\\s*\\$verify\\s+/u/\\S+\\s+\\S+\\s*\\S+\\s+\"[^\"]+\"\\s+\\S+\\s+\\S+\\s+\\S+");
	private static final Pattern QUOTES_PATTERN = Pattern.compile("\"[^\"]+\"");
	private boolean validSummon;
	
	private String verifyWho;
	private String firstName;
	private String lastName;
	private String streetAddress;
	private String city;
	private String state;
	private String country;
	
	
	public boolean isSummonedBy(String title, String body) {
		Matcher m = UNPAID_PATTERN.matcher(body);
		
		return m.find();
	}
	/* (non-Javadoc)
	 * @see me.timothy.bots.summon.Summon#parse(me.timothy.jreddit.info.Message)
	 */
	@Override
	public void parse(String subject, String body) {
		validSummon = false;
		Matcher m = UNPAID_PATTERN.matcher(body);
		if(!m.find())
			return;
		
		String group = m.group().trim();
		
		Matcher quotesMatcher = QUOTES_PATTERN.matcher(group);
		if(!quotesMatcher.find()) {
			return;
		}
		streetAddress = quotesMatcher.group();
		if(quotesMatcher.find()) {
			return;
		}
		
		group = group.replace(streetAddress, "");
		group = group.replaceAll("\\s+", " ");
		
		String[] split = group.split(" ");
		
		verifyWho = BotUtils.getUser(split[0]);
		firstName = split[1];
		lastName = split[2];
		city = split[3];
		state = split[4];
		country = split[5];
		validSummon = true;
	}

	@Override
	public String applyDatabaseChanges(FileConfiguration config, Database database) {
		LoansFileConfiguration lcf = (LoansFileConfiguration) config;
		if(!validSummon) {
			return lcf.getBadVerifySummon();
		}
		
		LoansDatabase ldb = (LoansDatabase) database;
		
		List<Applicant> applicantsWithThatInfo = ldb.getApplicantsByInfo(firstName, lastName, streetAddress, city, state, country);
		if(applicantsWithThatInfo.size() > 1) {
			LogManager.getLogger().warn("Multiple applicants matching " + firstName + " " + lastName + "!!");
		}
		
		if(applicantsWithThatInfo.size() == 0) {
			return lcf.getGoodVerifySummonDNE();
		}
		
		Applicant a = applicantsWithThatInfo.get(0);
		
		if(a.getUsername().equalsIgnoreCase(verifyWho)) 
			return lcf.getGoodVerifySummonExists();
		else
			return lcf.getGoodVerifySummonDNE();
	}

}
