package com.dscalzi.obsidianbot;

import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter{

	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		String content = e.getMessage().toString().toLowerCase();
		if(content.contains("fuck")){
			Message message = e.getChannel().sendMessage(e.getAuthor().getAsMention() + " is a cursing scumbag");
			new Timer().schedule(new TimerTask() {
				public void run(){
					message.deleteMessage();
				}
			}, 3000);
		}
	}
	
}
