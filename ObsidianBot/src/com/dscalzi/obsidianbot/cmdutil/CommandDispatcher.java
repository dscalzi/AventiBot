package com.dscalzi.obsidianbot.cmdutil;

import java.util.Optional;

import com.dscalzi.obsidianbot.ObsidianBot;

import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class CommandDispatcher {

	public static void dispatchCommand(MessageReceivedEvent e, String cmd){
		Optional<CommandExecutor> exec = ObsidianBot.getInstance().getCommandRegistry().getExecutor(cmd);
		
		String msg = e.getMessage().getContent();
		String argStr = msg.substring(msg.indexOf(cmd) + cmd.length()).trim();
		
		String[] args = argStr.split("\\s");
		
		exec.ifPresent((cmdEx) -> {
			cmdEx.onCommand(e, cmd, args);
		});
		//If not present, ignore.
	}
	
}
