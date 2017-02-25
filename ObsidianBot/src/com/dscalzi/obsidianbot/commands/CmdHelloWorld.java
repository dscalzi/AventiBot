package com.dscalzi.obsidianbot.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.cmdutil.PermissionNode;
import com.dscalzi.obsidianbot.cmdutil.PermissionUtil;
import com.dscalzi.obsidianbot.cmdutil.PermissionNode.NodeType;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdHelloWorld implements CommandExecutor{

	private final PermissionNode permHelloWorld = PermissionNode.get(NodeType.COMMAND, "helloworld");
	
	public final Set<PermissionNode> nodes;
	
	public CmdHelloWorld(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permHelloWorld
				));
	}
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permHelloWorld, e.getGuild(), true)) return false;
		
		e.getChannel().sendMessage("Hello, " + e.getAuthor().getAsMention() + "! Fine day, isn't it?").queue();
		
		return false;
	}

	@Override
	public Set<PermissionNode> getNodes() {
		return nodes;
	}

}
