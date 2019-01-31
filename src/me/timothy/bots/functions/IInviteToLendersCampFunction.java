package me.timothy.bots.functions;

import me.timothy.bots.models.Username;

/**
 * Invite a particular user to lenders camp
 * 
 * @author Timothy
 */
public interface IInviteToLendersCampFunction {
	/**
	 * Invites the given user to lenders camp
	 * @param username the user to invite
	 */
	public void inviteToLendersCamp(Username username);
}
