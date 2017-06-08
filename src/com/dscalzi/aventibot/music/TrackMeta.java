/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.music;

import java.util.HashMap;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class TrackMeta{

	private final AudioTrack track;
	private final User requester;
	private final MessageChannel requestedIn;
	
	private final Map<User, Integer> requestedSkips;
	
	public TrackMeta(AudioTrack track, User requester, MessageChannel requestedIn){
		this.track = track;
		this.requester = requester;
		this.requestedIn = requestedIn;
		this.requestedSkips = new HashMap<User, Integer>();
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
		return addSkip(user, 1);
	}
	
	public boolean addSkip(User user, Integer weight){
		if(requestedSkips.containsKey(user)) return false;
		requestedSkips.put(user, weight);
		return true;
	}
	
	public boolean revokeSkip(User user){
		return requestedSkips.remove(user) != null;
	}
	
	public boolean modifySkip(User user, Integer newWeight){
		if(!requestedSkips.containsKey(user)) return false;
		requestedSkips.put(user, newWeight);
		return true;
	}
	
	public boolean hasVoted(User user){
		return requestedSkips.containsKey(user);
	}
	
	public int getNumSkips(){
		int sks = 0;
		for(Integer i : requestedSkips.values()) sks += i.intValue();
		return sks;
	}
	
	public Map<User, Integer> getSkipMap(){
		return requestedSkips;
	}
	
}
