package com.dscalzi.obsidianbot.commands;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;

import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class IPCommand implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args) {
		
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
		
		String msg = "Connect to the server using the IP ```\nhub.obsidiancraft.com```";
		
		e.getAuthor().getPrivateChannel().sendMessage(msg);
	}

}
