/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2020 Daniel D. Scalzi
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.cmdline.CommandLineExecutor;
import com.dscalzi.aventibot.cmdutil.CommandDispatcher;
import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.util.JDAUtils;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CmdHardRestart implements CommandExecutor{

	private final PermissionNode permHardRestart = PermissionNode.get(NodeType.COMMAND, "hardrestart");
	
	public final Set<PermissionNode> nodes;
	
	public CmdHardRestart(){
		nodes = new HashSet<>(Collections.singletonList(
				permHardRestart
		));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		if(!PermissionUtil.hasPermission(e.getAuthor(), permHardRestart, JDAUtils.getGuildFromCombinedEvent(e), false)){
			return CommandResult.NO_PERMISSION;
		}
		
		try {
			ProcessBuilder builder = null;
			if(CommandLineExecutor.usingCmdLine()){
				if(!CommandLineExecutor.headless()){
					e.getChannel().sendMessage("Restarting is not supported for terminal based command line startup.").queue();
					return CommandResult.ERROR;
				} else {
					builder = new ProcessBuilder("java", "-jar", AventiBot.getDataPathFull(), "--cmdline", "--headless");
				}
			} else {
				builder = new ProcessBuilder("java", "-jar", AventiBot.getDataPathFull(), "--abNow");
			}
			e.getChannel().sendMessage("Restarting..").queue();
			builder.start();
			CommandDispatcher.displayResult(CommandResult.SUCCESS, e.getMessage(), v -> {
				try {
					if(AventiBot.getStatus() == BotStatus.CONNECTED){
						AventiBot.getInstance().shutdown();
					}
				} catch (Exception ex){
					//Shutdown
					Runtime.getRuntime().exit(0);
				}
				Runtime.getRuntime().exit(0);
			});
		} catch (IOException e1) {
			e.getChannel().sendMessage("Failed to restart..").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		}
		
		
		return CommandResult.IGNORE;
	}

	@Override
	public Set<PermissionNode> provideNodes() {
		return nodes;
	}

	
	
}
