/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.commands;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.music.LavaWrapper;
import com.dscalzi.aventibot.music.TrackMeta;
import com.dscalzi.aventibot.music.TrackScheduler;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.PageList;
import com.dscalzi.aventibot.util.TimeUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;

public class CmdMusicControl implements CommandExecutor{
	
	public final Set<PermissionNode> nodes;
	
	public CmdMusicControl(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
					permForceSkip,
					permPause,
					permPlay,
					permPlaylist,
					permResume,
					permSkip,
					permStop
				));
	}
	
	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(e.getChannelType() == ChannelType.PRIVATE){
			e.getChannel().sendMessage("You must use this command in a guild.").queue();
			return false;
		}
		
		if(e.getGuild() != null){
			String cname = e.getChannel().getName().toLowerCase();
			if(!e.getChannel().getId().equals("229380785646469127") && !cname.contains("music") && !cname.contains("debug")){
				e.getChannel().sendMessage("Don't be an asshole, use this command in music.").queue((m) -> {
					new Timer().schedule(new TimerTask() {
						public void run(){
							if(m != null)
								m.delete().queue();
							if(e.getMessage() != null)
								e.getMessage().delete().queue();
						}
					}, 5000);
				});
				return false;
			}
		}
		
		AudioPlayer player = LavaWrapper.getInstance().getAudioPlayer(e.getGuild());
		TrackScheduler scheduler = LavaWrapper.getInstance().getScheduler(e.getGuild());
		
		switch(cmd.toLowerCase()){
		case "play":
			this.cmdPlay(e, player, scheduler, args);
			break;
		case "playlist":
			this.cmdPlaylist(e, player, scheduler, args);
			break;
		case "pause":
			this.cmdPause(e, player, scheduler);
			break;
		case "resume":
			this.cmdResume(e, player, scheduler);
			break;
		case "forceskip":
			this.cmdForceSkip(e, player, scheduler);
			break;
		case "skip":
			this.cmdSkip(e, player, scheduler);
			break;
		case "stop":
			this.cmdStop(e, player, scheduler);
			break;
		}
		
		return false;
	}
	
	private final PermissionNode permPlay = PermissionNode.get(NodeType.COMMAND, "play");
	private void cmdPlay(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler, String[] args){
		if(!PermissionUtil.hasPermission(e.getAuthor(), permPlay, e.getGuild())) return;
		
		if(args.length == 0) {
			e.getChannel().sendMessage("Play what..? Don't waste my time.").queue();
			return;
		}
		
		if(connectWithUser(e.getGuild().getMember(e.getAuthor()), e.getGuild()) == null) {
			e.getChannel().sendMessage("You currently aren't in a channel, sorry!").queue();
			return;
		}
		
		if(args.length > 0 && !args[0].equals("ytsearch:")){
			try{
				URI test = URI.create(args[0]);
				test.toURL();
			} catch (Throwable t){
				String[] temp = new String[args.length+1];
				System.arraycopy(args, 0, temp, 1, args.length);
				temp[0] = "ytsearch:";
				args = temp;
			}
		}
		
		LavaWrapper.getInstance().getAudioPlayerManager().loadItem(String.join(" ", args).trim(), 
				new AudioLoadResultHandler() {

			@Override
			public void trackLoaded(AudioTrack track) {
				scheduler.queue(new TrackMeta(track, e.getAuthor(), e.getChannel()));
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				if(playlist.isSearchResult()){
					if(playlist.getTracks().size() > 0){
						scheduler.queue(new TrackMeta(playlist.getTracks().get(0), e.getAuthor(), e.getChannel()));
					}
				} else if(playlist.getSelectedTrack() != null){
					scheduler.queuePlaylist(playlist, e.getAuthor(), e.getChannel());
					scheduler.queue(new TrackMeta(playlist.getSelectedTrack(), e.getAuthor(), e.getChannel()));
				} else {
					scheduler.queuePlaylist(playlist, e.getAuthor(), e.getChannel());
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
	}
	
	private final PermissionNode permPlaylist = PermissionNode.get(NodeType.COMMAND, "playlist");
	private void cmdPlaylist(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler, String[] args){
		if(!PermissionUtil.hasPermission(e.getAuthor(), permPlaylist, e.getGuild())) return;
		
		Queue<TrackMeta> q = scheduler.getQueue();
		if(q.isEmpty()){
			e.getChannel().sendMessage("Nothing is queued.").queue();
			return;
		}
		
		e.getChannel().sendTyping().queue((v) -> {
			List<TrackMeta> queued = new ArrayList<TrackMeta>(Arrays.asList(q.toArray(new TrackMeta[0])));
			TrackMeta current = queued.get(0);
			queued.remove(0);
			PageList<TrackMeta> tracks = new PageList<TrackMeta>(queued);
			int page;
			try{
				page = args.length == 0 ? 0 : Integer.parseInt(args[0])-1;
			} catch (NumberFormatException ex){
				page = 0;
			}
			if((page > tracks.size()-1 && tracks.size() != 0) || page < 0){
				e.getChannel().sendMessage("Page not found, sorry.").queue();
				return;
			}
			
			EmbedBuilder eb = new EmbedBuilder().setColor(SettingsManager.getGlobalConfig().getBotColor());
			eb.addField(new Field("Currently Playing:", current.getTrack().getInfo().title + " (" + TimeUtils.formatTrackDuration(current.getTrack().getPosition()) + "/" + TimeUtils.formatTrackDuration(current.getTrack().getDuration()) + ")", false));
			String desc = "";
			
			if(tracks.size() != 0)
				for(int i=0; i<tracks.getPage(page).size(); ++i){
					TrackMeta t = tracks.getPage(page).get(i);
					desc += "\n" + (i+1+5*page) + ") " + t.getTrack().getInfo().title + " (" + TimeUtils.formatTrackDuration(t.getTrack().getDuration()) + ")";
				}
			
			if(desc.length() > 0) eb.addField(new Field("Up Next:", desc, false));
			eb.setFooter("Playlist Length " + TimeUtils.formatTrackDuration(scheduler.getPlaylistDuration()) + (tracks.size() > 0 ? " | Page " + (page+1) + " of " + tracks.size(): ""), "http://i.imgur.com/Y3rbhFt.png");
			
			
			e.getChannel().sendMessage(new MessageBuilder().setEmbed(eb.build()).build()).queue();
		});
		
	}
	
	private final PermissionNode permPause = PermissionNode.get(NodeType.COMMAND, "pause");
	private void cmdPause(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permPause, e.getGuild())) return;
		
		Optional<TrackMeta> otm = scheduler.getCurrent();
		if(otm.isPresent()){
			TrackMeta tm = otm.get();
			if(player.isPaused()){
				e.getChannel().sendMessage("The player is already paused.").queue();
				return;
			}
			e.getChannel().sendTyping().queue((v) -> {
				EmbedBuilder eb = new EmbedBuilder().setTitle("Paused " + tm.getTrack().getInfo().title, null).setColor(SettingsManager.getGlobalConfig().getBotColor());
				eb.setDescription("Song Duration: (" + TimeUtils.formatTrackDuration(tm.getTrack().getPosition()) + "/" + TimeUtils.formatTrackDuration(tm.getTrack().getDuration()) + ")");
				eb.setFooter("Use " + AventiBot.commandPrefix + "resume to unpause.", "http://i.imgur.com/ccX8Pvi.png");
				e.getChannel().sendMessage(new MessageBuilder().setEmbed(eb.build()).build()).queue();
				player.setPaused(true);
			});
		} else
			e.getChannel().sendMessage("Nothing is currently playing.").queue();
	}
	
	private final PermissionNode permResume = PermissionNode.get(NodeType.COMMAND, "resume");
	private void cmdResume(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permResume, e.getGuild())) return;
		
		if(!player.isPaused()){
			e.getChannel().sendMessage("The player is not paused.").queue();
			return;
		}
		Optional<TrackMeta> otm = scheduler.getCurrent();
		if(otm.isPresent()){
			TrackMeta tm = otm.get();
			e.getChannel().sendTyping().queue((v) -> {
				EmbedBuilder eb = new EmbedBuilder().setTitle("Resumed " + tm.getTrack().getInfo().title, null).setColor(SettingsManager.getGlobalConfig().getBotColor());
				eb.setDescription("Song Duration: (" + TimeUtils.formatTrackDuration(tm.getTrack().getPosition()) + "/" + TimeUtils.formatTrackDuration(tm.getTrack().getDuration()) + ")");
				eb.setFooter("Use " + AventiBot.commandPrefix + "pause to pause.", "http://i.imgur.com/ccX8Pvi.png");
				e.getChannel().sendMessage(new MessageBuilder().setEmbed(eb.build()).build()).queue();
				player.setPaused(false);
			});
		} else {
			e.getChannel().sendMessage("Player resumed.").queue();
			player.setPaused(false);
		}
	}
	
	private final PermissionNode permForceSkip = PermissionNode.get(NodeType.COMMAND, "forceskip");
	private void cmdForceSkip(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permForceSkip, e.getGuild())) return;
		
		Optional<TrackMeta> otm = scheduler.getCurrent();
		if(otm.isPresent()){
			TrackMeta tm = otm.get();
			e.getChannel().sendMessage("Skipped " + tm.getTrack().getInfo().title);
			player.stopTrack();
		} else
			e.getChannel().sendMessage("Nothing to skip.").queue();
	}
	
	private final PermissionNode permSkip = PermissionNode.get(NodeType.COMMAND, "skip");
	private void cmdSkip(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		e.getChannel().sendMessage("Command coming soon, for now you can use forceskip.").queue();
	}
	
	private final PermissionNode permStop = PermissionNode.get(NodeType.COMMAND, "stop");
	private void cmdStop(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permStop, e.getGuild())) return;
		
		AudioManager am = e.getGuild().getAudioManager();
		if(!am.isConnected()){
			e.getChannel().sendMessage("Already stopped.").queue();
			return;
		}
		e.getChannel().sendTyping().queue((v) -> {
			scheduler.clearQueue();
			am.closeAudioConnection();
			e.getChannel().sendMessage("Stopped playing.").queue();
		});
	}
	
	private VoiceChannel connectWithUser(Member member, Guild g){
		AudioManager am = g.getAudioManager();
		for(VoiceChannel vc : g.getVoiceChannels()){
			if(vc.getMembers().contains(member)){
				if(am.isConnected() && am.getConnectedChannel().equals(vc)) return vc;
				else {am.openAudioConnection(vc); return vc; }
			}
		}
		return null;
	}

	@Override
	public Set<PermissionNode> getNodes() {
		return nodes;
	}

}
