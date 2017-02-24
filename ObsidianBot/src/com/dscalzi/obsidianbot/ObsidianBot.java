package com.dscalzi.obsidianbot;

import java.io.File;

import javax.security.auth.login.LoginException;
import javax.xml.ws.http.HTTPException;

import com.dscalzi.obsidianbot.cmdutil.CommandListener;
import com.dscalzi.obsidianbot.cmdutil.CommandRegistry;
import com.dscalzi.obsidianbot.cmdutil.PermissionUtil;
import com.dscalzi.obsidianbot.commands.CmdAuthor;
import com.dscalzi.obsidianbot.commands.CmdBlacklist;
import com.dscalzi.obsidianbot.commands.CmdClear;
import com.dscalzi.obsidianbot.commands.CmdHelloWorld;
import com.dscalzi.obsidianbot.commands.CmdHelp;
import com.dscalzi.obsidianbot.commands.CmdMusicControl;
import com.dscalzi.obsidianbot.commands.CmdRoleId;
import com.dscalzi.obsidianbot.commands.CmdSay;
import com.dscalzi.obsidianbot.commands.CmdShutdown;
import com.dscalzi.obsidianbot.console.ConsoleUser;
import com.dscalzi.obsidianbot.music.LavaWrapper;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.entities.impl.GameImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ObsidianBot {
	
	private static final String token;
	public static final String commandPrefix;
	
	private static BotStatus status;
	private static ObsidianBot instance;
	
	static {
		commandPrefix = "--";
		//token = "MjMxOTA2OTY2MzA0MTk0NTYx.CtjFKQ.Y5nPGpJwQy5kVSLfra-01dvD-_A";
		token = "MjMxOTA2OTY2MzA0MTk0NTYx.C5FcMQ.AnCpNadk33r9eGczmOV6SX5QfOw";
		status = BotStatus.NULL;
	}
	
	private JDA jda;
	private ConsoleUser console;
	private CommandRegistry registry;
	
	private ObsidianBot(){
		this.registry = new CommandRegistry();
		if(!this.connect()) return;
		this.console = ConsoleUser.build(jda);
		((JDAImpl)jda).getPrivateChannelMap().put("consolepm", console.getPrivateChannel());
	}
	
	public static boolean launch(){
		if(status == BotStatus.NULL) {
			status = BotStatus.LAUNCHED;
			instance = new ObsidianBot();
			LavaWrapper.initialize();
			if(status == BotStatus.CONNECTED){
				instance.registerCommands();
				instance.registerListeners();
				try{
					PermissionUtil.loadJson();
				} catch(Throwable t){
					SimpleLog.getLog("ObsidianBot").fatal("Error occured loading permissions.. shutting down!");
					t.printStackTrace();
					ObsidianBot.getInstance().shutdown();
				}
			}
			return true;
		}
		return false;
	}
	
	private void registerCommands(){
		CmdMusicControl mcc = new CmdMusicControl();
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
		this.registry.register("blacklist", new CmdBlacklist());
		this.registry.register("unblacklist", new CmdBlacklist());
	}
	
	private void registerListeners(){
		jda.addEventListener(new CommandListener());
	}
	
	public boolean connect(){
		try {
			jda = new JDABuilder(AccountType.BOT)
					.setToken(ObsidianBot.token)
					.setGame(new GameImpl("on ObsidianCraft", "hub.obsidiancraft.com", GameType.DEFAULT))
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
		ObsidianBot.status = BotStatus.SHUTDOWN;
		jda.shutdown(true);
		LavaWrapper.getInstance().getAudioPlayerManager().shutdown();
	}
	
	public static ObsidianBot getInstance(){
		return ObsidianBot.instance;
	}
	
	public static BotStatus getStatus(){
		return ObsidianBot.status;
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
		String pth = ObsidianBot.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("/", File.separator);
		return pth.substring(1, pth.lastIndexOf(File.separator));
	}
	
}
