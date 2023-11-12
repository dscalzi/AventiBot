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
import com.dscalzi.aventibot.util.IconUtil;
import com.dscalzi.aventibot.util.JDAUtils;
import com.google.gson.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmdUrbanDictionary implements CommandExecutor {

    private static final Pattern SUB_DEF_REG = Pattern.compile("\\[(.+?)]");

    private final PermissionNode permUrbanDictionary = PermissionNode.get(NodeType.COMMAND, "urbandictionary");

    public final Set<PermissionNode> nodes;

    public CmdUrbanDictionary() {
        nodes = new HashSet<>(Collections.singletonList(
                permUrbanDictionary
        ));
    }

    @Override
    public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {

        if (!PermissionUtil.hasPermission(e.getAuthor(), permUrbanDictionary, JDAUtils.getGuildFromCombinedEvent(e), true))
            return CommandResult.NO_PERMISSION;

        String term = String.join(" ", args);
        URL url;
        try {
            String strURL = "https://api.urbandictionary.com/v0/define?term=" + URLEncoder.encode(term, StandardCharsets.UTF_8);
            url = new URL(strURL);
        } catch (MalformedURLException ex) {
            e.getChannel().sendMessage("Sorry, I couldn't look that up! There was an error while encoding it.").queue();
            return CommandResult.ERROR;
        }

        String json;

        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int status = con.getResponseCode();
            if (status != 200) {
                e.getChannel().sendMessage("Sorry, I couldn't look that up! There was an error sending the request.").queue();
                return CommandResult.ERROR;
            }
            try (InputStreamReader isr = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8);
                 BufferedReader in = new BufferedReader(isr)) {
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();
                json = content.toString();
            }
        } catch (IOException e1) {
            e.getChannel().sendMessage("Sorry, I couldn't look that up! There was an error sending the request.").queue();
            e1.printStackTrace();
            return CommandResult.ERROR;
        }

        JsonDeserializer<ZonedDateTime> d =
                (data, type, context) -> DateTimeFormatter.ISO_DATE_TIME.parse(data.getAsString(), ZonedDateTime::from);

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class, d)
                .create();

        ApiResult result;

        try {
            result = gson.fromJson(json, ApiResult.class);
        } catch(Throwable t) {
            e.getChannel().sendMessage("Failed to process request from Urban Dictionary!").queue();
            t.printStackTrace();
            return CommandResult.ERROR;
        }

        List<UrbanDefinition> res = result.list();
        if (!res.isEmpty()) {
            UrbanDefinition def = res.get(0);
            EmbedBuilder b = new EmbedBuilder();
            String athEncode = URLEncoder.encode(def.author(), StandardCharsets.UTF_8);
            b.setColor(Color.decode("#1d2439"));
            b.setAuthor(def.author, "https://www.urbandictionary.com/author.php?author=" + athEncode, IconUtil.URBAN_DICTIONARY.getURL());
            b.setTitle(def.word(), def.permalink());
            b.setTimestamp(def.written_on());

            String defString = linkSubTerms(def.definition());
            String exampleString = linkSubTerms(def.example());

            Field defField = new Field("Definition", defString.length() > MessageEmbed.VALUE_MAX_LENGTH ? defString.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 1) + '\u2026' : defString, false);
            Field exField = new Field("Example", "*" + (exampleString.length() > MessageEmbed.VALUE_MAX_LENGTH - 2 ? exampleString.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 3) + '\u2026' : exampleString) + "*", false);
            b.addField(defField);
            b.addField(exField);
            b.setFooter(((char) 9650) + " " + def.thumbs_up() + " / " + ((char) 9660) + " " + def.thumbs_down(), null);

            e.getChannel().sendMessageEmbeds(b.build()).queue();

            return CommandResult.SUCCESS;
        } else {
            e.getChannel().sendMessage("No definitions found for *" + term + "*.").queue();
            return CommandResult.SUCCESS;
        }
    }

    record ApiResult(
            List<UrbanDefinition> list
    ) { }

    record UrbanDefinition(
            String definition,
            String permalink,
            int thumbs_up,
            int thumbs_down,
            String author,
            String word,
            long defid,
            ZonedDateTime written_on,
            String example
    ) { }

    private String linkSubTerms(String s) {
        final Matcher m = SUB_DEF_REG.matcher(s);
        final StringBuilder b = new StringBuilder();
        while (m.find()) {
            String t = URLEncoder.encode(m.group(1), StandardCharsets.UTF_8);
            m.appendReplacement(b, "[" + m.group(1) + "](https://www.urbandictionary.com/define.php?term=" + t + ")");
        }
        m.appendTail(b);
        return b.toString();
    }

    @Override
    public Set<PermissionNode> provideNodes() {
        return nodes;
    }

}
