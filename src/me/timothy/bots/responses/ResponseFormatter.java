package me.timothy.bots.responses;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formats responses using a response info!
 * 
 * @author Timothy
 */
public class ResponseFormatter {
	private static final Pattern REPLACEMENT_PATTERN = Pattern.compile("<[^>]*>");
	private String format;
	private ResponseInfo info;
	
	/**
	 * Prepares a response formatter with the specified
	 * information to use
	 * @param format the format
	 * @param info the info
	 */
	public ResponseFormatter(String format, ResponseInfo info) {
		this.format = format;
		this.info = info;
	}
	
	/**
	 * Parses the current state of info and 
	 * gets the nice pretty response
	 * 
	 * @return the response
	 */
	public String getFormattedResponse() {
		Matcher matcher = REPLACEMENT_PATTERN.matcher(format);
		
		StringBuilder response = new StringBuilder();
		int indexThatResponseIsUpToInFormat = 0;
		while(matcher.find()) {
			int startIndexInFormatOfThisGroup = matcher.start();
			
			String whatToReplace = matcher.group();
			String keyToReplace = whatToReplace.substring(1, whatToReplace.length() - 1);
			String whatToReplaceWith = info.getObject(keyToReplace).toFormattedString(keyToReplace, info);
			
			String theInbetweenText = format.substring(indexThatResponseIsUpToInFormat, startIndexInFormatOfThisGroup);
			
			response.append(theInbetweenText).append(whatToReplaceWith);
			indexThatResponseIsUpToInFormat = matcher.end();
		}
		response.append(format.substring(indexThatResponseIsUpToInFormat));
		return response.toString();
	}
}
