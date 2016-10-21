package com.dscalzi.obsidianbot;

import javax.security.auth.login.LoginException;
import javax.xml.ws.http.HTTPException;

import com.dscalzi.obsidianbot.cmdutil.CommandListener;
import com.dscalzi.obsidianbot.cmdutil.CommandRegistry;
import com.dscalzi.obsidianbot.commands.AuthorCommand;
import com.dscalzi.obsidianbot.commands.HelloWorldCommand;
import com.dscalzi.obsidianbot.commands.HelpCommand;
import com.dscalzi.obsidianbot.commands.IPCommand;
import com.dscalzi.obsidianbot.commands.SayCommand;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.utils.SimpleLog;

public class ObsidianBot {
	
	private static final String token;
	public static final String commandPrefix;
	public static final long ocId;
	
	private static BotStatus status;
	private static ObsidianBot instance;
	
	static {
		commandPrefix = "--";
		token = "MjMxOTA2OTY2MzA0MTk0NTYx.CtjFKQ.Y5nPGpJwQy5kVSLfra-01dvD-_A";
		ocId = 211524927831015424L;
		status = BotStatus.NULL;
	}
	
	private JDA jda;
	private String id;
	private CommandRegistry registry;
	
	private ObsidianBot(){
		this.registry = new CommandRegistry();
		if(!this.connect()) return;
		this.registerCommands();
		this.registerListeners();
		this.id = jda.getSelfInfo().getId();
	}
	
	private void registerCommands(){
		this.registry.register("say", new SayCommand());
		this.registry.register("help", new HelpCommand());
		this.registry.register("ip", new IPCommand());
		this.registry.register("helloworld", new HelloWorldCommand());
		this.registry.register("author", new AuthorCommand());
	}
	
	private void registerListeners(){
		//jda.addEventListener(new MessageListener());
		jda.addEventListener(new CommandListener());
	}
	
	public static boolean launch(){
		if(status == BotStatus.NULL) {
			status = BotStatus.LAUNCHED;
			instance = new ObsidianBot();
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
	
	public JDA getJDA(){
		return this.jda;
	}
	
	public String getId(){
		return this.id;
	}
	
}
