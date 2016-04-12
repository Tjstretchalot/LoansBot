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
	
	public Warning() {
		this(-1, -1, -1, null, null, null, null, null, null);
	}

	/**
	 * Performs a sanity check to see if this Warning
	 * is potentially a valid entry
	 * @return if this is maybe valid
	 */
	public boolean isValid() {
		return (warnedUserId >= 1 && warningUserId >= 1 && violation != null && actionTaken != null && nextAction != null && notes != null);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actionTaken == null) ? 0 : actionTaken.hashCode());
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + id;
		result = prime * result + ((nextAction == null) ? 0 : nextAction.hashCode());
		result = prime * result + ((notes == null) ? 0 : notes.hashCode());
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
		result = prime * result + ((violation == null) ? 0 : violation.hashCode());
		result = prime * result + warnedUserId;
		result = prime * result + warningUserId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Warning other = (Warning) obj;
		if (actionTaken == null) {
			if (other.actionTaken != null)
				return false;
		} else if (!actionTaken.equals(other.actionTaken))
			return false;
		if (createdAt == null) {
			if (other.createdAt != null)
				return false;
		} else if (!createdAt.equals(other.createdAt))
			return false;
		if (id != other.id)
			return false;
		if (nextAction == null) {
			if (other.nextAction != null)
				return false;
		} else if (!nextAction.equals(other.nextAction))
			return false;
		if (notes == null) {
			if (other.notes != null)
				return false;
		} else if (!notes.equals(other.notes))
			return false;
		if (updatedAt == null) {
			if (other.updatedAt != null)
				return false;
		} else if (!updatedAt.equals(other.updatedAt))
			return false;
		if (violation == null) {
			if (other.violation != null)
				return false;
		} else if (!violation.equals(other.violation))
			return false;
		if (warnedUserId != other.warnedUserId)
			return false;
		if (warningUserId != other.warningUserId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Warning [id=" + id + ", warnedUserId=" + warnedUserId + ", warningUserId=" + warningUserId
				+ ", violation=" + violation + ", actionTaken=" + actionTaken + ", nextAction=" + nextAction
				+ ", notes=" + notes + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}
	
	
}
