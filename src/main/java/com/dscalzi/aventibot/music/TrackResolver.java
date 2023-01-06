package com.dscalzi.aventibot.music;

import com.github.topisenpai.lavasrc.mirror.MirroringAudioTrack;
import com.github.topisenpai.lavasrc.mirror.MirroringAudioTrackResolver;
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
        if (mirroringAudioTrack.getISRC() != null) {
            item = mirroringAudioTrack.loadItem(
                    ytsearch + mirroringAudioTrack.getISRC());
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
                    ytsearch + mirroringAudioTrack.getInfo().title + " " + mirroringAudioTrack.getISRC());
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
