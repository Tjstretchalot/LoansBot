package me.timothy.bots.summon.patterns;

import java.util.ArrayList;
import java.util.List;

import me.timothy.bots.responses.FormattableObject;
import me.timothy.bots.responses.ResponseInfo;
import me.timothy.bots.responses.ResponseInfoFactory;

/**
 * This handles the actual matching once given a pattern, for a specific string.
 * This architecture is used to make going between this api and the regex api
 * easier.
 * 
 * @author Timothy Moore
 */
public class SummonMatcher {
	/**
	 * The string that we're trying to find matches on
	 */
	private String string;
	
	/**
	 * The underlying pattern that we are trying to match.
	 */
	private List<ISummonToken> tokens;
	
	/**
	 * Where we are in the string so far.
	 */
	private int currentIndex;
	
	/**
	 * Where the current match started (-1 if no recent match)
	 */
	private int currentMatchStart;
	
	/**
	 * Where the current match ended
	 */
	@SuppressWarnings("unused")
	private int currentMatchEnd;
	
	/**
	 * The tokens that were actually matched in the current match.
	 */
	private List<ISummonToken> matchedTokens;
	
	/**
	 * Create a new matcher for the given tokens in the given string.
	 * 
	 * @param tokens the tokens
	 * @param string the string
	 */
	public SummonMatcher(List<ISummonToken> tokens, String string) {
		boolean foundNonOptional = false;
		for(ISummonToken token : tokens) {
			if(!token.isOptional()) {
				foundNonOptional = true;
				break;
			}
		}
		if(!foundNonOptional)
			throw new IllegalArgumentException("Without non-optional tokens this is an infinite loop!");
		
		this.tokens = tokens;
		this.string = string;
		
		currentIndex = 0;
		currentMatchStart = -1;
		currentMatchEnd = -1;
	}
	
	/**
	 * Tries to find the match in the string that hasn't been found before.
	 * 
	 * @return if a match was found
	 */
	public boolean find() {
		matchedTokens = new ArrayList<>();
		
		while(true) {
			if(currentIndex >= string.length())
				break;
			if(!skipWhitespace())
				break;
			
			currentMatchStart = currentIndex;
			
			matchedTokens.clear();
			boolean success = true;
			for(int currTokInd = 0; currTokInd < tokens.size(); currTokInd++) {
				ISummonToken token = tokens.get(currTokInd);
				
				if(!token.isOptional()) {
					if(!tryMatch(token)) {
						success = false;
						skipToken();
						break;
					}
					matchedTokens.add(token);
					skipWhitespace();
				}else {
					int startInd = currentIndex;
					if(!tryMatch(token)) {
						currentIndex = startInd;
					}else {
						matchedTokens.add(token);
						skipWhitespace();
					}
				}
			}
			
			if(success) {
				currentMatchEnd = currentIndex;
				return true;
			}
		}
		
		matchedTokens = null;
		currentMatchStart = -1;
		currentMatchEnd = -1;
		return false;
	}
	
	/**
	 * Get the group that was just found using find. This parses the tokens into formattable
	 * objects and then assigns them to the map using their id as the key. These are all put
	 * in as temporary objects, but you can call {@link #group(boolean)} to change that behavior.
	 * 
	 * @return the group that was just found
	 */
	public ResponseInfo group() {
		return group(false);
	}
	
	/**
	 * Get the group that was just found using find. May optionally use long-term objects
	 * to perform the grouping.
	 * 
	 * @param longTerm true if long term objects should be used, false for temporary
	 * @return the response info
	 */
	public ResponseInfo group(boolean longTerm) {
		if(currentMatchStart < 0)
			throw new IllegalStateException("There is no current match!");
		
		ResponseInfo result = new ResponseInfo(ResponseInfoFactory.base);
		
		int[] indByRef = new int[] { currentMatchStart };
		
		currentMatchStart = -1;
		currentMatchEnd = -1;
		
		for(int i = 0; i < matchedTokens.size(); i++) {
			if(i != 0)
				skipWhitespace(indByRef);
			
			ISummonToken token = matchedTokens.get(i);
			FormattableObject standard = getMatch(token, indByRef);
			if(standard != null) {
				if(longTerm)
					result.addLongtermObject(token.id(), standard);
				else
					result.addTemporaryObject(token.id(), standard);
			}
		}
		
		return result;
	}
	
