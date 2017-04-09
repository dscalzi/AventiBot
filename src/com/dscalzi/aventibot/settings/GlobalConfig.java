package com.dscalzi.aventibot.settings;

import java.awt.Color;

public class GlobalConfig {
	
	private String apiKey;
	private String currentGame;
	private transient Color botColor;
	private String botColorHex;
	
	public GlobalConfig(){
		
	}
	
	public GlobalConfig(String apiKey, String currentGame, String botColor) {
		this.apiKey = apiKey;
		this.currentGame = currentGame;
		setBotColor(botColor);
	}
	
	public String getAPIKey(){
		return apiKey;
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
