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
import com.dscalzi.aventibot.cmdutil.*;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.settings.GuildConfig;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.IconUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.IOException;
import java.util.*;

public class CmdSettingsControl implements CommandExecutor {

    private final PermissionNode permUpdate = PermissionNode.get(NodeType.SUBCOMMAND, "settings", "update");
    private final PermissionNode permInfo = PermissionNode.get(NodeType.SUBCOMMAND, "settings", "info");

    public final Set<PermissionNode> nodes;

    public CmdSettingsControl() {
        nodes = new HashSet<>(Arrays.asList(
                permUpdate,
                permInfo
        ));
    }

    @Override
    public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
        if (!e.isFromGuild()) {
            e.getChannel().sendMessage("This command may only be used in guilds!").queue();
            return CommandResult.NO_PERMISSION;
        }

        if (args.length > 0) {

            if (args[0].equalsIgnoreCase("update")) {
                if (!PermissionUtil.hasPermission(e.getAuthor(), permUpdate, e.getGuild())) {
                    return CommandResult.NO_PERMISSION;
                }
                if (args.length > 1) {
                    return this.cmdUpdate(e, args[1], args);
                }
                e.getChannel().sendMessage("Proper usage is `" + SettingsManager.getCommandPrefix(e.getGuild()) + "settings update <key> <value>`").queue();
                return CommandResult.IGNORE;
            }

            if (args[0].equalsIgnoreCase("info")) {
                if (!PermissionUtil.hasPermission(e.getAuthor(), permInfo, e.getGuild())) {
                    return CommandResult.NO_PERMISSION;
                }
                if (args.length > 1) {
                    return this.cmdInfo(e, args[1]);
                }
                return this.cmdInfoFull(e);
            }

            e.getChannel().sendMessage("Unknown subcommand: `" + args[0] + "`.").queue();
            return CommandResult.ERROR;
        }

        //TODO subcommand list
        e.getChannel().sendMessage("Subcommand list coming soon!").queue();
        return CommandResult.IGNORE;
    }

    private CommandResult cmdUpdate(MessageReceivedEvent e, String key, String[] args) {

        if (args.length < 3) {
            e.getChannel().sendMessage("Proper usage is " + SettingsManager.getCommandPrefix(e.getGuild()) + "settings update " + key.toLowerCase() + " <value>").queue();
            return CommandResult.ERROR;
        }

        GuildConfig current = SettingsManager.getGuildConfig(e.getGuild());
        CommandResult result = CommandResult.SUCCESS;
        switch (key) {
            case "colorHex" -> {
                try {
                    Color.decode(args[2]);
                    current.setColorHex(args[2]);
                    return CommandResult.SUCCESS;
                } catch (NumberFormatException e1) {
                    e.getChannel().sendMessage("Invalid color, must be a valid hex color code.").queue();
                    return CommandResult.ERROR;
                }
            }
            case "commandPrefix" -> current.setCommandPrefix(args[2]);
            default -> {
                e.getChannel().sendMessage("Unknown settings key: `" + key + "`.").queue();
                result = CommandResult.ERROR;
            }
        }
        if (result == CommandResult.SUCCESS) {
            try {
                SettingsManager.saveGuildConfig(e.getGuild(), current);
            } catch(IOException exception) {
                result = CommandResult.ERROR;
                e.getChannel().sendMessage("IOException while saving the config.").queue();
                exception.printStackTrace();
            }
        }

        return result;
    }

    private CommandResult cmdInfo(MessageReceivedEvent e, String key) {
        GuildConfig current = SettingsManager.getGuildConfig(e.getGuild());
        Object val = null;
        CommandResult result = CommandResult.SUCCESS;
        switch (key) {
            case "colorHex" -> val = current.getColorHex();
            case "commandPrefix" -> val = current.getCommandPrefix();
            default -> {
                e.getChannel().sendMessage("Unknown settings key: `" + key + "`.").queue();
                result = CommandResult.ERROR;
            }
        }
        if (result == CommandResult.SUCCESS) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Value of `" + key + "`", null);
            eb.setDescription("`" + val + "`");
            eb.setColor(current.getColorAWT());

            e.getChannel().sendMessageEmbeds(eb.build()).queue();
        }

        return result;
    }

    private CommandResult cmdInfoFull(MessageReceivedEvent e) {
        GuildConfig current = SettingsManager.getGuildConfig(e.getGuild());
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Configuration for " + e.getGuild().getName(), null);
        eb.setColor(current.getColorAWT());

        String proper = SettingsManager.getCommandPrefix(e.getGuild());
        if (proper.trim().equals(AventiBot.getInstance().getJDA().getSelfUser().getAsMention())) {
            proper = CommandDispatcher.getDisplayedMention(e.getGuild()) + " ";
        }
        eb.setFooter("Narrow Search | " + proper + "settings info <key>.", IconUtil.INFO.getURL());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("colorHex", current.getColorHex());
        map.put("commandPrefix", current.getCommandPrefix());

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            eb.appendDescription("**" + entry.getKey() + "**\t"
                    + "`" + entry.getValue() + "`\n");
        }

        e.getChannel().sendMessageEmbeds(eb.build()).queue();

        return CommandResult.SUCCESS;
    }


    @Override
    public Set<PermissionNode> provideNodes() {
        return nodes;
    }

}
