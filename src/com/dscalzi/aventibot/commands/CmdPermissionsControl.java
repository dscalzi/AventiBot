/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.util.InputUtils;

import javafx.util.Pair;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;

public class CmdPermissionsControl implements CommandExecutor{

	private final PermissionNode permAdd = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "add");
	private final PermissionNode permRemove = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "remove");
	private final PermissionNode permGrant = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "grant");
	private final PermissionNode permRevoke = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "revoke");
	
	public final Set<PermissionNode> nodes;
	
	public CmdPermissionsControl(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
				permAdd,
				permRemove,
				permGrant,
				permRevoke
				));
	}
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(args.length == 0){
			e.getChannel().sendMessage("Please choose a subcommand.").queue();
			return false;
		}
		
		switch(args[0]){
		case "remove":
			this.cmdRemove(e, rawArgs);
			break;
		case "add":
			this.cmdAdd(e, rawArgs);
			break;
		case "revoke":
			this.cmdRevoke(e, rawArgs);
			break;
		case "grant":
			this.cmdGrant(e, rawArgs);
			break;
		}
		
		return false;
	}
	
	private void cmdAdd(MessageReceivedEvent e, String[] rawArgs){
		if(!validate(e, permAdd)) return;
		
		if(rawArgs.length > 2){
			PermissionNode node = PermissionNode.get(rawArgs[1]);
			if(!AventiBot.getInstance().getCommandRegistry().getAllRegisteredNodes().contains(node)){
				e.getChannel().sendMessage("Unknown node, `" + node.toString() + "`, operation canceled.").queue();
				return;
			}
			String[] terms = new String[rawArgs.length-2];
			for(int i=0; i<terms.length; ++i) terms[i] = rawArgs[i+2];
			Pair<Set<Role>,Set<String>> result = InputUtils.parseBulkRoles(terms, e.getGuild());
			e.getChannel().sendTyping().queue((v) -> {
				EmbedBuilder eb = new EmbedBuilder().setAuthor("Permission Add Results", null, "http://i.imgur.com/7OfFSFx.png").setDescription("Target node `" + node + "`").setColor(SettingsManager.getGlobalConfig().getBotColor());
				if(result.getKey().size() != 0){	
					try {
						
						Set<Role> fails;
						if(result.getKey().size() == 1){
							Boolean b = PermissionUtil.permissionAdd(node, e.getGuild(), result.getKey().iterator().next());
							fails = b == null ? null : (b ? new HashSet<Role>() : new HashSet<Role>(result.getKey()));
						} else
							fails = PermissionUtil.bulkPermissionAdd(node, e.getGuild(), result.getKey());
						
						if(fails == null) {
							e.getChannel().sendMessage("Node does not require permission, operation canceled.").queue();
							return;
						}
						
						result.getKey().removeAll(fails);
							
						if(result.getKey().size() > 0) eb.addField(new Field("Successfully Added", convertRolesToMentions(result.getKey()).toString(), true));
						
						if(fails.size() > 0) eb.addField(new Field("Failed to Add (Already Allowed)", convertRolesToMentions(fails).toString(), true));
					} catch (IOException e1) {
						e.getChannel().sendMessage("Unexpected error, operation failed").queue();
						e1.printStackTrace();
						return;
					}
				}
				if(result.getValue().size() > 0) eb.addField(new Field("Failed Term(s)", result.getValue().toString(), true));
				
				MessageEmbed em = eb.build();
				if(em.getFields().size() > 0) e.getChannel().sendMessage(em).queue();
				return;
			});
			return;
		}
		e.getChannel().sendMessage("Proper usage: " + AventiBot.commandPrefix + "permissions add <node> <ranks>").queue();
	}
	
	private void cmdRemove(MessageReceivedEvent e, String[] rawArgs){
		
		if(!validate(e, permRemove)) return;
		
		if(rawArgs.length > 2){
			PermissionNode node = PermissionNode.get(rawArgs[1]);
			if(!AventiBot.getInstance().getCommandRegistry().getAllRegisteredNodes().contains(node)){
				e.getChannel().sendMessage("Unknown node, `" + node.toString() + "`, operation canceled.").queue();
				return;
			}
			String[] terms = new String[rawArgs.length-2];
			for(int i=0; i<terms.length; ++i) terms[i] = rawArgs[i+2];
			Pair<Set<Role>,Set<String>> result = InputUtils.parseBulkRoles(terms, e.getGuild());
			e.getChannel().sendTyping().queue((v) -> {
				EmbedBuilder eb = new EmbedBuilder().setAuthor("Permission Remove Results", null, "http://i.imgur.com/voGutMQ.png").setDescription("Target node `" + node + "`").setColor(SettingsManager.getGlobalConfig().getBotColor());
				if(result.getKey().size() != 0){
					try {
						Set<Role> fails;
						if(result.getKey().size() == 1){
							Boolean b = PermissionUtil.permissionRemove(node, e.getGuild(), result.getKey().iterator().next());
							fails = b == null ? null : (b ? new HashSet<Role>() : new HashSet<Role>(result.getKey()));
						} else
							fails = PermissionUtil.bulkPermissionRemove(node, e.getGuild(), result.getKey());
						
						if(fails == null) {
							e.getChannel().sendMessage("Node does not require permission, operation canceled.").queue();
							return;
						}
						
						result.getKey().removeAll(fails);
						
						if(result.getKey().size() > 0) eb.addField(new Field("Successfully Removed", convertRolesToMentions(result.getKey()).toString(), true));
						
						if(fails.size() > 0) eb.addField(new Field("Failed to Remove (Already not allowed)", convertRolesToMentions(fails).toString(), true));
					} catch (IOException e1) {
						e.getChannel().sendMessage("Unexpected error, operation failed").queue();
						e1.printStackTrace();
						return;
					}
				}
				
				if(result.getValue().size() > 0) 
					eb.addField(new Field("Failed Term(s)", result.getValue().toString(), true));
				
				MessageEmbed em = eb.build();
				if(em.getFields().size() > 0) e.getChannel().sendMessage(em).queue();
				return;
			});
			return;
		}
		e.getChannel().sendMessage("Proper usage: " + AventiBot.commandPrefix + "permissions remove <node> <ranks>").queue();
		
	}
	
	private void cmdGrant(MessageReceivedEvent e, String[] rawArgs){
		if(!validate(e, permGrant)) return;
		
		if(rawArgs.length > 1){
			int termStart = 2;
			String roleTerm;
			if(rawArgs[1].substring(0, 1).matches("['\"]")){
				Pair<Integer, String> loopResults = InputUtils.parseFullTerm(rawArgs, 1);
				roleTerm = loopResults.getValue();
				termStart = loopResults.getKey();
			} else 
				roleTerm = rawArgs[1];
			Role r = InputUtils.parseRole(roleTerm, e.getGuild());
			if(r == null){
				e.getChannel().sendMessage("Unable to find a role matching `" + roleTerm + "`.");
				return;
			}
			if(rawArgs.length > termStart){
				Set<PermissionNode> nodes = new HashSet<PermissionNode>();
				for(int i=termStart; i<rawArgs.length; ++i)	nodes.add(PermissionNode.get(rawArgs[i]));
				Set<PermissionNode> invalids = new HashSet<PermissionNode>();
				Set<PermissionNode> registered = AventiBot.getInstance().getCommandRegistry().getAllRegisteredNodes();
				final Iterator<PermissionNode> it = nodes.iterator();
				while(it.hasNext()){
					PermissionNode pn = it.next();
					if(!registered.contains(pn)){
						invalids.add(pn);
						it.remove();
					}
				}
				e.getChannel().sendTyping().queue((v) -> {
					EmbedBuilder eb = new EmbedBuilder().setAuthor("Permission Grant Results", null, "http://i.imgur.com/7OfFSFx.png").setDescription("Target Role " + r.getAsMention()).setColor(SettingsManager.getGlobalConfig().getBotColor());
					if(nodes.size() > 0){
						try {
							Set<PermissionNode> fails;
							if(nodes.size() == 1){
								Boolean b = PermissionUtil.permissionAdd(nodes.iterator().next(), e.getGuild(), r);
								fails = b == null || !b ? new HashSet<PermissionNode>(nodes) : new HashSet<PermissionNode>();
							} else
								fails = PermissionUtil.bulkPermissionGrant(r, e.getGuild(), nodes);
							
							nodes.removeAll(fails);
							
							if(nodes.size() > 0) eb.addField(new Field("Successfully Granted", nodes.toString(), true));
							
							if(fails.size() > 0) eb.addField(new Field("Failed (Already Granted)", fails.toString(), true));
						} catch (IOException e1) {
							e.getChannel().sendMessage("Unexpected error, operation failed").queue();
							e1.printStackTrace();
							return;
						}
					} 
					
					if(invalids.size() > 0)	eb.addField(new Field("Invalid Node(s)", invalids.toString(), true));
					
					MessageEmbed em = eb.build();
					if(em.getFields().size() > 0) e.getChannel().sendMessage(em).queue();
					return;
				});
				return;
			}
		}
		e.getChannel().sendMessage("Proper usage: " + AventiBot.commandPrefix + "permissions grant <node> <ranks>").queue();
	}
	
	private void cmdRevoke(MessageReceivedEvent e, String[] rawArgs){
		if(!validate(e, permRevoke)) return;
		
		if(rawArgs.length > 1){
			int termStart = 2;
			String roleTerm;
			if(rawArgs[1].substring(0, 1).matches("['\"]")){
				Pair<Integer, String> loopResults = InputUtils.parseFullTerm(rawArgs, 1);
				roleTerm = loopResults.getValue();
				termStart = loopResults.getKey();
			} else 
				roleTerm = rawArgs[1];
			Role r = InputUtils.parseRole(roleTerm, e.getGuild());
			if(r == null){
				e.getChannel().sendMessage("Unable to find a role matching `" + roleTerm + "`.");
				return;
			}
			if(rawArgs.length > termStart){
				Set<PermissionNode> nodes = new HashSet<PermissionNode>();
				for(int i=termStart; i<rawArgs.length; ++i)	nodes.add(PermissionNode.get(rawArgs[i]));
				Set<PermissionNode> invalids = new HashSet<PermissionNode>();
				Set<PermissionNode> registered = AventiBot.getInstance().getCommandRegistry().getAllRegisteredNodes();
				final Iterator<PermissionNode> it = nodes.iterator();
				while(it.hasNext()){
					PermissionNode pn = it.next();
					if(!registered.contains(pn)){
						invalids.add(pn);
						it.remove();
					}
				}
				e.getChannel().sendTyping().queue((v) -> {
					EmbedBuilder eb = new EmbedBuilder().setAuthor("Permission Revoke Results", null, "http://i.imgur.com/voGutMQ.png").setDescription("Target Role " + r.getAsMention()).setColor(SettingsManager.getGlobalConfig().getBotColor());
					if(nodes.size() > 0){
						try {
							Set<PermissionNode> fails;
							if(nodes.size() == 1){
								Boolean b = PermissionUtil.permissionRemove(nodes.iterator().next(), e.getGuild(), r);
								fails = b == null || !b ? new HashSet<PermissionNode>(nodes) : new HashSet<PermissionNode>();
							} else
								fails = PermissionUtil.bulkPermissionRevoke(r, e.getGuild(), nodes);
							
							nodes.removeAll(fails);
							
							if(nodes.size() > 0) eb.addField(new Field("Successfully Revoked", nodes.toString(), true));
							
							if(fails.size() > 0) eb.addField(new Field("Failed (Already Revoked)", fails.toString(), true));
						} catch (IOException e1) {
							e.getChannel().sendMessage("Unexpected error, operation failed").queue();
							e1.printStackTrace();
							return;
						}
					} 
					
					if(invalids.size() > 0)	eb.addField(new Field("Invalid Node(s)", invalids.toString(), true));
					
					MessageEmbed em = eb.build();
					if(em.getFields().size() > 0) e.getChannel().sendMessage(em).queue();
					return;
				});
				return;
			}
		}
		e.getChannel().sendMessage("Proper usage: " + AventiBot.commandPrefix + "permissions revoke <node> <ranks>").queue();
	}
	
	private boolean validate(MessageReceivedEvent e, PermissionNode permission){
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permission, e.getGuild())) return false;
		
		return true;
	}
	
	private Set<String> convertRolesToMentions(Set<Role> roles){
		Set<String> rls = new HashSet<String>();
		for(Role r : roles) rls.add(r.getAsMention());
		return rls;
	}
	
	@Override
	public Set<PermissionNode> getNodes() {
		return nodes;
	}

}
