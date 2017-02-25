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

import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdBlacklist implements CommandExecutor{
	
	private final PermissionNode permBlacklist = PermissionNode.get(NodeType.COMMAND, "blacklist");
	private final PermissionNode permUnBlacklist = PermissionNode.get(NodeType.COMMAND, "unblacklist");
	
	public final Set<PermissionNode> nodes;
	
	public CmdBlacklist(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permBlacklist,
					permUnBlacklist
				));
	}
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs){
		
		User u = args.length > 0 ? InputUtils.parseUser(e.getMessage(), rawArgs[0]) : null;
		
		switch(cmd.toLowerCase()){
		case "blacklist":
			this.cmdBlacklist(e, u, args);
			break;
		case "unblacklist":
			this.cmdUnBlacklist(e, u, args);
			break;
		}
		
		return false;
	}
	
	private void cmdBlacklist(MessageReceivedEvent e, User u, String[] args){
		
		if(!validate(e, u, args, permBlacklist)) return;
		
		try {
			boolean result = PermissionUtil.blacklistUser(u, PermissionNode.get(args[1]), e.getGuild());
			e.getChannel().sendMessage(result ? "Successfully blacklisted " + u.getAsMention() + " from `" + args[1] + "`." 
					: "User is already blacklisted from `" + args[1] + "`.").queue();
		} catch (IOException e1){
			e.getChannel().sendMessage("Unexpected error occurred, operation failed.").queue();
			e1.printStackTrace();
		}
	}
	
	private void cmdUnBlacklist(MessageReceivedEvent e, User u, String[] args){
		
		if(!validate(e, u, args, permUnBlacklist)) return;
		
		try {
			boolean result = PermissionUtil.unBlacklistUser(u, PermissionNode.get(args[1]), e.getGuild());
			e.getChannel().sendMessage(result ? "Successfully unblacklisted " + u.getAsMention() + " from `" + args[1] + "`." 
					: "User not blacklisted from `" + args[1] + "`.").queue();
		} catch (IOException e1) {
			e.getChannel().sendMessage("Unexpected error occurred, operation failed.").queue();
			e1.printStackTrace();
		}
	}
	
	private boolean validate(MessageReceivedEvent e, User u, String[] args, PermissionNode permission){
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permission, e.getGuild())) return false;
		
		if(args.length < 2){
			e.getChannel().sendMessage("Proper usage is " + ObsidianBot.commandPrefix + "blacklist <user> <permission>").queue();
			return false;
		}
		
		if(u == null){
			e.getChannel().sendMessage("Could not find that user!").queue();
			return false;
		}
		
		if(!ObsidianBot.getInstance().getCommandRegistry().getAllRegisteredNodes().contains(args[1])){
			e.getChannel().sendMessage("Invalid node.").queue();
			return false;
		}
		
		return true;
	}
	
	@Override
	public Set<PermissionNode> getNodes(){
		return nodes;
	}
	
	
}
