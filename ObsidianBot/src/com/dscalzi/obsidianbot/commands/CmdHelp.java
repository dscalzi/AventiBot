package com.dscalzi.obsidianbot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.cmdutil.PermissionUtil;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdHelp implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), e.getGuild(), "help.command", true)) return false;
		
		String msg = "Help message coming soon!";
		
		e.getAuthor().openPrivateChannel().queue((pc) -> pc.sendMessage(msg).queue());
		
		return true;
	}

	@Override
	public List<String> getNodes() {
		return new ArrayList<String>(Arrays.asList("help.command"));
	}

}
