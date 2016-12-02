package com.dscalzi.obsidianbot.commands;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class IPCommand implements CommandExecutor{

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
		
		e.getAuthor().getPrivateChannel().sendMessage(msg).queue();
	}

}
