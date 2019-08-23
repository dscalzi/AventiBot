/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2019 Daniel D. Scalzi
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

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.util.Pair;

import net.dv8tion.jda.api.entities.Guild;

public class GuildConfig{

	public static final  Map<Pair<String, Object>, Method> keyMap;
	
	static {
		keyMap = new HashMap<Pair<String, Object>, Method>();
		try {
			keyMap.put(new Pair<String, Object>("colorHex", "#0f579d"), GuildConfig.class.getMethod("setColor", String.class));
			keyMap.put(new Pair<String, Object>("commandPrefix", "--"), GuildConfig.class.getMethod("setCommandPrefix", String.class));
		} catch (NoSuchMethodException | SecurityException e) {
			//Shouldn't happen since this is hard coded.
			e.printStackTrace();
		}
	}
	
	private transient Color colorAWT;
	private transient javafx.scene.paint.Color colorJFX;
	private String colorHex;
	private String commandPrefix;
	
	public GuildConfig(){
		
	}
	
	public GuildConfig(String defaultColor){
		setColor(colorHex);
	}
	
	public Color getColorAWT(){
		if(colorAWT == null) setColor(getColorHex());
		return colorAWT;
	}
	
	public javafx.scene.paint.Color getColorJFX(){
		if(colorJFX == null) setColor(getColorHex());
		return colorJFX;
	}
	
	public String getColorHex(){
		return colorHex;
	}
	
	public void setColor(String defaultColor){
		try {
			colorHex = defaultColor;
			this.colorAWT = Color.decode(colorHex);
			this.colorJFX = javafx.scene.paint.Color.web(colorHex);
		} catch (IllegalArgumentException | NullPointerException e){
			//Assign default
			colorHex = "#0f579d";
			this.colorAWT = Color.decode(colorHex);
		}
	}
	
	public String getCommandPrefix(Guild g){
		
		if(commandPrefix.equalsIgnoreCase("@MENTION") && AventiBot.getStatus() == BotStatus.CONNECTED)
			return g.getMember(AventiBot.getInstance().getJDA().getSelfUser()).getAsMention() + " ";
		
		return this.commandPrefix;
	}
	
	/**
	 * Returns raw command prefix specified in the configuration
	 * without any modifications. 
	 */
	public String getRawCommandPrefix(){
		return this.commandPrefix;
	}
	
	public void setCommandPrefix(String commandPrefix){
		this.commandPrefix = commandPrefix;
	}
	
}
