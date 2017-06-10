/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.security.auth.login.LoginException;
import javax.xml.ws.http.HTTPException;

import com.dscalzi.aventibot.cmdutil.CommandListener;
import com.dscalzi.aventibot.cmdutil.CommandRegistry;
import com.dscalzi.aventibot.commands.CmdAuthor;
import com.dscalzi.aventibot.commands.CmdClear;
import com.dscalzi.aventibot.commands.CmdHardRestart;
import com.dscalzi.aventibot.commands.CmdHelloWorld;
import com.dscalzi.aventibot.commands.CmdHelp;
import com.dscalzi.aventibot.commands.CmdMusicControl;
import com.dscalzi.aventibot.commands.CmdPermissionsControl;
import com.dscalzi.aventibot.commands.CmdRoleId;
import com.dscalzi.aventibot.commands.CmdSay;
import com.dscalzi.aventibot.commands.CmdSettingsControl;
import com.dscalzi.aventibot.commands.CmdShutdown;
import com.dscalzi.aventibot.commands.CmdSoftReload;
import com.dscalzi.aventibot.console.ConsoleUser;
import com.dscalzi.aventibot.music.LavaWrapper;
import com.dscalzi.aventibot.settings.GlobalConfig;
import com.dscalzi.aventibot.settings.SettingsManager;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;

public class AventiBot {
	
	private static BotStatus status;
	private static AventiBot instance;
	
	static {
		status = BotStatus.NULL;
	}
	
	private JDA jda;
	private ConsoleUser console;
	private CommandRegistry registry;
	
	private AventiBot(){
		this.registry = new CommandRegistry();
		if(!this.connect()) return;
		/*try {
			jda.getSelfUser().getManager().setAvatar(Icon.from(new File("C:/Users/Asus/Desktop/HgXD7h2O.jpg"))).queue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	public static boolean launch(){
		if(status == BotStatus.NULL) {
			status = BotStatus.LAUNCHED;
			instance = new AventiBot();
			return true;
		}
		return false;
	}
	
	private void postConntectionSetup(){
		registerCommands();
		registerListeners();
		LavaWrapper.initialize();
		this.console = ConsoleUser.build(jda);
		((JDAImpl)jda).getPrivateChannelMap().put(console.getIdLong(), console.getPrivateChannel());
	}
	
	private void registerCommands(){
		CmdMusicControl mcc = new CmdMusicControl();
		this.registry.register("play", mcc);
		this.registry.register("playlist", mcc);
		this.registry.register("skip", mcc);
		this.registry.register("cancelskip", mcc);
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
		this.registry.register("permissions", new CmdPermissionsControl());
		this.registry.register("settings", new CmdSettingsControl());
		this.registry.register("softreload", new CmdSoftReload());
		this.registry.register("hardrestart", new CmdHardRestart());
	}
	
	private void registerListeners(){
		jda.addEventListener(new CommandListener());
		jda.addEventListener(new MessageListener());
	}
	
	public boolean connect(){
		try {
			GlobalConfig g = SettingsManager.loadGlobalConfig();
			JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT)
					.setAutoReconnect(true)
					.setToken(g.getToken());
			if(!g.getCurrentGame().isEmpty()) 
				jdaBuilder.setGame(Game.of(g.getCurrentGame()));
			jda = jdaBuilder.buildBlocking();
			status = BotStatus.CONNECTED;
			postConntectionSetup();
		} catch (LoginException | IllegalArgumentException | InterruptedException | HTTPException | RateLimitedException | IOException e) {
			status = BotStatus.LAUNCHED;
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
	
	public static void setCurrentGame(String name){
		if(AventiBot.getStatus() == BotStatus.CONNECTED && getInstance() != null)
			getInstance().getJDA().getPresence().setGame(name != null && !name.isEmpty() ? Game.of(name) : null);
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
	
	public static String getDataPath(){
		String pth;
		try {
			pth = AventiBot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replace("/", File.separator);
		} catch (URISyntaxException e) {
			SimpleLog.getLog("AventiBot").fatal("The paths on your machine cannot be converted to URIs, I am speechless.");
			e.printStackTrace();
			return null;
		}
		return pth.substring(1, pth.lastIndexOf(File.separator));
	}
	
}
