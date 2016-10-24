package com.dscalzi.obsidianbot.util;

import java.util.List;

import com.dscalzi.obsidianbot.ObsidianBot;

import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class InputUtils {

	public static TextChannel parseChannel(Message m, String reference){
		TextChannel chnl = ObsidianBot.getInstance().getJDA().getTextChannelById(reference);
		if(chnl != null)
			return chnl;
		List<TextChannel> channels = m.getMentionedChannels();
		for(TextChannel tc : channels){
			if(tc.getName().equals(reference.replace("#", ""))){
				return tc;
			}
		}
		return null;
	}
	
	public static User parseUser(Message m, String reference){
		User usr = ObsidianBot.getInstance().getJDA().getUserById(reference);
		if(usr != null)
			return usr;
		List<User> users = m.getMentionedUsers();
		for(User u : users){
			if(u.getAsMention().equals(reference.replace("!", ""))){
				return u;
			}
		}
		return null;
	}
	
}
