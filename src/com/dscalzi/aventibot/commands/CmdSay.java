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
import com.dscalzi.aventibot.util.InputUtils;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdSay implements CommandExecutor{
	
	private final PermissionNode permSay = PermissionNode.get(NodeType.COMMAND, "say");
	
	public final Set<PermissionNode> nodes;
	
	public CmdSay(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permSay
				));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permSay, e.getGuild())) return CommandResult.NO_PERMISSION;
		
		if(args.length == 0){
			e.getChannel().sendMessage("Why are you trying to get me to say nothing.. lol").queue();
			return CommandResult.ERROR;
		}
		
		MessageChannel ch = (args.length > 0) ? InputUtils.parseChannel(e.getMessage(), args[0]) : null;
		
		if(ch != null && e.getGuild() != null){
			if(!e.getGuild().getTextChannels().contains(ch)){
				e.getChannel().sendMessage("I cannot message other guilds for you, sorry!").queue();
				return CommandResult.ERROR;
			}
		}
		
		String message = e.getMessage().getRawContent().substring((ch == null) ? e.getMessage().getRawContent().indexOf(cmd) + cmd.length() : e.getMessage().getRawContent().indexOf(rawArgs[0]) + rawArgs[0].length());
		MessageBuilder mb = new MessageBuilder();
		mb.append(message);
		
		if(ch == null) {
			if(e.getAuthor() instanceof ConsoleUser){
				e.getChannel().sendMessage("Please specify a valid channel!").queue();
				return CommandResult.ERROR;
			} else if(e.isFromType(ChannelType.PRIVATE))
				ch = e.getPrivateChannel();
			else
				ch = e.getTextChannel();
		}
		
		if(!(e.getMessage().isFromType(ChannelType.PRIVATE)))
			e.getMessage().delete().queue();
		
		ch.sendMessage(message).queue();
		
		return CommandResult.IGNORE;
	}

	@Override
	public Set<PermissionNode> getNodes() {
		return nodes;
	}

}
