/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2019 Daniel D. Scalzi
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

package com.dscalzi.aventibot.music;

import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.IconUtil;
import com.dscalzi.aventibot.util.TimeUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.managers.AudioManager;

public class TrackScheduler extends AudioEventAdapter implements EventListener{

	private static final int PLAYLIST_LIMIT = 200;
	
	private final Queue<TrackMeta> queue;
	private final AudioPlayer player;
	
	private final Guild associatedGuild;
	
	public TrackScheduler(AudioPlayer player, Guild associatedGuild){
		this.queue = new LinkedBlockingQueue<TrackMeta>();
		this.player = player;
		this.associatedGuild = associatedGuild;
		AventiBot.getInstance().getJDA().addEventListener(this);
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
				eb.setFooter("Estimated Wait Time: " + TimeUtils.formatTrackDuration(waitTime), IconUtil.CLOCK.getURL());
				meta.getRequestedIn().sendMessage(eb.build()).queue();
			});
		}
		return true;
	}
	
	public boolean queuePlaylist(AudioPlaylist playlist, User user, MessageChannel requestedIn, boolean selectedFirst){
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
			
			int adjustedLimit = selectedFirst ? PLAYLIST_LIMIT-1 : PLAYLIST_LIMIT;
			
			if(selectedFirst) {
				TrackMeta m = new TrackMeta(playlist.getSelectedTrack(), user, requestedIn);
				playlistLength += playlist.getSelectedTrack().getDuration();
				queue.add(m);
			}
			
			for(int i=0; i<Math.min(adjustedLimit, playlist.getTracks().size()); ++i){
				AudioTrack aT = playlist.getTracks().get(i);
				if(!selectedFirst || aT != playlist.getSelectedTrack()) {
					TrackMeta m = new TrackMeta(playlist.getTracks().get(i), user, requestedIn);
					playlistLength += playlist.getTracks().get(i).getDuration();
					queue.add(m);
				}
			}
			
			EmbedBuilder eb = new EmbedBuilder().setTitle("Added Playlist " + playlist.getName() + " to the Queue.", null);
			eb.setColor(SettingsManager.getColorAWT(associatedGuild));
			eb.setDescription("Collective length: " + TimeUtils.formatTrackDuration(playlistLength));
			if(waitTime > 0) eb.setFooter("Estimated Wait Time: " + TimeUtils.formatTrackDuration(waitTime), IconUtil.CLOCK.getURL());
			requestedIn.sendMessage(eb.build()).queue();
			
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
				eb.setFooter("Up next: " + it.next().getTrack().getInfo().title, IconUtil.PLAY.getURL());
			eb.setDescription("Requested by " + current.getRequester().getAsMention() + "\n" +
				"| Runtime " + TimeUtils.formatTrackDuration(current.getTrack().getDuration()));
			current.getRequestedIn().sendMessage(eb.build()).queue();
		});
		
	}
	
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		queue.poll();
		
		AudioManager am = associatedGuild.getAudioManager();
		
		if(am.isConnected() && am.getConnectedChannel().getMembers().size() == 1){
			clearQueue();
			new Thread(() -> am.closeAudioConnection()).start();
			return;
		}
		
		if(!queue.isEmpty()) player.playTrack(queue.element().getTrack());
		else {
			new Thread(() -> am.closeAudioConnection()).start();
		}
	}
	
	@Override
	public void onEvent(GenericEvent event){
		if(event instanceof GuildVoiceLeaveEvent){
			GuildVoiceLeaveEvent e = (GuildVoiceLeaveEvent)event;
			Optional<VoiceChannel> vcOpt = getCurrentChannel();
			if(e.getGuild().equals(associatedGuild) && vcOpt.isPresent() && e.getChannelLeft().equals(vcOpt.get())){
				modifyVoteWeight(e.getMember().getUser(), 0);
			}
		} else if(event instanceof GuildVoiceJoinEvent){
			GuildVoiceJoinEvent e = (GuildVoiceJoinEvent)event;
			Optional<VoiceChannel> vcOpt = getCurrentChannel();
			if(e.getGuild().equals(associatedGuild) && vcOpt.isPresent() && e.getChannelJoined().equals(vcOpt.get())){
				modifyVoteWeight(e.getMember().getUser(), 1);
			}
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
	
	public int voteSkipCurrent(User u){
		//Return key, 0 = success; 1 = already voted; 2 = voted and skipped; -1 = nothing playing.
		Optional<TrackMeta> otm = getCurrent();
		if(otm.isPresent()){
			boolean ret;
			//Requester of the song does not need to vote.
			if(u.equals(otm.get().getRequester()))
				ret = otm.get().addSkip(u, getCurrentChannel().get().getMembers().size());
			else 
				ret = otm.get().addSkip(u);
			if(ret) {
				boolean ret2 = attemptVoteSkip(otm.get());
				if(ret2) return 2;
			}
			return ret ? 0 : 1;
		}
		return -1;
	}
	
	public int cancelVoteSkip(User u){
		//Return key, 0 = success; 1 = hasn't voted; -1 = nothing playing.
		Optional<TrackMeta> otm = getCurrent();
		if(otm.isPresent()){
			boolean ret = otm.get().revokeSkip(u);
			return ret ? 0 : 1;
		}
		return -1;
	}
	
	public int modifyVoteWeight(User u, Integer weight){
		//Return key, 0 = modified; 1 = not modified; 2 = modified and skipped; -1 = nothing playing.
		Optional<TrackMeta> otm = getCurrent();
		if(otm.isPresent()){
			boolean ret = otm.get().modifySkip(u, weight);
			if(ret) {
				boolean ret2 = attemptVoteSkip(otm.get());
				if(ret2) return 2;
			}
			return ret ? 0 : 1;
		}
		return -1;
	}
	
	private boolean attemptVoteSkip(TrackMeta current){
		if(current != null){
			VoiceChannel vc = getCurrentChannel().get();
			if((double)current.getNumSkips()/(vc.getMembers().size()-1) >= .5){
				//current.getRequestedIn().sendTyping().queue(v -> {
					//current.getRequestedIn().sendMessage("Skipped " + current.getTrack().getInfo().title).queue();
				//});
				player.stopTrack();
				return true;
			}
		}
		return false;
	}
	
	public Optional<VoiceChannel> getCurrentChannel(){
		VoiceChannel vc = associatedGuild.getMember(AventiBot.getInstance().getJDA().getSelfUser()).getVoiceState().getChannel();
		return vc == null ? Optional.empty() : Optional.of(vc);
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
