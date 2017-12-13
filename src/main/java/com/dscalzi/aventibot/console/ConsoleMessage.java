/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.console;

import java.util.ArrayList;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.impl.ReceivedMessage;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

public class ConsoleMessage extends ReceivedMessage {
	
	public ConsoleMessage(MessageChannel channel, String content, User author) {
		super(-1L, channel, MessageType.DEFAULT, false, false, false, false, content, "-1", author, null,
				new ArrayList<MessageReaction>(), new ArrayList<Attachment>(), new ArrayList<MessageEmbed>());
	}
	
	@Override
	public RestAction<Void> pin() {
		// Unsupported
		return new RestAction.EmptyRestAction<Void>(getJDA(), null);
	}
	
	@Override
	public RestAction<Void> unpin() {
		// Unsupported
		return new RestAction.EmptyRestAction<Void>(getJDA(), null);
	}
	
	@Override
	public RestAction<Void> addReaction(Emote emote) {
		// Unsupported
		return new RestAction.EmptyRestAction<Void>(getJDA(), null);
	}
	
	@Override
	public RestAction<Void> addReaction(String unicode) {
		// Unsupported
		return new RestAction.EmptyRestAction<Void>(getJDA(), null);
	}
	
	@Override
	public RestAction<Void> clearReactions() {
		// Unsupported
		return new RestAction.EmptyRestAction<Void>(getJDA(), null);
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
        return new AuditableRestAction.EmptyRestAction<Void>(getJDA());
    }

	@Override
	protected void unsupported() {
		throw new UnsupportedOperationException("Operation is unsupported on console messages.");
	}

}
