/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.console;

import java.util.function.Consumer;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.requests.RequestFuture;
import net.dv8tion.jda.core.requests.Route;
import net.dv8tion.jda.core.requests.Route.CompiledRoute;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

/**
 * Class to avoid queuing messages sent to the console.
 * 
 * @author Daniel D. Scalzi
 *
 */
public class ConsoleMessageAction extends MessageAction {

	private Message message;
	
	public ConsoleMessageAction(JDA api, CompiledRoute route, MessageChannel channel, Message message) {
		super(api, route, channel);
		this.message = message;
	}

	public ConsoleMessageAction(JDA api, Route.CompiledRoute route, MessageChannel channel, StringBuilder contentBuilder) {
		super(api, route, channel, contentBuilder);
	}
	
	@Override
	public void queue() {
        this.queue(null, null);
    }
	
	@Override
	public void queue(Consumer<Message> success) {
        queue(success, null);
    }
	
	
	@Override
	public void queue(Consumer<Message> success, Consumer<Throwable> failure) {
        //Should never have a failure.
		/*if(success == null)
			success = DEFAULT_SUCCESS;
		if (failure == null)
			failure = DEFAULT_FAILURE;*/
		if(success != null)
			success.accept(message);
    }
	
	@Override
	public RequestFuture<Message> submit() {
        return submit(true);
    }

    @Override
    public RequestFuture<Message> submit(boolean shouldQueue) {
        return new ConsoleFuture<>(message);
    }
}
