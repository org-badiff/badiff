package org.badiff.hadoop.aig;

import java.util.Random;

public class InputBytesGenerator {
	private Random random;
	private byte[] from;
	private byte[] to;
	
	public InputBytesGenerator(long seed, int iteration) {
		this(seed, iteration, 64*1024);
	}
	
	public InputBytesGenerator(long seed, int iteration, int length) {
		random = new Random(seed + iteration); // predictable Random
		
		from = new byte[length];
		to = new byte[length];
		
		// initialize from and to
		random.nextBytes(from);
		System.arraycopy(from, 0, to, 0, from.length);
		
		// Do a whole lot of random swaps
		for(int i = 0; i < length / 512; i++) {
			byte[] b1 = new byte[random.nextInt(1024)];
			byte[] b2 = new byte[b1.length];
			
			int pos1 = random.nextInt(to.length - b1.length);
			int pos2 = random.nextInt(to.length - b2.length);
			
			System.arraycopy(to, pos1, b1, 0, b1.length);
			System.arraycopy(to, pos2, b2, 0, b2.length);
			
			System.arraycopy(b1, 0, to, pos2, b1.length);
			System.arraycopy(b2, 0, to, pos1, b2.length);
		}
	}
	
	public byte[] getFrom() {
		return from;
	}
	
	public byte[] getTo() {
		return to;
	}
}
