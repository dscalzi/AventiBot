package com.dscalzi.obsidianbot;

import javax.security.auth.login.LoginException;

import com.dscalzi.obsidianbot.cmdutil.CommandListener;
import com.dscalzi.obsidianbot.cmdutil.CommandRegistry;
import com.dscalzi.obsidianbot.commands.AuthorCmd;
import com.dscalzi.obsidianbot.commands.HelloWorldExecutor;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;

public class ObsidianBot {
	
	/* Temporary Credentials */
	private static final String token = "MjMxOTA2OTY2MzA0MTk0NTYx.CtjFKQ.Y5nPGpJwQy5kVSLfra-01dvD-_A";
	
	/* Temporary Globals */
	public static final String commandPrefix = "~/";
	
	public static void main(String[] args){
		ObsidianBot.launch();
	}
	
	private static ObsidianBot instance;
	private static boolean launched;
	
	private JDA jda;
	private String id;
	private CommandRegistry registry;
	
	private ObsidianBot(){
		this.registry = new CommandRegistry();
		this.connect();
		this.registerCommands();
		this.registerListeners();
		this.id = jda.getSelfInfo().getId();
	}
	
	private void registerCommands(){
		this.registry.register("helloworld", new HelloWorldExecutor());
		this.registry.register("author", new AuthorCmd());
	}
	
	private void registerListeners(){
		jda.addEventListener(new CommandListener());
	}
	
	public static boolean launch(){
		if(!launched) {
			instance = new ObsidianBot();
			launched = true;
			return launched;
		}
		return !launched;
	}
	
	public static ObsidianBot getInstance(){
		return ObsidianBot.instance;
	}
	
	public boolean connect(){
		try {
			jda = new JDABuilder().setBotToken(ObsidianBot.token).buildBlocking();
			jda.setAutoReconnect(true);
			return true;
		} catch (LoginException | IllegalArgumentException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
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
