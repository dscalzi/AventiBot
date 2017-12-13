/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.cmdline;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.cmdutil.CommandDispatcher;
import com.dscalzi.aventibot.console.ConsoleUser;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.SystemMessage;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandLineConsole {

	private final Logger LOG;
	private final Scanner input;
	private volatile boolean control;
	
	public CommandLineConsole(){
		this.LOG = LoggerFactory.getLogger("Console");
		this.input = new Scanner(System.in);
		this.control = false;
	}
	
	public void start(){
		if(control) return;
		control = true;
		while(control){
			String line = input.nextLine();
			
			if(AventiBot.getStatus() == BotStatus.SHUTDOWN){
				LOG.info("AventiBot has been shutdown, no further commands will be received.");
			} else if(AventiBot.getStatus() != BotStatus.CONNECTED){
				LOG.info("Please launch AventiBot to use the command line!");
			} else {
				JDA api = AventiBot.getInstance().getJDA();
				ConsoleUser console = AventiBot.getInstance().getConsole();
				
				LOG.info(line);
				
				SystemMessage m = new SystemMessage(-1L,
						((JDAImpl)api).getPrivateChannelById(-1L),
						MessageType.DEFAULT,
						false,
						false,
						false,
						false,
						line,
						"-1",
						console,
						OffsetDateTime.now(),
						new ArrayList<MessageReaction>(),
						new ArrayList<Attachment>(),
						new ArrayList<MessageEmbed>());
				MessageReceivedEvent mre = new MessageReceivedEvent(api, -1, m);
				CommandDispatcher.dispatchCommand(mre, CommandDispatcher.parseMessage(mre));
			}
		}
		input.close();
	}
	
	public void shutdown(){
		control = false;
		//input.close();
	}
	
	public boolean isShutdown(){
		return control;
	}
	
}
