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

package com.dscalzi.aventibot.console;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.requests.CompletedRestAction;

import javax.annotation.Nonnull;

public class ConsoleMessage extends ReceivedMessage {
	
	public ConsoleMessage(MessageChannel channel, String content, User author) {
		super(-1L,
				channel,
				MessageType.DEFAULT,
				null,
				false,
				false,
				new TLongHashSet(),
				new TLongHashSet(),
				false,
				false,
				content,
				"-1",
				author,
				null,
				null,
				OffsetDateTime.now(),
				new ArrayList<>(),
				new ArrayList<>(),
				new ArrayList<>(),
				new ArrayList<>(),
				new ArrayList<>(),
				0,
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
	public RestAction<Void> addReaction(@Nonnull Emote emote) {
		// Unsupported
	    return new CompletedRestAction<>(getJDA(), null);
	}

	@Nonnull
	@Override
	public RestAction<Void> addReaction(@Nonnull String unicode) {
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
    public MessageAction editMessage(@Nonnull CharSequence newContent) {
		// Unsupported
    	return new ConsoleMessageAction(getJDA(), null, channel, this);
    }

	@Nonnull
    @Override
    public MessageAction editMessage(@Nonnull MessageEmbed newContent) {
    	// Unsupported
    	return new ConsoleMessageAction(getJDA(), null, channel, this);
    }

	@Nonnull
    @Override
    public MessageAction editMessageFormat(@Nonnull String format, @Nonnull Object... args) {
    	// Unsupported
    	return new ConsoleMessageAction(getJDA(), null, channel, this);
    }

	@Nonnull
    @Override
    public MessageAction editMessage(@Nonnull Message newContent) {
    	// Unsupported
    	return new ConsoleMessageAction(getJDA(), null, channel, this);
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
