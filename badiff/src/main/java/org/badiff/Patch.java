package org.badiff;

import java.util.Set;

/**
 * A pseudo-collection of {@link PatchOp}s mapped by relative path
 * from a root directory.
 * @author robin
 *
 */
public interface Patch extends FileApplyable {
	/**
	 * Return the number of {@link PatchOp}s in this {@link Patch}
	 * @return
	 */
	public int size();
	/**
	 * Return the relative paths in this {@link Patch}
	 * @return
	 */
	public Set<String> keySet();
	/**
	 * Return whether this {@link Patch} contains the argument relative path
	 * @param path
	 * @return
	 */
	public boolean containsKey(String path);
	/**
	 * Returns the {@link PatchOp} for the argument relative path
	 * @param path
	 * @return
	 */
	public PatchOp get(String path);
	/**
	 * Sets the {@link PatchOp} for the argument relative path, returning the old value
	 * @param path
	 * @param diff
	 * @return
	 */
	public PatchOp put(String path, PatchOp diff);
	
	/**
	 * Overwrite this {@link Patch} with the argument
	 * @param other
	 */
	public void store(Patch other);
}
