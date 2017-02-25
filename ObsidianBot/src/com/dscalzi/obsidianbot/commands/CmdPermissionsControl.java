package com.dscalzi.obsidianbot.commands;

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
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdPermissionsControl implements CommandExecutor{

	private final PermissionNode permBulkRemove = PermissionNode.get(NodeType.SUBCOMMAND, "permissions", "bulkremove");
	
	public final Set<PermissionNode> nodes;
	
	public CmdPermissionsControl(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
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
		}
		
		return false;
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
							String retVal = "";
							
							if(failedTerms.size() > 0) 
								retVal += "No results for the term" + (failedTerms.size() == 1 ? "" : "s") + " " + failedTerms + ".\n";
							
							if(fails.size() > 0){
								Set<String> rls = new HashSet<String>();
								for(Role r : fails) rls.add(r.getAsMention());
								retVal += "Did not remove " + rls + " because " + (rls.size() == 1 ? "it was " : "they were ") + "not allowed.\n";
							}
								
							if(result.getKey().size() > 0){
								Set<String> rls = new HashSet<String>();
								for(Role r : successes) rls.add(r.getAsMention());
								retVal += "Successfully removed " + rls + " from `" + node + "`.";
							}
							if(retVal.length() > 0) e.getChannel().sendMessage(retVal).queue();
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
