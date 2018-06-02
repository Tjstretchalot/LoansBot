package me.timothy.bots.summon.patterns;

import java.util.List;

/**
 * <p>Acts like a non-regular-expression pattern matcher for the types of 
 * summons that this loansbot responds to. This acts as a replacement
 * for the previous regular expression matches, which became unwieldy
 * when reddit transitioned to new reddit and began auto-producing 
 * markdown in some situations.</p>
 * 
 * This always assumes that the pattern should start and end at a space 
 * delimiter. Futhermore, patterns are a series of specific tokens that
 * are connected by standard spaces. The tokens may be string literals,
 * username references, or money amounts.
 * 
 * This should be created using the SummonPatternFactory
 * 
 * @author Timothy
 */
public class SummonPattern {
	/**
	 * The tokens in this summon pattern in the order we want them
	 * to appear.
	 */
	private List<ISummonToken> tokens;
	
	/**
	 * Create a new summon pattern. Best done through the factory.
	 * 
	 * @param tokens the tokens in order that we expect.
	 */
	public SummonPattern(List<ISummonToken> tokens) {
		this.tokens = tokens;
	}
	
	/**
	 * Create a matcher ready for parsing the given string.
	 * @param string the string to parse
	 * @return the corresponding matcher.
	 */
	public SummonMatcher matcher(String string) {
		return new SummonMatcher(tokens, string);
	}
}
