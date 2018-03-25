/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE for license information.
 */
package com.dscalzi.aventibot.ui;

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.AventiBot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class TerminalExecutor extends Application {

	protected static List<String> parameters;
	
	public static void main(String[] args){
		launch(args);
	}
	
	private final Logger LOG = LoggerFactory.getLogger("Launcher");
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		parameters = this.getParameters().getRaw();		
		FXMLLoader loader = new FXMLLoader();
		
		Scene root;
		
		try(InputStream fxmlStream = getClass().getResourceAsStream("/assets/fxml/TerminalFXML.fxml")){
			root = (Scene) loader.load(fxmlStream);
		}
		
		LOG.info("Starting terminal..");
		
		root.getStylesheets().add(getClass().getResource("/assets/styles/styles.css").toExternalForm());
		
		primaryStage.setOnCloseRequest(e -> {
			try {
				if(AventiBot.getStatus() == BotStatus.CONNECTED){
					AventiBot.getInstance().shutdown();
				}
			} catch (Exception ex){
				//Shutdown
				Runtime.getRuntime().exit(0);
			}
			Runtime.getRuntime().exit(0);
		});
		
		
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/assets/images/avatar.png")));
		primaryStage.setTitle("AventiBot Launcher");
		primaryStage.setScene(root);
		primaryStage.show();
		
		LOG.info("Done!");
	}

}
