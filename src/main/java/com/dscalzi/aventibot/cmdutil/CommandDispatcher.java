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
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.settings.SettingsManager;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandDispatcher {

	public static void dispatchCommand(MessageReceivedEvent e, String cmd){
		Optional<CommandExecutor> exec = AventiBot.getInstance().getCommandRegistry().getExecutor(cmd);
		
		if(exec.isPresent()){
			
			CommandExecutor cmdEx = exec.get();
			
			String fakeCmd = cmd;
			String msg = e.getMessage().getContent();
			if(cmd.equals(AventiBot.getInstance().getJDA().getSelfUser().getAsMention()) &&
				msg.startsWith(getDisplayedMention(e.getGuild()))){
				fakeCmd = getDisplayedMention(e.getGuild());
			}
			String argStr = msg.substring(msg.indexOf(fakeCmd) + fakeCmd.length()).trim();
			String[] args = cleanArgsArray((argStr.length() > 0) ? argStr.split("\\s") : new String[0]);
			
			String rawMsg = e.getMessage().getRawContent();
			String rawArgStr = rawMsg.substring(rawMsg.indexOf(cmd) + cmd.length()).trim();
			String[] rawArgs = cleanArgsArray((rawArgStr.length() > 0) ?  rawArgStr.split("\\s") : new String[0]);
			
			String fullArgs = String.join(" ", args).trim();
			
			String cmdPrefix = SettingsManager.getCommandPrefix(e.getGuild());
			if(cmdPrefix.equals(AventiBot.getInstance().getJDA().getSelfUser().getAsMention()))
				cmdPrefix = cmdPrefix + " ";
			
			LoggerFactory.getLogger("CommandDispatcher").info("User " + e.getAuthor().getName() + " (" + e.getAuthor().getId() + ") has just run the command '" + cmdPrefix + cmd + (fullArgs.length() > 0 ? " " : "") + fullArgs + "'");
			
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
		String prefix = SettingsManager.getCommandPrefix(e.getGuild()).trim();
		c = c.substring(prefix.length());
		if(prefix.equals(e.getGuild() == null ? AventiBot.getInstance().getJDA().getSelfUser().getAsMention() : e.getGuild().getMember(AventiBot.getInstance().getJDA().getSelfUser()).getAsMention()))
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
		displayResult(result, m, null);
	}
	
	/**
	 * Utility method that allows async commands to display their result.
	 * 
	 * @param result The result of the command operation.
	 * @param m The message to display the result on.
	 * @param success The callback on this action.
	 */
	public static void displayResult(CommandResult result, Message m, Consumer<Void> success){
		if(result != null && result != CommandResult.IGNORE){
			if(m.getIdLong() > 0){
				if(success != null)
					m.addReaction(result.getEmote()).queue(success);
				else
					m.addReaction(result.getEmote()).queue();
			} else {
				if(success != null)
					success.accept(null);
			}
		}
	}
	
	public static String getDisplayedMention(Guild g){
		if(g != null){
			Member m = g.getMemberById(AventiBot.getInstance().getJDA().getSelfUser().getId());
			if(m != null)
				return "@" + g.getMemberById(AventiBot.getInstance().getJDA().getSelfUser().getId()).getEffectiveName();
		}
		return "@" + AventiBot.getInstance().getJDA().getSelfUser().getName();
	}
	
}
