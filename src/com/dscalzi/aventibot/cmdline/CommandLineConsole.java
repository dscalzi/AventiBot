/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.cmdline;

import java.util.Scanner;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.cmdutil.CommandDispatcher;
import com.dscalzi.aventibot.console.ConsoleUser;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.MessageImpl;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class CommandLineConsole {

	private final SimpleLog LOG;
	private final Scanner input;
	private volatile boolean control;
	
	public CommandLineConsole(){
		this.LOG = SimpleLog.getLog("Console");
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
				
				MessageImpl m = new MessageImpl(-1L, ((JDAImpl)api).getPrivateChannelById(-1L), false);
				m.setContent(line);
				m.setAuthor(console);
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
