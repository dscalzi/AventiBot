package com.dscalzi.obsidianbot.cmdutil;

import com.dscalzi.obsidianbot.ObsidianBot;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		//Bot does not accept commands from foreign discord guilds.
		if(!e.getMessage().isFromType(ChannelType.PRIVATE)){
			if(!(e.getGuild().equals(ObsidianBot.getInstance().getGuild()))){ 
				return;
			}
		}
		
		String content = e.getMessage().getContent().trim();
		String senderId = e.getAuthor().getId();
		if(content.startsWith(ObsidianBot.commandPrefix)){
			//Bot is not allowed to dispatch commands through chat.
			if(senderId.equals(ObsidianBot.getInstance().getId())){
				return;
			}
			
			CommandDispatcher.dispatchCommand(e, CommandDispatcher.parseMessage(e));
		}
	}
	
}
