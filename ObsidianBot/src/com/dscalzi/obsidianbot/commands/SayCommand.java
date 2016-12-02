package com.dscalzi.obsidianbot.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.ObsidianRoles;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.console.Console.ConsoleUser;
import com.dscalzi.obsidianbot.util.InputUtils;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

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
		
		if(!(e.getAuthor() instanceof ConsoleUser)){
			if(!ObsidianBot.getInstance().getGuild().isMember(e.getAuthor()))
				return false;
			
			List<Role> userRoles = ObsidianBot.getInstance().getGuild().getMemberById(e.getAuthor().getId()).getRoles();
			
			if(Collections.disjoint(userRoles, allowedRoles))
				return false;
		}
		
		System.out.println("Got here");
		
		if(args.length == 0){
			e.getChannel().sendMessage("Why are you trying to get me to say nothing.. lol").queue();
			return false;
		}
		
		MessageChannel ch = (args.length > 0) ? InputUtils.parseChannel(e.getMessage(), args[0]) : null;
		
		String message = e.getMessage().getRawContent().substring((ch == null) ? (ObsidianBot.commandPrefix + cmd).length() : e.getMessage().getRawContent().indexOf(rawArgs[0]) + rawArgs[0].length());
		MessageBuilder mb = new MessageBuilder();
		mb.appendString(message);
		
		if(ch == null) {
			if(e.getAuthor() instanceof ConsoleUser)
				ch = ObsidianBot.getInstance().getJDA().getTextChannelById("211524927831015424");
			else if(e.isFromType(ChannelType.PRIVATE))
				ch = e.getPrivateChannel();
			else
				ch = e.getTextChannel();
		}
		
		if(!(e.getMessage().isFromType(ChannelType.PRIVATE)))
			e.getMessage().deleteMessage().queue();
		
		ch.sendMessage(message).queue();
		
		return true;
	}

}
