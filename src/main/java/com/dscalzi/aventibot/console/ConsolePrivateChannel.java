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

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

public class ConsolePrivateChannel implements PrivateChannel {

    private final long id;
    private final User user;
    private final JDA api;

    protected ConsolePrivateChannel(User user, JDA api) {
        this.user = user;
        this.api = api;
        this.id = user.getIdLong();
    }

    @Nonnull
    @Override
    public MessageCreateAction sendMessage(CharSequence text) {
        LoggerFactory.getLogger("??? -> Me").info(text.toString());
        ConsoleMessage msg = new ConsoleMessage(this, text.toString(), user);
        return new ConsoleMessageCreateAction(this, msg);
    }

    @Nonnull
    @Override
    public MessageCreateAction sendMessage(@Nonnull MessageCreateData msg) {
        LoggerFactory.getLogger("??? -> Me").info(msg.getContent());
        ConsoleMessage x = new ConsoleMessage(this, msg.getContent(), user);
        return new ConsoleMessageCreateAction(this, x);
    }

    @Nonnull
    @Override
    public MessageCreateAction sendMessageEmbeds(@Nonnull MessageEmbed embed, @Nonnull MessageEmbed... other) {
        LoggerFactory.getLogger("Embeded Message").info("Unable to display embed on terminal.");
        ConsoleMessage msg = new ConsoleMessage(this, null, user);
        return new ConsoleMessageCreateAction(this, msg);
    }

    @Nonnull
    @Override
    public MessageCreateAction sendMessageEmbeds(@Nonnull Collection<? extends MessageEmbed> embeds) {
        LoggerFactory.getLogger("Embeded Message").info("Unable to display embed on terminal.");
        ConsoleMessage msg = new ConsoleMessage(this, null, user);
        return new ConsoleMessageCreateAction(this, msg);
    }

    @Nonnull
    @Override
    public String getId() {
        return user.getId();
    }

    @Nonnull
    @Override
    public User getUser() {
        return this.user;
    }

    @NotNull
    @Override
    public RestAction<User> retrieveUser() {
        return new CompletedRestAction<>(getJDA(), this.user);
    }

    @Nonnull
    @Override
    public JDA getJDA() {
        return this.api;
    }

    @NotNull
    @Override
    public RestAction<Void> delete() {
        return new CompletedRestAction<>(getJDA(), null);
    }

    @Nonnull
    @Override
    public String getName() {
        return getUser().getName();
    }

    @Nonnull
    @Override
    public ChannelType getType() {
        return ChannelType.PRIVATE;
    }

    @Nonnull
    @Override
    public String getLatestMessageId() {
        // Not supported
        return "-1";
    }

    @Override
    public long getLatestMessageIdLong() {
        // Not supported
        return 0L;
    }

    @Override
    public boolean canTalk() {
        return true;
    }

    @Override
    public long getIdLong() {
        return id;
    }

}
