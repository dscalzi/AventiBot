package com.dscalzi.obsidianbot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdHelp implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		String sender = e.getAuthor().getId();
		
		String msg = "Help message coming soon!";
		
		ObsidianBot.getInstance().getJDA().getUserById(sender).getPrivateChannel().sendMessage(msg).queue();
		
		return true;
	}

	@Override
	public List<String> getNodes() {
		return new ArrayList<String>(Arrays.asList("help.command"));
	}

}
