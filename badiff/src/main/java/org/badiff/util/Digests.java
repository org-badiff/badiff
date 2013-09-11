package org.badiff.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.naming.directory.NoSuchAttributeException;

import org.badiff.io.NoopOutputStream;

public class Digests {

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
	
	public static MessageDigest digest(String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm);
		} catch(NoSuchAlgorithmException nsae) {
			return null;
		}
	}
	
	public static byte[] digest(File file, MessageDigest digest) throws IOException {
		DigestInputStream digin = new DigestInputStream(new FileInputStream(file), digest);
		Streams.copy(digin, new NoopOutputStream());
		digin.close();
		return digin.getMessageDigest().digest();
	}
	
	public static String pretty(byte[] digest) {
		StringBuilder sb = new StringBuilder();
		for(byte b : digest) {
			sb.append(String.format("%02x", 0xff & b));
		}
		return sb.toString();
	}
	
	private Digests() {}

}
