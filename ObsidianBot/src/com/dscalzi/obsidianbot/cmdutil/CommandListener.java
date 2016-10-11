package com.dscalzi.obsidianbot.cmdutil;

import com.dscalzi.obsidianbot.ObsidianBot;

import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		String content = e.getMessage().getContent().trim();
		String senderId = e.getAuthor().getId();
		if(content.startsWith(ObsidianBot.commandPrefix)){
			/* Bot is not allowed to dispatch commands */
			if(senderId.equals(ObsidianBot.getInstance().getId())){
				return;
			}
			String cmd = content.substring(ObsidianBot.commandPrefix.length(), (content.indexOf(" ") > -1 ? content.indexOf(" ") : content.length()));
			CommandDispatcher.dispatchCommand(e, cmd);
		}
	}
	
}
