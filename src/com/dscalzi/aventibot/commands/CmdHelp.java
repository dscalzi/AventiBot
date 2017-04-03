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
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdHelp implements CommandExecutor{

	private final PermissionNode permHelp = PermissionNode.get(NodeType.COMMAND, "help", true);
	
	public final Set<PermissionNode> nodes;
	
	public CmdHelp(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permHelp
				));
	}
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permHelp, e.getGuild(), true)) return false;
		
		String msg = "Help message coming soon!";
		
		e.getAuthor().openPrivateChannel().queue((pc) -> pc.sendMessage(msg).queue());
		
		return true;
	}

	@Override
	public Set<PermissionNode> getNodes() {
		return nodes;
	}

}
