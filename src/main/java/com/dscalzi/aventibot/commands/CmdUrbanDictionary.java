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

import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdUrbanDictionary implements CommandExecutor {

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
				if(root.has("result_type") && root.get("result_type").getAsString().equalsIgnoreCase("exact")) {
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
							b.setAuthor(ath, "https://www.urbandictionary.com/author.php?author=" + athEncode, "https://i.imgur.com/quPH023.png");
							b.setTitle(def.get("word").getAsString(), def.get("permalink").getAsString());
							Field defField = new Field("Definition", def.get("definition").getAsString(), false);
							Field exField = new Field("Example", "*" + def.get("example").getAsString() + "*", false);
							b.addField(defField);
							b.addField(exField);
							b.setFooter("" + ((char)9650) + " " + def.get("thumbs_up").getAsInt() + " / " + ((char)9660) + " " + def.get("thumbs_down").getAsInt(), null);
							
							e.getChannel().sendMessage(b.build()).queue();;
							
							return CommandResult.SUCCESS;
						}
					}
				} else {
					e.getChannel().sendMessage("No definitions found for *" + term + "*.").queue();
					return CommandResult.SUCCESS;
				}
			}
		}
		
		return CommandResult.ERROR;
	}

	@Override
	public Set<PermissionNode> provideNodes() {
		return nodes;
	}

}
