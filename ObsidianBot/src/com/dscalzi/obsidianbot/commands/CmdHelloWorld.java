package com.dscalzi.obsidianbot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.cmdutil.PermissionUtil;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdHelloWorld implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), e.getGuild(), "helloworld.command", true)) return false;
		
		e.getChannel().sendMessage("Hello, " + e.getAuthor().getAsMention() + "! Fine day, isn't it?").queue();
		
		return false;
	}

	@Override
	public List<String> getNodes() {
		return new ArrayList<String>(Arrays.asList("helloworld.command"));
	}

}
