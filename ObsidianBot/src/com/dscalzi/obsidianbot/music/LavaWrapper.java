package com.dscalzi.obsidianbot.music;

import java.util.HashMap;
import java.util.Map;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

import net.dv8tion.jda.core.entities.Guild;

public class LavaWrapper {

	private static LavaWrapper instance;
	private static boolean initialized;
	
	private final AudioPlayerManager playerManager;
	private final Map<Guild, AudioPlayer> cache;
	private final Map<Guild, TrackScheduler> listenerCache;
	
	private LavaWrapper(){
		playerManager = new DefaultAudioPlayerManager();
		cache = new HashMap<Guild, AudioPlayer>();
		listenerCache = new HashMap<Guild, TrackScheduler>();
		playerManager.registerSourceManager(new YoutubeAudioSourceManager());
		playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
		playerManager.registerSourceManager(new BandcampAudioSourceManager());
		playerManager.registerSourceManager(new VimeoAudioSourceManager());
		playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
		playerManager.registerSourceManager(new HttpAudioSourceManager());
		playerManager.registerSourceManager(new LocalAudioSourceManager());
		AudioSourceManagers.registerRemoteSources(playerManager);
	}
	
	public static boolean initialize(){
		if(!initialized){
			initialized = true;
			instance = new LavaWrapper();
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
