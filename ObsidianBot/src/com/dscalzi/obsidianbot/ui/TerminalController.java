package com.dscalzi.obsidianbot.ui;

import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

import com.dscalzi.obsidianbot.BotStatus;
import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.console.CommandLine;
import com.dscalzi.obsidianbot.console.CommandLog;
import com.dscalzi.obsidianbot.music.LavaWrapper;

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
					ObsidianBot.getInstance().getJDA().addEventListener(new EventListener(){
						@Override
						public void onEvent(Event e){
							if(e instanceof ShutdownEvent){
								SimpleLog.getLog("Launcher").info("===================================");
								SimpleLog.getLog("Launcher").info("ObsidianBot JDA has been shutdown..");
								SimpleLog.getLog("Launcher").info("===================================");
								terminate_button.setDisable(true);
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
		ObsidianBot.getInstance().getJDA().shutdown(true);
		LavaWrapper.getInstance().getAudioPlayerManager().shutdown();
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
