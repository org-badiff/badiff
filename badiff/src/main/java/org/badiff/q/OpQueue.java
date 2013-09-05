package org.badiff.q;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.badiff.Applyable;
import org.badiff.Diff;
import org.badiff.Op;

/**
 * A unidirectional double-buffering queue of {@link Op}.
 * Contains internally two {@link Deque} of {@link Op}: {@link #ready}
 * and {@link #pending}.  Calls to {@link #poll()} draw from {@link #ready}
 * unless it is empty, in which case they call {@link #shift()} to move
 * an element from {@link #pending} to {@link #ready}.<p>
 * 
 * {@link OpQueue} also implements {@link Iterator}.  Don't mix the {@link #poll()} and {@link #offer(Op)}
 * methods with the {@link Iterator} methods.<p>
 * 
 * {@link OpQueue} instances are <b>lazy sequences</b>, and will instantiate
 * or process their contents only in response to calls to {@link #poll()}.
 * @author robin
 *
 */
public class OpQueue implements Applyable, Iterator<Op> {

	/**
	 * The next {@link Op} to be returned by the iterator.
	 */
	protected Op iterNext;
	/**
	 * The {@link Op}s which are ready to be {@link #poll()}ed
	 */
	protected Deque<Op> ready = new ArrayDeque<Op>();
	/**
	 * The {@link Op}s which are ready to be {@link #shift()}ed
	 */
	protected Deque<Op> pending = new ArrayDeque<Op>();

	/**
	 * Draw the next {@link Op} from this {@link OpQueue}, returning
	 * null if this {@link OpQueue} is empty.
	 * @return
	 */
	public Op poll() {
		Op e = ready.pollFirst();
		if(e == null) {
			shift();
			e = ready.pollFirst();
		}
		return e;
	}
	
	/**
	 * Place a {@link Op} in {@link #pending}
	 * @param e
	 * @return
	 */
	public boolean offer(Op e) {
		return pending.offerLast(e);
	}
	
	/**
	 * Drain all the {@link Op}s in this object to a {@link List}
	 * @param c
	 * @return
	 */
	public <T extends List<Op>> T drainTo(T c) {
		for(Op e = poll(); e != null; e = poll())
			c.add(e);
		return c;
	}
	
	/**
	 * Drain all the {@link Op}s in this object to another {@link OpQueue}
	 * @param q
	 * @return
	 */
	public <T extends OpQueue> T drainTo(T q) {
		for(Op e = poll(); e != null; e = poll())
			q.offer(e);
		return q;
	}
	
	/**
	 * Overwrite the argument {@link Diff} with the remaining {@link Op}s in this object.
	 * Calls {@link Diff#store(Iterator)} with this object as the argument.
	 * @param diff
	 * @return
	 * @throws IOException
	 */
	public <T extends Diff> T drainTo(T diff) throws IOException {
		diff.store(this);
		return diff;
	}
	
	/**
	 * Called when a {@link Op} should be moved from {@link #pending} to {@link #ready}.
	 * Override this method to provide lazy sequences that populate {@link #pending} on demand.
	 */
	protected void shift() {
		if(ready.size() == 0)
			shiftReady();
	}
	
	/**
	 * Actually moves a {@link Op} from {@link #pending} to {@link #ready}
	 * @return
	 */
	protected boolean shiftReady() {
		Op e = pending.pollFirst();
		if(e != null)
			ready.offerLast(e);
		return e != null;
	}

	@Override
	public void apply(InputStream orig, OutputStream target)
			throws IOException {
		for(Op e = poll(); e != null; e = poll())
			e.apply(orig, target);
	}

	@Override
	public boolean hasNext() {
		if(iterNext == null)
			iterNext = poll();
		return iterNext != null;
	}

	@Override
	public Op next() {
		if(iterNext == null)
			iterNext = poll();
		try {
			return iterNext;
		} finally {
			iterNext = null;
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
}
