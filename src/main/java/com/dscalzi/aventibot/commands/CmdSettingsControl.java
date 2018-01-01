/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.commands;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.cmdutil.CommandDispatcher;
import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.settings.GuildConfig;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.IconUtil;

import javafx.util.Pair;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdSettingsControl implements CommandExecutor{

	private final PermissionNode permUpdate = PermissionNode.get(NodeType.SUBCOMMAND, "settings", "update");
	private final PermissionNode permInfo = PermissionNode.get(NodeType.SUBCOMMAND, "settings", "info");
	
	public final Set<PermissionNode> nodes;
	
	public CmdSettingsControl(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permUpdate,
					permInfo
				));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		if(e.getGuild() == null){
			e.getChannel().sendMessage("This command may only be used in guilds!").queue();
			return CommandResult.NO_PERMISSION;
		}
		
		if(args.length > 0){
			
			if(args[0].equalsIgnoreCase("update")){
				if(!PermissionUtil.hasPermission(e.getAuthor(), permUpdate, e.getGuild())){
					return CommandResult.NO_PERMISSION;
				}
				if(args.length > 1){
					String prop = args[1].toLowerCase();
					for(Map.Entry<Pair<String, Object>, Method> entry : GuildConfig.keyMap.entrySet()){
						if(entry.getKey().getKey().toLowerCase().equals(prop)){
							switch(prop){
							case "colorhex":
								return cmdUpdateColor(e, args);
							default:
								return genericStringUpdate(e, entry.getKey().getKey(), entry.getValue(), args);
							}
						}
					}
					e.getChannel().sendMessage("Unknown settings key: `" + prop + "`.").queue();
					return CommandResult.ERROR;
				}
				e.getChannel().sendMessage("Proper usage is `" + SettingsManager.getCommandPrefix(e.getGuild()) + "settings update <key> <value>`").queue();
				return CommandResult.IGNORE;
			}
			
			if(args[0].equalsIgnoreCase("info")){
				if(!PermissionUtil.hasPermission(e.getAuthor(), permInfo, e.getGuild())){
					return CommandResult.NO_PERMISSION;
				}
				if(args.length > 1){
					String prop = args[1].toLowerCase();
					for(Map.Entry<Pair<String, Object>, Method> entry : GuildConfig.keyMap.entrySet()){
						if(entry.getKey().getKey().toLowerCase().equals(prop)){
							return cmdInfo(e, entry.getKey().getKey(), entry.getKey().getValue());
						}
					}
					e.getChannel().sendMessage("Unknown settings key: `" + prop + "`.").queue();
					return CommandResult.ERROR;
				}
				return this.cmdInfoFull(e);
			}
			
			e.getChannel().sendMessage("Unknown subcommand: `" + args[0] + "`.").queue();
			return CommandResult.ERROR;
		}
		
		//TODO subcommand list
		e.getChannel().sendMessage("Subcommand list comming soon!").queue();
		return CommandResult.IGNORE;
	}
	
	private CommandResult genericStringUpdate(MessageReceivedEvent e, String key, Method setter, String[] args){
		GuildConfig current = SettingsManager.getGuildConfig(e.getGuild());
		
		if(args.length < 3){
			e.getChannel().sendMessage("Proper usage is " + SettingsManager.getCommandPrefix(e.getGuild()) + "settings update " + key.toLowerCase() + " <value>").queue();
			return CommandResult.ERROR;
		}
		try {
			setter.invoke(current, args[2]);
			SettingsManager.saveGuildConfig(e.getGuild(), current);
		} catch (IllegalArgumentException e1) {
			e.getChannel().sendMessage("You have provided an improper value.").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		} catch (InvocationTargetException | IllegalAccessException e1) {
			e.getChannel().sendMessage("Error occured while updating the value.").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		} catch (IOException e1) {
			e.getChannel().sendMessage("Error occured while saving the new configuration.").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		}
		
		return CommandResult.SUCCESS;
		
	}
	
	private CommandResult cmdUpdateColor(MessageReceivedEvent e, String[] args){
		GuildConfig g = SettingsManager.getGuildConfig(e.getGuild());
		if(args.length < 3){
			e.getChannel().sendMessage("Proper usage is " + SettingsManager.getCommandPrefix(e.getGuild()) + "settings update color [hex value]").queue();
			return CommandResult.ERROR;
		}
		try {
			Color.decode(args[2]);
			g.setColor(args[2]);
			SettingsManager.saveGuildConfig(e.getGuild(), g);
			return CommandResult.SUCCESS;
		} catch (NumberFormatException e1){
			e.getChannel().sendMessage("Invalid color, must be a valid hex color code.").queue();
			return CommandResult.ERROR;
		} catch (IOException e1) {
			e.getChannel().sendMessage("Unable to complete request, could not update configuration file.").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		}
	}
	
	private CommandResult cmdInfo(MessageReceivedEvent e, String key, Object def){
		GuildConfig current = SettingsManager.getGuildConfig(e.getGuild());
		Object val = getFieldValue(current, key);
		if(val == null){
			e.getChannel().sendMessage("Internal error while retrieving the settings value.").queue();
			return CommandResult.ERROR;
		}
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Value of `" + key + "`", null);
		eb.setDescription("`" + val + "` | [def `" + def + "`]");
		eb.setColor(current.getColorAWT());
		
		e.getChannel().sendMessage(eb.build()).queue();
		
		return CommandResult.SUCCESS;
	}
	
	private CommandResult cmdInfoFull(MessageReceivedEvent e){
		GuildConfig current = SettingsManager.getGuildConfig(e.getGuild());
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Configuration for " + e.getGuild().getName(), null);
		eb.setColor(current.getColorAWT());
		String proper = SettingsManager.getCommandPrefix(e.getGuild());
		if(proper.trim().equals(AventiBot.getInstance().getJDA().getSelfUser().getAsMention())){
			proper = CommandDispatcher.getDisplayedMention(e.getGuild()) + " ";
		}
		eb.setFooter("Narrow Search | " + proper + "settings info <key>.", IconUtil.INFO.getURL());
		
		
		for(Pair<String, Object> keys : GuildConfig.keyMap.keySet()){
			Object val = getFieldValue(current, keys.getKey());
			if(val == null) val = "ERROR";
			eb.appendDescription("**" + keys.getKey() + "**\n"
					+ "`" + val + "` | [def `" + keys.getValue() + "`]\n");
		}
		
		e.getChannel().sendMessage(eb.build()).queue();
		
		return CommandResult.SUCCESS;
	}
	
	private Object getFieldValue(GuildConfig src, String key){
		Object val = null;
		try {
			for(Field f : src.getClass().getDeclaredFields()){
				if(f.getName().equalsIgnoreCase(key)){
					f.setAccessible(true);
					val = f.get(src);
					f.setAccessible(false);
				}
			}
		}  catch (SecurityException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
			return null;
		}
		return val == null ? "null" : val;
	}
	

	@Override
	public Set<PermissionNode> provideNodes() {
		return nodes;
	}

}
