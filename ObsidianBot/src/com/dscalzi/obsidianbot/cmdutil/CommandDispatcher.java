package com.dscalzi.obsidianbot.cmdutil;

import java.util.Optional;

import com.dscalzi.obsidianbot.ObsidianBot;

import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class CommandDispatcher {

	public static void dispatchCommand(MessageReceivedEvent e, String cmd){
		Optional<CommandExecutor> exec = ObsidianBot.getInstance().getCommandRegistry().getExecutor(cmd);
		
		String msg = e.getMessage().getContent();
		String rawMsg = e.getMessage().getRawContent();
		String argStr = msg.substring(msg.indexOf(cmd) + cmd.length()).trim();
		String rawArgStr = rawMsg.substring(rawMsg.indexOf(cmd) + cmd.length()).trim();
		
		String[] args = (argStr.length() > 0) ? argStr.split("\\s") : new String[0];
		String[] rawArgs = (rawArgStr.length() > 0) ?  rawArgStr.split("\\s") : new String[0];
		
		exec.ifPresent((cmdEx) -> {
			cmdEx.onCommand(e, cmd, args, rawArgs);
			
			String fullArgs = " ";
			for(String s : args) fullArgs += s + " ";
			fullArgs = fullArgs.trim();
			
			SimpleLog.getLog("CommandDispatcher").info("User " + e.getAuthor().getUsername() + " (" + e.getAuthor().getId() + ") has just run the command '" + ObsidianBot.commandPrefix + cmd + (fullArgs.length() > 0 ? " " : "") + fullArgs + "'");
		});
	}
	
	public static String parseMessage(MessageReceivedEvent e){
		String content = e.getMessage().getContent();
		String cmd;
		try{
			cmd = content.substring(ObsidianBot.commandPrefix.length(), (content.indexOf(" ") > -1 ? content.indexOf(" ") : content.length()));
		} catch (Exception ex){
			return null;
		}
		
		return cmd;
	}
	
}
