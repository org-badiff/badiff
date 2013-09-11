package org.badiff.q;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.badiff.Op;

public class PumpingOpQueue extends OpQueue {
	protected static ThreadFactory workerFactory(final OpQueue source) {
		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, PumpingOpQueue.class.getSimpleName() + " for " + source);
				t.setDaemon(true);
				return t;
			}
		};
	}

	protected ExecutorService worker;
	protected Runnable task;
	protected OpQueue source;
	protected BlockingQueue<Op> pipe;
	
	public PumpingOpQueue(OpQueue source) {
		this(source, 1024);
	}
	
	public PumpingOpQueue(OpQueue source, int capacity) {
		this.source = source;
		worker = Executors.newSingleThreadExecutor(workerFactory(source));
		pipe = new ArrayBlockingQueue<Op>(capacity);
	}

	protected Runnable createTask() {
		return new Runnable() {
			@Override
			public void run() {
				try {
					for(Op e = source.poll(); e != null; e = source.poll())
						pipe.put(e);
				} catch(InterruptedException ie) {
					Thread.currentThread().interrupt();
				} finally {
					worker.shutdown();
					while(true) {
						try {
							pipe.put(new Op(Op.STOP, 1, null));
							break;
						} catch(InterruptedException ie2) {
						}
					}
				}
			}
		};
	}
	
	@Override
	protected boolean pull() {
		if(task == null) {
			worker.execute(task = createTask());
		}
		Op e = pipe.poll();
		if(e != null) {
			if(e.getOp() == Op.STOP)
				return false;
			prepare(e);
			return true;
		}
		if(!worker.isShutdown()) {
			while(true) {
				try {
					e = pipe.take();
					break;
				} catch(InterruptedException ie) {
				}
			}
			if(e.getOp() == Op.STOP)
				return false;
			prepare(e);
			return true;
		}
		return false;
	}
	
}
