/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE for license information.
 */
package com.dscalzi.aventibot.cmdline;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.settings.GlobalConfig;
import com.dscalzi.aventibot.settings.SettingsManager;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.EventListener;

public class CommandLineExecutor {
	
	private static boolean usingCmdLine = false;
	private static boolean headless = false;
	
	private static final Logger LOG = LoggerFactory.getLogger("Launcher");
	private static CommandLineConsole console;
	
	public static void main(String[] args){
		List<String> lstArgs = Arrays.asList(args);
		usingCmdLine = true;
		if(!checkSettings()){
			LOG.error(MarkerFactory.getMarker("FATAL"), "Specify your bot's access token then relaunch.");
			System.exit(0);
		}
		//Start Console Thread
		if(!lstArgs.contains("--headless")){
			console = new CommandLineConsole();
			Thread th = new Thread(() -> console.start());
			th.start();
		} else {
			headless = true;
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
							LOG.error(MarkerFactory.getMarker("FATAL"), "Error while releasing log file:");
							e1.printStackTrace();
						}
						System.exit(0);
					}
				}
			});
		} else {
			LOG.error(MarkerFactory.getMarker("FATAL"), "Unable to connect to discord. Try relaunching.");
			if(console != null) console.shutdown();
			try {
				o.closeLogger();
			} catch (IOException e1) {
				LOG.error(MarkerFactory.getMarker("FATAL"), "Error while releasing log file:");
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
			LOG.error(MarkerFactory.getMarker("FATAL"), "Unable to load global config. This error is FATAL, shutting down..");
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
	
	public static boolean headless(){
		return headless;
	}
	
}
