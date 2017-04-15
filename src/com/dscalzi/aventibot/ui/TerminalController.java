/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.console.CommandLine;
import com.dscalzi.aventibot.console.CommandLog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.SimpleLog;

public class TerminalController implements Initializable {

	protected static boolean markSoftShutdown;
	
	private static final SimpleLog LOG = SimpleLog.getLog("Launcher");
	
	@FXML private Button launch_button;
	@FXML private Button terminate_button;
	
	@FXML private TextArea console_log;
	@FXML private TextField commandline;
	
	private CommandLog console;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		this.setupTerminal();
	}
	
	@FXML
	private void handleSettingsButton(ActionEvent e){
		FXMLLoader loader = new FXMLLoader();
		Scene root;
		
		try(InputStream fxmlStream = getClass().getResourceAsStream("SettingsFXML.fxml")){
			root = (Scene) loader.load(fxmlStream);
			root.getStylesheets().add(getClass().getResource("styles/settings.css").toExternalForm());
		} catch (IOException e1) {
			LOG.fatal("Unable to open settings window..");
			e1.printStackTrace();
			if(markSoftShutdown){
				this.softShutdown();
			}
			return;
		}
		
		Stage stage = new Stage();
		
		stage.setOnCloseRequest(event -> {
			SettingsController c = loader.<SettingsController>getController();
			
			if(c.getState() == SettingsController.SettingsState.NOT_SAVED){
				Alert alert = new Alert(AlertType.WARNING);
	            alert.setTitle("Settings Not Saved");
	            alert.setHeaderText("Your changes have not been saved.");
	            alert.setContentText("You have not saved the changes made to the bot settings. Are you sure about this?");
	            
	            alert.initOwner(stage);
	            
	            ButtonType saveCloseButton = new ButtonType("Save and Close");
	            ButtonType closeButton = new ButtonType("Close Without Saving");
	            ButtonType cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

	            alert.getButtonTypes().setAll(saveCloseButton, closeButton, cancelButton);

	            Optional<ButtonType> result = alert.showAndWait();
	            if(result.get() == saveCloseButton){
	            	c.getSaveButton().fire();
	            } else if(result.get() == closeButton){
	                //do nothing
	            } else if(result.get() == cancelButton){
	            	event.consume();
	            }
			}
			
		});
		
		stage.getIcons().add(new Image(getClass().getResourceAsStream("styles/avatar.png")));
		stage.initOwner(launch_button.getScene().getWindow());
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle("AventiBot Settings");
		stage.setScene(root);
		stage.show();
	}
	
	@FXML
	private void handleLaunchButton(ActionEvent e){
		Platform.runLater(() -> {
			synchronized(this){
				launch_button.setDisable(true);
				boolean success = false;
				if(AventiBot.getStatus() == BotStatus.NULL){
					AventiBot.launch();
					success = AventiBot.getStatus() == BotStatus.CONNECTED;
				}else{
					success = AventiBot.getInstance().connect();
				}
				if(success){
					launch_button.setVisible(false);
					launch_button.setManaged(false);
					AventiBot.getInstance().getJDA().addEventListener(new EventListener(){
						@Override
						public void onEvent(Event e){
							if(e instanceof ShutdownEvent){
								LOG.info("===================================");
								LOG.info("AventiBot JDA has been shutdown..");
								LOG.info("===================================");
								terminate_button.setDisable(true);
								LOG.info("Releasing log file - no more output will be logged.");
								try {
									console.closeLogger();
								} catch (IOException e1) {
									LOG.fatal("Error while releasing log file:");
									e1.printStackTrace();
								}
							}
						}
					});
				}
				else
					launch_button.setDisable(false);
			}
		});
	}
	
	public void softShutdown(){
		launch_button.setDisable(true);
	}
	
	@FXML
	private void handleTerminateButton(ActionEvent e){
		AventiBot.getInstance().shutdown();
		terminate_button.setDisable(true);
	}
	
	private void setupTerminal(){
		@SuppressWarnings("unused")
		CommandLine cl = new CommandLine(commandline);
		console = new CommandLog(console_log);
		PrintStream ps = new PrintStream(console, true);
		System.setOut(ps);
		System.setErr(ps);
	}
	
	/**
     * Hack TooltipBehavior 
     */
    static {
        try {
            Tooltip obj = new Tooltip();
            Class<?> clazz = obj.getClass().getDeclaredClasses()[0];
            Constructor<?> constructor = clazz.getDeclaredConstructor(
                    Duration.class,
                    Duration.class,
                    Duration.class,
                    boolean.class);
            constructor.setAccessible(true);
            Object tooltipBehavior = constructor.newInstance(
                    new Duration(100),  //open
                    new Duration(30000), //visible
                    new Duration(200),  //close
                    false);
            Field fieldBehavior = obj.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            fieldBehavior.set(obj, tooltipBehavior);
        }
        catch (Exception e) {
        	SimpleLog.getLog("Launcher").warn("Could not change tooltip behavior, report to developer!");
        	e.printStackTrace();
        }
    }
	
}
