package com.dscalzi.obsidianbot;

import javax.security.auth.login.LoginException;
import javax.xml.ws.http.HTTPException;

import com.dscalzi.obsidianbot.cmdutil.CommandListener;
import com.dscalzi.obsidianbot.cmdutil.CommandRegistry;
import com.dscalzi.obsidianbot.commands.AuthorCommand;
import com.dscalzi.obsidianbot.commands.ClearCommand;
import com.dscalzi.obsidianbot.commands.HelloWorldCommand;
import com.dscalzi.obsidianbot.commands.HelpCommand;
import com.dscalzi.obsidianbot.commands.IPCommand;
import com.dscalzi.obsidianbot.commands.SayCommand;
import com.dscalzi.obsidianbot.console.Console;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.utils.SimpleLog;

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
		this.console = new Console(jda);
		((JDAImpl)jda).getPmChannelMap().put("consolepm", console.getPrivateChannel());
		this.guild = jda.getGuildById(guildId);
		this.id = jda.getSelfInfo().getId();
	}
	
	private void registerCommands(){
		this.registry.register("say", new SayCommand());
		this.registry.register("help", new HelpCommand());
		this.registry.register("ip", new IPCommand());
		this.registry.register("helloworld", new HelloWorldCommand());
		this.registry.register("author", new AuthorCommand());
		this.registry.register("clear", new ClearCommand());
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
			jda = new JDABuilder().setBotToken(ObsidianBot.token).buildBlocking();
			jda.setAutoReconnect(true);
			status = BotStatus.CONNECTED;
		} catch (LoginException | IllegalArgumentException | InterruptedException | HTTPException e) {
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
