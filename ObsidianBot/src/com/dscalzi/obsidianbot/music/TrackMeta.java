package com.dscalzi.obsidianbot.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class TrackMeta {

	private final AudioTrack track;
	private final User requester;
	private final MessageChannel requestedIn;
	
	private int requestedSkips;
	
	public TrackMeta(AudioTrack track, User requester, MessageChannel requestedIn){
		this.track = track;
		this.requester = requester;
		this.requestedIn = requestedIn;
	}

	public AudioTrack getTrack() {
		return track;
	}

	public User getRequester() {
		return requester;
	}

	public MessageChannel getRequestedIn() {
		return requestedIn;
	}
	
	public void addSkip(){
		++requestedSkips;
	}
	
	public int getSkips(){
		return requestedSkips;
	}
	
}
