package com.dscalzi.obsidianbot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dscalzi.obsidianbot.BotStatus;
import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.cmdutil.PermissionUtil;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdShutdown implements CommandExecutor{
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), e.getGuild(), "shutdown.command")) return false;
		
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
	public List<String> getNodes() {
		return new ArrayList<String>(Arrays.asList("shutdown.command"));
	}
	
}
