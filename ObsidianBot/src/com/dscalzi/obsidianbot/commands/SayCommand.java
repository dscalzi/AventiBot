package com.dscalzi.obsidianbot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dscalzi.obsidianbot.Console;
import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.ObsidianRoles;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;

import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class SayCommand implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args) {
		
		List<Role> r;
		
		if(e.getAuthor() instanceof Console)
			r = new ArrayList<Role>(Arrays.asList(ObsidianRoles.DEVELOPER.getRole()));
		else
			r = e.getGuild().getRolesForUser(e.getAuthor());
		
		if(!r.contains(ObsidianRoles.DEVELOPER.getRole())){
			return false;
		}
		
		String message = e.getMessage().getContent().substring((ObsidianBot.commandPrefix + cmd).length());
		
		e.getMessage().deleteMessage();
		
		ObsidianBot.getInstance().getJDA().getTextChannelById("211524927831015424").sendMessage(message);
		
		return true;
	}

}
