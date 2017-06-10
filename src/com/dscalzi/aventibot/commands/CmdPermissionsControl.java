/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.cmdutil.PermissionResult;
import com.dscalzi.aventibot.util.IconUtil;
import com.dscalzi.aventibot.util.InputUtils;

import javafx.util.Pair;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class CmdPermissionsControl implements CommandExecutor{
	
	private final PermissionNode permGrant = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "grant");
	private final PermissionNode permRevoke = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "revoke");
	private final PermissionNode permBlacklist = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "blacklist");
	private final PermissionNode permUnblacklist = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "unblacklist");
	private final PermissionNode permEnable = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "enable");
	private final PermissionNode permDisable = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "disable");
	
	public final Set<PermissionNode> nodes;
	
	public CmdPermissionsControl(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
				permGrant,
				permRevoke,
				permBlacklist,
				permUnblacklist,
				permEnable,
				permDisable
				));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(args.length == 0){
			e.getChannel().sendMessage(constructSubcommandTree(e.getGuild())).queue();
			return CommandResult.ERROR;
		}
		
		String sub = args[0].toLowerCase();
		
		switch(sub){
		case "grant":
			return this.cmdWritePermissionChange(e, sub, true, rawArgs);
		case "revoke":
			return this.cmdWritePermissionChange(e, sub, false, rawArgs);
		case "blacklist":
			return this.cmdWriteBlacklistChange(e, sub, true, rawArgs);
		case "unblacklist":
			return this.cmdWriteBlacklistChange(e, sub, false, rawArgs);
		case "enable":
			return this.cmdWriteNodePermissionChange(e, sub, true, rawArgs);
		case "disable":
			return this.cmdWriteNodePermissionChange(e, sub, false, rawArgs);
		}
		
		e.getChannel().sendMessage("Unknown subcommand: *" + args[0] + "*.").queue();
		return CommandResult.ERROR;
	}
	
	private CommandResult cmdWritePermissionChange(MessageReceivedEvent e, String label, boolean add, String[] rawArgs){
		if(!PermissionUtil.hasPermission(e.getAuthor(), add ? permGrant : permRevoke, e.getGuild(), false)){
			return CommandResult.NO_PERMISSION;
		}
		
		String[] terms = new String[rawArgs.length-1];
		for(int i=0; i<terms.length; ++i) terms[i] = rawArgs[i+1];
		if(rawArgs.length < 3) return cmdWritePermissionFormat(e, label, add);
		
		Pair<String[], String[]> ps = partition(terms, add ? "to" : "from");
		if(ps == null) return cmdWritePermissionFormat(e, label, add);
			
		Pair<Set<Role>,Set<String>> roles = InputUtils.parseBulkRoles(ps.getValue(), e.getGuild());
		Pair<Set<PermissionNode>,Set<String>> nodes = PermissionUtil.validateNodes(new HashSet<String>(Arrays.asList(ps.getKey())));
		try {
			PermissionResult r = PermissionUtil.writePermissionChange(e.getGuild(), roles.getKey(), nodes.getKey(), add);
			for(String s : roles.getValue()) r.addInvalidRole(s);
			for(String s : nodes.getValue()) r.addInvalidNode(s);
			for(Role ro : roles.getKey()) r.addMentionable(ro);
			e.getChannel().sendMessage(r.construct(true)).queue();
			if(r.hasLog()){
				List<String> log = r.constructLog();
				for(String s : log) e.getChannel().sendMessage(s).queue();
			}
		} catch (IOException e1) {
			e.getChannel().sendMessage("Unexpected error, operation failed").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		}
		
		return CommandResult.SUCCESS;
	}
	
	private CommandResult cmdWritePermissionFormat(MessageReceivedEvent e, String label, boolean add){
		e.getChannel().sendMessage("Proper format is `" + SettingsManager.getCommandPrefix(e.getGuild()) + "permissions " + label + " <node(s)> " + (add ? "to" : "from") + " <role(s)>`").queue();
		return CommandResult.ERROR;
	}
	
	private CommandResult cmdWriteBlacklistChange(MessageReceivedEvent e, String label, boolean add, String[] rawArgs){
		if(!PermissionUtil.hasPermission(e.getAuthor(), add ? permBlacklist : permUnblacklist, e.getGuild(), false)){
			return CommandResult.NO_PERMISSION;
		}
		
		String[] terms = new String[rawArgs.length-1];
		for(int i=0; i<terms.length; ++i) terms[i] = rawArgs[i+1];
		if(rawArgs.length < 3) return cmdWriteBlacklistFormat(e, label, add);
		
		Pair<String[], String[]> ps = partition(terms, "for");
		if(ps == null) return cmdWriteBlacklistFormat(e, label, add);
		
		Pair<Set<User>,Set<String>> users = InputUtils.parseBulkMembers(ps.getValue(), e.getGuild());
		Pair<Set<PermissionNode>,Set<String>> nodes = PermissionUtil.validateNodes(new HashSet<String>(Arrays.asList(ps.getKey())));
		
		try {
			PermissionResult r = PermissionUtil.writeBlacklistChange(e.getGuild(), users.getKey(), nodes.getKey(), add);
			for(String s : users.getValue()) r.addInvalidUser(s);
			for(String s : nodes.getValue()) r.addInvalidNode(s);
			for(User u : users.getKey()) r.addMentionable(u);
			e.getChannel().sendMessage(r.construct(true)).queue();
			if(r.hasLog()){
				List<String> log = r.constructLog();
				for(String s : log) e.getChannel().sendMessage(s).queue();
			}
		} catch (IOException e1) {
			e.getChannel().sendMessage("Unexpected error, operation failed").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		}
		
		return CommandResult.SUCCESS;
	}
	
	private CommandResult cmdWriteBlacklistFormat(MessageReceivedEvent e, String label, boolean add){
		e.getChannel().sendMessage("Proper format is `" + SettingsManager.getCommandPrefix(e.getGuild()) + "permissions " + label + " <node(s)> for <users(s)>`").queue();
		return CommandResult.ERROR;
	}
	
	private Pair<String[], String[]> partition(String[] args, String term){
		for(int i=0; i<args.length; ++i){
			if(args[i].equalsIgnoreCase(term)){
				String[] first = new String[i];
				String[] second = new String[args.length-i-1];
				for(int k=0,g=0; k<args.length; ++k){
					if(k<i){
						first[k] = args[k];
					} else if(k>i){
						second[g] = args[k];
						++g;
					}
				}
				return new Pair<String[], String[]>(first, second);
			}
		}
		return null;
	}
	
	private CommandResult cmdWriteNodePermissionChange(MessageReceivedEvent e, String label, boolean enable, String[] rawArgs){
		if(!PermissionUtil.hasPermission(e.getAuthor(), enable ? permEnable : permDisable, e.getGuild(), false)){
			return CommandResult.NO_PERMISSION;
		}
		
		String[] terms = new String[rawArgs.length-1];
		for(int i=0; i<terms.length; ++i) terms[i] = rawArgs[i+1];
		if(rawArgs.length < 2){
			e.getChannel().sendMessage("Proper format is `" + SettingsManager.getCommandPrefix(e.getGuild()) + "permissions " + label + " <node(s)>`").queue();
			return CommandResult.ERROR;
		}
		
		Pair<Set<PermissionNode>,Set<String>> nodes = PermissionUtil.validateNodes(new HashSet<String>(Arrays.asList(terms)));
		
		try {
			PermissionResult r = PermissionUtil.writeNodeChange(e.getGuild(), nodes.getKey(), enable);
			for(String s : nodes.getValue()) r.addInvalidNode(s);
			e.getChannel().sendMessage(r.construct(true)).queue();
			if(r.hasLog()){
				List<String> log = r.constructLog();
				for(String s : log) e.getChannel().sendMessage(s).queue();
			}
		} catch (IOException e1) {
			e.getChannel().sendMessage("Unexpected error, operation failed").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		}
		
		return CommandResult.SUCCESS;
	}
	
	private MessageEmbed constructSubcommandTree(Guild g){
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setAuthor("Please choose a subcommand.", null, null);
		eb.setDescription("`grant` - Grant permissions to roles.\n"
				+ "`revoke` - Revoke permissions from roles.\n"
				+ "`blacklist` - Add users to a node's blacklist.\n"
				+ "`unblacklist` - Remove users from a node's blacklist.\n"
				+ "`enable` - Make a specific node require permission.\n"
				+ "`disable` - Disable a node's permission requirement.");
		eb.setFooter("Usage | " + SettingsManager.getCommandPrefix(g) + "permissions <subcommand>", IconUtil.INFO.getURL());
		eb.setColor(SettingsManager.getColorAWT(g));
		
		return eb.build();
	}
	
	@Override
	public Set<PermissionNode> getNodes() {
		return nodes;
	}

}
