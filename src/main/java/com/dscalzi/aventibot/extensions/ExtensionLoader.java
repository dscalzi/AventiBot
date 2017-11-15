/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.extensions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

public class ExtensionLoader {

	private static final Logger LOG = LoggerFactory.getLogger("ExtensionLoader");
	
	public static File getBaseExtensionsDirectory() {
		File f = new File("extensions");
		if(!f.exists()){
			if(f.mkdirs()) return f;
			else {
				LOG.error(MarkerFactory.getMarker("FATAL"), "Unable to create settings directory!");
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
