package com.dscalzi.obsidianbot.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.music.LavaWrapper;
import com.dscalzi.obsidianbot.music.TrackMeta;
import com.dscalzi.obsidianbot.music.TrackScheduler;
import com.dscalzi.obsidianbot.util.TimeUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;

public class CmdMusicControl implements CommandExecutor{
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		AudioPlayer player = LavaWrapper.getInstance().getAudioPlayer(e.getGuild());
		TrackScheduler scheduler = LavaWrapper.getInstance().getScheduler(player);
		
		switch(cmd.toLowerCase()){
		case "pause":
			this.pauseCmd(e, player, scheduler);
			break;
		case "resume":
			this.resumeCmd(e, player, scheduler);
			break;
		case "forceskip":
			this.forceSkipCmd(e, player, scheduler);
			break;
		case "skip":
			this.skipCmd(e, player, scheduler);
			break;
		case "stop":
			this.stopCmd(e, player, scheduler);
			break;
		}
		
		return false;
	}
	
	private void pauseCmd(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		Optional<TrackMeta> otm = scheduler.getCurrent();
		if(otm.isPresent()){
			TrackMeta tm = otm.get();
			if(player.isPaused()){
				e.getChannel().sendMessage("The player is already paused.").queue();
				return;
			}
			e.getChannel().sendTyping().queue();
			EmbedBuilder eb = new EmbedBuilder().setTitle("Paused " + tm.getTrack().getInfo().title).setColor(Color.decode("#df4efc"));
			eb.setDescription("Song Duration: (" + TimeUtils.formatTrackDuration(tm.getTrack().getPosition()) + "/" + TimeUtils.formatTrackDuration(tm.getTrack().getDuration()) + ")");
			eb.setFooter("Use " + ObsidianBot.commandPrefix + "resume to unpause.", "http://i.imgur.com/ccX8Pvi.png");
			e.getChannel().sendMessage(new MessageBuilder().setEmbed(eb.build()).build()).queue();
			player.setPaused(true);
		} else
			e.getChannel().sendMessage("Nothing is currently playing.").queue();
	}
	
	private void resumeCmd(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		if(!player.isPaused()){
			e.getChannel().sendMessage("The player is not paused.").queue();
			return;
		}
		Optional<TrackMeta> otm = scheduler.getCurrent();
		if(otm.isPresent()){
			TrackMeta tm = otm.get();
			e.getChannel().sendTyping().queue();
			EmbedBuilder eb = new EmbedBuilder().setTitle("Resumed " + tm.getTrack().getInfo().title).setColor(Color.decode("#df4efc"));
			eb.setDescription("Song Duration: (" + TimeUtils.formatTrackDuration(tm.getTrack().getPosition()) + "/" + TimeUtils.formatTrackDuration(tm.getTrack().getDuration()) + ")");
			eb.setFooter("Use " + ObsidianBot.commandPrefix + "pause to pause.", "http://i.imgur.com/ccX8Pvi.png");
			e.getChannel().sendMessage(new MessageBuilder().setEmbed(eb.build()).build()).queue();
			player.setPaused(false);
		} else {
			e.getChannel().sendMessage("Player resumed.").queue();
			player.setPaused(false);
		}
	}
	
	private void forceSkipCmd(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		Optional<TrackMeta> otm = scheduler.getCurrent();
		if(otm.isPresent()){
			TrackMeta tm = otm.get();
			e.getChannel().sendMessage("Skipped " + tm.getTrack().getInfo().title);
			player.stopTrack();
		} else
			e.getChannel().sendMessage("Nothing to skip.").queue();
	}
	
	private void skipCmd(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		e.getChannel().sendMessage("Command coming soon, for now you can use forceskip.").queue();
	}
	
	private void stopCmd(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		AudioManager am = ObsidianBot.getInstance().getGuild().getAudioManager();
		if(!am.isConnected()){
			e.getChannel().sendMessage("Already stopped.").queue();
			return;
		}
		e.getChannel().sendTyping().queue();
		scheduler.clearQueue();
		am.closeAudioConnection();
		e.getChannel().sendMessage("Stopped playing.").queue();
	}

	@Override
	public List<String> getNodes() {
		return new ArrayList<String>(Arrays.asList("pause.command", "resume.command", "forceskip.command", 
				"skip.command", "stop.command"));
	}

}
