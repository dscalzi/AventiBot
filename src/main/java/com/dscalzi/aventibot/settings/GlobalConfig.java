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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class GlobalConfig {

    private String token;
    private String currentGame;
    private String defaultColorHex;
    private transient Color defaultColorAWT;
    private transient javafx.scene.paint.Color defaultColorJFX;
    private String defaultCommandPrefix;

    private SpotifyConfig spotifyConfig;

    public GlobalConfig() {
        this(
                null,
                "Developed by Dan",
                "#0f579d",
                "--",
                new SpotifyConfig()
        );
    }

    public GlobalConfig(String token, String currentGame, String defaultColorHex, String defaultCommandPrefix, SpotifyConfig spotifyConfig) {
        this.token = token;
        this.currentGame = currentGame;
        setDefaultColorHex(defaultColorHex);
        this.defaultCommandPrefix = defaultCommandPrefix;
        this.spotifyConfig = spotifyConfig;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpotifyConfig {
        private String clientId;
        private String clientSecret;
        private String countryCode;
    }

    public void setDefaultColorHex(String defaultColor) {
        try {
            this.defaultColorHex = defaultColor;
            this.defaultColorAWT = Color.decode(defaultColorHex);
            this.defaultColorJFX = javafx.scene.paint.Color.web(defaultColor);
        } catch (IllegalArgumentException | NullPointerException e) {
            //Assign default
            e.printStackTrace();
            this.defaultColorHex = "#0f579d";
            this.defaultColorAWT = Color.decode(defaultColorHex);
        }
    }

    public Color getDefaultColorAWT() {
        if (defaultColorAWT == null) setDefaultColorHex(getDefaultColorHex());
        return defaultColorAWT;
    }

    public javafx.scene.paint.Color getDefaultColorJFX() {
        if (defaultColorJFX == null) setDefaultColorHex(getDefaultColorHex());
        return defaultColorJFX;
    }

    public String getSendableCommandPrefix() {
        if (defaultCommandPrefix.equalsIgnoreCase("@MENTION") && AventiBot.getStatus() == BotStatus.CONNECTED)
            return AventiBot.getInstance().getJDA().getSelfUser().getAsMention();

        return this.defaultCommandPrefix;
    }

}
