package com.dscalzi.obsidianbot.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.cmdutil.PermissionNode;
import com.dscalzi.obsidianbot.cmdutil.PermissionUtil;
import com.dscalzi.obsidianbot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.obsidianbot.util.InputUtils;

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
			if(!ObsidianBot.getInstance().getCommandRegistry().getAllRegisteredNodes().contains(node)){
				e.getChannel().sendMessage("Unknown node, `" + node.toString() + "`, operation canceled.").queue();
				return;
			}
			String[] terms = new String[rawArgs.length-2];
			for(int i=0; i<terms.length; ++i) 
				terms[i] = rawArgs[i+2];
			Pair<Set<Role>,Set<String>> result = InputUtils.parseBulkRoles(terms, e.getGuild());
			e.getChannel().sendTyping().queue((v) -> {
				try {
					EmbedBuilder eb = new EmbedBuilder().setAuthor("Permission Add Results", null, "http://i.imgur.com/7OfFSFx.png").setDescription("Target node `" + node + "`").setColor(Color.decode("#df4efc"));
					if(result.getKey().size() != 0){
						
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
						
						Set<Role> successes = result.getKey();
						successes.removeAll(fails);
							
						Set<String> failedTerms = result.getValue();
							
						if(result.getKey().size() > 0){
							Set<String> rls = new HashSet<String>();
							for(Role r : successes) rls.add(r.getAsMention());
							eb.addField(new Field("Successfully Added", rls.toString(), true));
						}
						
						if(failedTerms.size() > 0)
							eb.addField(new Field("Failed Term(s)", failedTerms.toString(), true));
						
						if(fails.size() > 0){
							Set<String> rls = new HashSet<String>();
							for(Role r : fails) rls.add(r.getAsMention());
							eb.addField(new Field("Failed to Add (Already Allowed)", rls.toString(), true));
						}
						
						MessageEmbed em = eb.build();
						if(em.getFields().size() > 0) e.getChannel().sendMessage(em).queue();
					} else {
						eb.addField(new Field("Failed Term(s)", result.getValue().toString(), true));
						e.getChannel().sendMessage(eb.build()).queue();
						return;
					}
				} catch (IOException e1) {
					e.getChannel().sendMessage("Unexpected error, operation failed").queue();
					e1.printStackTrace();
					return;
				}
			});
			return;
		}
		e.getChannel().sendMessage("Proper usage: " + ObsidianBot.commandPrefix + "permissions add <node> <ranks>").queue();
	}
	
	private void cmdRemove(MessageReceivedEvent e, String[] rawArgs){
		
		if(!validate(e, permRemove)) return;
		
		if(rawArgs.length > 2){
			PermissionNode node = PermissionNode.get(rawArgs[1]);
			if(!ObsidianBot.getInstance().getCommandRegistry().getAllRegisteredNodes().contains(node)){
				e.getChannel().sendMessage("Unknown node, `" + node.toString() + "`, operation canceled.").queue();
				return;
			}
			String[] terms = new String[rawArgs.length-2];
			for(int i=0; i<terms.length; ++i) 
				terms[i] = rawArgs[i+2];
			Pair<Set<Role>,Set<String>> result = InputUtils.parseBulkRoles(terms, e.getGuild());
			e.getChannel().sendTyping().queue((v) -> {
				try {
					EmbedBuilder eb = new EmbedBuilder().setAuthor("Permission Remove Results", null, "http://i.imgur.com/voGutMQ.png").setDescription("Target node `" + node + "`").setColor(Color.decode("#df4efc"));
					if(result.getKey().size() != 0){
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
						
						Set<Role> successes = result.getKey();
						successes.removeAll(fails);
						
						Set<String> failedTerms = result.getValue();
						
						if(result.getKey().size() > 0){
							Set<String> rls = new HashSet<String>();
							for(Role r : successes) rls.add(r.getAsMention());
							eb.addField(new Field("Successfully Removed", rls.toString(), true));
						}
						
						if(failedTerms.size() > 0) 
							eb.addField(new Field("Failed Term(s)", failedTerms.toString(), true));
						
						if(fails.size() > 0){
							Set<String> rls = new HashSet<String>();
							for(Role r : fails) rls.add(r.getAsMention());
							eb.addField(new Field("Failed to Remove (Already not allowed)", rls.toString(), true));
						}
						
						MessageEmbed em = eb.build();
						if(em.getFields().size() > 0) e.getChannel().sendMessage(em).queue();
						return;
					} else{
						eb.addField(new Field("Failed Term(s)", result.getValue().toString(), true));
						e.getChannel().sendMessage(eb.build()).queue();
						return;
					}
				} catch (IOException e1) {
					e.getChannel().sendMessage("Unexpected error, operation failed").queue();
					e1.printStackTrace();
					return;
				}
			});
			return;
		}
		e.getChannel().sendMessage("Proper usage: " + ObsidianBot.commandPrefix + "permissions remove <node> <ranks>").queue();
		
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
			} else {
				roleTerm = rawArgs[1];
			}
			Role r = InputUtils.parseRole(roleTerm, e.getGuild());
			if(r == null){
				e.getChannel().sendMessage("Unable to find a role matching `" + roleTerm + "`.");
				return;
			}
			if(rawArgs.length > termStart){
				Set<PermissionNode> nodes = new HashSet<PermissionNode>();
				for(int i=termStart; i<rawArgs.length; ++i){
					nodes.add(PermissionNode.get(rawArgs[i]));
				}
				e.getChannel().sendTyping().queue((v) -> {
					EmbedBuilder eb = new EmbedBuilder().setAuthor("Permission Grant Results", null, "http://i.imgur.com/7OfFSFx.png").setDescription("Target Role " + r.getAsMention()).setColor(Color.decode("#df4efc"));
					Set<PermissionNode> invalids = new HashSet<PermissionNode>();
					Set<PermissionNode> registered = ObsidianBot.getInstance().getCommandRegistry().getAllRegisteredNodes();
					Set<PermissionNode> nodesCopy = new HashSet<PermissionNode>(nodes);
					for(PermissionNode pn : nodesCopy){
						if(!registered.contains(pn)) {
							invalids.add(pn);
							nodesCopy.remove(pn);
						}
					}
					if(nodesCopy.size() > 0){
						try {
							Set<PermissionNode> fails;
							if(nodesCopy.size() == 1){
								Boolean b = PermissionUtil.permissionAdd(nodes.iterator().next(), e.getGuild(), r);
								fails = b == null || !b ? new HashSet<PermissionNode>(nodesCopy) : new HashSet<PermissionNode>();
							} else
								fails = PermissionUtil.bulkPermissionGrant(r, e.getGuild(), nodesCopy);
							
							nodesCopy.removeAll(fails);
							
							if(nodesCopy.size() > 0){
								eb.addField(new Field("Successfully Granted", nodesCopy.toString(), true));
							}
							
							if(fails.size() > 0){
								eb.addField(new Field("Failed (already Granted)", fails.toString(), true));
							}
							
							if(invalids.size() > 0){
								eb.addField(new Field("Invalid Node(s)", invalids.toString(), true));
							}
							
							MessageEmbed em = eb.build();
							if(em.getFields().size() > 0) e.getChannel().sendMessage(em).queue();
							return;
						} catch (IOException e1) {
							e.getChannel().sendMessage("Unexpected error, operation failed").queue();
							e1.printStackTrace();
							return;
						}
					} else {
						eb.addField(new Field("Invalid Node(s)", invalids.toString(), true));
						e.getChannel().sendMessage(eb.build()).queue();
						return;
					}
				});
				return;
			}
		}
		e.getChannel().sendMessage("Proper usage: " + ObsidianBot.commandPrefix + "permissions grant <node> <ranks>").queue();
	}
	
	private void cmdRevoke(MessageReceivedEvent e, String[] args){
		if(!validate(e, permRevoke)) return;
		
	}
	
	private boolean validate(MessageReceivedEvent e, PermissionNode permission){
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permission, e.getGuild())) return false;
		
		return true;
	}
	
	@Override
	public Set<PermissionNode> getNodes() {
		return nodes;
	}

}
