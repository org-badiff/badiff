package org.badiff.io;

import java.io.IOException;

/**
 * {@link RuntimeException} wrapper around {@link IOException}
 * @author robin
 *
 */
public class RuntimeIOException extends RuntimeException {
	private static final long serialVersionUID = 0;
	
	public RuntimeIOException(IOException cause) {
		super(cause);
	}

	public RuntimeIOException(String message, IOException cause) {
		super(message, cause);
	}

}
