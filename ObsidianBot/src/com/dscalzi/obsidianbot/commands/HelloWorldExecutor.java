package com.dscalzi.obsidianbot.commands;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;

import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class HelloWorldExecutor implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args) {
		
		e.getChannel().sendMessage("Hello, " + e.getAuthor().getAsMention() + "! Fine day, isn't it?");
		
		return false;
	}

}
