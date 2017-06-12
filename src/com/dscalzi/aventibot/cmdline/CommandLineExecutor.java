/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.cmdline;

import java.io.IOException;
import java.io.PrintStream;
import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.SimpleLog;

public class CommandLineExecutor {
	
	private static final SimpleLog LOG = SimpleLog.getLog("Launcher");
	private static CommandLineConsole console;
	
	public static void main(String[] args){
		//Start Console Thread
		console = new CommandLineConsole();
		Thread th = new Thread(() -> console.start());
		th.start();
		
		CommandLineOutput o = new CommandLineOutput();
		PrintStream ps = new PrintStream(o, true);
		System.setOut(ps);
		System.setErr(ps);
		
		boolean success = false;
		if(AventiBot.getStatus() == BotStatus.NULL){
			AventiBot.launch();
			success = AventiBot.getStatus() == BotStatus.CONNECTED;
		}else{
			success = AventiBot.getInstance().connect();
		}
		if(success){
			AventiBot.getInstance().getJDA().addEventListener(new EventListener(){
				@Override
				public void onEvent(Event e){
					if(e instanceof ShutdownEvent){
						console.shutdown();
						LOG.info("===================================");
						LOG.info("AventiBot JDA has been shutdown..");
						LOG.info("===================================");
						LOG.info("Releasing log file - no more output will be logged.");
						//LOG.info("Press any key to exit..");
						try {
							o.closeLogger();
						} catch (IOException e1) {
							LOG.fatal("Error while releasing log file:");
							e1.printStackTrace();
						}
					}
				}
			});
		} else {
			LOG.fatal("Unable to connect to discord. Try relaunching.");
			console.shutdown();
		}
	}
	
}
