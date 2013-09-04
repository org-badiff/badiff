package org.badiff;

public class Op {
	public static final byte STOP = 0x0;
	public static final byte DELETE = 0x1;
	public static final byte INSERT = 0x2;
	public static final byte NEXT = 0x3;
	
	private byte op;
	private int run;
	private byte[] data;
	
	public Op(byte op, int run, byte[] data) {
		if((op & 0x3) != op)
			throw new IllegalArgumentException("invalid op");
		if(run < 0 || data != null && run > data.length)
			throw new IllegalArgumentException("invalid run");
		if(op == INSERT && data == null)
			throw new IllegalArgumentException("invalid data");
		this.op = op;
		this.run = run;
		this.data = data;
	}
	
	public byte getOp() {
		return op;
	}
	
	public int getRun() {
		return run;
	}
	
	public byte[] getData() {
		return data;
	}
}
