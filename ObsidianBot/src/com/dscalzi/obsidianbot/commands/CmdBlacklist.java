package com.dscalzi.obsidianbot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.cmdutil.PermissionUtil;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdBlacklist implements CommandExecutor{
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), "blacklist.command")) return false;
		
		return false;
	}
	
	@Override
	public List<String> getNodes(){
		return new ArrayList<String>(Arrays.asList("blacklist.command"));
	}
	
	
}
