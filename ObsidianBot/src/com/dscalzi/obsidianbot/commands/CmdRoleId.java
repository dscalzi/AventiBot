package com.dscalzi.obsidianbot.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.cmdutil.PermissionUtil;
import com.dscalzi.obsidianbot.console.ConsoleUser;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdRoleId implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), e.getGuild(), "roleid.command")) return false;
		
		if(e.getChannelType().equals(ChannelType.PRIVATE)){
			e.getPrivateChannel().sendMessage("You must use this command in a guild.");
			return false;
		}
			
		Guild tg = e.getGuild();
		
		if(args.length == 0){
			e.getChannel().sendMessage("Please give me one or more ranks to look up.").queue();
			return false;
		}
		
		List<Role> roles = new ArrayList<Role>(e.getMessage().getMentionedRoles());
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
			List<Role> result = tg.getRolesByName(s, true);
			if(result.size() == 0) {
				Role r = tg.getRoleById(s);
				if(r == null) failedTerms.add(s);
				else roles.add(r);
			} else roles.addAll(result);
		}
		//Clear dupes
		for(int i=roles.size()-1; i>=0; --i){
			for(int z=i-1; z>=0; --z){
				if(roles.get(i).equals(roles.get(z))){
					roles.remove(z);
					--i;
				}
			}
		}
		
		if(failedTerms.size() > 0){
			e.getChannel().sendMessage("No results for the term" + (failedTerms.size() == 1 ? "" : "s") +" " + failedTerms).queue();
			return false;
		}
		
		
		if(e.getAuthor() instanceof ConsoleUser){
			for(Role r : roles){
				e.getChannel().sendMessage(r.getName() + " --> " + r.getId()).queue();
			}
		} else {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(roles.size() == 1 ? roles.get(0).getColor() : Color.decode("#df4efc"));
			for(Role r : roles){
				eb.addField(new Field(r.getAsMention(), "`" + r.getId() + "`", false));
			}
			e.getChannel().sendMessage(eb.build()).queue();
		}
		
		return false;
	}

	@Override
	public List<String> getNodes() {
		return new ArrayList<String>(Arrays.asList("roleid.command"));
	}

}
