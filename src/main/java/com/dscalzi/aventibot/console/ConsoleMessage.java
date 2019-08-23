/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2019 Daniel D. Scalzi
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

import java.util.ArrayList;

import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import net.dv8tion.jda.internal.requests.EmptyRestAction;

public class ConsoleMessage extends ReceivedMessage {
	
	public ConsoleMessage(MessageChannel channel, String content, User author) {
		super(-1L, channel, MessageType.DEFAULT, false, false, new TLongHashSet(), new TLongHashSet(), false, false, content, "-1", author, null,
				null, new ArrayList<MessageReaction>(), new ArrayList<Attachment>(), new ArrayList<MessageEmbed>());
	}
	
	@Override
	public RestAction<Void> pin() {
		// Unsupported
		return new EmptyRestAction<Void>(getJDA());
	}
	
	@Override
	public RestAction<Void> unpin() {
		// Unsupported
	    return new EmptyRestAction<Void>(getJDA());
	}
	
	@Override
	public RestAction<Void> addReaction(Emote emote) {
		// Unsupported
	    return new EmptyRestAction<Void>(getJDA());
	}
	
	@Override
	public RestAction<Void> addReaction(String unicode) {
		// Unsupported
	    return new EmptyRestAction<Void>(getJDA());
	}
	
	@Override
	public RestAction<Void> clearReactions() {
		// Unsupported
	    return new EmptyRestAction<Void>(getJDA());
	}
	
	@Override
    public MessageAction editMessage(CharSequence newContent) {
		// Unsupported
    	return new ConsoleMessageAction(getJDA(), null, channel, this);
    }

    @Override
    public MessageAction editMessage(MessageEmbed newContent) {
    	// Unsupported
    	return new ConsoleMessageAction(getJDA(), null, channel, this);
    }

    @Override
    public MessageAction editMessageFormat(String format, Object... args) {
    	// Unsupported
    	return new ConsoleMessageAction(getJDA(), null, channel, this);
    }

    @Override
    public MessageAction editMessage(Message newContent) {
    	// Unsupported
    	return new ConsoleMessageAction(getJDA(), null, channel, this);
    }

    @Override
    public AuditableRestAction<Void> delete() {
    	// Unsupported
        return new EmptyRestAction<Void>(getJDA());
    }

	@Override
	protected void unsupported() {
		throw new UnsupportedOperationException("Operation is unsupported on console messages.");
	}

}
