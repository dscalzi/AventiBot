/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE for license information.
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

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdHelloWorld implements CommandExecutor{

	private final PermissionNode permHelloWorld = PermissionNode.get(NodeType.COMMAND, "helloworld");
	
	public final Set<PermissionNode> nodes;
	
	public CmdHelloWorld(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permHelloWorld
				));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permHelloWorld, e.getGuild(), true)) return CommandResult.NO_PERMISSION;
		
		e.getChannel().sendMessage("Hello, " + e.getAuthor().getAsMention() + "! Fine day, isn't it?").queue();
		
		return CommandResult.SUCCESS;
	}

	@Override
	public Set<PermissionNode> provideNodes() {
		return nodes;
	}

}
