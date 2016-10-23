package com.dscalzi.obsidianbot.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.ObsidianRoles;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.console.Console;

import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class SayCommand implements CommandExecutor{

	private final List<Role> allowedRoles;
	
	public SayCommand(){
		allowedRoles = new ArrayList<Role>();
		
		/* Staff */
		allowedRoles.add(ObsidianRoles.ADMIN.getRole());
		allowedRoles.add(ObsidianRoles.DEVELOPER.getRole());
		allowedRoles.add(ObsidianRoles.SEMI_ADMIN.getRole());
		allowedRoles.add(ObsidianRoles.MODERATOR.getRole());
		allowedRoles.add(ObsidianRoles.STEWARD.getRole());
	}
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		List<Role> userRoles;
		if(!(e.getAuthor() instanceof Console)){
			userRoles = ObsidianBot.getInstance().getGuild().getRolesForUser(e.getAuthor());
		
			if(Collections.disjoint(userRoles, allowedRoles))
				return false;
		}
		
		String message = e.getMessage().getContent().substring((ObsidianBot.commandPrefix + cmd).length());
		
		if(!(e.getMessage().isPrivate()))
			e.getMessage().deleteMessage();
		
		ObsidianBot.getInstance().getJDA().getTextChannelById("211524927831015424").sendMessage(message);
		
		return true;
	}

}
