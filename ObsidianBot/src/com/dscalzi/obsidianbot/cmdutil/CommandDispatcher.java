package com.dscalzi.obsidianbot.cmdutil;

import java.util.Optional;

import com.dscalzi.obsidianbot.ObsidianBot;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class CommandDispatcher {

	public static void dispatchCommand(MessageReceivedEvent e, String cmd){
		Optional<CommandExecutor> exec = ObsidianBot.getInstance().getCommandRegistry().getExecutor(cmd);
		
		exec.ifPresent((cmdEx) -> {
			String msg = e.getMessage().getContent();
			String argStr = msg.substring(msg.indexOf(cmd) + cmd.length()).trim();
			String[] args = (argStr.length() > 0) ? argStr.split("\\s") : new String[0];
			
			String rawMsg = e.getMessage().getRawContent();
			String rawArgStr = rawMsg.substring(rawMsg.indexOf(cmd) + cmd.length()).trim();
			String[] rawArgs = (rawArgStr.length() > 0) ?  rawArgStr.split("\\s") : new String[0];
			
			cmdEx.onCommand(e, cmd, args, rawArgs);
			
			String fullArgs = String.join(" ", args).trim();
			
			SimpleLog.getLog("CommandDispatcher").info("User " + e.getAuthor().getName() + " (" + e.getAuthor().getId() + ") has just run the command '" + ObsidianBot.commandPrefix + cmd + (fullArgs.length() > 0 ? " " : "") + fullArgs + "'");
		});
	}
	
	public static String parseMessage(MessageReceivedEvent e){
		String c = e.getMessage().getContent();
		try{
			return c.substring(ObsidianBot.commandPrefix.length(), (c.indexOf(" ") > -1 ? c.indexOf(" ") : c.length()));
		} catch (Exception ex){
			return null;
		}
	}
	
}
