package org.badiff.cli;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class URLProperties extends Properties {
	public static URLProperties fromClasspath(URL url) {
		try {
			return new URLProperties(url);
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	public URLProperties(URL url) throws IOException {
		InputStream in = url.openStream();
		try {
			load(in);
		} finally {
			in.close();
		}
	}
}
