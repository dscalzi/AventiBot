/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2023 Daniel D. Scalzi
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

import com.dscalzi.aventibot.cmdutil.*;
import com.dscalzi.aventibot.music.LavaWrapper;
import com.dscalzi.aventibot.music.TrackMeta;
import com.dscalzi.aventibot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CmdSheik implements CommandExecutor {

    public final Set<PermissionNode> nodes;

    public CmdSheik() {
        nodes = new HashSet<>(Collections.singletonList(
                permSheik
        ));
    }

    // TODO This is a low-effort implementation. Music should be abstracted into a utility so we arent touching the other command executor.
    // One day this will be cleaned up.
    private final PermissionNode permSheik = PermissionNode.get(PermissionNode.NodeType.COMMAND, "sheik");

    @Override
    public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
        if (!PermissionUtil.hasPermission(e.getAuthor(), permSheik, e.getGuild())) return CommandResult.NO_PERMISSION;

        if (!e.isFromGuild()) {
            e.getChannel().sendMessage("You must use this command in a guild.").queue();
            return CommandResult.ERROR;
        }

        if (CmdMusicControl.connectWithUser(e.getGuild().getMember(e.getAuthor()), e.getGuild()) == null) {
            e.getChannel().sendMessage("You currently aren't in a channel, sorry!").queue();
            return CommandResult.ERROR;
        }

        AudioPlayer player = LavaWrapper.getInstance().getAudioPlayer(e.getGuild());
        TrackScheduler scheduler = LavaWrapper.getInstance().getScheduler(e.getGuild());

        if (player.getPlayingTrack() != null) {
            e.getChannel().sendMessage("You cannot use this command while music is playing!").queue();
            return CommandResult.ERROR;
        }

        LavaWrapper.getInstance().getAudioPlayerManager().loadItem("https://www.youtube.com/watch?v=kpoMGHYKGo8",
                new AudioLoadResultHandler() {

                    @Override
                    public void trackLoaded(AudioTrack track) {
                        scheduler.queue(new TrackMeta(track, e.getAuthor(), e.getChannel()));
                        CommandDispatcher.displayResult(CommandResult.SUCCESS, e.getMessage());
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist playlist) {
                        e.getChannel().sendMessage("Something went wrong.").queue();
                        CommandDispatcher.displayResult(CommandResult.ERROR, e.getMessage());
                    }

                    @Override
                    public void noMatches() {
                        e.getChannel().sendMessage("No matches :(").queue();
                        if (scheduler.getQueue().isEmpty())
                            e.getGuild().getAudioManager().closeAudioConnection();
                        CommandDispatcher.displayResult(CommandResult.ERROR, e.getMessage());
                    }

                    @Override
                    public void loadFailed(FriendlyException exception) {
                        e.getChannel().sendMessage("Load failed :(").queue();
                        if (scheduler.getQueue().isEmpty())
                            e.getGuild().getAudioManager().closeAudioConnection();
                        CommandDispatcher.displayResult(CommandResult.ERROR, e.getMessage());
                        throw exception;
                    }

                });

        return CommandResult.IGNORE;

    }

    @Override
    public Set<PermissionNode> provideNodes() {
        return nodes;
    }

}
