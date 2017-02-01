package com.dscalzi.obsidianbot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdBlacklist implements CommandExecutor{
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public List<String> getNodes(){
		return new ArrayList<String>(Arrays.asList("blacklist.command"));
	}
	
	
}
