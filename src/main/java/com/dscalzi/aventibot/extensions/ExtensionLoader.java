/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.extensions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.core.utils.SimpleLog;

public class ExtensionLoader {

	private static final SimpleLog LOG = SimpleLog.getLog("ExtensionLoader");
	
	public static File getBaseExtensionsDirectory() {
		File f = new File("extensions");
		if(!f.exists()){
			if(f.mkdirs()) return f;
			else {
				LOG.fatal("Unable to create settings directory!");
				return null;
			}
		}
		return f;
	}
	
	public static List<File> getCandidates() {
		List<File> candidates = new ArrayList<File>();
		File fB = getBaseExtensionsDirectory();
		if(fB != null && fB.isDirectory()) {
			File[] allFiles = fB.listFiles();
			for(File f : allFiles) {
				if(!f.isDirectory() && f.getName().toLowerCase().endsWith(".jar")) {
					candidates.add(f);
				}
			}
		}
		return candidates;
	}
	
}
