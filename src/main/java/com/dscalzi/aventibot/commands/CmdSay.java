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

import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.console.ConsoleUser;
import com.dscalzi.aventibot.util.InputUtils;
import com.dscalzi.aventibot.util.JDAUtils;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CmdSay implements CommandExecutor {

    private final PermissionNode permSay = PermissionNode.get(NodeType.COMMAND, "say");

    public final Set<PermissionNode> nodes;

    public CmdSay() {
        nodes = new HashSet<>(Collections.singletonList(
                permSay
        ));
    }

    @Override
    public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {

        if (!PermissionUtil.hasPermission(e.getAuthor(), permSay, JDAUtils.getGuildFromCombinedEvent(e)))
            return CommandResult.NO_PERMISSION;

        if (args.length == 0) {
            e.getChannel().sendMessage("Why are you trying to get me to say nothing.. lol").queue();
            return CommandResult.ERROR;
        }

        MessageChannel ch = InputUtils.parseChannel(e.getMessage(), args[0]);

        if (ch != null && e.isFromGuild()) {
            if (!e.getGuild().getTextChannels().contains(ch)) {
                e.getChannel().sendMessage("I cannot message other guilds for you, sorry!").queue();
                return CommandResult.ERROR;
            }
        }

        String message = e.getMessage().getContentRaw().substring((ch == null) ? e.getMessage().getContentRaw().indexOf(cmd) + cmd.length() : e.getMessage().getContentRaw().indexOf(rawArgs[0]) + rawArgs[0].length());
        MessageCreateBuilder mb = new MessageCreateBuilder();
        mb.addContent(message);

        if (ch == null) {
            if (e.getAuthor() instanceof ConsoleUser) {
                e.getChannel().sendMessage("Please specify a valid channel!").queue();
                return CommandResult.ERROR;
            } else if (e.isFromType(ChannelType.PRIVATE))
                ch = e.getChannel().asPrivateChannel();
            else
                ch = e.getChannel();
        }

        if (!(e.getMessage().isFromType(ChannelType.PRIVATE)))
            e.getMessage().delete().queue();

        ch.sendMessage(message).queue();

        return CommandResult.IGNORE;
    }

    @Override
    public Set<PermissionNode> provideNodes() {
        return nodes;
    }

}
