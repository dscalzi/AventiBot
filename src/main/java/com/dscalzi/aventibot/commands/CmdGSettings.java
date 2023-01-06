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
import com.dscalzi.aventibot.settings.GlobalConfig;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.InputUtils;
import com.dscalzi.aventibot.util.JDAUtils;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CmdGSettings implements CommandExecutor {

    private final PermissionNode permUpdate = PermissionNode.get(NodeType.SUBCOMMAND, "gsettings", "update");
    private final PermissionNode permInfo = PermissionNode.get(NodeType.SUBCOMMAND, "gsettings", "info");

    public final Set<PermissionNode> nodes;

    public CmdGSettings() {
        nodes = new HashSet<>(Arrays.asList(
                permUpdate,
                permInfo
        ));
    }

    @Override
    public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
        // Temporary until I rewrite this whole bot.
        if (!e.getAuthor().getId().equals("169197209630277642")) {
            return CommandResult.NO_PERMISSION;
        }
        if (args.length > 0) {

            if (args[0].equalsIgnoreCase("update")) {
                if (args.length >= 3) {
                    String prop = args[1];
                    GlobalConfig current = Objects.requireNonNull(SettingsManager.getGlobalConfig());
                    CommandResult result = CommandResult.SUCCESS;
                    switch (prop) {
                        case "currentGame" -> {
                            String newVal = InputUtils.join(args, 2);
                            current.setCurrentGame(newVal);
                            AventiBot.setCurrentGame(newVal);
                        }
                        case "defaultColorHex" -> current.setDefaultColorHex(args[2]);
                        case "defaultCommandPrefix" -> current.setDefaultCommandPrefix(args[2]);
                        default -> {
                            e.getChannel().sendMessage("Unknown settings key: `" + prop + "`.").queue();
                            result = CommandResult.ERROR;
                        }
                    }

                    if(result == CommandResult.SUCCESS) {
                        try {
                            SettingsManager.saveGlobalConfig(current);
                        } catch(IOException exception) {
                            result = CommandResult.ERROR;
                            e.getChannel().sendMessage("IOException while saving the config.").queue();
                            exception.printStackTrace();
                        }
                    }

                    return result;
                }
                e.getChannel().sendMessage("Proper usage is `" + SettingsManager.getCommandPrefix(JDAUtils.getGuildFromCombinedEvent(e)) + "gsettings update <key> <value>`").queue();
                return CommandResult.IGNORE;
            }

        }
        return null;
    }

    @Override
    public Set<PermissionNode> provideNodes() {
        return nodes;
    }

}
