/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.ui;

import java.io.InputStream;

import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.AventiBot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.dv8tion.jda.core.utils.SimpleLog;

public class TerminalExecutor extends Application {

	public static void main(String[] args){
		launch(args);
	}
	
	private final SimpleLog LOG = SimpleLog.getLog("Launcher");
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		
		Scene root;
		
		try(InputStream fxmlStream = getClass().getResourceAsStream("TerminalFXML.fxml")){
			root = (Scene) loader.load(fxmlStream);
		}
		
		LOG.info("Starting terminal..");
		
		root.getStylesheets().add(getClass().getResource("styles/styles.css").toExternalForm());
		
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
		
		
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("styles/avatar.png")));
		primaryStage.setTitle("AventiBot Launcher");
		primaryStage.setScene(root);
		primaryStage.show();
		
		LOG.info("Done!");
	}

}
