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
				for(int i=0; i<terms.length; ++i) terms[i] = args[i+2];
				Pair<Set<Role>,Set<String>> result = InputUtils.parseBulkRoles(e.getMessage(), e.getGuild(), terms);
				
				if(result.getValue().size() > 0) e.getChannel().sendMessage("Could not find roles " + result.getValue().toString()).queue();
				
				if(result.getKey().size() == 0) return;
				
				try {
					Set<Role> fails = PermissionUtil.bulkPermissionRemove(PermissionNode.get(node), e.getGuild(), result.getKey());
					
					if(fails == null) {
						e.getChannel().sendMessage("Node does not require permission, operation canceled.").queue();
						return;
					}
					
					if(fails.size() > 0) e.getChannel().sendMessage("Failed for " + fails.toString()).queue();
					
					e.getChannel().sendMessage("Done").queue();
					
				} catch (IOException e1) {
					e.getChannel().sendMessage("Unexpected error, operation failed").queue();
					e1.printStackTrace();
				}
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
