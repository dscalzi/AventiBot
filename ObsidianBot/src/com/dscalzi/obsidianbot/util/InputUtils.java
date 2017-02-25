package com.dscalzi.obsidianbot.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.dscalzi.obsidianbot.ObsidianBot;

import javafx.util.Pair;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

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
	
	/**
	 * Parse a message for roles. It will look for mentions, strings that match role names,
	 * and role ids.
	 * 
	 * @param m Message to look through.
	 * @param g Target guild.
	 * @param args Message contents converted to arguments.
	 * @return A pair of data, the key is a set of located roles, the value is a set of failed terms.
	 */
	public static Pair<Set<Role>, Set<String>> parseBulkRoles(Message m, Guild g, String[] args){
		if(g == null) return null;
		if(args.length == 0) return null;
		List<Role> roles = new ArrayList<Role>(m.getMentionedRoles());
		Set<String> terms = new LinkedHashSet<String>();
		Set<String> failedTerms = new LinkedHashSet<String>();
		for(int i=0; i<args.length; ++i){
			String s = args[i];
			if(s.startsWith("@")) continue;
			if(s.substring(0, 1).matches("['\"]")){
				String arg = s.substring(1);
				inner:
				for(int z=i+1; i<args.length; ++z){
					if(args[z].substring(args[z].length()-1).matches("['\"]")){
						arg += " " + args[z].substring(0, args[z].length()-1);
						i=z;
						break inner;
					}
					arg += " " + args[z];
				}
				terms.add(arg);
				continue;
			}
			terms.add(s);
		}
		for(String s : terms){
			List<Role> result = g.getRolesByName(s, true);
			if(result.size() == 0) {
				Role r = g.getRoleById(s);
				if(r == null) failedTerms.add(s);
				else roles.add(r);
			} else roles.addAll(result);
		}
		//Clear dupes
		Set<Role> fRoles = new LinkedHashSet<Role>(roles);
		return new Pair<Set<Role>, Set<String>>(fRoles, failedTerms);
	}
	
}