	/**
	 * Until the currentIndex is at the end of the string or 
	 * it doesn't correspond with a whitespace character, 
	 * increment current index.
	 * 
	 * @return true if we found a non-whitespace character, false otherwise
	 */
	private boolean skipWhitespace() {
		if(currentIndex >= string.length())
			return false;
		
		while(Character.isWhitespace(string.charAt(currentIndex))) {
			if(currentIndex == string.length() - 1)
				return false;
			
			currentIndex++;
		}
		return true;
	}
	
	/**
	 * Skip whitespace in the parsing section, throwing helpful errors if anything
	 * fails, rather than returning anything.
	 * 
	 * @param ind the array with one element which is the index to search at, modified
	 * to the next non-whitespace character.
	 */
	private void skipWhitespace(int[] ind) {
		if(ind[0] >= string.length())
			throw new IllegalStateException("already at end of string");
		
		while(Character.isWhitespace(string.charAt(ind[0]))) {
			if(ind[0] == string.length() - 1)
				throw new IllegalStateException("whitespace took us to end of string");
			
			ind[0]++;
		}
	}
	
	/**
	 * Skip the current token by incrementing currentIndex until
	 * we are at a token end delimiter.
	 */
	private void skipToken() {
		while(!isTokenEnd()) {
			currentIndex++;
		}
	}
	
	/**
	 * <p>Attempts to match the current token to the current non-whitespace
	 * location in the string. Increments the current index so that it 
	 * ends just after the last processed character, regardless of success
	 * or failure.</p>
	 * 
	 * <p>Note that if this doesn't end on a whitespace character, than it
	 * is inside a token. The remainder of the token can be safely skipped
	 * using skipToken</p>
	 * 
	 * @param token the token we will attempt to match
	 * @return true if success, false otherwise
	 */
	private boolean tryMatch(ISummonToken token) {
		if(currentIndex >= string.length())
			return false;
		
		if(!token.start(string.charAt(currentIndex)))
			return false;
		
		currentIndex++;
		
		while(!isTokenEnd()) {
			if(!token.next(string.charAt(currentIndex)))
				return false;
			currentIndex++;
		}
		
		return token.finish();
	}
	
	/**
	 * Get the 
	 * @param token
	 * @param refStartIndex
	 * @return
	 */
	private FormattableObject getMatch(ISummonToken token, int[] refStartIndex) {
		if(refStartIndex[0] >= string.length())
			throw new IllegalStateException("Already at end of string");
		
		if(!token.start(string.charAt(refStartIndex[0])))
			throw new IllegalStateException("Token start doesn't match");
		
		refStartIndex[0]++;
		
		while(!isTokenEnd(refStartIndex[0])) {
			if(!token.next(string.charAt(refStartIndex[0])))
				throw new IllegalStateException("No match for token at " + refStartIndex[0]);
			refStartIndex[0]++;
		}
		
		if(!token.finish())
			throw new IllegalStateException("Finished token too early");
		
		return token.toStandard();
	}
	
	/**
	 * Determines if the currentIndex is at a location that would signify
	 * the end of a token.
	 * 
	 * @return if the current index is the end of a token
	 */
	private boolean isTokenEnd() {
		return isTokenEnd(currentIndex);
	}
	
	/**
	 * Determines if the given index is at a location that would signify
	 * the end of a token.
	 * 
	 * @param ind the index
	 * @return if ind is at the end of a token
	 */
	private boolean isTokenEnd(int ind) {
		return ind >= string.length() || Character.isWhitespace(string.charAt(ind));
	}
}
