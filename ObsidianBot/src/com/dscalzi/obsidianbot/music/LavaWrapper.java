package com.dscalzi.obsidianbot.music;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class LavaWrapper {

	private static LavaWrapper instance;
	private static boolean initialized;
	
	private final AudioPlayerManager playerManager;
	private final Map<String, AudioPlayer> cache;
	private final Map<AudioPlayer, AudioEventListener> listenerCache;
	
	private LavaWrapper(){
		playerManager = new DefaultAudioPlayerManager();
		cache = new HashMap<String, AudioPlayer>();
		listenerCache = new HashMap<AudioPlayer, AudioEventListener>();
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
	
	public AudioPlayer getAudioPlayer(String id){
		if(cache.containsKey(id))
			return cache.get(id);
		else {
			AudioPlayer player = playerManager.createPlayer();
			TrackScheduler trackScheduler = new TrackScheduler();
			player.addListener(trackScheduler);
			
			listenerCache.put(player, trackScheduler);
			cache.put(id, player);
			
			return player;
		}
	}
	
	public Future<Void> loadItem(String identifier, AudioPlayer p){
		return playerManager.loadItem(identifier, new AudioLoadResultHandler() {

			@Override
			public void trackLoaded(AudioTrack track) {
				p.playTrack(track);
				
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void noMatches() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
}
