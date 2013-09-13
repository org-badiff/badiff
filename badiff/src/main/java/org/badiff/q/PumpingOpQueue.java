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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.badiff.Op;

/**
 * {@link OpQueue} that aggressively pulls elements from its source in a separate thread,
 * and places them in a {@link BlockingQueue} for retrieval by {@link #pull()}.
 * @author robin
 *
 */
public class PumpingOpQueue extends OpQueue {
	protected ThreadFactory workerFactory(final OpQueue source) {
		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, PumpingOpQueue.this.toString());
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
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " <- " + source;
	}
	
}
