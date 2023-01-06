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
import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.util.JDAUtils;
import com.dscalzi.aventibot.util.TimeUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class CmdAuthor implements CommandExecutor {

    private final PermissionNode permAuthor = PermissionNode.get(NodeType.COMMAND, "author");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);

    public final Set<PermissionNode> nodes;

    public CmdAuthor() {
        nodes = new HashSet<>(Collections.singletonList(
                permAuthor
        ));
    }

    @Override
    public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {

        if (!PermissionUtil.hasPermission(e.getAuthor(), permAuthor, JDAUtils.getGuildFromCombinedEvent(e), true))
            return CommandResult.NO_PERMISSION;

        User author = AventiBot.getInstance().getJDA().getUserById("169197209630277642");
        String avatar = author.getAvatarUrl();
        if (avatar == null) avatar = author.getDefaultAvatarUrl();

        EmbedBuilder b = new EmbedBuilder();
        b.setAuthor("Daniel Scalzi", "https://twitter.com/d_scalzi", avatar);
        b.setColor(Color.decode("#0f579d"));
        b.setDescription("AventiBot was developed by Daniel Scalzi (" + author.getAsMention() + ")");

        long uptime = AventiBot.getInstance().getUptime();

        OffsetDateTime since = OffsetDateTime.ofInstant(Instant.ofEpochMilli(AventiBot.getInstance().getLaunchTime()), ZoneId.systemDefault());

        String upStr = "Uptime: " + TimeUtils.formatUptime(uptime) + " (since " + since.format(formatter) + ")";

        String ver = AventiBot.getVersion();
        ver = !ver.equals("Debug") ? "v" + ver : "Debug Mode";
        b.setFooter(ver + " | " + upStr, null);

        e.getChannel().sendMessageEmbeds(b.build()).queue();

        return CommandResult.SUCCESS;
    }

    @Override
    public Set<PermissionNode> provideNodes() {
        return nodes;
    }

}
