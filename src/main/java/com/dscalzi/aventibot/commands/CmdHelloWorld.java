/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2018 Daniel D. Scalzi
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
