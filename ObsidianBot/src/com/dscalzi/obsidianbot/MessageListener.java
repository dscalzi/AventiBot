package com.dscalzi.obsidianbot;

import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter{

	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		String content = e.getMessage().toString().toLowerCase();
		if(content.contains("fuck")){
			e.getChannel().sendMessage(e.getAuthor().getAsMention() + " is a cursing scumbag").queue((m) -> {
				new Timer().schedule(new TimerTask() {
					public void run(){
						if(m != null){
							m.deleteMessage().queue();
						}
					}
				}, 3000);
			});
		}
	}
	
}
