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

	private final PermissionNode permBulkAdd = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "bulkadd");
	private final PermissionNode permBulkRemove = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "bulkremove");
	
	public final Set<PermissionNode> nodes;
	
	public CmdPermissionsControl(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
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
		case "bulkremove":
			this.cmdBulkRemove(e, args);
			break;
		case "bulkadd":
			this.cmdBulkAdd(e, args);
			break;
		}
		
		return false;
	}

	private void cmdBulkAdd(MessageReceivedEvent e, String[] args){
		if(!validate(e, permBulkAdd)) return;
		
		if(args.length > 1){
			String node = args[1];
			if(args.length > 2){
				String[] terms = new String[args.length-2];
				for(int i=0; i<terms.length; ++i) 
					terms[i] = args[i+2];
				Pair<Set<Role>,Set<String>> result = InputUtils.parseBulkRoles(e.getMessage(), e.getGuild(), terms);
				e.getChannel().sendTyping().queue((v) -> {
					try {
						if(result.getKey().size() != 0){
							Set<Role> fails = PermissionUtil.bulkPermissionAdd(PermissionNode.get(node), e.getGuild(), result.getKey());
							
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
							
							EmbedBuilder eb = new EmbedBuilder().setAuthor("Bulk Add Results", null, "http://i.imgur.com/7OfFSFx.png").setDescription("Target node `" + node + "`").setColor(Color.decode("#df4efc"));
							
							for(Field f : fields) eb.addField(f);
							
							if(fields.size() > 0) e.getChannel().sendMessage(eb.build()).queue();
						}
					} catch (IOException e1) {
						e.getChannel().sendMessage("Unexpected error, operation failed").queue();
						e1.printStackTrace();
					}
				});
			}
		} else {
			e.getChannel().sendMessage("Proper usage: " + ObsidianBot.commandPrefix + "permission bulkadd <node> <ranks>").queue();
			return;
		}
	}
	
	private void cmdBulkRemove(MessageReceivedEvent e, String[] args){
		
		if(!validate(e, permBulkRemove)) return;
		
		if(args.length > 1){
			String node = args[1];
			if(args.length > 2){
				String[] terms = new String[args.length-2];
				for(int i=0; i<terms.length; ++i) 
					terms[i] = args[i+2];
				Pair<Set<Role>,Set<String>> result = InputUtils.parseBulkRoles(e.getMessage(), e.getGuild(), terms);
				e.getChannel().sendTyping().queue((v) -> {
					try {
						if(result.getKey().size() != 0){
							Set<Role> fails = PermissionUtil.bulkPermissionRemove(PermissionNode.get(node), e.getGuild(), result.getKey());
							
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
								fields.add(new Field("Failed to Remove (Were not allowed)", rls.toString(), true));
							}
							
							EmbedBuilder eb = new EmbedBuilder().setAuthor("Bulk Remove Results", null, "http://i.imgur.com/voGutMQ.png").setDescription("Target node `" + node + "`").setColor(Color.decode("#df4efc"));
							for(Field f : fields) eb.addField(f);
							
							if(fields.size() > 0) e.getChannel().sendMessage(eb.build()).queue();
						}
					} catch (IOException e1) {
						e.getChannel().sendMessage("Unexpected error, operation failed").queue();
						e1.printStackTrace();
					}
				});
			}
		} else {
			e.getChannel().sendMessage("Proper usage: " + ObsidianBot.commandPrefix + "permission bulkremove <node> <ranks>").queue();
			return;
		}
		
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
