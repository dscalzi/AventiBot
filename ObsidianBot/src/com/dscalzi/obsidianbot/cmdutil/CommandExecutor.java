package com.dscalzi.obsidianbot.cmdutil;

import java.util.Set;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface CommandExecutor {
	
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs);
	
	public Set<PermissionNode> getNodes();
	
}
