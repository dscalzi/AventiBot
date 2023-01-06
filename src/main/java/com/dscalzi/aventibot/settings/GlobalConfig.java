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
import com.dscalzi.aventibot.util.Pair;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GlobalConfig {

    public static final Map<Pair<String, Object>, Method> keyMap;

    static {
        keyMap = new HashMap<>();
        try {
            keyMap.put(new Pair<>("token", null), GlobalConfig.class.getMethod("setToken", String.class));
            keyMap.put(new Pair<>("currentGame", "Developed by Dan"), GlobalConfig.class.getMethod("setCurrentGame", String.class));
            keyMap.put(new Pair<>("defaultColorHex", "#0f579d"), GlobalConfig.class.getMethod("setDefaultColor", String.class));
            keyMap.put(new Pair<>("defaultCommandPrefix", "--"), GlobalConfig.class.getMethod("setDefaultCommandPrefix", String.class));
            // TODO This needs to be rewritten to use a POJO, etc. Implementing this using the current setup.
            keyMap.put(new Pair<>("spotifyClientId", null), GlobalConfig.class.getMethod("setSpotifyClientId", String.class));
            keyMap.put(new Pair<>("spotifyClientSecret", null), GlobalConfig.class.getMethod("setSpotifyClientSecret", String.class));
            keyMap.put(new Pair<>("spotifyCountryCode", null), GlobalConfig.class.getMethod("setSpotifyCountryCode", String.class));
        } catch (NoSuchMethodException | SecurityException e) {
            //Shouldn't happen since this is hard coded.
            e.printStackTrace();
        }
    }

    private String token;
    private String currentGame;
    private String defaultColorHex;
    private transient Color defaultColorAWT;
    private transient javafx.scene.paint.Color defaultColorJFX;
    private String defaultCommandPrefix;

    private String spotifyClientId;
    private String spotifyClientSecret;
    private String spotifyCountryCode;

    public GlobalConfig() { /* For deserialization. */ }

    public GlobalConfig(String token, String currentGame, String defaultColorHex, String defaultCommandPrefix) {
        this.token = token;
        this.currentGame = currentGame;
        setDefaultColor(defaultColorHex);
        this.defaultCommandPrefix = defaultCommandPrefix;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(String currentGame) {
        this.currentGame = currentGame;
    }

    public Color getDefaultColorAWT() {
        if (defaultColorAWT == null) setDefaultColor(getDefaultColorHex());
        return defaultColorAWT;
    }

    public javafx.scene.paint.Color getDefaultColorJFX() {
        if (defaultColorJFX == null) setDefaultColor(getDefaultColorHex());
        return defaultColorJFX;
    }

    public String getDefaultColorHex() {
        return defaultColorHex;
    }

    public void setDefaultColor(String defaultColor) {
        try {
            defaultColorHex = defaultColor;
            this.defaultColorAWT = Color.decode(defaultColorHex);
            this.defaultColorJFX = javafx.scene.paint.Color.web(defaultColor);
        } catch (IllegalArgumentException | NullPointerException e) {
            //Assign default
            defaultColorHex = "#0f579d";
            this.defaultColorAWT = Color.decode(defaultColorHex);
        }
    }

    public String getCommandPrefix() {
        if (defaultCommandPrefix.equalsIgnoreCase("@MENTION") && AventiBot.getStatus() == BotStatus.CONNECTED)
            return AventiBot.getInstance().getJDA().getSelfUser().getAsMention();

        return this.defaultCommandPrefix;
    }

    /**
     * Returns raw command prefix specified in the configuration
     * without any modifications.
     */
    public String getRawCommandPrefix() {
        return this.defaultCommandPrefix;
    }

    public void setDefaultCommandPrefix(String defaultCommandPrefix) {
        this.defaultCommandPrefix = defaultCommandPrefix;
    }

    public String getSpotifyClientId() {
        return spotifyClientId;
    }

    public void setSpotifyClientId(String spotifyClientId) {
        this.spotifyClientId = spotifyClientId;
    }

    public String getSpotifyClientSecret() {
        return spotifyClientSecret;
    }

    public void setSpotifyClientSecret(String spotifyClientSecret) {
        this.spotifyClientSecret = spotifyClientSecret;
    }

    public String getSpotifyCountryCode() {
        return spotifyCountryCode;
    }

    public void setSpotifyCountryCode(String spotifyCountryCode) {
        this.spotifyCountryCode = spotifyCountryCode;
    }

}
