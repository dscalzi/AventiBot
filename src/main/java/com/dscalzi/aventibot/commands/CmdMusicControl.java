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

import com.dscalzi.aventibot.cmdutil.CommandDispatcher;
import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.music.LavaWrapper;
import com.dscalzi.aventibot.music.TrackMeta;
import com.dscalzi.aventibot.music.TrackScheduler;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.IconUtil;
import com.dscalzi.aventibot.util.PageList;
import com.dscalzi.aventibot.util.TimeUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class CmdMusicControl implements CommandExecutor{
	
	public final Set<PermissionNode> nodes;
	
	public CmdMusicControl(){
		nodes = new HashSet<>(Arrays.asList(
					permForceSkip,
					permPause,
					permPlay,
					permPlaylist,
					permResume,
					permSkip,
					permCancelSkip,
					permStop,
					permShuffle
				));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!e.isFromGuild()){
			e.getChannel().sendMessage("You must use this command in a guild.").queue();
			return CommandResult.ERROR;
		}
		
		if(e.getGuild() != null){
			String cname = e.getChannel().getName().toLowerCase();
			if(!cname.contains("music") && !cname.contains("debug")){
				e.getChannel().sendMessage("Please use this command in the music channel.").queue((m) -> {
					new Timer().schedule(new TimerTask() {
						public void run(){
							if(m != null)
								m.delete().queue();
							if(e.getMessage() != null)
								e.getMessage().delete().queue();
						}
					}, 5000);
				});
				return CommandResult.ERROR;
			}
		}
		
		AudioPlayer player = LavaWrapper.getInstance().getAudioPlayer(e.getGuild());
		TrackScheduler scheduler = LavaWrapper.getInstance().getScheduler(e.getGuild());
		
		switch(cmd.toLowerCase()){
		case "play":
			return this.cmdPlay(e, player, scheduler, false, args);
		case "shuffleplay":
			return this.cmdPlay(e, player, scheduler, true, args);
		case "playlist":
			return this.cmdPlaylist(e, player, scheduler, args);
		case "pause":
			return this.cmdPause(e, player, scheduler);
		case "resume":
			return this.cmdResume(e, player, scheduler);
		case "forceskip":
			return this.cmdForceSkip(e, player, scheduler);
		case "skip":
			return this.cmdSkip(e, scheduler);
		case "cancelskip":
			return this.cmdCancelSkip(e, scheduler);
		case "stop":
			return this.cmdStop(e, player, scheduler);
		case "shuffle":
			return this.cmdShuffle(e, player, scheduler);
		}
		
		e.getChannel().sendMessage("Unknown subcommand: *args[0]*").queue();
		
		return CommandResult.ERROR;
	}
	
	private final PermissionNode permPlay = PermissionNode.get(NodeType.COMMAND, "play");
	private CommandResult cmdPlay(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler, final boolean shuffle, String[] args){
		if(!PermissionUtil.hasPermission(e.getAuthor(), permPlay, e.getGuild())) return CommandResult.NO_PERMISSION;
		
		if(args.length == 0) {
			e.getChannel().sendMessage("Play what..? Don't waste my time.").queue();
			return CommandResult.ERROR;
		}
		
		if(connectWithUser(e.getGuild().getMember(e.getAuthor()), e.getGuild()) == null) {
			e.getChannel().sendMessage("You currently aren't in a channel, sorry!").queue();
			return CommandResult.ERROR;
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
				CommandDispatcher.displayResult(CommandResult.SUCCESS, e.getMessage());
			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				if(playlist.isSearchResult()){
					if(playlist.getTracks().size() > 0){
						scheduler.queue(new TrackMeta(playlist.getTracks().get(0), e.getAuthor(), e.getChannel()));
					}
				} else {
					scheduler.queuePlaylist(playlist, e.getAuthor(), e.getChannel(), playlist.getSelectedTrack() != null, shuffle);
				}
				CommandDispatcher.displayResult(CommandResult.SUCCESS, e.getMessage());
			}

			@Override
			public void noMatches() {
				e.getChannel().sendMessage("No matches :(").queue();
				if(scheduler.getQueue().isEmpty())
					e.getGuild().getAudioManager().closeAudioConnection();
				CommandDispatcher.displayResult(CommandResult.ERROR, e.getMessage());
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				e.getChannel().sendMessage("Load failed :(").queue();
				if(scheduler.getQueue().isEmpty())
					e.getGuild().getAudioManager().closeAudioConnection();
				CommandDispatcher.displayResult(CommandResult.ERROR, e.getMessage());
				throw exception;
			}
			
		});
		
		return CommandResult.IGNORE;
		
	}
	
	private final PermissionNode permPlaylist = PermissionNode.get(NodeType.COMMAND, "playlist");
	private CommandResult cmdPlaylist(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler, String[] args){
		if(!PermissionUtil.hasPermission(e.getAuthor(), permPlaylist, e.getGuild())) return CommandResult.NO_PERMISSION;
		
		Queue<TrackMeta> q = scheduler.getQueue();
		if(q.isEmpty()){
			e.getChannel().sendMessage("Nothing is queued.").queue();
			return CommandResult.ERROR;
		}
		
		e.getChannel().sendTyping().queue((v) -> {
			List<TrackMeta> queued = new ArrayList<>(Arrays.asList(q.toArray(new TrackMeta[0])));
			TrackMeta current = queued.get(0);
			queued.remove(0);
			PageList<TrackMeta> tracks = new PageList<>(queued);
			int page;
			try{
				page = args.length == 0 ? 0 : Integer.parseInt(args[0])-1;
			} catch (NumberFormatException ex){
				page = 0;
			}
			if((page > tracks.size()-1 && tracks.size() != 0) || page < 0){
				e.getChannel().sendMessage("Page not found, sorry.").queue();
				CommandDispatcher.displayResult(CommandResult.ERROR, e.getMessage());
				return;
			}
			
			Optional<VoiceChannel> vc = scheduler.getCurrentChannel();
			int usrs = -1;
			int required = -1;
			if(vc.isEmpty()) {
				e.getChannel().sendMessage("\u2757 Warning - Not connected to voice! Report this issue.").queue();
			} else {
				usrs = vc.get().getMembers().size()-1;
				required = (int)Math.ceil(usrs/2D);
			}
			
			EmbedBuilder eb = new EmbedBuilder().setColor(SettingsManager.getColorAWT(e.getGuild()));
			int skips = current.getNumSkips();
			eb.addField(new Field("Currently Playing:", current.getTrack().getInfo().title + " (" + TimeUtils.formatTrackDuration(current.getTrack().getPosition()) + "/" + TimeUtils.formatTrackDuration(current.getTrack().getDuration()) + ")" + (usrs > -1 && skips > 0 ? (" | Votes " + skips + "/" + required + " (" + (int)(((double)skips/required)*100) + "%)") : ""), false));
			StringBuilder desc = new StringBuilder();
			
			if(tracks.size() != 0)
				for(int i=0; i<tracks.getPage(page).size(); ++i){
					TrackMeta t = tracks.getPage(page).get(i);
					desc.append("\n").append(i + 1 + 5 * page).append(") ").append(t.getTrack().getInfo().title).append(" (").append(TimeUtils.formatTrackDuration(t.getTrack().getDuration())).append(")");
				}
			
			if(desc.length() > 0) eb.addField(new Field("Up Next:", desc.toString(), false));
			eb.setFooter("Playlist Length " + TimeUtils.formatTrackDuration(scheduler.getPlaylistDuration()) + (tracks.size() > 0 ? " | Page " + (page+1) + " of " + tracks.size(): ""), IconUtil.CLOCK.getURL());
			
			
			e.getChannel().sendMessageEmbeds(eb.build()).queue();
			
			CommandDispatcher.displayResult(CommandResult.SUCCESS, e.getMessage());
		});
		
		return CommandResult.IGNORE;
		
	}
	
	private final PermissionNode permPause = PermissionNode.get(NodeType.COMMAND, "pause");
	private CommandResult cmdPause(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permPause, e.getGuild())) return CommandResult.NO_PERMISSION;
		
		Optional<TrackMeta> otm = scheduler.getCurrent();
		if(otm.isPresent()){
			TrackMeta tm = otm.get();
			if(player.isPaused()){
				e.getChannel().sendMessage("The player is already paused.").queue();
				return CommandResult.ERROR;
			}
			e.getChannel().sendTyping().queue((v) -> {
				EmbedBuilder eb = new EmbedBuilder().setTitle("Paused " + tm.getTrack().getInfo().title, null).setColor(SettingsManager.getColorAWT(e.getGuild()));
				eb.setDescription("Song Duration: (" + TimeUtils.formatTrackDuration(tm.getTrack().getPosition()) + "/" + TimeUtils.formatTrackDuration(tm.getTrack().getDuration()) + ")");
				eb.setFooter("Use " + SettingsManager.getCommandPrefix(e.getGuild()) + "resume to unpause.", IconUtil.INFO.getURL());
				e.getChannel().sendMessageEmbeds(eb.build()).queue();
				player.setPaused(true);
				CommandDispatcher.displayResult(CommandResult.SUCCESS, e.getMessage());
			});
		} else {
			e.getChannel().sendMessage("Nothing is currently playing.").queue();
			return CommandResult.ERROR;
		}
		return CommandResult.IGNORE;
	}
	
	private final PermissionNode permResume = PermissionNode.get(NodeType.COMMAND, "resume");
	private CommandResult cmdResume(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permResume, e.getGuild())) return CommandResult.NO_PERMISSION;
		
		if(!player.isPaused()){
			e.getChannel().sendMessage("The player is not paused.").queue();
			return CommandResult.ERROR;
		}
		Optional<TrackMeta> otm = scheduler.getCurrent();
		if(otm.isPresent()){
			TrackMeta tm = otm.get();
			e.getChannel().sendTyping().queue((v) -> {
				EmbedBuilder eb = new EmbedBuilder().setTitle("Resumed " + tm.getTrack().getInfo().title, null).setColor(SettingsManager.getColorAWT(e.getGuild()));
				eb.setDescription("Song Duration: (" + TimeUtils.formatTrackDuration(tm.getTrack().getPosition()) + "/" + TimeUtils.formatTrackDuration(tm.getTrack().getDuration()) + ")");
				eb.setFooter("Use " + SettingsManager.getCommandPrefix(e.getGuild()) + "pause to pause.", IconUtil.INFO.getURL());
				e.getChannel().sendMessageEmbeds(eb.build()).queue();
				player.setPaused(false);
				CommandDispatcher.displayResult(CommandResult.SUCCESS, e.getMessage());
			});
		} else {
			e.getChannel().sendMessage("Player resumed.").queue();
			player.setPaused(false);
			return CommandResult.SUCCESS;
		}
		return CommandResult.IGNORE;
	}
	
	private final PermissionNode permForceSkip = PermissionNode.get(NodeType.COMMAND, "forceskip");
	private CommandResult cmdForceSkip(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permForceSkip, e.getGuild())) return CommandResult.NO_PERMISSION;
		
		Optional<TrackMeta> otm = scheduler.getCurrent();
		if(otm.isPresent()){
			//TrackMeta tm = otm.get();
			//e.getChannel().sendMessage("Force Skipped " + tm.getTrack().getInfo().title).queue();
			player.stopTrack();
			return CommandResult.SUCCESS;
		} else {
			e.getChannel().sendMessage("Nothing to skip.").queue();
			return CommandResult.ERROR;
		}
	}
	
	private final PermissionNode permSkip = PermissionNode.get(NodeType.COMMAND, "skip");
	private CommandResult cmdSkip(MessageReceivedEvent e, TrackScheduler scheduler){
		if(!PermissionUtil.hasPermission(e.getAuthor(), permSkip, e.getGuild())) return CommandResult.NO_PERMISSION;
		
		int result = scheduler.voteSkipCurrent(e.getAuthor());
		if(result == 0){
			//Vote placed, haven't skipped.
			TrackMeta tm = scheduler.getCurrent().get();
			e.getChannel().sendTyping().queue(v -> {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(SettingsManager.getColorAWT(e.getGuild()));
				eb.setTitle("Voted to skip " + tm.getTrack().getInfo().title, null);
				int skips = tm.getNumSkips();
				int usrs = scheduler.getCurrentChannel().get().getMembers().size()-1;
				int required = (int) Math.ceil(usrs/2D);
				eb.setFooter("Votes - " + skips + "/" + required
						+ " (" + (int)(((double)skips/required)*100) + "%)", IconUtil.VOTE.getURL());
				e.getChannel().sendMessageEmbeds(eb.build()).queue();
			});
			return CommandResult.SUCCESS;
		} else if(result == 1){
			e.getChannel().sendMessage("You have already voted to skip this song!").queue();
			return CommandResult.ERROR;
		} else if(result == 2){
			//No response is needed by this function.
			return CommandResult.SUCCESS;
		} else {
			e.getChannel().sendMessage("Nothing to skip.").queue();
			return CommandResult.ERROR;
		}
	}
	
	private final PermissionNode permCancelSkip = PermissionNode.get(NodeType.COMMAND, "cancelskip");
	private CommandResult cmdCancelSkip(MessageReceivedEvent e, TrackScheduler scheduler){
		if(!PermissionUtil.hasPermission(e.getAuthor(), permCancelSkip, e.getGuild())) return CommandResult.NO_PERMISSION;
		
		int result = scheduler.cancelVoteSkip(e.getAuthor());
		
		if(result == 0){
			TrackMeta tm = scheduler.getCurrent().get();
			e.getChannel().sendTyping().queue(v -> {
				EmbedBuilder eb = new EmbedBuilder();
				eb.setColor(SettingsManager.getColorAWT(e.getGuild()));
				eb.setTitle("Cancelled vote to skip " + tm.getTrack().getInfo().title, null);
				int skips = tm.getNumSkips();
				int usrs = scheduler.getCurrentChannel().get().getMembers().size()-1;
				eb.setFooter("Votes - " + skips + "/" + usrs
						+ " (" + (int)(((double)skips/usrs)*100) + "%)", IconUtil.VOTE.getURL());
				e.getChannel().sendMessageEmbeds(eb.build()).queue();
			});
			return CommandResult.SUCCESS;
		} else if(result == 1){
			e.getChannel().sendMessage("You have not voted to skip this song!").queue();
			return CommandResult.ERROR;
		} else {
			e.getChannel().sendMessage("Nothing to skip.").queue();
			return CommandResult.ERROR;
		}
	}
	
	private final PermissionNode permStop = PermissionNode.get(NodeType.COMMAND, "stop");
	private CommandResult cmdStop(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permStop, e.getGuild())) return CommandResult.NO_PERMISSION;
		
		AudioManager am = e.getGuild().getAudioManager();
		if(!am.isConnected()){
			e.getChannel().sendMessage("Already stopped.").queue();
			return CommandResult.ERROR;
		}
		e.getChannel().sendTyping().queue((v) -> {
			scheduler.clearQueue();
			am.closeAudioConnection();
			e.getChannel().sendMessage("Stopped playing.").queue();
			CommandDispatcher.displayResult(CommandResult.SUCCESS, e.getMessage());
		});
		return CommandResult.IGNORE;
	}

	private final PermissionNode permShuffle = PermissionNode.get(NodeType.COMMAND, "shuffle");
	private CommandResult cmdShuffle(MessageReceivedEvent e, AudioPlayer player, TrackScheduler scheduler){

		if(!PermissionUtil.hasPermission(e.getAuthor(), permShuffle, e.getGuild())) return CommandResult.NO_PERMISSION;

		AudioManager am = e.getGuild().getAudioManager();
		if(!am.isConnected()){
			e.getChannel().sendMessage("Not currently playing.").queue();
			return CommandResult.ERROR;
		}
		e.getChannel().sendTyping().queue((v) -> {
			scheduler.shuffle();
			e.getChannel().sendMessage("Shuffled playlist.").queue();
			CommandDispatcher.displayResult(CommandResult.SUCCESS, e.getMessage());
		});
		return CommandResult.IGNORE;
	}
	
	public static VoiceChannel connectWithUser(Member member, Guild g){
		AudioManager am = g.getAudioManager();
		VoiceChannel vc = member.getVoiceState().getChannel().asVoiceChannel();
		if(vc != null && (!am.isConnected() || !am.getConnectedChannel().equals(vc))) {
			am.openAudioConnection(vc);
			am.setSelfDeafened(true);
		}
		return vc;
	}

	@Override
	public Set<PermissionNode> provideNodes() {
		return nodes;
	}

}
