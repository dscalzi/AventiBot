package com.dscalzi.obsidianbot.commands;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;

import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class AuthorCmd implements CommandExecutor {

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args) {
		
		String author = ObsidianBot.getInstance().getJDA().getUserById("169197209630277642").getAsMention();
		
		e.getChannel().sendMessage(author + " is my daddy :)");
		
		return false;
	}

}
