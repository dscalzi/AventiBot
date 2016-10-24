package com.dscalzi.obsidianbot.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.ObsidianRoles;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.console.Console;
import com.dscalzi.obsidianbot.util.InputUtils;

import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.MessageChannel;
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
		
		if(args.length == 0){
			e.getChannel().sendMessage("Why are you trying to get me to say nothing.. lol");
			return false;
		}
		
		MessageChannel ch = (args.length > 0) ? InputUtils.parseChannel(e.getMessage(), args[0]) : null;
		
		String message = e.getMessage().getRawContent().substring((ch == null) ? (ObsidianBot.commandPrefix + cmd).length() : e.getMessage().getRawContent().indexOf(rawArgs[0]) + rawArgs[0].length());
		MessageBuilder mb = new MessageBuilder();
		mb.appendString(message);
		
		if(ch == null) {
			if(e.getAuthor() instanceof Console)
				ch = ObsidianBot.getInstance().getJDA().getTextChannelById("211524927831015424");
			else if(e.isPrivate())
				ch = e.getPrivateChannel();
			else
				ch = e.getTextChannel();
		}
		
		if(!(e.getMessage().isPrivate()))
			e.getMessage().deleteMessage();
		
		ch.sendMessage(message);
		
		return true;
	}

}
