package com.dscalzi.obsidianbot.commands;

import java.awt.Color;
import java.util.Iterator;
import java.util.Queue;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.music.LavaWrapper;
import com.dscalzi.obsidianbot.music.TrackMeta;
import com.dscalzi.obsidianbot.music.TrackScheduler;
import com.dscalzi.obsidianbot.util.TimeUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class PlaylistCmd implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		AudioPlayer player = LavaWrapper.getInstance().getAudioPlayer(e.getGuild().getId());
		TrackScheduler scheduler = LavaWrapper.getInstance().getScheduler(player);
		
		Queue<TrackMeta> q = scheduler.getQueue();
		
		if(q.isEmpty()){
			e.getChannel().sendMessage("Nothing is queued.").queue();;
			return true;
		}
		
		e.getChannel().sendTyping().queue();
		
		Iterator<TrackMeta> it = q.iterator();
		EmbedBuilder eb = new EmbedBuilder().setColor(Color.decode("#df4efc"));
		
		String desc = "";
		long timeUntilEnd = 0;
		
		int pos = 1;
		boolean curr = true;
		while(it.hasNext()){
			TrackMeta t = it.next();
			if(curr){
				curr = false;
				eb.addField(new Field("Currently Playing:", t.getTrack().getInfo().title + " (" + TimeUtils.formatTrackDuration(t.getTrack().getPosition()) + "/" + TimeUtils.formatTrackDuration(t.getTrack().getDuration()) + ")", false));
				timeUntilEnd += t.getTrack().getDuration() - t.getTrack().getPosition();
				continue;
			}
			desc += "\n" + pos + ") " + t.getTrack().getInfo().title + " (" + TimeUtils.formatTrackDuration(t.getTrack().getDuration()) + ")";
			timeUntilEnd += t.getTrack().getDuration();
			++pos;
		}
		
		if(desc.length() > 0)
			eb.addField(new Field("Up Next:", desc, false));
		eb.setFooter("Playlist Length " + TimeUtils.formatTrackDuration(timeUntilEnd), "http://i.imgur.com/Y3rbhFt.png");
		
		
		e.getChannel().sendMessage(new MessageBuilder().setEmbed(eb.build()).build()).queue();;
		
		return true;
	}

}
