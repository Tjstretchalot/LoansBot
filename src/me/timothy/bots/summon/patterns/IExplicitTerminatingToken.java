package me.timothy.bots.summon.patterns;

/**
 * This interface should be implemented by tokens which do not want to use
 * whitespace as a delimiter
 * 
 * @author Timothy
 */
public interface IExplicitTerminatingToken {
	public boolean isTokenEnd();
}
