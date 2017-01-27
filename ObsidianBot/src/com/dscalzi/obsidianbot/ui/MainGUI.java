package com.dscalzi.obsidianbot.ui;

import java.io.PrintStream;

import com.dscalzi.obsidianbot.BotStatus;
import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.console.CommandLine;
import com.dscalzi.obsidianbot.console.CommandLog;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.dv8tion.jda.core.utils.SimpleLog;

/**
 * Use TerminalExecutor instead.
 *
 */
@Deprecated
public class MainGUI extends Application{

	public static void main(String[] args){
		launch();
	}
	
	private final SimpleLog LOG = SimpleLog.getLog("Launcher");
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		final ImageView iva = new ImageView();
		{
			iva.setImage(new Image(getClass().getResource("styles/avatar.png").toExternalForm()));
		}
		
		HBox mainLayout = new HBox(2);
		{
		
			//Left Aligned Pane
			VBox leftLayout = new VBox();
			{
				leftLayout.setId("leftpane-vbox");
				leftLayout.setAlignment(Pos.BOTTOM_CENTER);
			
				Button terminateButton = new Button();
				Button startButton = new Button();
				{
					startButton.setText("Launch ObsidianBot");
					startButton.setId("button-start");
					startButton.setOnAction(e -> {
							Platform.runLater(() -> {
								synchronized(this){
									startButton.setDisable(true);
									boolean success = false;
									if(ObsidianBot.getStatus() == BotStatus.NULL){
										ObsidianBot.launch();
										success = ObsidianBot.getStatus() == BotStatus.CONNECTED;
									}else{
										success = ObsidianBot.getInstance().connect();
									}
									if(success){
										startButton.setVisible(false);
										startButton.setManaged(false);
										terminateButton.setManaged(true);
										terminateButton.setVisible(true);
									}
									else
										startButton.setDisable(false);
								}
							});
						});
				}
				
				{
					terminateButton.setText("End Proccess");
					terminateButton.setId("button-terminate");
					terminateButton.setOnAction(e -> { 
							ObsidianBot.getInstance().getJDA().shutdown(true);
							terminateButton.setDisable(true);
						});
					terminateButton.setVisible(false);
					terminateButton.setManaged(false);
				}
				
				leftLayout.getChildren().addAll(/*iva, */startButton, terminateButton);
			}
			
			VBox rightLayout = new VBox();
			{
				rightLayout.setId("rightpane-vbox");
				
				TextArea ta = new TextArea();
				{
					ta.setId("console-textarea");
					ta.setWrapText(true);
					ta.setEditable(false);
				}
				
				TextField tf = new TextField();
				{
					tf.setId("textfield-commandline");
				}
				@SuppressWarnings("unused")
				CommandLine cl = new CommandLine(tf);
				
				rightLayout.getChildren().addAll(ta, tf);
				VBox.setVgrow(ta, Priority.ALWAYS);
				
				CommandLog console = new CommandLog(ta);
				{
					PrintStream ps = new PrintStream(console, true);
					System.setOut(ps);
					System.setErr(ps);
				}
				
				LOG.info("Starting launcher..");
			}
			
			mainLayout.setId("main-hbox");
			mainLayout.getChildren().addAll(leftLayout, rightLayout);
			HBox.setHgrow(rightLayout, Priority.ALWAYS);
		}
		
		Scene scene = new Scene(mainLayout, 750, 350);
		scene.getStylesheets().add(getClass().getResource("styles/styles.css").toExternalForm());
		
		primaryStage.setOnCloseRequest(e -> {
			try {
				if(ObsidianBot.getStatus() == BotStatus.CONNECTED)
					ObsidianBot.getInstance().getJDA().shutdown(true);
			} catch (Exception ex){
				//Shutdown
				Runtime.getRuntime().exit(0);
			}
			Runtime.getRuntime().exit(0);
		});
		
		
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("styles/avatar.png")));
		primaryStage.setTitle("ObsidianBot Launcher");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		LOG.info("Done!");
	}

}
