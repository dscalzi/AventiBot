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

package com.dscalzi.aventibot.cmdutil;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Set;

/**
 * Interface representing a CommandExecutor object.
 *
 * @author Daniel D. Scalzi
 */
public interface CommandExecutor {

    /**
     * Called when the command is received.
     *
     * @param e       The MessageRecievedEvent that triggered the command.
     * @param cmd     The command string which was typed.
     * @param args    The message contents split into arguments.
     * @param rawArgs The <strong>raw</strong> message contents split into arguments.
     * @return The result of the command operation.
     */
    CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs);

    /**
     * This method should provide a Set of PermissionNode objects, which are the
     * permissions used by this command executor. This is essentially registering
     * those nodes. This method must not return null.
     *
     * @return Never null Set of PermissionNode objects used by this command executor.
     */
    Set<PermissionNode> provideNodes();

}
