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
package org.badiff.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.badiff.io.RandomInput;
import org.badiff.io.RandomInputStream;

/**
 * Utility class for working with {@link MessageDigest}s
 * @author robin
 *
 */
public class Digests {
	/**
	 * Return a default message digest.  Tries SHA-1, then MD5, then throws an exception.
	 * @return
	 */
	public static MessageDigest defaultDigest() {
		try {
			return MessageDigest.getInstance("SHA-1");
		} catch(NoSuchAlgorithmException nsae) {
			try {
				return MessageDigest.getInstance("MD5");
			} catch(NoSuchAlgorithmException nsae2) {
				throw new RuntimeException("Could not find SHA-1 or MD5");
			}
		}
	}
	
	/**
	 * Returns the specified digest, or throws a {@link RuntimeException}
	 * @param algorithm
	 * @return
	 */
	public static MessageDigest digest(String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm);
		} catch(NoSuchAlgorithmException nsae) {
			throw new RuntimeException(nsae);
		}
	}
	
	/**
	 * Compute the hash of a {@link File} given a {@link MessageDigest}
	 * @param file
	 * @param digest
	 * @return
	 * @throws IOException
	 */
	public static byte[] digest(File file, MessageDigest digest) throws IOException {
		DigestInputStream digin = new DigestInputStream(new FileInputStream(file), digest);
		Data.copy(new DataInputStream(digin), Data.NOOP_OUT);
		digin.close();
		return digin.getMessageDigest().digest();
	}
	
	public static byte[] digest(RandomInput input, MessageDigest digest) throws IOException {
		DigestInputStream digin = new DigestInputStream(new RandomInputStream(input), digest);
		Data.copy(new DataInputStream(digin), Data.NOOP_OUT);
		return digin.getMessageDigest().digest();
	}
	
	/**
	 * Pretty-print a digest.
	 * Converts a byte array into a string of hex characters.
	 * @param digest
	 * @return
	 */
	public static String pretty(byte[] digest) {
		StringBuilder sb = new StringBuilder();
		for(byte b : digest) {
			sb.append(String.format("%02x", 0xff & b));
		}
		return sb.toString();
	}
	
	private Digests() {}

}
