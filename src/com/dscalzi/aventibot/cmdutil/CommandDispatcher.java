/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.cmdutil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.settings.SettingsManager;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class CommandDispatcher {

	public static void dispatchCommand(MessageReceivedEvent e, String cmd){
		Optional<CommandExecutor> exec = AventiBot.getInstance().getCommandRegistry().getExecutor(cmd);
		
		if(exec.isPresent()){
			
			CommandExecutor cmdEx = exec.get();
			
			String msg = e.getMessage().getContent();
			String argStr = msg.substring(msg.indexOf(cmd) + cmd.length()).trim();
			String[] args = cleanArgsArray((argStr.length() > 0) ? argStr.split("\\s") : new String[0]);
			
			String rawMsg = e.getMessage().getRawContent();
			String rawArgStr = rawMsg.substring(rawMsg.indexOf(cmd) + cmd.length()).trim();
			String[] rawArgs = cleanArgsArray((rawArgStr.length() > 0) ?  rawArgStr.split("\\s") : new String[0]);
			
			String fullArgs = String.join(" ", args).trim();
			
			String cmdPrefix = SettingsManager.getCommandPrefix(e.getGuild());
			if(cmdPrefix.equals(AventiBot.getInstance().getJDA().getSelfUser().getAsMention()))
				cmdPrefix = cmdPrefix + " ";
			
			SimpleLog.getLog("CommandDispatcher").info("User " + e.getAuthor().getName() + " (" + e.getAuthor().getId() + ") has just run the command '" + cmdPrefix + cmd + (fullArgs.length() > 0 ? " " : "") + fullArgs + "'");
			
			CommandResult result = cmdEx.onCommand(e, cmd, args, rawArgs);
			
			displayResult(result, e.getMessage());
		}
	}
	
	private static String[] cleanArgsArray(String[] args){
		if(args.length == 0) return args;
		List<String> argsTemp = new ArrayList<String>(Arrays.asList(args));
		for(int i=argsTemp.size()-1; i>=0; --i)	if(argsTemp.get(i).isEmpty()) argsTemp.remove(i);
		return argsTemp.toArray(new String[0]);
	}
	
	public static String parseMessage(MessageReceivedEvent e){
		String c = e.getMessage().getRawContent();
		String prefix = SettingsManager.getCommandPrefix(e.getGuild());
		c = c.substring(prefix.length());
		if(prefix.equals(AventiBot.getInstance().getJDA().getSelfUser().getAsMention()))
			c = c.trim();
		
		try{
			return c.substring(0, (c.indexOf(" ") > -1 ? c.indexOf(" ") : c.length()));
		} catch (Exception ex){
			return null;
		}
	}
	
	/**
	 * Utility method that allows async commands to display their result.
	 * 
	 * @param result The result of the command operation.
	 * @param m The message to display the result on.
	 */
	public static void displayResult(CommandResult result, Message m){
		if(result != null && result != CommandResult.IGNORE){
			if(m.getIdLong() > 0){
				m.addReaction(result.getEmote()).queue();
			}
		}
	}
	
}
