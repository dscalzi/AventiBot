/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter{

	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		String content = e.getMessage().getContentDisplay().toLowerCase();
		if(content.equals("just do it")){
			e.getChannel().sendMessage("Yesterday you said tomorrow SO JUST DO IT!").queue();
			e.getChannel().sendMessage("https://www.youtube.com/watch?v=1IzYQYYAdw0").queue();
		}
	}
	
}
