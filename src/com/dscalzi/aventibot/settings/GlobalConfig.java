package com.dscalzi.aventibot.settings;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javafx.util.Pair;

public class GlobalConfig {
	
	public static final Map<Pair<String, Object>, Method> keyMap;
	
	static {
		keyMap = new HashMap<Pair<String, Object>, Method>();
		try {
			keyMap.put(new Pair<String, Object>("token", "NULL"), GlobalConfig.class.getMethod("setToken", String.class));
			keyMap.put(new Pair<String, Object>("currentGame", "Developed by Dan"), GlobalConfig.class.getMethod("setCurrentGame", String.class));
			keyMap.put(new Pair<String, Object>("botColorHex", "#0f579d"), GlobalConfig.class.getMethod("setBotColor", String.class));
		} catch (NoSuchMethodException | SecurityException e) {
			//Shouldn't happen since this is hard coded.
			e.printStackTrace();
		}
	}
	
	private String token;
	private String currentGame;
	private transient Color botColor;
	private String botColorHex;
	
	public GlobalConfig(){
		//For deserialization.
	}
	
	public GlobalConfig(String token, String currentGame, String botColor) {
		this.token = token;
		this.currentGame = currentGame;
		setBotColor(botColor);
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
	
	public Color getBotColor(){
		if(botColor == null) setBotColor(getBotColorHex());
		return botColor;
	}
	
	public String getBotColorHex(){
		return botColorHex;
	}
	
	public void setBotColor(String botColor){
		try {
			botColorHex = botColor;
			this.botColor = Color.decode(botColorHex);
		} catch (NumberFormatException | NullPointerException e){
			//Assign default
			botColorHex = "#0f579d";
			this.botColor = Color.decode(botColorHex);
		}
	}
	
}
