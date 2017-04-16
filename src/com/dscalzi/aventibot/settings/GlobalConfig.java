package com.dscalzi.aventibot.settings;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;

import javafx.util.Pair;

public class GlobalConfig {
	
	public static final Map<Pair<String, Object>, Method> keyMap;
	
	static {
		keyMap = new HashMap<Pair<String, Object>, Method>();
		try {
			keyMap.put(new Pair<String, Object>("token", "NULL"), GlobalConfig.class.getMethod("setToken", String.class));
			keyMap.put(new Pair<String, Object>("currentGame", "Developed by Dan"), GlobalConfig.class.getMethod("setCurrentGame", String.class));
			keyMap.put(new Pair<String, Object>("defaultColorHex", "#0f579d"), GlobalConfig.class.getMethod("setDefaultColor", String.class));
			keyMap.put(new Pair<String, Object>("defaultCommandPrefix", "--"), GlobalConfig.class.getMethod("setDefaultCommandPrefix", String.class));
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
	
	public GlobalConfig(){ /* For deserialization. */ }
	
	public GlobalConfig(String token, String currentGame, String defaultColorHex, String defaultCommandPrefix) {
		this.token = token;
		this.currentGame = currentGame;
		setDefaultColor(defaultColorHex);
		this.defaultCommandPrefix = defaultCommandPrefix;
	}
	
	public String getToken(){
		return token;
	}
	
	public void setToken(String token){
		this.token = token;
	}
	
	public String getCurrentGame(){
		return currentGame;
	}
	
	public void setCurrentGame(String currentGame){
		this.currentGame = currentGame;
	}
	
	public Color getDefaultColorAWT(){
		if(defaultColorAWT == null) setDefaultColor(getDefaultColorHex());
		return defaultColorAWT;
	}
	
	public javafx.scene.paint.Color getDefaultColorJFX(){
		if(defaultColorJFX == null) setDefaultColor(getDefaultColorHex());
		return defaultColorJFX;
	}
	
	public String getDefaultColorHex(){
		return defaultColorHex;
	}
	
	public void setDefaultColor(String defaultColor){
		try {
			defaultColorHex = defaultColor;
			this.defaultColorAWT = Color.decode(defaultColorHex);
			this.defaultColorJFX = javafx.scene.paint.Color.web(defaultColor);
		} catch (IllegalArgumentException | NullPointerException e){
			//Assign default
			defaultColorHex = "#0f579d";
			this.defaultColorAWT = Color.decode(defaultColorHex);
		}
	}
	
	public String getCommandPrefix(){
		if(defaultCommandPrefix.equalsIgnoreCase("@MENTION") && AventiBot.getStatus() == BotStatus.CONNECTED)
			return AventiBot.getInstance().getJDA().getSelfUser().getAsMention();
		
		return this.defaultCommandPrefix;
	}
	
	public void setDefaultCommandPrefix(String defaultCommandPrefix){
		this.defaultCommandPrefix = defaultCommandPrefix;
	}
	
}
