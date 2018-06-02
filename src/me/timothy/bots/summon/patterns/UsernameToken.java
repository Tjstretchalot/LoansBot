package me.timothy.bots.summon.patterns;

import me.timothy.bots.responses.FormattableObject;
import me.timothy.bots.responses.GenericFormattableObject;

/**
 * This token is meant to match something delimiting a username on
 * reddit. In order to make sure this isn't an over-zealous match
 * this token only looks for things that would link the user-page
 * automatically after reddit markdown parsing.
 * 
 * For example, this would match
 *   * u/johndoe
 *   * /u/johndoe
 *   * [u/johndoe](https://reddit.com/u/johndoe]
 *   * [johndoe](https://reddit.com/u/johndoe]
 *   * [/u/johndoe](https://reddit.com/u/johndoe]
 *   
 * This will not match when the visible text is not some form of
 * the linked users username.
 * 
 * After parsing, this is just a GenericFormattableObject with 
 * reference to the username, ie. in all of the above examples
 * it would be 'johndoe'
 * 
 * @author Timothy
 *
 */
public class UsernameToken implements ISummonToken {
	private String id;
	private boolean optional;
	
	/**
	 * This is true if the first character we parsed was a 
	 * '[', which indicates we are starting a link block.
	 */
	private boolean isLink;
	
	/**
	 * This is true if we are currently parsing the innards of 
	 * the text-link section. Here this will include if we've
	 * seen a first slash, the u, or the second slash yet or not.
	 */
	private boolean inLinkTextSection;
	
	/**
	 * This is true if we just recieved the ']' signifying the end of 
	 * a link text section, and we are expecting the '(' to start off
	 * the link section.
	 */
	private boolean expectingLinkOpenParens;
	
	/**
	 * This is true if we have already seen the ')' so we can't parse any
	 * more characters for this token.
	 */
	private boolean seenCloseLinkParens;
	
	/**
	 * When either in the link text section or just seen the first
	 * character, this is whether or not we have seen the leading
	 * slash before the user tag, which is optional.
	 */
	private boolean seenLeadingSlash;
	
	/**
	 * This is if we have seen the non-optional u (if not a link) or if we 
	 * have seen the u already (if a link)
	 * 
	 * In link tags, for the following example:
	 * 
	 * [uramazing](https://reddit.com/user/uramazing)
	 * 
	 * we will set the seenU tag to true, and in the next
	 * character we won't see the trailing slash so we will
	 * assume the previous u was actually a part of the username
	 * and this flag will stay true to signal we're in the parsing-username
	 * part.
	 */
	private boolean seenU;
	
	/**
	 * This can only be set after we've seen the u, and it tells us if we've seen the
	 * trailing slash already.
	 * 
	 * Ex. u/johndoe, there that '/' trails the 'u' and is called the trailing slash.
	 */
	private boolean seenTrailingSlash;
	
	/**
	 * This is the username that is being referenced. In links, the information is given
	 * twice and this is referring to the visible section. In non-linked sections this
	 * will be the only variable storing the text of the username and will always start
	 * immediately after the trailing slash.
	 */
	private StringBuilder textUsername;
	
	/**
	 * Rather than try to parse the link bit-by-bit we simply dump each character
	 * in the link into this stringbuilder and then, once we have the whole thing
	 * we decide if it is valid.
	 */
	private StringBuilder link;
	
	
	/**
	 * Creates a new token that is looking to find usernames,
	 * and will be referred to by the specified id.
	 * 
	 * @param id the id to refer to this username as. ex "user1"
	 * @param optional if this token is optional
	 */
	public UsernameToken(String id, boolean optional) {
		this.id = id;
		this.optional = optional;
	}
	
	@Override
	public String id() {
		return id;
	}
	
	@Override
	public boolean isOptional() {
		return optional;
	}

	@Override
	public boolean start(char c) {
		isLink = false;
		inLinkTextSection = false;
		expectingLinkOpenParens = false;
		seenCloseLinkParens = false;
		seenLeadingSlash = false;
		seenU = false;
		seenTrailingSlash = false;
		textUsername = null;
		link = null;
		
		
		if(c == '[') {
			isLink = true;
			inLinkTextSection = true;
			return true;
		}else if(c == '/') {
			seenLeadingSlash = true;
			return true;
		}else if(c == 'u') {
			seenU = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean next(char c) {
		if(inLinkTextSection || !isLink) {
			if(c == ']') {
				if(inLinkTextSection) {
					if(seenU && !seenTrailingSlash)
						return false;
					
					if(textUsername == null || textUsername.length() < 3)
						return false;
					
					expectingLinkOpenParens = true;
					inLinkTextSection = false;
					return true;
				}
				
				return false;
			}else if(c == '/') {
				if(!seenLeadingSlash && !seenU) {
					seenLeadingSlash = true;
					return true;
				}else if(seenU && !seenTrailingSlash) {
					seenTrailingSlash = true;
					return true;
				}
				
				return false;
			}else if(!seenU) {
				if(c == 'u') {
					seenU = true;
					return true;
				}else if(seenLeadingSlash || !isLink) {
					return false;
				}
			}
			
			if(!Character.isDigit(c) && !Character.isAlphabetic(c) && c != '-' && c != '_') {
				return false;
			}
			
			if(!seenTrailingSlash) {
				if(!isLink)
					return false;

				if(seenU) {
					textUsername = new StringBuilder();
					textUsername.append('u');
				}
				
				seenTrailingSlash = true;
				seenU = true;
			}
			
			if(textUsername == null)
				textUsername = new StringBuilder();
			textUsername.append(c);
			
			return true;
		}else if(expectingLinkOpenParens) {
			if(c != '(')
				return false;
			
			expectingLinkOpenParens = false;
			return true;
		}else if(seenCloseLinkParens) {
			return false;
		}else {
			if(c == ')') {
				if(link == null || link.length() < 5) {
					return false;
				}
				
				seenCloseLinkParens = true;
				return true;
			}
			
			if(link == null)
				link = new StringBuilder();
			link.append(c);
			return true;
		}
	}

	@Override
	public boolean finish() {
		if(!isLink) {
			if(!seenU)
				return false;
			if(!seenTrailingSlash)
				return false;
			
			if(textUsername == null || textUsername.length() < 3)
				return false;
			
			return true;
		}else {
			if(!seenCloseLinkParens)
				return false;
			
			if(textUsername == null || textUsername.length() < 3)
				return false;
			
			if(link == null || link.length() < 5)
				return false;
			
			String linkStr = link.toString();
			if(linkStr.startsWith("https://"))
				linkStr = linkStr.substring(8);
			else if(linkStr.startsWith("http://"))
				linkStr = linkStr.substring(7);
			else
				return false;
			
			if(linkStr.startsWith("www."))
				linkStr = linkStr.substring(4);
			
			if(linkStr.startsWith("reddit.com/user/"))
				linkStr = linkStr.substring(16);
			else if(linkStr.startsWith("reddit.com/u/"))
				linkStr = linkStr.substring(13);
			else
				return false;
			
			if(!linkStr.equals(textUsername.toString()))
				return false;
			
			return true;
		}
	}

	@Override
	public FormattableObject toStandard() {
		return new GenericFormattableObject(textUsername.toString());
	}

}
