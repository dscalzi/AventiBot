/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.commands;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;


public class CmdAuthor implements CommandExecutor {
	
	private final PermissionNode permAuthor = PermissionNode.get(NodeType.COMMAND, "author");
	
	public final Set<PermissionNode> nodes;
	
	public CmdAuthor(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permAuthor
				));
	}
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permAuthor, e.getGuild(), true)) return false;
		
		User author = AventiBot.getInstance().getJDA().getUserById("169197209630277642");
		String avatar = author.getAvatarUrl();
		if(avatar == null) avatar = author.getDefaultAvatarUrl();
		
		EmbedBuilder b = new EmbedBuilder();
		b.setAuthor("Daniel Scalzi", "https://twitter.com/d_scalzi", avatar);
		b.setColor(Color.decode("#0f579d"));
		b.setDescription("AventiBot was developed by Daniel Scalzi (" + author.getAsMention() + ")");
		
		e.getChannel().sendMessage(b.build()).queue();
		
		return true;
	}

	@Override
	public Set<PermissionNode> getNodes() {
		return nodes;
	}

}
