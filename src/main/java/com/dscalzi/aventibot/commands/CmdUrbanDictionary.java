/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE for license information.
 */
package com.dscalzi.aventibot.commands;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.util.IconUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdUrbanDictionary implements CommandExecutor {

    private static final Pattern SUB_DEF_REG = Pattern.compile("\\[(.+?)\\]");
    
	private final PermissionNode permUrbanDictionary = PermissionNode.get(NodeType.COMMAND, "urbandictionary");
	
	public final Set<PermissionNode> nodes;
	
	public CmdUrbanDictionary(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
				permUrbanDictionary
			));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		
		if(!PermissionUtil.hasPermission(e.getAuthor(), permUrbanDictionary, e.getGuild(), true)) return CommandResult.NO_PERMISSION;
		
		String term = String.join(" ", args);
		String strURL = "";
		URL url = null;
		try {
			strURL = "http://api.urbandictionary.com/v0/define?term=" + URLEncoder.encode(term, "UTF-8");
			url = new URL(strURL);
		} catch (UnsupportedEncodingException | MalformedURLException ex) {
			e.getChannel().sendMessage("Sorry, I couldn't look that up! There was an error while encoding it.").queue();
			return CommandResult.ERROR;
		}
		
		String json = null;
		
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int status = con.getResponseCode();
			if(status != 200) {
				e.getChannel().sendMessage("Sorry, I couldn't look that up! There was an error sending the request.").queue();
				return CommandResult.ERROR;
			}
			try(InputStreamReader isr = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8);
				BufferedReader in = new BufferedReader(isr);){
				String inputLine;
				StringBuffer content = new StringBuffer();
				while((inputLine = in.readLine()) != null) {
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
		
		if(json != null) {
			JsonParser p = new JsonParser();
			JsonElement parsed = p.parse(json);
			
			if(parsed.isJsonObject()) {
				JsonObject root = parsed.getAsJsonObject();
				if(root.has("list") && root.get("list").isJsonArray()) {
					JsonArray res = root.get("list").getAsJsonArray();
					if(res.size() > 0) {
						JsonObject def = res.get(0).getAsJsonObject();
						EmbedBuilder b = new EmbedBuilder();
						String ath = def.get("author").getAsString();
						String athEncode = "";
						try {
							athEncode = URLEncoder.encode(ath, "UTF-8");
						} catch (UnsupportedEncodingException ex) {
							// Why would this ever happen?
							athEncode = "whoops";
							ex.printStackTrace();
						}
						b.setColor(Color.decode("#1d2439"));
						b.setAuthor(ath, "https://www.urbandictionary.com/author.php?author=" + athEncode, IconUtil.URBAN_DICTIONARY.getURL());
						b.setTitle(def.get("word").getAsString(), def.get("permalink").getAsString());
						
						String defString = linkSubTerms(def.get("definition").getAsString());
						String exampleString = linkSubTerms(def.get("example").getAsString());
						
						Field defField = new Field("Definition", defString.length() > MessageEmbed.VALUE_MAX_LENGTH ? defString.substring(0, MessageEmbed.VALUE_MAX_LENGTH-1) + '\u2026' : defString , false);
						Field exField = new Field("Example", "*" + (exampleString.length() > MessageEmbed.VALUE_MAX_LENGTH-2 ? exampleString.substring(0, MessageEmbed.VALUE_MAX_LENGTH-3) + '\u2026' : exampleString) + "*", false);
						b.addField(defField);
						b.addField(exField);
						b.setFooter("" + ((char)9650) + " " + def.get("thumbs_up").getAsInt() + " / " + ((char)9660) + " " + def.get("thumbs_down").getAsInt(), null);
						
						e.getChannel().sendMessage(b.build()).queue();;
						
						return CommandResult.SUCCESS;
					} else {
					    e.getChannel().sendMessage("No definitions found for *" + term + "*.").queue();
	                    return CommandResult.SUCCESS;
					}
				}
			}
		}
		
		return CommandResult.ERROR;
	}
	
	private String linkSubTerms(String s) {
	    final Matcher m = SUB_DEF_REG.matcher(s);
	    final StringBuffer b = new StringBuffer();
        while(m.find()) {
            try {
                String t = URLEncoder.encode(m.group(1), "UTF-8");
                m.appendReplacement(b, "[" + m.group(1) + "](https://www.urbandictionary.com/define.php?term=" + t + ")");
            } catch (UnsupportedEncodingException e1) {
                // Won't happen?
                e1.printStackTrace();
            }
        }
        m.appendTail(b);
        return b.toString();
	}

	@Override
	public Set<PermissionNode> provideNodes() {
		return nodes;
	}

}
