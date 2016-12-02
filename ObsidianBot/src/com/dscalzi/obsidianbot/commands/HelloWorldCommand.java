package com.dscalzi.obsidianbot.commands;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class HelloWorldCommand implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		e.getChannel().sendMessage("Hello, " + e.getAuthor().getAsMention() + "! Fine day, isn't it?").queue();
		
		return false;
	}

}
