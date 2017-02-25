package com.dscalzi.obsidianbot.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.cmdutil.PermissionNode;
import com.dscalzi.obsidianbot.cmdutil.PermissionUtil;
import com.dscalzi.obsidianbot.cmdutil.PermissionNode.NodeType;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;


public class CmdAuthor implements CommandExecutor {
	
	private final PermissionNode permAuthor = PermissionNode.get(NodeType.COMMAND, "author");
	
	public final Set<PermissionNode> nodes;
	
	public CmdAuthor(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permAuthor
				));
	}
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permAuthor, e.getGuild(), true)) return false;
		
		String author = ObsidianBot.getInstance().getJDA().getUserById("169197209630277642").getAsMention();
		
		e.getChannel().sendMessage(author + " is my daddy :)").queue();
		
		return true;
	}

	@Override
	public Set<PermissionNode> getNodes() {
		return nodes;
	}

}
