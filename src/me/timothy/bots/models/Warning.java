package me.timothy.bots.models;

import java.sql.Timestamp;

/**
 * Describes a warning that was issued to a user
 * @author Timothy
 *
 */
public class Warning {
	public int id;
	public int warnedUserId;
	public int warningUserId;
	public String violation;
	public String actionTaken;
	public String nextAction;
	public String notes;
	public Timestamp createdAt;
	public Timestamp updatedAt;
	
	/**
	 * Creates the warning
	 * @param id the id in mysql or an invalid value (less than 1)
	 * @param warnedUserId the id of the user who was warned
	 * @param warningUserId the id of the user who warned
	 * @param violation description of the violation
	 * @param actionTaken description of the action
	 * @param nextAction suggested action if another warning is issued
	 * @param notes additional notes
	 * @param createdAt when this was created
	 * @param updatedAt when this was last updated
	 */
	public Warning(int id, int warnedUserId, int warningUserId, String violation, String actionTaken, String nextAction, String notes, 
			Timestamp createdAt, Timestamp updatedAt) {
		this.id = id;
		this.warnedUserId = warnedUserId;
		this.warningUserId = warningUserId;
		this.violation = violation;
		this.actionTaken = actionTaken;
		this.nextAction = nextAction;
		this.notes = notes;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	/**
	 * Performs a sanity check to see if this Warning
	 * is potentially a valid entry
	 * @return if this is maybe valid
	 */
	public boolean isValid() {
		return (warnedUserId >= 1 && warningUserId >= 1 && violation != null && actionTaken != null && nextAction != null && notes != null);
	}
}
