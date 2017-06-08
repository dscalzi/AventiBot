/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.cmdutil;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.settings.SettingsManager;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.SimpleLog;

public class CommandListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		if(e.getMessage().getRawContent().trim().startsWith(SettingsManager.getCommandPrefix(e.getGuild()))){
			if(e.getChannelType() != ChannelType.PRIVATE)
				if(!PermissionUtil.isInitialized(e.getGuild())){
					try{
						PermissionUtil.loadJson(e.getGuild());
					} catch(Throwable t){
						SimpleLog.getLog("AventiBot").fatal("Error occured loading permissions.. unable to process"
								+ "requests for guild " + e.getGuild().getName() + "(" + e.getGuild().getId() +  ")!");
						t.printStackTrace();
						return;
					}
				}
			if(!e.getAuthor().getId().equals(AventiBot.getInstance().getJDA().getSelfUser().getId())){
				CommandDispatcher.dispatchCommand(e, CommandDispatcher.parseMessage(e));
			}
		}
	}
	
}
