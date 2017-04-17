/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.commands;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.settings.GuildConfig;
import com.dscalzi.aventibot.settings.SettingsManager;

import javafx.util.Pair;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdSettingsControl implements CommandExecutor{

	private final PermissionNode permUpdateColor = PermissionNode.get(NodeType.SUBCOMMAND, "settings", "update", "color");
	
	public final Set<PermissionNode> nodes;
	
	public CmdSettingsControl(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
				permUpdateColor
				));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(args.length > 0){
			
			if(args.length > 1){
				String prop = args[1].toLowerCase();
				for(Map.Entry<Pair<String, Object>, Method> entry : GuildConfig.keyMap.entrySet()){
					if(entry.getKey().getKey().toLowerCase().equals(prop)){
						switch(prop){
						case "colorhex":
							return cmdUpdateColor(e, args);
						default:
							return genericStringUpdate(e, entry.getKey().getKey(), entry.getValue(), args);
						}
					}
				}
			}
			
			
		}
		
		
		return CommandResult.ERROR;
	}
	
	private CommandResult genericStringUpdate(MessageReceivedEvent e, String key, Method setter, String[] args){
		GuildConfig current = SettingsManager.getGuildConfig(e.getGuild());
		
		if(args.length < 3){
			e.getChannel().sendMessage("Proper usage is " + current.getCommandPrefix() + "settings update " + key.toLowerCase() + " <value>").queue();
			return CommandResult.ERROR;
		}
		try {
			setter.invoke(current, args[2]);
			SettingsManager.saveGuildConfig(e.getGuild(), current);
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
	
	private CommandResult cmdUpdateColor(MessageReceivedEvent e, String[] args){
		GuildConfig g = SettingsManager.getGuildConfig(e.getGuild());
		if(args.length < 3){
			e.getChannel().sendMessage("Proper usage is " + g.getCommandPrefix() + "settings update color [hex value]").queue();
			return CommandResult.ERROR;
		}
		try {
			Color.decode(args[2]);
			g.setColor(args[2]);
			SettingsManager.saveGuildConfig(e.getGuild(), g);
			return CommandResult.SUCCESS;
		} catch (NumberFormatException e1){
			e.getChannel().sendMessage("Invalid color, must be a valid hex color code.").queue();
			return CommandResult.ERROR;
		} catch (IOException e1) {
			e.getChannel().sendMessage("Unable to complete request, could not update configuration file.").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		}
	}
	

	@Override
	public Set<PermissionNode> getNodes() {
		return nodes;
	}

}
