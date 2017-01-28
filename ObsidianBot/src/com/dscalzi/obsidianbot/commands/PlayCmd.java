package com.dscalzi.obsidianbot.commands;

import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.music.AudioPlayerSendHandler;
import com.dscalzi.obsidianbot.music.LavaWrapper;
import com.dscalzi.obsidianbot.music.TrackMeta;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;

public class PlayCmd implements CommandExecutor{

	private AudioManager am;
	
	public PlayCmd(){
		this.am = ObsidianBot.getInstance().getGuild().getAudioManager();
	}
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		VoiceChannel currentChannel = am.isConnected() ? am.getConnectedChannel() : null;
		
		VoiceChannel targetChannel = voiceConnect(e.getAuthor());
		
		if(targetChannel == null) {
			e.getChannel().sendMessage("You currently aren't in a channel, sorry!").queue();
			return false;
		}
		
		
		AudioPlayer player = LavaWrapper.getInstance().getAudioPlayer(targetChannel.getGuild().getId());
		if(currentChannel == null){
			am.setSendingHandler(new AudioPlayerSendHandler(player));
		}
		
		String q = String.join(" ", args).trim();
		
		if(q.length() == 0) {
			e.getChannel().sendMessage("Play what..? Don't waste my time.").queue();
			return false;
		}
		
		LavaWrapper.getInstance().getAudioPlayerManager().loadItem(q, new AudioLoadResultHandler() {

			@Override
			public void trackLoaded(AudioTrack track) {
				LavaWrapper.getInstance().getScheduler(player).queue(new TrackMeta(track, e.getAuthor(), e.getChannel()));
				
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				if(playlist.isSearchResult()){
					if(playlist.getTracks().size() > 0){
						LavaWrapper.getInstance().getScheduler(player).queue(new TrackMeta(playlist.getTracks().get(0), e.getAuthor(), e.getChannel()));
					}
				}
			}

			@Override
			public void noMatches() {
				e.getChannel().sendMessage("No matches :(").queue();
				
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				e.getChannel().sendMessage("Load failed :(").queue();
				throw exception;
				
			}
			
		});
		
		return false;
	}
	
	private VoiceChannel voiceConnect(User user){
		
		for(Guild g : ObsidianBot.getInstance().getJDA().getGuilds()){
			for(VoiceChannel vc : g.getVoiceChannels()){
				for(Member m : vc.getMembers()){
					if(m.getUser().equals(user)){
						if(am.isConnected() && am.getConnectedChannel().equals(vc)) return vc;
						else {am.openAudioConnection(vc); return vc; }
					}
				}
			}
		}
		
		return null;
	}

}
