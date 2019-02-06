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
