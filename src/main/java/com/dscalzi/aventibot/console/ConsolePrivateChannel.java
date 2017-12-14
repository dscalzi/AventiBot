/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.console;

import org.slf4j.LoggerFactory;

import net.dv8tion.jda.client.entities.Call;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

public class ConsolePrivateChannel implements PrivateChannel {
	
    private final long id;
    private final User user;
    private final JDA api;

    protected ConsolePrivateChannel(User user, JDA api) {
        this.user = user;
        this.api = api;
        this.id = user.getIdLong();
    }
	
    @Override
	public MessageAction sendMessage(CharSequence text) {
    	LoggerFactory.getLogger("??? -> Me").info(text.toString());
    	ConsoleMessage msg = new ConsoleMessage(this, text.toString(), user);
    	return new ConsoleMessageAction(api, null, this, msg);
	}

	@Override
	public MessageAction sendMessage(Message msg) {
		LoggerFactory.getLogger(msg.getAuthor().getDiscriminator() + " -> Me").info(msg.getContentRaw());
		return new ConsoleMessageAction(api, null, this, msg);
	}
	
	@Override
	public MessageAction sendMessage(MessageEmbed embed) {
		LoggerFactory.getLogger("Embeded Message").info("Unable to display embed on terminal.");
		ConsoleMessage msg = new ConsoleMessage(this, null, user);
		return new ConsoleMessageAction(api, null, this, msg);
	}

	@Override
	public String getId() {
		return user.getId();
	}

	@Override
	public User getUser() {
		return this.user;
	}

	@Override
	public JDA getJDA() {
		return this.api;
	}

	@Override
	public String getName() {
		return getUser().getName();
	}

	@Override
	public ChannelType getType() {
		return ChannelType.PRIVATE;
	}

	@Override
	public RestAction<Void> close() {
		// Not Supported
		return new RestAction.EmptyRestAction<Void>(api, null);
	}

	@Override
	public RestAction<Call> startCall() {
		// Not supported
		return null;
	}

	@Override
	public Call getCurrentCall() {
		// Not supported
		return null;
	}

	@Override
	public boolean isFake() {
		// Not supported
		return false;
	}

	@Override
	public String getLatestMessageId() {
		// Not supported
		return null;
	}

	@Override
	public boolean hasLatestMessage() {
		// Not supported
		return false;
	}

	@Override
	public long getLatestMessageIdLong() {
		// Not supported
		return 0L;
	}

	@Override
	public long getIdLong() {
		return id;
	}

}
