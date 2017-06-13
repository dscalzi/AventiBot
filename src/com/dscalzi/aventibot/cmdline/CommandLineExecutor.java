/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.cmdline;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.settings.GlobalConfig;
import com.dscalzi.aventibot.settings.SettingsManager;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.SimpleLog;

public class CommandLineExecutor {
	
	private static boolean usingCmdLine = false;
	
	private static final SimpleLog LOG = SimpleLog.getLog("Launcher");
	private static CommandLineConsole console;
	
	public static void main(String[] args){
		List<String> lstArgs = Arrays.asList(args);
		usingCmdLine = true;
		if(!checkSettings()){
			LOG.fatal("Specify your bot's access token then relaunch.");
			System.exit(0);
		}
		//Start Console Thread
		if(!lstArgs.contains("--headless")){
			console = new CommandLineConsole();
			Thread th = new Thread(() -> console.start());
			th.start();
		}
		
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
						if(console != null)	console.shutdown();
						LOG.info("===================================");
						LOG.info("AventiBot JDA has been shutdown..");
						LOG.info("===================================");
						LOG.info("Releasing log file - no more output will be logged.");
						try {
							o.closeLogger();
						} catch (IOException e1) {
							LOG.fatal("Error while releasing log file:");
							e1.printStackTrace();
						}
						System.exit(0);
					}
				}
			});
		} else {
			LOG.fatal("Unable to connect to discord. Try relaunching.");
			if(console != null) console.shutdown();
			try {
				o.closeLogger();
			} catch (IOException e1) {
				LOG.fatal("Error while releasing log file:");
				e1.printStackTrace();
			}
			System.exit(0);
		}
	}
	
	private static boolean checkSettings(){
		GlobalConfig g;
		try {
			g = SettingsManager.getGlobalConfig();
			if(g == null){
				throw new IOException();
			}
		} catch(IOException e){
			LOG.fatal("Unable to load global config. This error is FATAL, shutting down..");
			e.printStackTrace();
			return false;
		}
		if(g.getToken() == "NULL"){
			return false;
		} else {
			return true;
		}
	}
	
	public static boolean usingCmdLine(){
		return usingCmdLine;
	}
	
}
