package com.dscalzi.obsidianbot.commands;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.ObsidianRoles;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.console.Console.ConsoleUser;
import com.dscalzi.obsidianbot.util.InputUtils;
import com.dscalzi.obsidianbot.util.TimeUtils;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class ClearCommand implements CommandExecutor{

	private volatile boolean processing;
	private volatile long lastRun;
	
	private final List<Role> allowedRoles;
	
	public ClearCommand(){
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
				
			List<Role> userRoles = ObsidianBot.getInstance().getGuild().getMemberById(e.getAuthor().getId()).getRoles();;
		
			if(Collections.disjoint(userRoles, allowedRoles))
				return false;
		}
		
		if(processing){
			e.getChannel().sendMessage("I'm currently clearing out a channel, try again later!").queue();
			return false;
		}
		
		long timeLeft = System.currentTimeMillis() - lastRun;
		if(timeLeft < 10000){
			timeLeft =  10 - (timeLeft/1000L);
			e.getChannel().sendMessage("This command is currently in cooldown, please try again in " + timeLeft + " seconds.").queue();
			return false;
		}
		
		//The default text channel is the one the command was sent from.
		TextChannel channel = e.isFromType(ChannelType.PRIVATE) ? null : (TextChannel) e.getChannel();
		//Default target is everyone.
		User target = null;
		//Default limit for clearing a channel is 50 messages. Max is 100
		int limit = 50;
		//Default threshold is 1 hour.
		long threshold = OffsetDateTime.now().toEpochSecond() - 3600;
		
		for(int i=0; i<args.length; ++i){
			if(i+1 <args.length){
				if(args[i].equalsIgnoreCase("-a")){
					try{
						limit = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException ex){
						e.getChannel().sendMessage("Sorry, but you didn't give me an integer for the amount parameter (-a).").queue();
						return false;
					}
					if(limit > 100) limit = 100;
					if(limit < 1){
						e.getChannel().sendMessage("I cannot delete less than one message, sorry.").queue();
						return false;
					}
					++i;
				} else if(args[i].equalsIgnoreCase("-u")){
					target = InputUtils.parseUser(e.getMessage(), rawArgs[i+1]);
					if(target == null){
						e.getChannel().sendMessage("Sorry, but I couldn't find the user you specified.").queue();
						return false;
					}
					++i;
				} else if(args[i].equalsIgnoreCase("-c")){
					channel = InputUtils.parseChannel(e.getMessage(), args[i+1]);
					if(channel == null){
						e.getChannel().sendMessage("Sorry, I could not find the channel you specified.").queue();
						return false;
					}
					++i;
				} else if(args[i].equalsIgnoreCase("-t")){
					try {
						threshold = TimeUtils.parseDateDiff(args[i+1], false);
					} catch (Exception e1) {
						e.getChannel().sendMessage("Invalid date format.").queue();
						return false;
					}
					++i;
				}
			}
		}
		
		if(channel == null){
			e.getChannel().sendMessage("You must specify a channel.").queue();
			return false;
		}
		
		if(e.getChannel().equals(channel))
			e.getMessage().deleteMessage();
		
		this.clear(limit, threshold, channel, target, e.getChannel());
		
		return false;
	}
	
	private void clear(int limit, long threshold, TextChannel channel, User target, MessageChannel origin){
		if(limit > 100 | limit < 1) return;
		processing = true;
		
		channel.getHistory().retrievePast(limit).queue((history) -> {
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
				origin.sendMessage(successPt1 + " from "+ channel.getAsMention() + successPt2).queue();
			} else {
				channel.sendMessage(successPt1 + successPt2).queue();
			}
			
			processing = false;
			lastRun = System.currentTimeMillis();
		}, (exception) -> {
			processing = false;
			lastRun = System.currentTimeMillis();
		});
	}
	
	private int delete(List<Message> history, long threshold, User target){
		int deleted = 0;
		for(int i=0; i<history.size(); ++i){
			Message msg = history.get(i);
			if(msg.getCreationTime().toEpochSecond() < threshold)
				break;
			if(target != null && !msg.getAuthor().equals(target))
				continue;
			try{
				msg.deleteMessage().block();
			} catch (RateLimitedException ex){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return deleted;
				}
				//Try one last time
				try {
					msg.deleteMessage().block();
				} catch (RateLimitedException e) {
					e.printStackTrace();
					return deleted;
				}
			}
			++deleted;
			history.remove(i);
			--i;
		}
		return deleted;
	}

}
