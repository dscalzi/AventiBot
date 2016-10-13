package com.dscalzi.obsidianbot.commands;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;

import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class HelpCommand implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args) {
		
		String sender = e.getAuthor().getId();
		
		String msg = "Help message coming soon!";
		
		ObsidianBot.getInstance().getJDA().getUserById(sender).getPrivateChannel().sendMessage(msg);
		
		return true;
	}

}
