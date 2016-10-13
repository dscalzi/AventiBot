package com.dscalzi.obsidianbot.cmdutil;

import java.util.Optional;

import com.dscalzi.obsidianbot.ObsidianBot;

import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class CommandDispatcher {

	public static void dispatchCommand(MessageReceivedEvent e, String cmd){
		Optional<CommandExecutor> exec = ObsidianBot.getInstance().getCommandRegistry().getExecutor(cmd);
		
		String msg = e.getMessage().getContent();
		String argStr = msg.substring(msg.indexOf(cmd) + cmd.length()).trim();
		
		String[] args = argStr.split("\\s");
		
		exec.ifPresent((cmdEx) -> {
			cmdEx.onCommand(e, cmd, args);
			
			String fullArgs = "";
			for(String s : args) fullArgs += s;
			fullArgs = fullArgs.trim();
			
			SimpleLog.getLog("CommandDispatcher").info("User " + e.getAuthor().getUsername() + " (" + e.getAuthor().getId() + ") has just run the command '" + ObsidianBot.commandPrefix + cmd + fullArgs + "'");
		});
		//If not present, ignore.
	}
	
}
