/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.cmdutil;

public enum CommandResult {

	SUCCESS("\u2611"),
	ERROR("\u274C"),
	NO_PERMISSION("\u26D4"),
	IGNORE(null);
	
	public String unicode;
	
	private CommandResult(String unicode){
		this.unicode = unicode;
	}
	
	public String getEmote(){
		return unicode;
	}
	
}
