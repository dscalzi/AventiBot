/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.settings.GuildConfig;
import com.dscalzi.aventibot.settings.SettingsManager;

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
				switch(prop){
				case "color":
					return cmdUpdateColor(e, args);
				default:
					e.getChannel().sendMessage("Unknown settings property: `" + prop + "`.").queue();
					return CommandResult.ERROR;
				}
			}
			
			
		}
		
		
		return CommandResult.ERROR;
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
