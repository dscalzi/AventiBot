/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2022 Daniel D. Scalzi
 *
 * https://github.com/dscalzi/AventiBot
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.dscalzi.aventibot.cmdutil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.dscalzi.aventibot.console.CommandLine;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.JDAUtils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandDispatcher {

	private static final Logger log = LoggerFactory.getLogger(CommandLine.class);

	public static void dispatchCommand(MessageReceivedEvent e, String cmd){
		
		if(cmd == null) {
			return;
		}
		
		Optional<CommandExecutor> exec = AventiBot.getInstance().getCommandRegistry().getExecutor(cmd);
		
		if(exec.isPresent()){
			
			CommandExecutor cmdEx = exec.get();
			
			String fakeCmd = cmd;
			String msg = e.getMessage().getContentDisplay();
			if(cmd.equals(AventiBot.getInstance().getJDA().getSelfUser().getAsMention()) &&
				msg.startsWith(getDisplayedMention(JDAUtils.getGuildFromCombinedEvent(e)))){
				fakeCmd = getDisplayedMention(JDAUtils.getGuildFromCombinedEvent(e));
			}
			String argStr = msg.substring(msg.indexOf(fakeCmd) + fakeCmd.length()).trim();
			String[] args = cleanArgsArray((argStr.length() > 0) ? argStr.split("\\s") : new String[0]);
			
			String rawMsg = e.getMessage().getContentRaw();
			String rawArgStr = rawMsg.substring(rawMsg.indexOf(cmd) + cmd.length()).trim();
			String[] rawArgs = cleanArgsArray((rawArgStr.length() > 0) ?  rawArgStr.split("\\s") : new String[0]);
			
			String fullArgs = String.join(" ", args).trim();
			
			String cmdPrefix = SettingsManager.getCommandPrefix(JDAUtils.getGuildFromCombinedEvent(e));
			if(cmdPrefix.equals(AventiBot.getInstance().getJDA().getSelfUser().getAsMention()))
				cmdPrefix = cmdPrefix + " ";
			
			log.info("User " + e.getAuthor().getName() + " (" + e.getAuthor().getId() + ") has just run the command '" + cmdPrefix + cmd + (fullArgs.length() > 0 ? " " : "") + fullArgs + "'");
			
			CommandResult result = cmdEx.onCommand(e, cmd, args, rawArgs);
			
			displayResult(result, e.getMessage());
		}
	}
	
	private static String[] cleanArgsArray(String[] args){
		if(args.length == 0) return args;
		List<String> argsTemp = new ArrayList<>(Arrays.asList(args));
		for(int i=argsTemp.size()-1; i>=0; --i)	if(argsTemp.get(i).isEmpty()) argsTemp.remove(i);
		return argsTemp.toArray(new String[0]);
	}
	
	public static String parseMessage(MessageReceivedEvent e){
		String c = e.getMessage().getContentRaw();
		String prefix = SettingsManager.getCommandPrefix(JDAUtils.getGuildFromCombinedEvent(e)).trim();
		if(c.length() <= prefix.length()) {
			return null;
		}
		c = c.substring(prefix.length());
		if(prefix.equals(!e.isFromGuild() ? AventiBot.getInstance().getJDA().getSelfUser().getAsMention() : e.getGuild().getMember(AventiBot.getInstance().getJDA().getSelfUser()).getAsMention()))
			c = c.trim();
		
		try{
			return c.substring(0, (c.contains(" ") ? c.indexOf(" ") : c.length()));
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
					m.addReaction(Emoji.fromUnicode(result.getEmote())).queue(success);
				else
					m.addReaction(Emoji.fromUnicode(result.getEmote())).queue();
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
