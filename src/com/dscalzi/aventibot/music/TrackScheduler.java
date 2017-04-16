/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.music;

import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.TimeUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.managers.AudioManager;

public class TrackScheduler extends AudioEventAdapter{

	private static final int PLAYLIST_LIMIT = 200;
	
	private final Queue<TrackMeta> queue;
	private final AudioPlayer player;
	
	private final Guild associatedGuild;
	
	public TrackScheduler(AudioPlayer player, Guild associatedGuild){
		this.queue = new LinkedBlockingQueue<TrackMeta>();
		this.player = player;
		this.associatedGuild = associatedGuild;
	}
	
	public boolean queue(TrackMeta meta){
		if(meta == null || meta.getTrack() == null) return false;
		
		queue.add(meta);
		if(player.getPlayingTrack() == null) player.playTrack(meta.getTrack());
		else {
			meta.getRequestedIn().sendTyping().queue((v) -> {
				long waitTime = 0;
				Iterator<TrackMeta> it = queue.iterator();
				while(it.hasNext()){
					AudioTrack t = it.next().getTrack();
					if(t.equals(player.getPlayingTrack())){
						waitTime += t.getDuration() - t.getPosition();
						continue;
					}
					if(meta.getTrack().equals(t)) break;
					waitTime += t.getDuration();
				}
				
				EmbedBuilder eb = new EmbedBuilder().setTitle("Added " + meta.getTrack().getInfo().title + " to the Queue.", null);
				eb.setColor(SettingsManager.getColorAWT(associatedGuild));
				eb.setDescription("Runtime: " + TimeUtils.formatTrackDuration(meta.getTrack().getDuration()));
				eb.setFooter("Estimated Wait Time: " + TimeUtils.formatTrackDuration(waitTime), "http://i.imgur.com/Y3rbhFt.png");
				meta.getRequestedIn().sendMessage(new MessageBuilder().setEmbed(eb.build()).build()).queue();
			});
		}
		return true;
	}
	
	public boolean queuePlaylist(AudioPlaylist playlist, User user, MessageChannel requestedIn){
		if(playlist == null || playlist.getTracks() == null || playlist.getTracks().size() < 1) return false;
		
		requestedIn.sendTyping().queue((v) -> {
			
			long waitTime = 0;
			long playlistLength = 0;
			
			Iterator<TrackMeta> it = queue.iterator();
			while(it.hasNext()){
				AudioTrack t = it.next().getTrack();
				if(t.equals(player.getPlayingTrack())){
					waitTime += t.getDuration() - t.getPosition();
					continue;
				}
			}
			
			for(int i=0; i<Math.min(PLAYLIST_LIMIT, playlist.getTracks().size()); ++i){
				TrackMeta m = new TrackMeta(playlist.getTracks().get(i), user, requestedIn);
				playlistLength += playlist.getTracks().get(i).getDuration();
				queue.add(m);
			}
			
			EmbedBuilder eb = new EmbedBuilder().setTitle("Added Playlist " + playlist.getName() + " to the Queue.", null);
			eb.setColor(SettingsManager.getColorAWT(associatedGuild));
			eb.setDescription("Collective length: " + TimeUtils.formatTrackDuration(playlistLength));
			if(waitTime > 0) eb.setFooter("Estimated Wait Time: " + TimeUtils.formatTrackDuration(waitTime), "http://i.imgur.com/Y3rbhFt.png");
			requestedIn.sendMessage(new MessageBuilder().setEmbed(eb.build()).build()).queue();
			
			if(player.getPlayingTrack() == null) player.playTrack(queue.element().getTrack());
			
		});
		
		return true;
	}
	
	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		TrackMeta current = queue.element();
		current.getRequestedIn().sendTyping().queue((v) -> {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(SettingsManager.getColorAWT(associatedGuild));
			eb.setTitle("Now playing " + current.getTrack().getInfo().title, null);
			Iterator<TrackMeta> it = queue.iterator();
			it.next();
			if(it.hasNext())
				eb.setFooter("Up next: " + it.next().getTrack().getInfo().title, "http://i.imgur.com/nEw5Gsk.png");
			eb.setDescription("Requested by " + current.getRequester().getAsMention() + "\n" +
				"| Runtime " + TimeUtils.formatTrackDuration(current.getTrack().getDuration()));
			current.getRequestedIn().sendMessage(new MessageBuilder().setEmbed(eb.build()).build()).queue();
		});
		
	}
	
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		queue.poll();
		
		AudioManager am = associatedGuild.getAudioManager();
		
		if(am.isConnected() && am.getConnectedChannel().getMembers().size() == 1){
			clearQueue();
			am.closeAudioConnection();
			return;
		}
		
		if(!queue.isEmpty()) player.playTrack(queue.element().getTrack());
		else {
			am.closeAudioConnection();
		}
	}
	
	public Queue<TrackMeta> getQueue(){
		return this.queue;
	}
	
	public boolean pauseCurrent(){
		if(player.getPlayingTrack() == null) return false;
		player.setPaused(true);
		return true;
	}
	
	public Optional<TrackMeta> getCurrent(){
		if(player.getPlayingTrack() == null) return Optional.empty();
		return Optional.of(queue.element());
	}
	
	public void voteSkipCurrent(User u){
		queue.element().addSkip(u);
	}
	
	public long getPlaylistDuration(){
		long d = 0;
		Iterator<TrackMeta> it = queue.iterator();
		while(it.hasNext()){
			TrackMeta meta = it.next();
			if(player.getPlayingTrack() != null && player.getPlayingTrack().equals(meta.getTrack())){
				d += player.getPlayingTrack().getDuration() - player.getPlayingTrack().getPosition();
				continue;
			}
			d += meta.getTrack().getDuration();
		}
		return d;
	}
	
	public void clearQueue(){
		queue.clear();
		player.stopTrack();
	}
	
}
