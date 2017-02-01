package com.dscalzi.obsidianbot.cmdutil;

import java.util.List;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public interface CommandExecutor {
	
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs);
	
	public List<String> getNodes();
	
}
