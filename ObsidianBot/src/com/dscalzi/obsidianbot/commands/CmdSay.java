package com.dscalzi.obsidianbot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.cmdutil.PermissionUtil;
import com.dscalzi.obsidianbot.console.Console.ConsoleUser;
import com.dscalzi.obsidianbot.util.InputUtils;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdSay implements CommandExecutor{
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), "say.command")) return false;
		
		System.out.println("Got here");
		
		if(args.length == 0){
			e.getChannel().sendMessage("Why are you trying to get me to say nothing.. lol").queue();
			return false;
		}
		
		MessageChannel ch = (args.length > 0) ? InputUtils.parseChannel(e.getMessage(), args[0]) : null;
		
		String message = e.getMessage().getRawContent().substring((ch == null) ? (ObsidianBot.commandPrefix + cmd).length() : e.getMessage().getRawContent().indexOf(rawArgs[0]) + rawArgs[0].length());
		MessageBuilder mb = new MessageBuilder();
		mb.append(message);
		
		if(ch == null) {
			if(e.getAuthor() instanceof ConsoleUser)
				ch = ObsidianBot.getInstance().getJDA().getTextChannelById("211524927831015424");
			else if(e.isFromType(ChannelType.PRIVATE))
				ch = e.getPrivateChannel();
			else
				ch = e.getTextChannel();
		}
		
		if(!(e.getMessage().isFromType(ChannelType.PRIVATE)))
			e.getMessage().deleteMessage().queue();
		
		ch.sendMessage(message).queue();
		
		return true;
	}

	@Override
	public List<String> getNodes() {
		return new ArrayList<String>(Arrays.asList("say.command"));
	}

}
