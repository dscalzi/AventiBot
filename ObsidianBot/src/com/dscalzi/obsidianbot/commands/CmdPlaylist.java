package com.dscalzi.obsidianbot.commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import com.dscalzi.obsidianbot.cmdutil.CommandExecutor;
import com.dscalzi.obsidianbot.music.LavaWrapper;
import com.dscalzi.obsidianbot.music.TrackMeta;
import com.dscalzi.obsidianbot.music.TrackScheduler;
import com.dscalzi.obsidianbot.util.PageList;
import com.dscalzi.obsidianbot.util.TimeUtils;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdPlaylist implements CommandExecutor{

	@Override
	public boolean onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		AudioPlayer player = LavaWrapper.getInstance().getAudioPlayer(e.getGuild());
		TrackScheduler scheduler = LavaWrapper.getInstance().getScheduler(player);
		
		Queue<TrackMeta> q = scheduler.getQueue();
		
		if(q.isEmpty()){
			e.getChannel().sendMessage("Nothing is queued.").queue();;
			return true;
		}
		
		e.getChannel().sendTyping().queue();
		List<TrackMeta> queued = new ArrayList<TrackMeta>(Arrays.asList(q.toArray(new TrackMeta[0])));
		TrackMeta current = queued.get(0);
		queued.remove(0);
		PageList<TrackMeta> tracks = new PageList<TrackMeta>(queued);
		int page;
		try{
			page= args.length == 0 ? 0 : Integer.parseInt(args[0])-1;
		} catch (NumberFormatException ex){
			page = 0;
		}
		if(page > tracks.size()-1 || page < 0){
			e.getChannel().sendMessage("Page not found, sorry.").queue();
			return false;
		}
		
		EmbedBuilder eb = new EmbedBuilder().setColor(Color.decode("#df4efc"));
		eb.addField(new Field("Currently Playing:", current.getTrack().getInfo().title + " (" + TimeUtils.formatTrackDuration(current.getTrack().getPosition()) + "/" + TimeUtils.formatTrackDuration(current.getTrack().getDuration()) + ")", false));
		String desc = "";
		
		for(int i=0; i<tracks.getPage(page).size(); ++i){
			TrackMeta t = tracks.getPage(page).get(i);
			desc += "\n" + (i+1+5*page) + ") " + t.getTrack().getInfo().title + " (" + TimeUtils.formatTrackDuration(t.getTrack().getDuration()) + ")";
		}
		
		if(desc.length() > 0) eb.addField(new Field("Up Next:", desc, false));
		eb.setFooter("Playlist Length " + TimeUtils.formatTrackDuration(scheduler.getPlaylistDuration()) + (tracks.size() > 0 ? " | Page " + (page+1) + " of " + tracks.size(): ""), "http://i.imgur.com/Y3rbhFt.png");
		
		
		e.getChannel().sendMessage(new MessageBuilder().setEmbed(eb.build()).build()).queue();;
		
		return true;
	}

	@Override
	public List<String> getNodes() {
		return new ArrayList<String>(Arrays.asList("playlist.command"));
	}
	
	

}
