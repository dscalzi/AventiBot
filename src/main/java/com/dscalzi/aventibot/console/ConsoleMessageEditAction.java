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

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.RestFuture;
import net.dv8tion.jda.internal.requests.restaction.MessageEditActionImpl;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Class to avoid queuing messages sent to the console.
 *
 * @author Daniel D. Scalzi
 */
public class ConsoleMessageEditAction extends MessageEditActionImpl {

    private Message message;

    public ConsoleMessageEditAction(MessageChannel channel, Message message) {
        super(channel, "-1");
        this.message = message;
    }

    @Override
    public void queue() {
        this.queue(null, null);
    }

    @Override
    public void queue(Consumer<? super Message> success) {
        queue(success, null);
    }


    @Override
    public void queue(Consumer<? super Message> success, Consumer<? super Throwable> failure) {
        //Should never have a failure.
		/*if(success == null)
			success = DEFAULT_SUCCESS;
		if (failure == null)
			failure = DEFAULT_FAILURE;*/
        if (success != null)
            success.accept(message);
    }

    @Nonnull
    @Override
    public CompletableFuture<Message> submit() {
        return submit(true);
    }

    @Nonnull
    @Override
    public CompletableFuture<Message> submit(boolean shouldQueue) {
        return new RestFuture<>(message);
    }
}
