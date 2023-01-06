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

import com.dscalzi.aventibot.settings.GlobalConfig;
import com.github.topisenpai.lavasrc.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

import com.sedmelluq.discord.lavaplayer.track.*;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LavaWrapper {

	private static final Logger log = LoggerFactory.getLogger(LavaWrapper.class);

	private static LavaWrapper instance;
	private static boolean initialized;

	private final AudioPlayerManager playerManager;
	private final Map<Guild, AudioPlayer> cache;
	private final Map<Guild, TrackScheduler> listenerCache;

	private LavaWrapper(GlobalConfig g){
		playerManager = new DefaultAudioPlayerManager();
		cache = new HashMap<>();
		listenerCache = new HashMap<>();
		playerManager.registerSourceManager(new YoutubeAudioSourceManager());
		if(g.getSpotifyClientId() != null) {
			log.info("Registering spotify.");
			playerManager.registerSourceManager(new SpotifySourceManager(
					g.getSpotifyClientId(),
					g.getSpotifyClientSecret(),
					g.getSpotifyCountryCode(),
					playerManager,
					mirroringAudioTrack -> {
						final String ytsearch = "ytsearch:";

                        // Try by isrc
                        AudioItem item = AudioReference.NO_TRACK;
                        if(mirroringAudioTrack.getISRC() != null) {
                            item = mirroringAudioTrack.loadItem(
                                    ytsearch + mirroringAudioTrack.getISRC());
							String title = null;
                            if(item instanceof InternalAudioTrack casted) {
								title = casted.getInfo().title;
                            }
							else if(item instanceof AudioPlaylist casted) {
								title = casted.getTracks().get(0).getInfo().title;
							}
							if(title != null) {
								if(!title.toLowerCase().contains(mirroringAudioTrack.getInfo().title.toLowerCase())) {
									log.info("Title Mismatch, Expected: {}. Actual: {}.", mirroringAudioTrack.getInfo().title, title);
									item = AudioReference.NO_TRACK;
								}
							}
                        }
                        // Try name + isrc
                        if(item == AudioReference.NO_TRACK) {
                            item = mirroringAudioTrack.loadItem(
                                    ytsearch + mirroringAudioTrack.getInfo().title + " " + mirroringAudioTrack.getISRC());
                        }

                        if(item == AudioReference.NO_TRACK) {
                            log.error("Failed to find track");
                        }

                        return item;
					}
			));
		}
		playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
		playerManager.registerSourceManager(new BandcampAudioSourceManager());
		playerManager.registerSourceManager(new BeamAudioSourceManager());
		playerManager.registerSourceManager(new VimeoAudioSourceManager());
		playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
		playerManager.registerSourceManager(new HttpAudioSourceManager());
		playerManager.registerSourceManager(new LocalAudioSourceManager());
		AudioSourceManagers.registerRemoteSources(playerManager);
	}

	public static boolean initialize(GlobalConfig g){
		if(!initialized){
			initialized = true;
			instance = new LavaWrapper(g);
			return true;
		}
		return false;
	}

	public static LavaWrapper getInstance(){
		return LavaWrapper.instance;
	}

	public AudioPlayerManager getAudioPlayerManager(){
		return playerManager;
	}

	public AudioPlayer getAudioPlayer(Guild id){
		if(cache.containsKey(id))
			return cache.get(id);
		else {
			AudioPlayer player = playerManager.createPlayer();
			TrackScheduler trackScheduler = new TrackScheduler(player, id);

			player.addListener(trackScheduler);

			listenerCache.put(id, trackScheduler);
			cache.put(id, player);

			id.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(player));

			return player;
		}
	}

	public TrackScheduler getScheduler(Guild id){
		return listenerCache.get(id);
	}

}
