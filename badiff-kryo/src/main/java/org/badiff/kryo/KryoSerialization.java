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
package org.badiff.kryo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.badiff.Op;
import org.badiff.imp.MemoryDiff;
import org.badiff.io.Serialization;
import org.badiff.io.Serialized;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoSerialization implements Serialization {
	private Kryo kryo;
	
	public KryoSerialization() {
		this(new Kryo());
		
		kryo.setRegistrationRequired(false);
		kryo.setReferences(false);
		kryo.setAutoReset(true);
		
		kryo.addDefaultSerializer(Serialized.class, SerializedSerializer.class);
		
		kryo.register(byte[].class);
		kryo.register(Op.class, new DiffOpSerializer());
		kryo.register(MemoryDiff.class, new SerializedSerializer<MemoryDiff>());
	}
	
	public KryoSerialization(Kryo kryo) {
		this.kryo = kryo;
	}

	@Override
	public <T> void writeObject(OutputStream out, Class<T> type, T object) throws IOException {
		if(out instanceof Output)
			kryo.writeObjectOrNull((Output) out, object, type);
		else {
			Output output = new Output(out);
			try {
				kryo.writeObjectOrNull(output, object, type);
			} finally {
				output.flush();
			}
		}
	}

	@Override
	public <T> T readObject(InputStream in, Class<T> type) throws IOException {
		Input input;
		if(in instanceof Input)
			input = (Input) in;
		else
			input = new Input(in, 1); // act like unbuffered stream
		return readObject(input, type);
	}

}
