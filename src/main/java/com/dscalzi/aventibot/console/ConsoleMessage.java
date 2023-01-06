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

package com.dscalzi.aventibot.console;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.requests.CompletedRestAction;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class ConsoleMessage extends ReceivedMessage {

    public ConsoleMessage(MessageChannel channel, String content, User author) {
        super(-1L,
                channel,
                MessageType.DEFAULT,
                null,
                false,
                0,
                false,
                false,
                content,
                "-1",
                author,
                null,
                null,
                OffsetDateTime.now(),
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                0,
                null,
                null);
    }

    @Nonnull
    @Override
    public RestAction<Void> pin() {
        // Unsupported
        return new CompletedRestAction<>(getJDA(), null);
    }

    @Nonnull
    @Override
    public RestAction<Void> unpin() {
        // Unsupported
        return new CompletedRestAction<>(getJDA(), null);
    }

    @Nonnull
    @Override
    public RestAction<Void> addReaction(@Nonnull Emoji emote) {
        // Unsupported
        return new CompletedRestAction<>(getJDA(), null);
    }

    @Nonnull
    @Override
    public RestAction<Void> clearReactions() {
        // Unsupported
        return new CompletedRestAction<>(getJDA(), null);
    }

    @Nonnull
    @Override
    public MessageEditAction editMessage(@Nonnull CharSequence newContent) {
        // Unsupported
        return new ConsoleMessageEditAction(channel, null);
    }

    @Nonnull
    @Override
    public MessageEditAction editMessageEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds) {
        // Unsupported
        return new ConsoleMessageEditAction(channel, null);
    }

    @Nonnull
    @Override
    public MessageEditAction editMessageComponents(@Nonnull Collection<? extends LayoutComponent> components) {
        // Unsupported
        return new ConsoleMessageEditAction(channel, null);
    }

    @Nonnull
    @Override
    public MessageEditAction editMessageFormat(@Nonnull String format, @Nonnull Object... args) {
        // Unsupported
        return new ConsoleMessageEditAction(channel, null);
    }

    @Nonnull
    @Override
    public MessageEditAction editMessageAttachments(@Nonnull Collection<? extends AttachedFile> attachments) {
        // Unsupported
        return new ConsoleMessageEditAction(channel, null);
    }

    @Nonnull
    @Override
    public MessageEditAction editMessage(@Nonnull MessageEditData newContent) {
        // Unsupported
        return new ConsoleMessageEditAction(channel, null);
    }

    @Nonnull
    @Override
    public AuditableRestAction<Void> delete() {
        // Unsupported
        return new CompletedRestAction<>(getJDA(), null);
    }

    @Override
    protected void unsupported() {
        throw new UnsupportedOperationException("Operation is unsupported on console messages.");
    }

}
