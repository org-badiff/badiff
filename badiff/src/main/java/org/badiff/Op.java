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
package org.badiff;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.badiff.io.Serialization;
import org.badiff.io.Serialized;
import org.badiff.util.Data;

/**
 * A single run-length-encoded operation in a {@link Diff}.
 * @author robin
 *
 */
public class Op implements Applyable, Serialized {
	public static final int VERSION = 1;
	
	/**
	 * Stop diffing
	 */
	public static final byte STOP = 0x0;
	/**
	 * Delete some bytes
	 */
	public static final byte DELETE = 0x1;
	/**
	 * Insert some bytes
	 */
	public static final byte INSERT = 0x2;
	/**
	 * Copy some bytes
	 */
	public static final byte NEXT = 0x3;
	
	/**
	 * The operation
	 */
	private byte op;
	/**
	 * The run-length of the operation
	 */
	private int run;
	/**
	 * The data for the operation
	 */
	private byte[] data;
	
	/*
	 * Required for deserialization
	 */
	public Op() {}
	
	/**
	 * Create a new {@link Op}
	 * @param op
	 * @param run
	 * @param data
	 */
	public Op(byte op, int run, byte[] data) {
		if((op & 0x3) != op)
			throw new IllegalArgumentException("invalid op");
		if(run < 1 && op != DELETE || data != null && run > data.length)
			throw new IllegalArgumentException("invalid run");
		if(op == INSERT && data == null)
			throw new IllegalArgumentException("invalid data");
		this.op = op;
		this.run = run;
		this.data = data;
	}

	@Override
	public String toString() {
		switch(op) {
		case STOP: return ";";
		case DELETE: return "-" + run;
		case INSERT: return "+" + run;
		case NEXT: return ">" + run;
		}
		return "?";
	}
	
	@Override
	public void apply(DataInput orig, DataOutput target) throws IOException {
		switch(op) {
		case DELETE:
			Data.skip(orig, run);
			break;
		case NEXT:
			Data.copy(orig, target, run);
			break;
		case INSERT:
			target.write(data, 0, run);
			break;
		}
	}

	/**
	 * Returns the operation, one of {@link #STOP}, {@link #DELETE}, {@link #INSERT}, or {@link #NEXT}
	 * @return
	 */
	public byte getOp() {
		return op;
	}
	
	/**
	 * Return the run-length of this operation
	 * @return
	 */
	public int getRun() {
		return run;
	}
	
	/**
	 * Return the data for this operation.  Only {@link #INSERT} and {@link #DELETE} have data.
	 * Only {@link #INSERT} is guaranteed to have data; {@link #DELETE} may have {@code null}. 
	 * @return
	 */
	public byte[] getData() {
		return data;
	}

	@Override
	public void serialize(Serialization serial, DataOutput out)
			throws IOException {
		long oprun = op | (data != null ? 0x4 : 0) | (((long) run) << 3);
		serial.writeObject(out, Long.class, oprun);
		if((op == INSERT || op == DELETE) && data != null)
			serial.writeObject(out, byte[].class, data);
	}

	@Override
	public void deserialize(Serialization serial, DataInput in)
			throws IOException {
		long oprun = serial.readObject(in, Long.class);
		op = (byte)(oprun & 0x3);
		boolean hasData = (oprun & 0x4) != 0;
		run = (int)(oprun >>> 3);
		if((op == INSERT || op == DELETE) && hasData)
			data = serial.readObject(in, byte[].class);
	}
}
