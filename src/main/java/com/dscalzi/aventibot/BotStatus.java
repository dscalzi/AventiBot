/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot;

public enum BotStatus {

	/**
	 * Bot is successfully connected.
	 */
	CONNECTED(),
	/**
	 * Bot is launched and initialized, but not connected.
	 */
	LAUNCHED(),
	/**
	 * Bot is shutdown (was signaled to shutdown JDA and release bindings).
	 */
	SHUTDOWN(),
	/**
	 * Bot has not yet been launched.
	 */
	NULL();
	
}