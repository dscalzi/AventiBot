/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2022 Daniel D. Scalzi
 *
 * https://github.com/dscalzi/AventiBot
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.dscalzi.aventibot.cmdutil;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.JDAUtils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

	private static final Logger log = LoggerFactory.getLogger(CommandListener.class);

	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		if(e.getMessage().getContentRaw().trim().startsWith(SettingsManager.getCommandPrefix(JDAUtils.getGuildFromCombinedEvent(e)))){
			if(e.getChannelType() != ChannelType.PRIVATE)
				if(!PermissionUtil.isInitialized(e.getGuild())){
					try{
						PermissionUtil.loadJson(e.getGuild());
					} catch(Throwable t){
						log.error(MarkerFactory.getMarker("FATAL"), "Error occured loading permissions.. unable to process"
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
