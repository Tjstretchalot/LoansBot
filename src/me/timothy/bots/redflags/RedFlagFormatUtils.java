package me.timothy.bots.redflags;

import java.util.List;

import me.timothy.bots.LoansBotUtils;
import me.timothy.bots.LoansDatabase;
import me.timothy.bots.LoansFileConfiguration;
import me.timothy.bots.models.RedFlag;
import me.timothy.bots.models.RedFlagReport;
import me.timothy.bots.responses.ResponseFormatter;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;

/**
 * Helps with formatting red flag reports
 * 
 * @author Timothy
 */
public class RedFlagFormatUtils {
	
	/**
	 * Take the response info generated from {@link RedFlagFormatUtils#getResponseInfo(LoansDatabase, LoansFileConfiguration, RedFlagReport)}
	 * and actually format it
	 * 
	 * @param db the database
	 * @param config the configuration
 	 * @param report the report to format
	 * @return the formatted markdown
	 */
	public static String formatReport(LoansDatabase db, LoansFileConfiguration config, RedFlagReport report) {
		String format = db.getResponseMapping().fetchByName("red_flag_report").responseBody;
		
		return new ResponseFormatter(format, getResponseInfo(db, config, report)).getFormattedResponse(config, db);
	}
	
	/**
	 * Get the response info corresponding with the given report. Handles the red flags sub-responses
	 * 
	 * @param db the database
	 * @param config the file configuration
	 * @param report the report
	 * @return the response info to use for the red flag report
	 */
	public static ResponseInfo getResponseInfo(LoansDatabase db, LoansFileConfiguration config, RedFlagReport report) {
		List<RedFlag> flags = db.getRedFlagMapping().fetchByReportID(report.id);
		
		ResponseInfo result = new ResponseInfo(ResponseInfoFactory.base);
		
		result.addTemporaryString("user", db.getUsernameMapping().fetchById(report.usernameId).username);
		result.addTemporaryString("created_at", LoansBotUtils.formatDate(report.createdAt));
		result.addTemporaryString("started_at", LoansBotUtils.formatDate(report.startedAt));
		result.addTemporaryString("completed_at", LoansBotUtils.formatDate(report.completedAt));
		
		if(flags.size() == 0) {
			result.addTemporaryString("flags", db.getResponseMapping().fetchByName("red_flags_list_no_flags").responseBody);
		}else {
			result.addTemporaryString("flags", formatRedFlags(db, config, flags));
		}
		
		return result;
	}
	
	
	/**
	 * Formats the set of flags using the red_flags configuration. Generates a table which has the headers
	 * from the red_flag_table_headers response and rows from the red_flag_table_row response.
	 * 
	 * @param db the database
	 * @param config the file configuration 
	 * @param flags the flags to format as a list
	 * @return the formatted markdown
	 */
	public static String formatRedFlags(LoansDatabase db, LoansFileConfiguration config, List<RedFlag> flags) {
		String header = db.getResponseMapping().fetchByName("red_flag_table_headers").responseBody;
		
		String rowFormat = db.getResponseMapping().fetchByName("red_flag_table_row").responseBody;
		
		StringBuilder result = new StringBuilder(header);
		for(RedFlag flag : flags) {
			ResponseInfo info = getResponseInfo(flag);
			String row = new ResponseFormatter(rowFormat, info).getFormattedResponse(config, db);
			result.append(row);
		}
		
		return result.toString();
	}
	
	/**
	 * Get the response info for the specific red flag given. Should be passed to the red_flag_table_row response
	 * 
	 * @param flag the red flag
	 * @return the corresponding response info
	 */
	public static ResponseInfo getResponseInfo(RedFlag flag) {
		ResponseInfo result = new ResponseInfo(ResponseInfoFactory.base);
		
		result.addTemporaryString("type", flag.type.name());
		result.addTemporaryString("description", flag.description);
		result.addTemporaryString("count", Integer.toString(flag.count));
		
		return result;
	}
}
