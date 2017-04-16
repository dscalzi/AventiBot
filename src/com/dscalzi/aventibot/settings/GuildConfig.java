package com.dscalzi.aventibot.settings;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;

import javafx.util.Pair;

public class GuildConfig {

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
	
	public String getCommandPrefix(){
		
		if(commandPrefix.equalsIgnoreCase("@MENTION") && AventiBot.getStatus() == BotStatus.CONNECTED)
			return AventiBot.getInstance().getJDA().getSelfUser().getAsMention();
		
		return this.commandPrefix;
	}
	
	public void setCommandPrefix(String commandPrefix){
		this.commandPrefix = commandPrefix;
	}
	
}
