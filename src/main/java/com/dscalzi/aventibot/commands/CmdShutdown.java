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

package com.dscalzi.aventibot.commands;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.util.JDAUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CmdShutdown implements CommandExecutor {

    private final PermissionNode permShutdown = PermissionNode.get(NodeType.COMMAND, "shutdown");

    public final Set<PermissionNode> nodes;

    public CmdShutdown() {
        nodes = new HashSet<>(Collections.singletonList(
                permShutdown
        ));
    }

    @Override
    public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {

        if (!PermissionUtil.hasPermission(e.getAuthor(), permShutdown, JDAUtils.getGuildFromCombinedEvent(e), false))
            return CommandResult.NO_PERMISSION;

        e.getChannel().sendMessage("Shutting down.. :(").queue(v -> {
            try {
                if (AventiBot.getStatus() == BotStatus.CONNECTED) {
                    AventiBot.getInstance().shutdown();
                }
            } catch (Exception ex) {
                //Shutdown
                Runtime.getRuntime().exit(0);
            }
        });

        //The JDA should be shutdown, so the result is null.
        return null;
    }

    @Override
    public Set<PermissionNode> provideNodes() {
        return nodes;
    }

}
