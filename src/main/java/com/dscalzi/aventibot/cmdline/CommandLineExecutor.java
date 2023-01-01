/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2022 Daniel D. Scalzi
 *
 * https://github.com/dscalzi/AventiBot
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.dscalzi.aventibot.cmdline;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.api.events.session.ShutdownEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.settings.GlobalConfig;
import com.dscalzi.aventibot.settings.SettingsManager;
import net.dv8tion.jda.api.hooks.EventListener;

public class CommandLineExecutor {

	private static final Logger log = LoggerFactory.getLogger(CommandLineExecutor.class);

	private static boolean usingCmdLine = false;
	private static boolean headless = false;

	private static CommandLineConsole console;
	
	public static void main(String[] args){
		List<String> lstArgs = Arrays.asList(args);
		usingCmdLine = true;
		if(!checkSettings()){
			log.error(MarkerFactory.getMarker("FATAL"), "Specify your bot's access token then relaunch.");
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
		
		boolean success;
		if(AventiBot.getStatus() == BotStatus.NULL){
			AventiBot.launch();
			success = AventiBot.getStatus() == BotStatus.CONNECTED;
		}else{
			success = AventiBot.getInstance().connect();
		}
		if(success){
			AventiBot.getInstance().getJDA().addEventListener((EventListener) e -> {
				if(e instanceof ShutdownEvent){
					if(console != null)	console.shutdown();
					log.info("===================================");
					log.info("AventiBot JDA has been shutdown..");
					log.info("===================================");
					log.info("Releasing log file - no more output will be logged.");
					try {
						o.closeLogger();
					} catch (IOException e1) {
						log.error(MarkerFactory.getMarker("FATAL"), "Error while releasing log file:");
						e1.printStackTrace();
					}
					System.exit(0);
				}
			});
		} else {
			log.error(MarkerFactory.getMarker("FATAL"), "Unable to connect to discord. Try relaunching.");
			if(console != null) console.shutdown();
			try {
				o.closeLogger();
			} catch (IOException e1) {
				log.error(MarkerFactory.getMarker("FATAL"), "Error while releasing log file:");
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
			log.error(MarkerFactory.getMarker("FATAL"), "Unable to load global config. This error is FATAL, shutting down..");
			e.printStackTrace();
			return false;
		}
		return g.getToken() != null;
	}
	
	public static boolean usingCmdLine(){
		return usingCmdLine;
	}
	
	public static boolean headless(){
		return headless;
	}
	
}
