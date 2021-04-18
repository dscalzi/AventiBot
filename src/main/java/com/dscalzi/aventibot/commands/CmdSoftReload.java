/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2021 Daniel D. Scalzi
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.console.ConsoleUser;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.JDAUtils;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CmdSoftReload implements CommandExecutor {

private final PermissionNode permSoftReload = PermissionNode.get(NodeType.COMMAND, "softreload");
	
	public final Set<PermissionNode> nodes;
	
	public CmdSoftReload(){
		nodes = new HashSet<>(Collections.singletonList(
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
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permSoftReload, JDAUtils.getGuildFromCombinedEvent(e), false)){
			return CommandResult.NO_PERMISSION;
		}
		
		// Private not allowed, e.getGuild() calls are safe.
		PermissionUtil.reload(e.getGuild());
		SettingsManager.reload(e.getGuild());
		e.getChannel().sendMessage("I've reloaded the settings and permissions for " + e.getGuild().getName() + ".").queue();
		return CommandResult.SUCCESS;
	}

	@Override
	public Set<PermissionNode> provideNodes() {
		return nodes;
	}

}
