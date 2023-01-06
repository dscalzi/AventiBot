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

package com.dscalzi.aventibot.settings;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.google.gson.InstanceCreator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;

import java.awt.*;

@Getter
@Setter
public class GuildConfig {

    private String colorHex;
    @Setter(AccessLevel.NONE)
    private transient Color colorAWT;
    @Setter(AccessLevel.NONE)
    private transient javafx.scene.paint.Color colorJFX;
    private String commandPrefix;

    public GuildConfig() { }

    public GuildConfig(GlobalConfig globalConfig) {
        this(globalConfig.getDefaultColorHex(), globalConfig.getDefaultCommandPrefix());
    }

    public GuildConfig(String colorHex, String commandPrefix) {
        setColorHex(colorHex);
        this.commandPrefix = commandPrefix;
    }

    public Color getColorAWT() {
        if (colorAWT == null) setColorHex(getColorHex());
        return colorAWT;
    }

    public javafx.scene.paint.Color getColorJFX() {
        if (colorJFX == null) setColorHex(getColorHex());
        return colorJFX;
    }

    public void setColorHex(String colorHex) {
        try {
            this.colorHex = colorHex;
            this.colorAWT = Color.decode(this.colorHex);
            this.colorJFX = javafx.scene.paint.Color.web(this.colorHex);
        } catch (IllegalArgumentException | NullPointerException e) {
            // Assign default
            this.colorHex = "#0f579d";
            this.colorAWT = Color.decode(this.colorHex);
        }
    }

    public String getSendableCommandPrefix(Guild g) {

        if (commandPrefix.equalsIgnoreCase("@MENTION") && AventiBot.getStatus() == BotStatus.CONNECTED)
            return g.getMember(AventiBot.getInstance().getJDA().getSelfUser()).getAsMention() + " ";

        return this.commandPrefix;
    }

    public static InstanceCreator<GuildConfig> instanceCreator(final GlobalConfig globalConfig) {
        return type -> new GuildConfig(globalConfig);
    }

}
