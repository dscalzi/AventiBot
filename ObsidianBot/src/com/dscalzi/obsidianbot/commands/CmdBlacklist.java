package com.dscalzi.obsidianbot.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.cmdutil.PermissionUtil;
import com.dscalzi.obsidianbot.util.InputUtils;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdBlacklist implements CommandExecutor{
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {

		switch(cmd.toLowerCase()){
		case "blacklist":
			this.cmdBlacklist(e, args, rawArgs);
			break;
		case "unblacklist":
			this.cmdUnBlacklist(e, args, rawArgs);
			break;
		}
		
		return false;
	}
	
	private void cmdBlacklist(MessageReceivedEvent e, String[] args, String[] rawArgs){
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), e.getGuild(), "blacklist.command")) return;
		
		try {
			PermissionUtil.blacklistUser(InputUtils.parseUser(e.getMessage(), rawArgs[0]), e.getGuild(), args[1]);
			e.getChannel().sendMessage("Done").queue();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void cmdUnBlacklist(MessageReceivedEvent e, String[] args, String[] rawArgs){
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), e.getGuild(), "unblacklist.command")) return;
		
		try {
			PermissionUtil.unBlacklistUser(InputUtils.parseUser(e.getMessage(), rawArgs[0]), e.getGuild(), args[1]);
			e.getChannel().sendMessage("Done").queue();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	public List<String> getNodes(){
		return new ArrayList<String>(Arrays.asList("blacklist.command", "unblacklist.command"));
	}
	
	
}
