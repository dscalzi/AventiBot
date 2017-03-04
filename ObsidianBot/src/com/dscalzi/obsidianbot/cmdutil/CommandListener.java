package com.dscalzi.obsidianbot.cmdutil;

import com.dscalzi.obsidianbot.ObsidianBot;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.SimpleLog;

public class CommandListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		if(e.getMessage().getContent().trim().startsWith(ObsidianBot.commandPrefix)){
			if(e.getChannelType() != ChannelType.PRIVATE)
				if(!PermissionUtil.isInitialized(e.getGuild())){
					try{
						PermissionUtil.loadJson(e.getGuild());
					} catch(Throwable t){
						SimpleLog.getLog("ObsidianBot").fatal("Error occured loading permissions.. unable to process"
								+ "requests for guild " + e.getGuild().getName() + "(" + e.getGuild().getId() +  ")!");
						t.printStackTrace();
						return;
					}
				}
			if(!e.getAuthor().getId().equals(ObsidianBot.getInstance().getId()))
				CommandDispatcher.dispatchCommand(e, CommandDispatcher.parseMessage(e));
		}
	}
	
}
