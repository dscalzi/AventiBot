package com.dscalzi.obsidianbot.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.dscalzi.obsidianbot.BotStatus;
import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.cmdutil.PermissionNode;
import com.dscalzi.obsidianbot.cmdutil.PermissionUtil;
import com.dscalzi.obsidianbot.cmdutil.PermissionNode.NodeType;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdShutdown implements CommandExecutor{
	
	private final PermissionNode permShutdown = PermissionNode.get(NodeType.COMMAND, "shutdown");
	
	public final Set<PermissionNode> nodes;
	
	public CmdShutdown(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permShutdown
				));
	}
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permShutdown, e.getGuild())) return false;
		
		e.getChannel().sendMessage("Shutting down.. :(").queue();
		
		try {
			if(ObsidianBot.getStatus() == BotStatus.CONNECTED){
				ObsidianBot.getInstance().shutdown();
			}
		} catch (Exception ex){
			//Shutdown
			Runtime.getRuntime().exit(0);
		}
		
		return true;
	}

	@Override
	public Set<PermissionNode> getNodes() {
		return nodes;
	}
	
}
