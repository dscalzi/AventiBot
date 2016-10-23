package com.dscalzi.obsidianbot.cmdutil;

import java.util.Optional;

import com.dscalzi.obsidianbot.ObsidianBot;

import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.utils.InviteUtil.AdvancedInvite;

public class CommandListener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent e){
		//Bot does not accept commands from foreign discord guilds.
		if(!e.getMessage().isPrivate()){
			if(!(e.getGuild().equals(ObsidianBot.getInstance().getGuild()))){ 
				return;
			}
		} else {
			//The following should never trigger since discord blocks messages to bots
			//if you share no common server with them. Therefore, this is currently dead
			//code but will remain as a security measure.
			if(!ObsidianBot.getInstance().getGuild().isMember(e.getAuthor())){
				e.getAuthor().getPrivateChannel().sendMessage("Sorry, you aren't allowed to boss me around like some common whore. Join my discord and I'll let you play with me all you want ;)");
				Optional<AdvancedInvite> opt = ObsidianBot.getInstance().getGuild().getInvites().stream().filter(i -> i.getCode().equals("MkmRnhd")).findFirst();
				opt.ifPresent(invite -> {
					e.getAuthor().getPrivateChannel().sendMessage("https://discord.gg/" + invite.getCode());
				});
			}
		}
		
		String content = e.getMessage().getContent().trim();
		String senderId = e.getAuthor().getId();
		if(content.startsWith(ObsidianBot.commandPrefix)){
			//Bot is not allowed to dispatch commands through chat.
			if(senderId.equals(ObsidianBot.getInstance().getId())){
				return;
			}
			
			CommandDispatcher.dispatchCommand(e, CommandDispatcher.parseMessage(e));
		}
	}
	
}
