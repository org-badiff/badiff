package org.badiff.cli;

import java.net.URL;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Options {
	protected Properties defaults;
	protected org.apache.commons.cli.Options options;
	
	public Options(URL classpathResource) {
		this(URLProperties.fromClasspath(classpathResource));
	}
	
	public Options(Properties defaults) {
		this.defaults = defaults;
		options = new org.apache.commons.cli.Options();
	}

	public void optional(String opt, String longOpt, boolean hasArg, String description) {
		if(defaults.containsKey(longOpt))
			description += " (default:" + defaults.getProperty(longOpt) + ")";
		Option option = new Option(opt, longOpt, hasArg, description);
		options.addOption(option);
	}
	
	public void required(String opt, String longOpt, boolean hasArg, String description) {
		Option option = new Option(opt, longOpt, hasArg, description);
		option.setRequired(true);
		options.addOption(option);
	}
	
	public CommandLine parse(String... args) throws ParseException {
		return new PosixParser().parse(options, args, defaults);
	}
}
