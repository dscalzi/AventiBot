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
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.UserImpl;

public class ConsoleUser extends UserImpl {

    private static final long _id;
    private static final String _username;
    private static final short _discriminator;
    private static final String _avatarId;
    private static final boolean _isBot;

    private static boolean limit;

    static {
        _id = -1L;
        _username = "AventiBot_Console";
        _discriminator = -1;
        _avatarId = null;
        _isBot = false;

        limit = false;
    }

    private final ConsolePrivateChannel privateChannel;

    private ConsoleUser(JDA api) {
        super(_id, (JDAImpl) api);
        this.setName(ConsoleUser._username);
        this.setGlobalName(ConsoleUser._username);
        this.setDiscriminator(ConsoleUser._discriminator);
        this.setAvatarId(ConsoleUser._avatarId);
        this.privateChannel = new ConsolePrivateChannel(this, api);
        this.privateChannelId = this.privateChannel.getIdLong();
        this.setBot(ConsoleUser._isBot);
    }

//	@Override
//	public PrivateChannel getPrivateChannel() {
//		return this.getPrivateChannel();
//	}

    public static ConsoleUser build(JDA api) {
        if (!limit) {
            limit = true;
            return new ConsoleUser(api);
        }
        throw new IllegalStateException("Cannot build more than one ConsoleUser!");
    }

    @Override
    public PrivateChannel getPrivateChannel() {
        return this.privateChannel;
    }
}
