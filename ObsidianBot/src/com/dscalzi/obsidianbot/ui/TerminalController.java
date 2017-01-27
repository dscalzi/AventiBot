package com.dscalzi.obsidianbot.ui;

import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

import com.dscalzi.obsidianbot.BotStatus;
import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.console.CommandLine;
import com.dscalzi.obsidianbot.console.CommandLog;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class TerminalController implements Initializable {

	@FXML private Button launch_button;
	@FXML private Button terminate_button;
	
	@FXML private TextArea console_log;
	@FXML private TextField commandline;
	
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
				if(ObsidianBot.getStatus() == BotStatus.NULL){
					ObsidianBot.launch();
					success = ObsidianBot.getStatus() == BotStatus.CONNECTED;
				}else{
					success = ObsidianBot.getInstance().connect();
				}
				if(success){
					launch_button.setVisible(false);
					launch_button.setManaged(false);
				}
				else
					launch_button.setDisable(false);
			}
		});
	}
	
	@FXML
	private void handleTerminateButton(ActionEvent e){
		ObsidianBot.getInstance().getJDA().shutdown(true);
		terminate_button.setDisable(true);
	}
	
	private void setupTerminal(){
		@SuppressWarnings("unused")
		CommandLine cl = new CommandLine(commandline);
		CommandLog console = new CommandLog(console_log);
		PrintStream ps = new PrintStream(console, true);
		System.setOut(ps);
		System.setErr(ps);
	}

}
