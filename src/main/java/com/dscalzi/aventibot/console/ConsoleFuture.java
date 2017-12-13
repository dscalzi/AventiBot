/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.console;

import net.dv8tion.jda.core.requests.RestFuture;

public class ConsoleFuture<T> extends RestFuture<T> {
	
    public ConsoleFuture(final T t) {
        super(t);
    }

    public ConsoleFuture(final Throwable t) {
        super(t);
    }

    @Override
    public boolean cancel(final boolean mayInterrupt) {

        return super.cancel(mayInterrupt);
    }
	
}
