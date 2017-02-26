package com.dscalzi.obsidianbot.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import net.dv8tion.jda.core.entities.MessageEmbed.Field;

public class CmdPermissionsControl implements CommandExecutor{

	private final PermissionNode permAdd = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "add");
	private final PermissionNode permRemove = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "remove");
	private final PermissionNode permBulkAdd = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "bulkadd");
	private final PermissionNode permBulkRemove = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "bulkremove");
	
	public final Set<PermissionNode> nodes;
	
	public CmdPermissionsControl(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
				permAdd,
				permRemove,
				permBulkAdd,
				permBulkRemove
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
			this.cmdRemove(e, args);
			break;
		case "add":
			this.cmdAdd(e, args);
			break;
		case "bulkremove":
			this.cmdBulkRemove(e, args);
			break;
		case "bulkadd":
			this.cmdBulkAdd(e, args);
			break;
		}
		
		return false;
	}

	private void cmdAdd(MessageReceivedEvent e, String[] args){
		if(!validate(e, permAdd)) return;
		if(args.length > 1){
			String node = args[1];
			if(args.length > 2){
				Role r = InputUtils.parseRole(e.getMessage(), args[2], e.getGuild());
				if(r == null){
					e.getChannel().sendMessage("Couldn't find a role matching " + args[2] + ", operation failed.").queue();
					return;
				}
				e.getChannel().sendTyping().queue((v) -> {
					try {
						Boolean result = PermissionUtil.permissionAdd(PermissionNode.get(node), e.getGuild(), r);
						
						if(result == null){
							e.getChannel().sendMessage("Permission node `" + node + "` either does not require permission, or is invalid.").queue();
							return;
						}
						
						if(result){
							e.getChannel().sendMessage("Successfully given " + r.getAsMention() + " permission for `" + node + "`.").queue();
							return;
						} else {
							e.getChannel().sendMessage(r.getAsMention() + " already has permission for `" + node + "`.").queue();
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
		}
		e.getChannel().sendMessage("Proper usage: " + ObsidianBot.commandPrefix + "permissions add <node> <rank>").queue();
	}
	
	private void cmdRemove(MessageReceivedEvent e, String[] args){
		if(!validate(e, permRemove)) return;
		if(args.length > 1){
			String node = args[1];
			if(args.length > 2){
				Role r = InputUtils.parseRole(e.getMessage(), args[2], e.getGuild());
				if(r == null){
					e.getChannel().sendMessage("Couldn't find a role matching " + args[2] + ", operation failed.").queue();
					return;
				}
				e.getChannel().sendTyping().queue((v) -> {
					try {
						Boolean result = PermissionUtil.permissionRemove(PermissionNode.get(node), e.getGuild(), r);
						
						if(result == null){
							e.getChannel().sendMessage("Permission node `" + node + "` either does not require permission, or is invalid.").queue();
							return;
						}
						
						if(result){
							e.getChannel().sendMessage("Successfully removed permission for `" + node + "` from " + r.getAsMention() + ".").queue();
							return;
						} else {
							e.getChannel().sendMessage(r.getAsMention() + " already doesn't have permission for `" + node + "`.").queue();
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
		}
		e.getChannel().sendMessage("Proper usage: " + ObsidianBot.commandPrefix + "permissions remove <node> <rank>").queue();
	}
	
	private void cmdBulkAdd(MessageReceivedEvent e, String[] args){
		if(!validate(e, permBulkAdd)) return;
		
		if(args.length > 1){
			String node = args[1];
			if(args.length > 2){
				if(args.length-2 == 0) {
					e.getChannel().sendMessage("Proper usage: " + ObsidianBot.commandPrefix + "permissions bulkadd <node> <ranks>").queue();
					return;
				}
				String[] terms = new String[args.length-2];
				for(int i=0; i<terms.length; ++i) 
					terms[i] = args[i+2];
				Pair<Set<Role>,Set<String>> result = InputUtils.parseBulkRoles(e.getMessage(), e.getGuild(), terms);
				e.getChannel().sendTyping().queue((v) -> {
					try {
						EmbedBuilder eb = new EmbedBuilder().setAuthor("Bulk Add Results", null, "http://i.imgur.com/7OfFSFx.png").setDescription("Target node `" + node + "`").setColor(Color.decode("#df4efc"));
						if(result.getKey().size() != 0){
							
							Set<Role> fails;
							if(result.getKey().size() == 1){
								Boolean b = PermissionUtil.permissionAdd(PermissionNode.get(node), e.getGuild(), result.getKey().iterator().next());
								fails = b == null ? null : (b ? new HashSet<Role>() : new HashSet<Role>(result.getKey()));
							} else
								fails = PermissionUtil.bulkPermissionAdd(PermissionNode.get(node), e.getGuild(), result.getKey());
							
							if(fails == null) {
								e.getChannel().sendMessage("Node does not require permission, operation canceled.").queue();
								return;
							}
							
							Set<Role> successes = result.getKey();
							successes.removeAll(fails);
							
							Set<String> failedTerms = result.getValue();
							List<Field> fields = new ArrayList<Field>();
							
							if(result.getKey().size() > 0){
								Set<String> rls = new HashSet<String>();
								for(Role r : successes) rls.add(r.getAsMention());
								fields.add(new Field("Successfully Added", rls.toString(), true));
							}
							
							if(failedTerms.size() > 0)
								fields.add(new Field("Failed Term(s)", failedTerms.toString(), true));
							
							if(fails.size() > 0){
								Set<String> rls = new HashSet<String>();
								for(Role r : fails) rls.add(r.getAsMention());
								fields.add(new Field("Failed to Add (Already Allowed)", rls.toString(), true));
							}
							
							for(Field f : fields) eb.addField(f);
							
							if(fields.size() > 0) e.getChannel().sendMessage(eb.build()).queue();
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
		}
		e.getChannel().sendMessage("Proper usage: " + ObsidianBot.commandPrefix + "permissions bulkadd <node> <ranks>").queue();
	}
	
	private void cmdBulkRemove(MessageReceivedEvent e, String[] args){
		
		if(!validate(e, permBulkRemove)) return;
		
		if(args.length > 1){
			String node = args[1];
			if(args.length > 2){
				if(args.length-2 == 0){
					e.getChannel().sendMessage("Proper usage: " + ObsidianBot.commandPrefix + "permissions bulkremove <node> <ranks>").queue();
					return;
				}
				String[] terms = new String[args.length-2];
				for(int i=0; i<terms.length; ++i) 
					terms[i] = args[i+2];
				Pair<Set<Role>,Set<String>> result = InputUtils.parseBulkRoles(e.getMessage(), e.getGuild(), terms);
				e.getChannel().sendTyping().queue((v) -> {
					try {
						EmbedBuilder eb = new EmbedBuilder().setAuthor("Bulk Remove Results", null, "http://i.imgur.com/voGutMQ.png").setDescription("Target node `" + node + "`").setColor(Color.decode("#df4efc"));
						if(result.getKey().size() != 0){
							Set<Role> fails;
							if(result.getKey().size() == 1){
								Boolean b = PermissionUtil.permissionRemove(PermissionNode.get(node), e.getGuild(), result.getKey().iterator().next());
								fails = b == null ? null : (b ? new HashSet<Role>() : new HashSet<Role>(result.getKey()));
							} else
								fails = PermissionUtil.bulkPermissionRemove(PermissionNode.get(node), e.getGuild(), result.getKey());
							
							if(fails == null) {
								e.getChannel().sendMessage("Node does not require permission, operation canceled.").queue();
								return;
							}
							
							Set<Role> successes = result.getKey();
							successes.removeAll(fails);
							
							Set<String> failedTerms = result.getValue();
							List<Field> fields = new ArrayList<Field>();
							
							if(result.getKey().size() > 0){
								Set<String> rls = new HashSet<String>();
								for(Role r : successes) rls.add(r.getAsMention());
								fields.add(new Field("Successfully Removed", rls.toString(), true));
							}
							
							if(failedTerms.size() > 0) 
								fields.add(new Field("Failed Term(s)", failedTerms.toString(), true));
							
							if(fails.size() > 0){
								Set<String> rls = new HashSet<String>();
								for(Role r : fails) rls.add(r.getAsMention());
								fields.add(new Field("Failed to Remove (Already not allowed)", rls.toString(), true));
							}
							
							for(Field f : fields) eb.addField(f);
							
							if(fields.size() > 0) e.getChannel().sendMessage(eb.build()).queue();
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
		}
		
		e.getChannel().sendMessage("Proper usage: " + ObsidianBot.commandPrefix + "permissions bulkremove <node> <ranks>").queue();
		
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
