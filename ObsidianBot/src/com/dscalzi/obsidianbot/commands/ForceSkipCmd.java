package com.dscalzi.obsidianbot.commands;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.music.LavaWrapper;
import com.dscalzi.obsidianbot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ForceSkipCmd implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		AudioPlayer player = LavaWrapper.getInstance().getAudioPlayer(e.getGuild().getId());
		TrackScheduler scheduler = LavaWrapper.getInstance().getScheduler(player);
		
		if(scheduler.skipCurrent()){
			
		}
		
		return false;
	}

}
