/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2022 Daniel D. Scalzi
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

import java.util.HashMap;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public class TrackMeta{

	private final AudioTrack track;
	private final User requester;
	private final MessageChannel requestedIn;
	
	private final Map<User, Integer> requestedSkips;
	
	public TrackMeta(AudioTrack track, User requester, MessageChannel requestedIn){
		this.track = track;
		this.requester = requester;
		this.requestedIn = requestedIn;
		this.requestedSkips = new HashMap<>();
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
		for(Integer i : requestedSkips.values()) sks += i;
		return sks;
	}
	
	public Map<User, Integer> getSkipMap(){
		return requestedSkips;
	}
	
}
