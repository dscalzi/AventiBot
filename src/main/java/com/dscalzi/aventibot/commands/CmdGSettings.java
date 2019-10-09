/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2019 Daniel D. Scalzi
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

package com.dscalzi.aventibot.commands;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.settings.GlobalConfig;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.InputUtils;
import com.dscalzi.aventibot.util.JDAUtils;
import com.dscalzi.aventibot.util.Pair;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CmdGSettings implements CommandExecutor{

	private final PermissionNode permUpdate = PermissionNode.get(NodeType.SUBCOMMAND, "gsettings", "update");
	private final PermissionNode permInfo = PermissionNode.get(NodeType.SUBCOMMAND, "gsettings", "info");

	public final Set<PermissionNode> nodes;
	
	public CmdGSettings(){
		nodes = new HashSet<>(Arrays.asList(
				permUpdate,
				permInfo
			));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		// Temporary until I rewrite this whole bot.
		if(!e.getAuthor().getId().equals("169197209630277642")) {
			return CommandResult.NO_PERMISSION;
		}
		if(args.length > 0) {
			
			if(args[0].equalsIgnoreCase("update")) {
				if(args.length > 1) {
					String prop = args[1].toLowerCase();
					for(Map.Entry<Pair<String, Object>, Method> entry : GlobalConfig.keyMap.entrySet()){
						if(entry.getKey().getKey().toLowerCase().equals(prop)){
							return genericStringUpdate(e, entry.getKey().getKey(), entry.getValue(), args);
						}
					}
					e.getChannel().sendMessage("Unknown settings key: `" + prop + "`.").queue();
					return CommandResult.ERROR;
				}
				e.getChannel().sendMessage("Proper usage is `" + SettingsManager.getCommandPrefix(JDAUtils.getGuildFromCombinedEvent(e)) + "gsettings update <key> <value>`").queue();
				return CommandResult.IGNORE;
			}
			
		}
		return null;
	}

	private CommandResult genericStringUpdate(MessageReceivedEvent e, String key, Method setter, String[] args){
		GlobalConfig current = SettingsManager.getGlobalConfig();
		
		if(args.length < 3){
			e.getChannel().sendMessage("Proper usage is " + SettingsManager.getCommandPrefix(JDAUtils.getGuildFromCombinedEvent(e)) + "gsettings update " + key.toLowerCase() + " <value>").queue();
			return CommandResult.ERROR;
		}
		try {
			String newVal = InputUtils.parseFullTerm(args, 2).getValue();
			setter.invoke(current, newVal);
			if(key.equals("currentGame")) {
				AventiBot.setCurrentGame(newVal);
			}
			SettingsManager.saveGlobalConfig(current);
		} catch (IllegalArgumentException e1) {
			e.getChannel().sendMessage("You have provided an improper value.").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		} catch (InvocationTargetException | IllegalAccessException e1) {
			e.getChannel().sendMessage("Error occured while updating the value.").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		} catch (IOException e1) {
			e.getChannel().sendMessage("Error occured while saving the new configuration.").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		}
		
		return CommandResult.SUCCESS;
		
	}

	@Override
	public Set<PermissionNode> provideNodes() {
		return nodes;
	}

}
