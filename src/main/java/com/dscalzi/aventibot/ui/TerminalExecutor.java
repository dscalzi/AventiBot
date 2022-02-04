/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2022 Daniel D. Scalzi
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

	private static final Logger log = LoggerFactory.getLogger(TerminalExecutor.class);

	protected static List<String> parameters;
	
	public static void main(String[] args){
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		parameters = this.getParameters().getRaw();		
		FXMLLoader loader = new FXMLLoader();
		
		Scene root;
		
		try(InputStream fxmlStream = getClass().getResourceAsStream("/assets/fxml/TerminalFXML.fxml")){
			root = loader.load(fxmlStream);
		}
		
		log.info("Starting terminal..");
		
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
		
		log.info("Done!");
	}

}
