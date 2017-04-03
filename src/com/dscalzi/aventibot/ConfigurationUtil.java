/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot;

import java.io.File;
import java.io.IOException;

import net.dv8tion.jda.core.utils.SimpleLog;

public class ConfigurationUtil {

	private static final SimpleLog log = SimpleLog.getLog("ConfigurationUtil");
	
	private static final File configurationFile;
	
	static {
		configurationFile = new File(AventiBot.getDataPath(), "config.json");
	}
	
	private static boolean verifyFile() throws IOException{
		if(!configurationFile.exists()) return configurationFile.createNewFile();
		return true;
	}
	
	public static void loadJson() throws IOException{
		if(!verifyFile()) return;
		log.info("Using config file located at " + configurationFile.getAbsolutePath());
	}
	
	
	
}
