package com.dscalzi.obsidianbot.cmdutil;

import net.dv8tion.jda.events.message.MessageReceivedEvent;

public interface CommandExecutor {
	
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs);
	
}
