package com.dscalzi.obsidianbot.music;

import java.awt.Color;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.dscalzi.obsidianbot.util.TimeUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

public class TrackScheduler extends AudioEventAdapter{

	private final Queue<TrackMeta> queue;
	private final AudioPlayer player;
	
	public TrackScheduler(AudioPlayer player){
		this.queue = new LinkedBlockingQueue<TrackMeta>();
		this.player = player;
	}
	
	public boolean queue(TrackMeta meta){
		if(meta == null || meta.getTrack() == null) return false;
		
		queue.add(meta);
		if(player.getPlayingTrack() == null) player.playTrack(meta.getTrack());
		else {
			meta.getRequestedIn().sendTyping().queue();
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
			
			EmbedBuilder eb = new EmbedBuilder().setTitle("Added " + meta.getTrack().getInfo().title + " to the Queue.");
			eb.setColor(Color.decode("#df4efc"));
			eb.setDescription("Runtime: " + TimeUtils.formatTrackDuration(meta.getTrack().getDuration()));
			eb.setFooter("Estimated Wait Time: " + TimeUtils.formatTrackDuration(waitTime), "http://i.imgur.com/Y3rbhFt.png");
			meta.getRequestedIn().sendMessage(new MessageBuilder().setEmbed(eb.build()).build()).queue();
		}
		return true;
	}
	
	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		TrackMeta current = queue.element();
		current.getRequestedIn().sendTyping().queue();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Color.decode("#df4efc"));
		eb.setTitle("Now playing " + current.getTrack().getInfo().title);
		Iterator<TrackMeta> it = queue.iterator();
		it.next();
		if(it.hasNext())
			eb.setFooter("Up next: " + it.next().getTrack().getInfo().title, "http://i.imgur.com/nEw5Gsk.png");
		eb.setDescription("Runtime " + TimeUtils.formatTrackDuration(current.getTrack().getDuration()));
		current.getRequestedIn().sendMessage(new MessageBuilder().setEmbed(eb.build()).build()).queue();
		
	}
	
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		queue.poll();
		
		if(!queue.isEmpty()) player.playTrack(queue.element().getTrack());
	}
	
	public Queue<TrackMeta> getQueue(){
		return this.queue;
	}
	
	public boolean skipCurrent(){
		if(player.getPlayingTrack() == null) return false;
		TrackMeta m = queue.element();
		m.getRequestedIn().sendMessage("Skipped " + m.getTrack().getInfo().title).queue();
		player.stopTrack();
		return true;
	}
	
	public void voteSkipCurrent(){
		queue.element().addSkip();
	}
	
	
}
