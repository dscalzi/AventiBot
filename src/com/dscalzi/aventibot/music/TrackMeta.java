/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.music;

import java.util.ArrayList;
import java.util.List;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class TrackMeta {

	private final AudioTrack track;
	private final User requester;
	private final MessageChannel requestedIn;
	
	private final List<User> requestedSkips;
	
	public TrackMeta(AudioTrack track, User requester, MessageChannel requestedIn){
		this.track = track;
		this.requester = requester;
		this.requestedIn = requestedIn;
		this.requestedSkips = new ArrayList<User>();
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
	
	public boolean addSkip(User user){
		if(requestedSkips.contains(user)) return false;
		return requestedSkips.add(user);
	}
	
	public int getSkips(){
		return requestedSkips.size();
	}
	
}
