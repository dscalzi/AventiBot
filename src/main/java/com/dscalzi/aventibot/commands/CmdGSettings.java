/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdGSettings implements CommandExecutor{

	private final PermissionNode permUpdate = PermissionNode.get(NodeType.SUBCOMMAND, "gsettings", "update");
	private final PermissionNode permInfo = PermissionNode.get(NodeType.SUBCOMMAND, "gsettings", "info");
	
	public final Set<PermissionNode> nodes;
	
	public CmdGSettings(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
				permUpdate,
				permInfo
			));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PermissionNode> provideNodes() {
		return nodes;
	}

}
