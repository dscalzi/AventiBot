/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.util;

public enum IconUtil {

	INFO("http://i.imgur.com/ccX8Pvi.png"),
	CLOCK("http://i.imgur.com/Y3rbhFt.png"),
	PLAY("http://i.imgur.com/nEw5Gsk.png"),
	ADD("http://i.imgur.com/7OfFSFx.png"),
	REMOVE("http://i.imgur.com/voGutMQ.png"),
	VOTE("http://i.imgur.com/saXkgYz.png");
	
	private final String URL;
	
	IconUtil(String URL){
		this.URL = URL;
	}
	
	public String getURL(){
		return this.URL;
	}
	
}
