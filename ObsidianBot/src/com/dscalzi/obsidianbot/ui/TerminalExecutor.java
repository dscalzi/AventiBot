package com.dscalzi.obsidianbot.ui;

import java.io.InputStream;
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
		
		Scene root;
		
		try(InputStream fxmlStream = getClass().getResourceAsStream("TerminalFXML.fxml")){
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
