/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE for license information.
 */
package com.dscalzi.aventibot;

import java.util.Arrays;
import java.util.List;

import com.dscalzi.aventibot.cmdline.CommandLineExecutor;
import com.dscalzi.aventibot.ui.TerminalExecutor;

public class LaunchWrapper {

	public static void main(String[] args) {
		List<String> lstArgs = Arrays.asList(args);
		if(lstArgs.contains("--cmdline")){
			CommandLineExecutor.main(args);
		} else {
			TerminalExecutor.main(args);
		}
	}

}
