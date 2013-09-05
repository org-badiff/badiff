package org.badiff.q;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.badiff.Op;
import org.badiff.alg.Graph;

/**
 * {@link OpQueue} that locates pairs of ({@link Op#DELETE},{@link Op#INSERT}) and
 * applies {@link Graph} to them, in parallel.  This {@link OpQueue} is <b>PARTIALLY LAZY</b>.
 * Partially lazy means that it will eagerly draw elements until all worker threads are active
 * any time a lazy element request is made.
 * @author robin
 *
 */
public class ParallelGraphOpQueue extends FilterOpQueue {
	
	/**
	 * The real source of elements
	 */
	protected OpQueue input;
	/**
	 * Thread pool for parallelization
	 */
	protected ThreadPoolExecutor pool;
	
	/**
	 * Thread-local of {@link Graph} to avoid allocating ridonkulous amounts of memory
	 */
	protected ThreadLocal<Graph> graphs = new ThreadLocal<Graph>() {
		protected Graph initialValue() {
			return new Graph(2049 * 2049);
		}
	};

	/**
	 * Create a new parallel graphing {@link OpQueue} with {@link Runtime#availableProcessors()}
	 * number of worker threads.
	 * @param source
	 */
	public ParallelGraphOpQueue(OpQueue source) {
		this(source, Runtime.getRuntime().availableProcessors());
	}
	
	/**
	 * Create a new parallel graphing {@link OpQueue} with the specified number of worker threads.
	 * @param source
	 * @param workers
	 */
	public ParallelGraphOpQueue(OpQueue source, int workers) {
		super(new ChainOpQueue(new OpQueue()));
		this.input = source;
		pool = new ThreadPoolExecutor(workers, workers, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		
	}
	
	@Override
	protected void filter() {
		// only do anything if there are inactive threads
		while(pool.getActiveCount() < pool.getCorePoolSize()) {
			Op e = input.poll();
			if(e == null) { 
				// ran out of input, so we can shutdown the pool
				pool.shutdown();
				break;
			}
			// look for DELETE,INSERT
			if(e.getOp() != Op.DELETE) {
				chain().getChain().peekLast().offer(e);
				continue;
			}
			final Op delete = e;
			e = input.poll();
			if(e.getOp() != Op.INSERT) {
				chain().getChain().peekLast().offer(delete);
				chain().getChain().peekLast().offer(e);
				continue;
			}
			final Op insert = e;
			
			// construct a task and submit it to the pool
			Callable<OpQueue> task = new Callable<OpQueue>() {
				@Override
				public OpQueue call() throws Exception {
					OpQueue graphed = new ReplaceOpQueue(delete.getData(), insert.getData());
					graphed = new GraphOpQueue(graphed, graphs.get());
					List<Op> ops = new ArrayList<Op>();
					graphed.drainTo(ops);
					graphed = new ListOpQueue(ops);
					return graphed;
				}
			};
			
			Future<OpQueue> future = pool.submit(task);

			// toss the future queue onto the chain and then a new empty queue also
			chain().offer(new FutureOpQueue(future));
			chain().offer(new OpQueue());
			
		}
		super.filter();
	}
	
	/**
	 * Returns the {@link ChainOpQueue} that holds the results of this parallel computation
	 * @return
	 */
	protected ChainOpQueue chain() {
		return (ChainOpQueue) source;
	}

	@Override
	protected boolean shiftPending() {
		// shutdown the pool when the OpQueue is emptied
		boolean ret = super.shiftPending();
		if(!ret)
			pool.shutdown();
		return ret;
	}
}
