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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.MarkerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class to handle the retrieval of settings directories.
 *
 * @author Daniel D. Scalzi
 */
@Slf4j
public class SettingsManager {

    private static GlobalConfig configCache = null;
    private static final Map<String, GuildConfig> gConfigCache = new HashMap<>();

    public static void reload() {
        configCache = null;
        gConfigCache.clear();
    }

    public static void reload(Guild g) {
        gConfigCache.remove(g.getId());
    }

    /* * * * *
     *
     * Retrieval Methods
     *
     * * * * */

    /**
     * Returns the base settings directory.
     *
     * @return The base settings directory if it exists/was created, otherwise null.
     */
    public static File getBaseSettingsDirectory() {
        File f = new File("settings");
        if (!f.exists()) {
            if (f.mkdirs()) return f;
            else {
                log.error(MarkerFactory.getMarker("FATAL"), "Unable to create settings directory!");
                return null;
            }
        }
        return f;
    }

    /**
     * Returns the settings directory for the specified Guild. The name of the
     * directory will be equal to the ID of the Guild.
     *
     * @param g The Guild to get the settings directory for.
     * @return The settings directory for the specified guild if it exists/was created, otherwise null.
     */
    public static File getSettingsDirectory(Guild g) {
        File f = new File(getBaseSettingsDirectory(), g.getId());
        if (!f.exists()) {
            if (f.mkdirs()) return f;
            else {
                log.error(MarkerFactory.getMarker("FATAL"), "Unable to create settings directory for guild " + g.getName() + " (" + g.getId() + ")!");
                return null;
            }
        }
        return f;
    }

    /**
     * Returns the permission file for the specified Guild. The name of the
     * directory will be equal to the ID of the Guild.
     *
     * @param g The Guild to get the permission file for.
     * @return The permission file for the specified guild if it exists/was created, otherwise null.
     */
    public static File getPermissionFile(Guild g) {
        File f = new File(getSettingsDirectory(g), "permissions.json");
        if (!f.exists()) {
            try {
                if (f.createNewFile()) return f;
                else {
                    log.error(MarkerFactory.getMarker("FATAL"), "Unable to create permission file for guild " + g.getName() + " (" + g.getId() + ")!");
                    return null;
                }
            } catch (IOException e) {
                log.error(MarkerFactory.getMarker("FATAL"), "Unable to create permission file for guild " + g.getName() + " (" + g.getId() + ")!");
                e.printStackTrace();
                return null;
            }
        }
        return f;
    }

    public static File getConfigurationFile(Guild g) {
        return new File(getSettingsDirectory(g), "configuration.json");
    }

    public static File getGlobalConfigurationFile() {
        return new File(getBaseSettingsDirectory(), "configuration.json");
    }

    /* * * * *
     *
     * Global Config Saving/Loading
     *
     * * * * */

    /**
     * Retrieves the cached GlobalConfig value that was stored the last time
     * it was loaded. If no value is already cached, it will load the value
     * and cache it.
     * <p>
     * In order to load the GlobalConfig directly from the serialized JSON form,
     * see {@link #loadGlobalConfig()}
     *
     * @return The latest cached GlobalConfig object.
     */
    public static GlobalConfig getGlobalConfig() {
        return configCache == null ? loadGlobalConfig() : configCache;
    }

    /**
     * Serializes the GlobalConfig object to JSON.
     *
     * @param g GlobalConfig object to serialize.
     */
    public static void saveGlobalConfig(GlobalConfig g) throws IOException {
        File target = SettingsManager.getGlobalConfigurationFile();

        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        try (FileWriter w = new FileWriter(target)) {
            gson.toJson(g, w);
        }
        configCache = g;
    }


    /**
     * Deserializes the GlobalConfig object from JSON.
     *
     * @return The GlobalConfig data directly from JSON.
     */
    public static GlobalConfig loadGlobalConfig() {
        try {
            File target = SettingsManager.getGlobalConfigurationFile();

            if (!target.exists()) {
                GlobalConfig g = new GlobalConfig();
                saveGlobalConfig(g);
                return g;
            }

            GlobalConfig g;
            try (FileReader reader = new FileReader(target)) {
                Gson gson = new Gson();
                g = gson.fromJson(reader, GlobalConfig.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            saveGlobalConfig(g);

            return g;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* * * * *
     *
     * Guild Config Saving/Loading
     *
     * * * * */

    /**
     * Retrieves the cached GuildConfig value that was stored the last time
     * it was loaded. If no value is already cached, it will load the value
     * and cache it.
     * <p>
     * In order to load the GuildConfig directly from the serialized JSON form,
     * see {@link #loadGuildConfig(Guild)}
     *
     * @return The latest cached GuildConfig object.
     */
    public static GuildConfig getGuildConfig(Guild g) {
        if (gConfigCache.containsKey(g.getId())) return gConfigCache.get(g.getId());
        else return loadGuildConfig(g);
    }

    /**
     * Serializes the GuildConfig object to JSON.
     *
     * @param id Guild to save the settings for.
     * @param g  GuildConfig object to serialize.
     */
    public static void saveGuildConfig(Guild id, GuildConfig g) throws IOException {
        File target = SettingsManager.getConfigurationFile(id);

        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        try (FileWriter w = new FileWriter(target)) {
            gson.toJson(g, w);
        }
        gConfigCache.put(id.getId(), g);
    }

    /**
     * Deserializes the configuration for a specific guild from JSON.
     *
     * @param id Guild to load the settings for.
     * @return The GuildConfig data directly from JSON.
     */
    public static GuildConfig loadGuildConfig(Guild id) {
        try {
            File target = SettingsManager.getConfigurationFile(id);

            if (!target.exists()) {
                GuildConfig g = new GuildConfig(Objects.requireNonNull(getGlobalConfig()));
                saveGuildConfig(id, g);
                return g;
            }

            GuildConfig g;
            try (FileReader reader = new FileReader(target)) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(GuildConfig.class, GuildConfig.instanceCreator(getGlobalConfig()))
                        .create();
                g = gson.fromJson(reader, GuildConfig.class);
            }
            saveGuildConfig(id, g);

            return g;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* * * * *
     *
     * Utility Methods
     *
     * * * * */

    public static String getCommandPrefix() {
        return getGlobalConfig().getSendableCommandPrefix();
    }

    public static String getCommandPrefix(Guild g) {
        if (g == null) return getCommandPrefix();
        else return getGuildConfig(g).getSendableCommandPrefix(g);
    }

    public static Color getColorAWT() {
        return getGlobalConfig().getDefaultColorAWT();
    }

    public static Color getColorAWT(Guild g) {
        if (g == null) return getColorAWT();
        else return getGuildConfig(g).getColorAWT();
    }

}
