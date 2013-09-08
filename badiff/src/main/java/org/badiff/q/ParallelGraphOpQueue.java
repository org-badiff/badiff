/**
 * badiff - byte array diff - fast pure-java byte-level diffing
 * 
 * Copyright (c) 2013, Robin Kirkman All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 * 2) Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 * 3) Neither the name of the badiff nor the names of its contributors may be 
 *    used to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.badiff.q;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.badiff.Diff;
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
	 * The chunk size
	 */
	protected int chunk;
	/**
	 * Thread pool for parallelization
	 */
	protected ThreadPoolExecutor pool;
	
	/**
	 * Thread-local of {@link Graph} to avoid allocating ridonkulous amounts of memory
	 */
	protected ThreadLocal<Graph> graphs = new ThreadLocal<Graph>() {
		protected Graph initialValue() {
			return newGraph();
		}
	};

	/**
	 * Create a new parallel graphing {@link OpQueue} with {@link Runtime#availableProcessors()}
	 * number of worker threads.
	 * @param source
	 */
	public ParallelGraphOpQueue(OpQueue source) {
		this(source, Runtime.getRuntime().availableProcessors(), Diff.DEFAULT_CHUNK);
	}
	
	/**
	 * Create a new parallel graphing {@link OpQueue} with the specified number of worker threads.
	 * @param source
	 * @param workers
	 */
	public ParallelGraphOpQueue(OpQueue source, int workers, int chunk) {
		super(new ChainOpQueue(new OpQueue()));
		this.input = source;
		this.chunk = chunk;
		pool = new ThreadPoolExecutor(workers, workers, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		
	}
	
	/*
	 * Offer the input to the actual input queue, not the wrapped chain
	 * (non-Javadoc)
	 * @see org.badiff.q.FilterOpQueue#offer(org.badiff.Op)
	 */
	@Override
	public boolean offer(Op e) {
		return input.offer(e);
	}
	
	/**
	 * Return a new {@link Graph} to be used by a thread computing graph diffs in parallel
	 * @return
	 */
	protected Graph newGraph() {
		return new Graph((chunk+1) * (chunk+1));
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
			if(e == null) {
				chain().getChain().peekLast().offer(delete);
				pool.shutdown();
				break;
			}
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
