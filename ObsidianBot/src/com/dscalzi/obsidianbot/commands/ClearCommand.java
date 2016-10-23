package com.dscalzi.obsidianbot.commands;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.ObsidianRoles;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.console.Console;
import com.dscalzi.obsidianbot.util.TimeUtils;

import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.exceptions.RateLimitedException;

public class ClearCommand implements CommandExecutor{

	private volatile boolean processing;
	
	private final List<Role> allowedRoles;
	
	public ClearCommand(){
		allowedRoles = new ArrayList<Role>();
		
		/* Staff */
		allowedRoles.add(ObsidianRoles.ADMIN.getRole());
		allowedRoles.add(ObsidianRoles.DEVELOPER.getRole());
		allowedRoles.add(ObsidianRoles.SEMI_ADMIN.getRole());
	}
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		/* Permission Check */
		List<Role> userRoles;
		if(!(e.getAuthor() instanceof Console)){
			userRoles = ObsidianBot.getInstance().getGuild().getRolesForUser(e.getAuthor());
		
			if(Collections.disjoint(userRoles, allowedRoles))
				return false;
		}
		
		if(processing){
			e.getChannel().sendMessage("I'm currently clearing out a channel, try again later!");
			return false;
		}
		
		//The default text channel is the one the command was sent from.
		TextChannel channel = e.isPrivate() ? null : (TextChannel) e.getChannel();
		//Default target is everyone.
		User target = null;
		//Default limit for clearing a channel is 50 messages. Max is 1000
		int limit = 50;
		//Default threshold is 1 hour.
		long threshold = OffsetDateTime.now().toEpochSecond() - 3600;
		
		for(int i=0; i<args.length; ++i){
			if(i+1 <args.length){
				if(args[i].equalsIgnoreCase("-a")){
					try{
						limit = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException ex){
						e.getChannel().sendMessage("Sorry, but you didn't give me an integer for the amount parameter (-a).");
						return false;
					}
					if(limit > 1000) limit = 1000;
					++i;
				} else if(args[i].equalsIgnoreCase("-u")){
					target = parseUser(e.getMessage(), rawArgs[i+1]);
					if(target == null){
						e.getChannel().sendMessage("Sorry, but I couldn't find the user you specified.");
						return false;
					}
					++i;
				} else if(args[i].equalsIgnoreCase("-c")){
					channel = parseChannel(e.getMessage(), args[i+1]);
					if(channel == null){
						e.getChannel().sendMessage("Sorry, I could not find the channel you specified.");
						return false;
					}
					++i;
				} else if(args[i].equalsIgnoreCase("-t")){
					try {
						threshold = TimeUtils.parseDateDiff(args[i+1], false);
					} catch (Exception e1) {
						e.getChannel().sendMessage("Invalid date format.");
						return false;
					}
					++i;
				}
			}
		}
		
		if(channel == null){
			e.getChannel().sendMessage("You must specify a channel.");
			return false;
		}
		
		if(e.getChannel().equals(channel))
			e.getMessage().deleteMessage();
		
		this.clear(limit, threshold, channel, target, e.getChannel());
		
		return false;
	}
	
	private void clear(int limit, long threshold, TextChannel channel, User target, MessageChannel origin){
		if(limit > 1000) return;
		
		new Thread(() -> {
			processing = true;
			
			List<Message> history = channel.getHistory().retrieve(limit);
			
			int deleted = delete(history, threshold, target);	
			
			//TODO Make the timestamp more user-friendly. Rather than "sice x GMT", "in the past hour";
			String successPt1 = "";
			String successPt2 = "";
			if(deleted > 0){
				successPt1 = "I have just cleared " + deleted + " message" + (deleted == 1 ? "" : "s");
				successPt2 = " that " + (deleted == 1 ? "was" : "were") + " sent " + ((target != null) ? "by " + target.getAsMention() + " " : "") + 
						"since " +Instant.ofEpochSecond(threshold).toString() + " GMT.";
			} else {
				successPt1 = "No messages were deleted";
			}
			
			if(!origin.equals(channel)){
				if(deleted > 0) channel.sendMessage(successPt1 + successPt2);
				origin.sendMessage(successPt1 + " from "+ channel.getAsMention() + successPt2);
			} else {
				channel.sendMessage(successPt1 + successPt2);
			}
			
			processing = false;
		}).start();
	}
	
	private int delete(List<Message> history, long threshold, User target){
		int deleted = 0;
		for(int i=0; i<history.size(); ++i){
			Message msg = history.get(i);
			if(msg.getTime().toEpochSecond() < threshold)
				break;
			if(target != null && !msg.getAuthor().equals(target))
				continue;
			try{
				msg.deleteMessage();
			} catch (RateLimitedException ex){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return deleted;
				}
				msg.deleteMessage();
			}
			++deleted;
			history.remove(i);
			--i;
		}
		return deleted;
	}
	
	public TextChannel parseChannel(Message m, String reference){
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
	
	public User parseUser(Message m, String reference){
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
