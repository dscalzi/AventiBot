package com.dscalzi.obsidianbot;

import javax.security.auth.login.LoginException;
import javax.xml.ws.http.HTTPException;

import com.dscalzi.obsidianbot.cmdutil.CommandListener;
import com.dscalzi.obsidianbot.cmdutil.CommandRegistry;
import com.dscalzi.obsidianbot.commands.AuthorCmd;
import com.dscalzi.obsidianbot.commands.ClearCmd;
import com.dscalzi.obsidianbot.commands.HelloWorldCmd;
import com.dscalzi.obsidianbot.commands.HelpCmd;
import com.dscalzi.obsidianbot.commands.IPCmd;
import com.dscalzi.obsidianbot.commands.MusicControlCmd;
import com.dscalzi.obsidianbot.commands.PlayCmd;
import com.dscalzi.obsidianbot.commands.PlaylistCmd;
import com.dscalzi.obsidianbot.commands.SayCmd;
import com.dscalzi.obsidianbot.console.Console;
import com.dscalzi.obsidianbot.music.LavaWrapper;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.impl.GameImpl;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ObsidianBot {
	
	private static final String token;
	public static final String commandPrefix;
	public static final String guildId;
	
	private static BotStatus status;
	private static ObsidianBot instance;
	
	static {
		commandPrefix = "--";
		token = "MjMxOTA2OTY2MzA0MTk0NTYx.CtjFKQ.Y5nPGpJwQy5kVSLfra-01dvD-_A";
		guildId = "211524927831015424";
		status = BotStatus.NULL;
	}
	
	private JDA jda;
	private Guild guild;
	private Console console;
	private String id;
	private CommandRegistry registry;
	
	private ObsidianBot(){
		this.registry = new CommandRegistry();
		if(!this.connect()) return;
		this.guild = jda.getGuildById(guildId);
		this.console = new Console(jda);
		((JDAImpl)jda).getPrivateChannelMap().put("consolepm", console.getUser().getPrivateChannel());
		this.id = jda.getSelfUser().getId();
		LavaWrapper.initialize();
	}
	
	private void registerCommands(){
		MusicControlCmd mcc = new MusicControlCmd();
		this.registry.register("play", new PlayCmd());
		this.registry.register("playlist", new PlaylistCmd());
		this.registry.register("forceskip", mcc);
		this.registry.register("pause", mcc);
		this.registry.register("resume", mcc);
		this.registry.register("say", new SayCmd());
		this.registry.register("help", new HelpCmd());
		this.registry.register("ip", new IPCmd());
		this.registry.register("helloworld", new HelloWorldCmd());
		this.registry.register("author", new AuthorCmd());
		this.registry.register("clear", new ClearCmd());
	}
	
	private void registerListeners(){
		//jda.addEventListener(new MessageListener());
		jda.addEventListener(new CommandListener());
	}
	
	public static boolean launch(){
		if(status == BotStatus.NULL) {
			status = BotStatus.LAUNCHED;
			instance = new ObsidianBot();
			if(status == BotStatus.CONNECTED){
				instance.registerCommands();
				instance.registerListeners();
			}
			return true;
		}
		return false;
	}
	
	public static ObsidianBot getInstance(){
		return ObsidianBot.instance;
	}
	
	public static BotStatus getStatus(){
		return ObsidianBot.status;
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
	
	public CommandRegistry getCommandRegistry(){
		return this.registry;
	}
	
	public Console getConsole(){
		return this.console;
	}
	
	public JDA getJDA(){
		return this.jda;
	}
	
	public Guild getGuild(){
		return this.guild;
	}
	
	public String getId(){
		return this.id;
	}
	
}
