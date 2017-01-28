package com.dscalzi.obsidianbot.ui;

import java.io.File;
import java.io.FileInputStream;

import com.dscalzi.obsidianbot.BotStatus;
import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.music.LavaWrapper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.dv8tion.jda.core.utils.SimpleLog;

public class TerminalExecutor extends Application{

	public static void main(String[] args){
		launch(args);
	}
	
	private final SimpleLog LOG = SimpleLog.getLog("Launcher");
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		File f = new File(getClass().getResource("TerminalFXML.fxml").getPath());
		
		Scene root;
		
		try(FileInputStream fxmlStream = new FileInputStream(f)){
			root = (Scene) loader.load(fxmlStream);
		}
		
		LOG.info("Starting terminal..");
		
		root.getStylesheets().add(getClass().getResource("styles/styles.css").toExternalForm());
		
		primaryStage.setOnCloseRequest(e -> {
			try {
				if(ObsidianBot.getStatus() == BotStatus.CONNECTED){
					ObsidianBot.getInstance().getJDA().shutdown(true);
					LavaWrapper.getInstance().getAudioPlayerManager().shutdown();
				}
			} catch (Exception ex){
				//Shutdown
				Runtime.getRuntime().exit(0);
			}
			Runtime.getRuntime().exit(0);
		});
		
		
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("styles/avatar.png")));
		primaryStage.setTitle("ObsidianBot Launcher");
		primaryStage.setScene(root);
		primaryStage.show();
		
		LOG.info("Done!");
	}

}
