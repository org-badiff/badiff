package org.badiff.q;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.badiff.Graph;
import org.badiff.Op;

public class ParallelGraphOpQueue extends FilterOpQueue {
	
	protected OpQueue input;
	protected ThreadPoolExecutor pool;
	
	protected ThreadLocal<Graph> graphs = new ThreadLocal<Graph>() {
		protected Graph initialValue() {
			return new Graph(4097 * 4097);
		}
	};

	public ParallelGraphOpQueue(OpQueue source, int workers) {
		super(new ChainOpQueue(new OpQueue()));
		this.input = source;
		pool = new ThreadPoolExecutor(workers, workers, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		
	}
	
	@Override
	protected void filter() {
		while(pool.getActiveCount() < pool.getCorePoolSize()) {
			Op e = input.poll();
			if(e == null)
				break;
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
			
			chain().offer(new FutureOpQueue(future));
			chain().offer(new OpQueue());
			
		}
		super.filter();
	}
	
	protected ChainOpQueue chain() {
		return (ChainOpQueue) source;
	}

	@Override
	protected boolean shiftPending() {
		boolean ret = super.shiftPending();
		if(!ret)
			pool.shutdown();
		return ret;
	}
}
