/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdShutdown implements CommandExecutor{
	
	private final PermissionNode permShutdown = PermissionNode.get(NodeType.COMMAND, "shutdown");
	
	public final Set<PermissionNode> nodes;
	
	public CmdShutdown(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permShutdown
				));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permShutdown, e.getGuild(), false)) return CommandResult.NO_PERMISSION;
		
		e.getChannel().sendMessage("Shutting down.. :(").queue(v -> {
			try {
				if(AventiBot.getStatus() == BotStatus.CONNECTED){
					AventiBot.getInstance().shutdown();
				}
			} catch (Exception ex){
				//Shutdown
				Runtime.getRuntime().exit(0);
			}
		});
		
		//The JDA should be shutdown, so the result is null.
		return null;
	}

	@Override
	public Set<PermissionNode> provideNodes() {
		return nodes;
	}
	
}
