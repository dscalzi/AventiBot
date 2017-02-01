package com.dscalzi.obsidianbot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdIP implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(args.length > 0){
			if(args[0].matches("^(?iu)(server|minecraft)")){
				sendServerIP(e);
				return true;
			}
		}
		
		sendServerIP(e);
		return true;
	}
	
	private void sendServerIP(MessageReceivedEvent e){
		
		String msg = "Connect to the server using the IP ```hub.obsidiancraft.com```";
		
		if(!e.getAuthor().hasPrivateChannel())
			e.getAuthor().openPrivateChannel();
		
		e.getAuthor().getPrivateChannel().sendMessage(msg).queue();
	}

	@Override
	public List<String> getNodes() {
		return new ArrayList<String>(Arrays.asList("ip.command"));
	}

}
