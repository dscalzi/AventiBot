/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.console.ConsoleUser;
import com.dscalzi.aventibot.settings.SettingsManager;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdSoftReload implements CommandExecutor {

private final PermissionNode permSoftReload = PermissionNode.get(NodeType.COMMAND, "softreload");
	
	public final Set<PermissionNode> nodes;
	
	public CmdSoftReload(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permSoftReload
				));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(e.getAuthor() instanceof ConsoleUser){
			PermissionUtil.reload();
			SettingsManager.reload();
			e.getChannel().sendMessage("Successfully reloaded all settings.").queue();
			return CommandResult.SUCCESS;
		}
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permSoftReload, e.getGuild(), false)){
			return CommandResult.NO_PERMISSION;
		}
		
		PermissionUtil.reload(e.getGuild());
		SettingsManager.reload(e.getGuild());
		e.getChannel().sendMessage("I've reloaded the settings and permissions for " + e.getGuild().getName() + ".").queue();
		return CommandResult.SUCCESS;
	}

	@Override
	public Set<PermissionNode> getNodes() {
		return nodes;
	}

}
