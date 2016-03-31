/**
 * 
 */
package me.timothy.bots.database;

/**
 * <p>An object-relational mapping (ORM) converts objects into relational
 * databases - saving to and loading from.</p>
 * 
 * <p>This interface is a more generic version of that, and is simply capable
 * of storing and retrieving objects from memory to <i>something else</i>.</p>
 * 
 * @author Timothy
 */
public interface ObjectMapping<A> {
	/**
	 * Saves/updates the object to/in the mapping, such as storing/updating in a relational
	 * database or sending over a network.
	 * 
	 * @param a the object to save
	 * @throws IllegalArgumentException if the object is not ready to be mapped
	 */
	public void save(A a) throws IllegalArgumentException;
}
