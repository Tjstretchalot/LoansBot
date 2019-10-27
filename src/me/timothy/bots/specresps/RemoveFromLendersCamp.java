package me.timothy.bots.specresps;

/**
 * A special response object which stores information about removing a user
 * from lenders camp if they are there.
 * 
 * @author Timothy
 */
public class RemoveFromLendersCamp {
	public static final String SPECIAL_KEY = "remove_from_lenders_camp";
	
	private String userToRemove;

	/**
	 * Create a new response to remove the given user from the lenders camp if
	 * they are currently allowed to use it.
	 * 
	 * @param userToRemove the user to remove from the lenders camp
	 */
	public RemoveFromLendersCamp(String userToRemove) {
		super();
		this.userToRemove = userToRemove;
	}

	
	/**
	 * @return the user who should be removed from lenders camp
	 */
	public String getUserToRemove() {
		return userToRemove;
	}


	@Override
	public String toString() {
		return "RemoveFromLendersCamp [userToRemove=" + userToRemove + "]";
	}
}
