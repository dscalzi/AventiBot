/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2019 Daniel D. Scalzi
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
import com.dscalzi.aventibot.console.ConsoleUser;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.InputUtils;
import com.dscalzi.aventibot.util.Pair;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdRoleId implements CommandExecutor{

	private final PermissionNode permRoleId = PermissionNode.get(NodeType.COMMAND, "roleid");
	
	public final Set<PermissionNode> nodes;
	
	public CmdRoleId(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permRoleId
				));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permRoleId, e.getGuild())) return CommandResult.NO_PERMISSION;
		
		if(e.getChannelType().equals(ChannelType.PRIVATE)){
			e.getPrivateChannel().sendMessage("You must use this command in a guild.").queue();
			return CommandResult.ERROR;
		}
		
		if(args.length == 0){
			e.getChannel().sendMessage("Please give me one or more ranks to look up.").queue();
			return CommandResult.ERROR;
		}
		
		Pair<Set<Role>, Set<String>> results = InputUtils.parseBulkRoles(rawArgs, e.getGuild());
		Set<Role> roles = results.getKey();
		Set<String> failedTerms = results.getValue();
		
		if(failedTerms.size() > 0){
			e.getChannel().sendMessage("No results for the term" + (failedTerms.size() == 1 ? "" : "s") + " " + failedTerms).queue();
			if(roles.size() == 0) return CommandResult.ERROR;
		}
		
		//TODO make console friendly, first arg must be guild id
		if(e.getAuthor() instanceof ConsoleUser){
			for(Role r : roles){
				e.getChannel().sendMessage(r.getName() + " --> " + r.getId()).queue();
			}
		} else {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(roles.size() == 1 ? roles.iterator().next().getColor() : SettingsManager.getColorAWT(e.getGuild()));
			String desc = "";
			for(Role r : roles){
				desc += r.getAsMention() + "\n`" + r.getId() + "`\n";
			}
			eb.setDescription(desc);
			e.getChannel().sendMessage(eb.build()).queue();
		}
		
		return CommandResult.SUCCESS;
	}

	@Override
	public Set<PermissionNode> provideNodes() {
		return nodes;
	}

}
