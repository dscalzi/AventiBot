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

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.util.InputUtils;
import com.dscalzi.aventibot.util.TimeUtils;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdClear implements CommandExecutor{

	private volatile boolean processing;
	private volatile long lastRun;
	
	private final PermissionNode permClear = PermissionNode.get(NodeType.COMMAND, "clear");
	
	public final Set<PermissionNode> nodes;
	
	public CmdClear(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permClear
				));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permClear, e.getGuild())) return CommandResult.NO_PERMISSION;
		
		if(processing){
			e.getChannel().sendMessage("I'm currently clearing out a channel, try again later!").queue();
			return CommandResult.ERROR;
		}
		
		long timeLeft = System.currentTimeMillis() - lastRun;
		if(timeLeft < 10000){
			timeLeft =  10 - (timeLeft/1000L);
			e.getChannel().sendMessage("This command is currently in cooldown, please try again in " + timeLeft + " seconds.").queue();
			return CommandResult.ERROR;
		}
		
		//The default text channel is the one the command was sent from.
		TextChannel channel = e.isFromType(ChannelType.PRIVATE) ? null : (TextChannel) e.getChannel();
		//Default target is everyone.
		User target = null;
		//Default limit for clearing a channel is 50 messages. Max is 100
		int limit = 50;
		//Default threshold is 1 hour.
		long threshold = OffsetDateTime.now().toEpochSecond() - 3600;
		
		for(int i=0; i<args.length; ++i){
			if(i+1 <args.length){
				if(args[i].equalsIgnoreCase("-a")){
					try{
						limit = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException ex){
						e.getChannel().sendMessage("Sorry, but you didn't give me an integer for the amount parameter (-a).").queue();
						return CommandResult.ERROR;
					}
					if(limit > 100) limit = 100;
					if(limit < 1){
						e.getChannel().sendMessage("I cannot delete less than one message, sorry.").queue();
						return CommandResult.ERROR;
					}
					++i;
				} else if(args[i].equalsIgnoreCase("-u")){
					target = InputUtils.parseUser(e.getMessage(), rawArgs[i+1]);
					if(target == null){
						e.getChannel().sendMessage("Sorry, but I couldn't find the user you specified.").queue();
						return CommandResult.ERROR;
					}
					++i;
				} else if(args[i].equalsIgnoreCase("-c")){
					channel = InputUtils.parseChannel(e.getMessage(), args[i+1]);
					if(channel == null){
						e.getChannel().sendMessage("Sorry, I could not find the channel you specified.").queue();
						return CommandResult.ERROR;
					}
					++i;
				} else if(args[i].equalsIgnoreCase("-t")){
					try {
						threshold = TimeUtils.parseDateDiff(args[i+1], false);
					} catch (Exception e1) {
						e.getChannel().sendMessage("Invalid date format.").queue();
						return CommandResult.ERROR;
					}
					++i;
				}
			}
		}
		
		if(channel == null){
			e.getChannel().sendMessage("You must specify a channel.").queue();
			return CommandResult.ERROR;
		}
		if(!e.getGuild().getTextChannels().contains(channel)){
			e.getChannel().sendMessage("I cannot clear messages in other guilds for you, sorry!").queue();
			return CommandResult.ERROR;
		}
		
		if(e.getChannel().equals(channel))
			e.getMessage().delete();
		
		this.clear(limit, threshold, channel, target, e.getChannel());
		
		return CommandResult.SUCCESS;
	}
	
	private void clear(int limit, long threshold, TextChannel channel, User target, MessageChannel origin){
		if(limit > 100 | limit < 1) return;
		processing = true;
		
		channel.getHistory().retrievePast(limit).queue((history) -> {
			int deleted = delete(history, threshold, target);	
			
			//TODO Make the timestamp more user-friendly. Rather than "sice x GMT", "in the past hour";
			String successPt1 = "";
			String successPt2 = "";
			if(deleted > 0){
				DateTimeFormatter format = DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US).withZone(ZoneOffset.UTC);
				successPt1 = "Clearing " + deleted + " message" + (deleted == 1 ? "" : "s");
				successPt2 = " that " + (deleted == 1 ? "has" : "have") + " been sent " + ((target != null) ? "by " + target.getAsMention() + " " : "") + 
						"since " + format.format(Instant.ofEpochSecond(threshold)) + ".";
			} else {
				successPt1 = "No messages were deleted";
			}
			
			if(!origin.equals(channel)){
				if(deleted > 0) channel.sendMessage(successPt1 + successPt2);
				origin.sendMessage(successPt1 + " from "+ channel.getAsMention() + successPt2).queue();
			} else {
				channel.sendMessage(successPt1 + successPt2).queue();
			}
			
			processing = false;
			lastRun = System.currentTimeMillis();
		}, (exception) -> {
			processing = false;
			lastRun = System.currentTimeMillis();
		});
	}
	
	private int delete(List<Message> history, long threshold, User target){
		int deleted = 0;
		for(int i=0; i<history.size(); ++i){
			Message msg = history.get(i);
			if(msg.getCreationTime().toEpochSecond() < threshold)
				break;
			if(target != null && !msg.getAuthor().equals(target))
				continue;
			msg.delete().queue();
			++deleted;
			history.remove(i);
			--i;
		}
		return deleted;
	}

	@Override
	public Set<PermissionNode> provideNodes() {
		return nodes;
	}

}
