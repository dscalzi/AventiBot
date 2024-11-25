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

package com.dscalzi.aventibot.music;

import com.github.topi314.lavasrc.mirror.MirroringAudioTrack;
import com.github.topi314.lavasrc.mirror.MirroringAudioTrackResolver;
import com.sedmelluq.discord.lavaplayer.track.*;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class TrackResolver implements MirroringAudioTrackResolver {

    public static final Pattern ALPHA_NUMERIC = Pattern.compile("[^a-zA-Z0-9 ]");
    public static final Pattern SPACES = Pattern.compile(" +");

    @Override
    public AudioItem apply(MirroringAudioTrack mirroringAudioTrack) {
        final String ytsearch = "ytsearch:";

        // Try by isrc
        AudioItem item = AudioReference.NO_TRACK;
        if (mirroringAudioTrack.getInfo().isrc != null) {
            item = mirroringAudioTrack.loadItem(
                    ytsearch + mirroringAudioTrack.getInfo().isrc);
            AudioTrack track = null;
            if (item instanceof InternalAudioTrack casted) {
                track = casted;
            } else if (item instanceof AudioPlaylist casted) {
                track = casted.getTracks().get(0);
            }
            if (track != null && !match(mirroringAudioTrack, track)) {
                item = AudioReference.NO_TRACK;
            }
        }
        // Try name + isrc
        if (item == AudioReference.NO_TRACK) {
            item = mirroringAudioTrack.loadItem(
                    ytsearch + mirroringAudioTrack.getInfo().title + " " + mirroringAudioTrack.getInfo().author + " " + mirroringAudioTrack.getInfo().isrc);
        }

        if (item == AudioReference.NO_TRACK) {
            log.error("Failed to find track");
        }

        return item;
    }

    protected boolean match(AudioTrack src, AudioTrack resolved) {
        if (src.getDuration() == resolved.getDuration()) {
            // Tracks are the same length.
            return true;
        }

        String srcTitle = sanitize(src.getInfo().title);
        String resolvedTitle = sanitize(resolved.getInfo().title);

        if (srcTitle.contains(resolvedTitle) || resolvedTitle.contains(srcTitle)) {
            // Likely match.
            return true;
        } else {
            log.info("Title Mismatch, Expected like: {}. Actual like: {}.", srcTitle, resolvedTitle);
        }

        return false;
    }

    public String sanitize(String s) {
        return SPACES.matcher(ALPHA_NUMERIC.matcher(s.toLowerCase()).replaceAll("")).replaceAll(" ");
    }

}
