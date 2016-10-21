package com.dscalzi.obsidianbot;

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
	 * Bot has not yet been launched.
	 */
	NULL();
	
}
