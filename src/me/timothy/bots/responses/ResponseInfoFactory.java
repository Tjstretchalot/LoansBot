package me.timothy.bots.responses;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.timothy.jreddit.info.Comment;

public class ResponseInfoFactory {
	private static final Pattern REPLACEMENT_PATTERN = Pattern.compile("<[^>]*>");
	public static final ResponseInfo base;
	
	static {
		base = new ResponseInfo();
		
		for(int i = 1; i <= 3; i++) {
			base.addLongtermObject("loans" + i, new LoanFormattableObject());
			base.addLongtermObject("applied" + i, new AppliedFormattableObject());
		}
	}
	
	/**
	 * Parses the information contained in the message based on the
	 * format that the message should be in. Returns null if the
	 * format does not match the message
	 * <br>
	 * <br>
	 * The format should be similiar to our response formats; e.g.
	 * <br>
	 * <p><code><pre>$loan &lt;user1&gt; &lt;money1&gt;</pre></code></p>
	 * <br>
	 * Allowed "special" codes:
	 * <ul>
	 *   <li> &lt;user#&gt;  - Parses some text either of the format /u/username or username,
	 *                         then strips the /u/ if it exists. The key is equal to the inside </li>
	 *                         
	 *   <li> &lt;money#&gt; - Parses some number of the form $0.00, 0.00, or 0.00$ then srips the
	 *                         dollar sign and parses. The key is equal to the inside (e.g. money1) </li>
	 * </ul>
	 * 
	 * This should only contain the part of the message that we want parsed and assumes its in the
	 * right format
	 * @param format
	 * @param message
	 * @return
	 */
	public static ResponseInfo getResponseInfo(String format,
				String message) {
		ResponseInfo result = new ResponseInfo(base);
		
		int indexOffsetOfMessage = 0;
		Matcher matcher = REPLACEMENT_PATTERN.matcher(format);
		while(matcher.find()) {
			String group = matcher.group();
			String key = group.substring(1, group.length() - 1);
			
			StringBuilder param = new StringBuilder();
			int delta = 0;
			for(int i = indexOffsetOfMessage + matcher.start(); i < message.length() && message.charAt(i) != ' '; i++) {
				param.append(message.charAt(i));
				delta++;
			}
			indexOffsetOfMessage += delta - group.length();
			
			if(key.startsWith("user")) {
				String username = param.toString();
				if(username.startsWith("/u/"))
					username = username.substring(3);
				result.addTemporaryString(key, username);
			}else if(key.startsWith("money")) {
				String moneyString = param.toString().replace("$", "");
				int amount = (int) Math.round(Double.parseDouble(moneyString) * 100);
				result.addTemporaryObject(key, new MoneyFormattableObject(amount));
			}
		}
		return result;
	}

	public static ResponseInfo getResponseInfo(String format, String message, Comment comment) {
		ResponseInfo result = getResponseInfo(format, message);
		result.addTemporaryString("author", comment.author());
		result.addTemporaryString("body", comment.body());
		result.addTemporaryString("quotable_body", comment.body().replace("\n", "\n>"));
		result.addTemporaryString("created_utc", Double.toString(comment.createdUTC()));
		if(comment.linkAuthor() != null) {
			result.addTemporaryString("link_author", comment.linkAuthor());
			result.addTemporaryString("link_url", comment.linkURL());
		}
		return result;
	}
}
