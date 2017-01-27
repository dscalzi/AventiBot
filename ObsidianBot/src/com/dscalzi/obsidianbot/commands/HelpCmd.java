package com.dscalzi.obsidianbot.commands;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class HelpCmd implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		String sender = e.getAuthor().getId();
		
		String msg = "Help message coming soon!";
		
		ObsidianBot.getInstance().getJDA().getUserById(sender).getPrivateChannel().sendMessage(msg).queue();
		
		return true;
	}

}
