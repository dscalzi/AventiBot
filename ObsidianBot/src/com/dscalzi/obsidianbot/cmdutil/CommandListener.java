package com.dscalzi.obsidianbot.cmdutil;

import com.dscalzi.obsidianbot.ObsidianBot;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		if(e.getMessage().getContent().trim().startsWith(ObsidianBot.commandPrefix)){
			if(!e.getAuthor().getId().equals(ObsidianBot.getInstance().getId()))
				CommandDispatcher.dispatchCommand(e, CommandDispatcher.parseMessage(e));
		}
	}
	
}
