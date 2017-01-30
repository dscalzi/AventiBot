package com.dscalzi.obsidianbot.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dscalzi.obsidianbot.BotStatus;
import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.ObsidianRoles;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.console.Console.ConsoleUser;
import com.dscalzi.obsidianbot.music.LavaWrapper;

import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ShutdownCommand implements CommandExecutor{

	private final List<Role> allowedRoles;

	public ShutdownCommand(){
		allowedRoles = new ArrayList<Role>();
		
		/* Staff */
		allowedRoles.add(ObsidianRoles.ADMIN.getRole());
		allowedRoles.add(ObsidianRoles.DEVELOPER.getRole());
		allowedRoles.add(ObsidianRoles.SEMI_ADMIN.getRole());
		allowedRoles.add(ObsidianRoles.MODERATOR.getRole());
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
		
		e.getChannel().sendMessage("Shutting down.. :(").queue();
		
		try {
			if(ObsidianBot.getStatus() == BotStatus.CONNECTED){
				ObsidianBot.getInstance().getJDA().shutdown(true);
				LavaWrapper.getInstance().getAudioPlayerManager().shutdown();
			}
		} catch (Exception ex){
			//Shutdown
			Runtime.getRuntime().exit(0);
		}
		
		return true;
	}
	
}
