/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot;

import java.io.File;

import javax.security.auth.login.LoginException;
import javax.xml.ws.http.HTTPException;

import com.dscalzi.aventibot.cmdutil.CommandListener;
import com.dscalzi.aventibot.cmdutil.CommandRegistry;
import com.dscalzi.aventibot.commands.CmdAuthor;
import com.dscalzi.aventibot.commands.CmdBlacklist;
import com.dscalzi.aventibot.commands.CmdClear;
import com.dscalzi.aventibot.commands.CmdHelloWorld;
import com.dscalzi.aventibot.commands.CmdHelp;
import com.dscalzi.aventibot.commands.CmdMusicControl;
import com.dscalzi.aventibot.commands.CmdPermissionsControl;
import com.dscalzi.aventibot.commands.CmdRoleId;
import com.dscalzi.aventibot.commands.CmdSay;
import com.dscalzi.aventibot.commands.CmdShutdown;
import com.dscalzi.aventibot.console.ConsoleUser;
import com.dscalzi.aventibot.music.LavaWrapper;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.entities.impl.GameImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;

public class AventiBot {
	
	private static final String token;
	public static final String commandPrefix;
	
	private static BotStatus status;
	private static AventiBot instance;
	
	static {
		commandPrefix = "--";
		//token = "MjMxOTA2OTY2MzA0MTk0NTYx.CtjFKQ.Y5nPGpJwQy5kVSLfra-01dvD-_A";
		token = "MjMxOTA2OTY2MzA0MTk0NTYx.C5FcMQ.AnCpNadk33r9eGczmOV6SX5QfOw";
		status = BotStatus.NULL;
	}
	
	private JDA jda;
	private ConsoleUser console;
	private CommandRegistry registry;
	
	private AventiBot(){
		this.registry = new CommandRegistry();
		if(!this.connect()) return;
		this.console = ConsoleUser.build(jda);
		((JDAImpl)jda).getPrivateChannelMap().put("consolepm", console.getPrivateChannel());
	}
	
	public static boolean launch(){
		if(status == BotStatus.NULL) {
			status = BotStatus.LAUNCHED;
			instance = new AventiBot();
			LavaWrapper.initialize();
			if(status == BotStatus.CONNECTED){
				instance.registerCommands();
				instance.registerListeners();
			}
			return true;
		}
		return false;
	}
	
	private void registerCommands(){
		CmdMusicControl mcc = new CmdMusicControl();
		CmdBlacklist cbl = new CmdBlacklist();
		this.registry.register("play", mcc);
		this.registry.register("playlist", mcc);
		this.registry.register("forceskip", mcc);
		this.registry.register("pause", mcc);
		this.registry.register("stop", mcc);
		this.registry.register("resume", mcc);
		this.registry.register("say", new CmdSay());
		this.registry.register("help", new CmdHelp());
		this.registry.register("helloworld", new CmdHelloWorld());
		this.registry.register("author", new CmdAuthor());
		this.registry.register("clear", new CmdClear());
		this.registry.register("shutdown", new CmdShutdown());
		this.registry.register("roleid", new CmdRoleId());
		this.registry.register("blacklist", cbl);
		this.registry.register("unblacklist", cbl);
		this.registry.register("permissions", new CmdPermissionsControl());
	}
	
	private void registerListeners(){
		jda.addEventListener(new CommandListener());
	}
	
	public boolean connect(){
		try {
			jda = new JDABuilder(AccountType.BOT)
					.setToken(AventiBot.token)
					.setGame(new GameImpl(commandPrefix + "help | Developed by Dan", "http://aventiumsoftworks.com/", GameType.DEFAULT))
					.buildBlocking();
			jda.setAutoReconnect(true);
			status = BotStatus.CONNECTED;
		} catch (LoginException | IllegalArgumentException | InterruptedException | HTTPException | RateLimitedException e) {
			SimpleLog.getLog("JDA").fatal("Failed to connect to Discord!");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void shutdown(){
		AventiBot.status = BotStatus.SHUTDOWN;
		jda.shutdown(true);
		LavaWrapper.getInstance().getAudioPlayerManager().shutdown();
	}
	
	public static AventiBot getInstance(){
		return AventiBot.instance;
	}
	
	public static BotStatus getStatus(){
		return AventiBot.status;
	}
	
	
	public CommandRegistry getCommandRegistry(){
		return this.registry;
	}
	
	public ConsoleUser getConsole(){
		return this.console;
	}
	
	public JDA getJDA(){
		return this.jda;
	}
	
	public String getId(){
		return this.jda.getSelfUser().getId();
	}
	
	public static String getDataPath(){
		String pth = AventiBot.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("/", File.separator);
		return pth.substring(1, pth.lastIndexOf(File.separator));
	}
	
}