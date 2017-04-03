/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.ui;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.console.CommandLine;
import com.dscalzi.aventibot.console.CommandLog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.SimpleLog;

public class TerminalController implements Initializable {

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
								SimpleLog log = SimpleLog.getLog("Launcher");
								SimpleLog.getLog("Launcher").info("===================================");
								SimpleLog.getLog("Launcher").info("AventiBot JDA has been shutdown..");
								SimpleLog.getLog("Launcher").info("===================================");
								terminate_button.setDisable(true);
								log.info("Releasing log file - no more output will be logged.");
								try {
									console.closeLogger();
								} catch (IOException e1) {
									log.fatal("Error while releasing log file:");
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
	
}
