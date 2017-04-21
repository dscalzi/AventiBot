package com.dscalzi.aventibot.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.settings.GlobalConfig;
import com.dscalzi.aventibot.settings.SettingsManager;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import net.dv8tion.jda.core.utils.SimpleLog;

public class SettingsController implements Initializable, ChangeListener<String>{

	private static final SimpleLog LOG = SimpleLog.getLog("Launcher");
	private GlobalConfig current;
	private SettingsState state;
	
	@FXML private Label status_text;
	@FXML private Button save_button;
	
	@FXML private TextField apikey_settings_field;
	@FXML private TextField currentgame_settings_field;
	@FXML private ColorPicker color_settings_picker;
	@FXML private TextField commandprefix_settings_field;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			current = SettingsManager.loadGlobalConfig();
			if(current == null){
				throw new IOException();
			}
		} catch (IOException e) {
			LOG.fatal("Unable to load global config. This error is FATAL, shutting down..");
			e.printStackTrace();
			if(AventiBot.getStatus() == BotStatus.CONNECTED)
				AventiBot.getInstance().shutdown();
			else 
				TerminalController.markSoftShutdown = true;
		}
		bindTextFields();
		setState(SettingsState.SAVED);
	}
	
	private void bindTextFields(){
		if(AventiBot.getStatus() == BotStatus.CONNECTED){
			apikey_settings_field.setDisable(true);
		}
		//Bind token
		apikey_settings_field.textProperty().addListener(this);
		apikey_settings_field.setText(current.getToken());
		//Bind current game
		currentgame_settings_field.textProperty().addListener(this);
		currentgame_settings_field.setText(current.getCurrentGame());
		//Bind command prefix
		commandprefix_settings_field.textProperty().addListener(this);
		commandprefix_settings_field.setText(current.getRawCommandPrefix());
		//Bind color
		//color_settings_picker.setValue(Color.web("#0f579d"));
		color_settings_picker.setValue(Color.web(current.getDefaultColorHex()));
		color_settings_picker.valueProperty().addListener((o, oV, nV) -> {
			if(!nV.equals(current.getDefaultColorJFX()))
				setState(SettingsState.NOT_SAVED);
			else
				setState(SettingsState.SAVED);
		});
	}

	@FXML
	private void handleSaveButton(ActionEvent e){
		GlobalConfig g = new GlobalConfig(apikey_settings_field.getText(),
				currentgame_settings_field.getText(),
				"#" + Integer.toHexString(color_settings_picker.getValue().hashCode()),
				commandprefix_settings_field.getText());
		try {
			AventiBot.setCurrentGame(currentgame_settings_field.getText());
			SettingsManager.saveGlobalConfig(g);
			current = g;
			setState(SettingsState.SAVED);
		} catch (IOException e1) {
			LOG.fatal("Failed to save global configuration settings..");
			e1.printStackTrace();
		}
	}
	
	@FXML
	private void handleShowFileButton(ActionEvent e){
		try {
			Runtime.getRuntime().exec("explorer.exe /select," + SettingsManager.getGlobalConfigurationFile().getAbsolutePath());
		} catch (IOException e1) {
			LOG.warn("Error while opening file explorer:");
			e1.printStackTrace();
		}
	}
	
	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		TextField source = (TextField) ((StringProperty) observable).getBean();
		if((source == apikey_settings_field && !newValue.equals(current.getToken())) ||
		   (source == currentgame_settings_field && !newValue.equals(current.getCurrentGame())))
			setState(SettingsState.NOT_SAVED);
		else
			setState(SettingsState.SAVED);
	}
	
	private void setState(SettingsState state){
		switch(state){
		case SAVED:
			status_text.setText("Status: Saved");
			break;
		case NOT_SAVED:
			status_text.setText("Status: Not Saved!");
			break;
		}
		this.state = state;
	}
	
	public SettingsState getState(){
		return this.state;
	}
	
	public Button getSaveButton(){
		return this.save_button;
	}
	
	public static enum SettingsState{
		
		SAVED(),
		NOT_SAVED();
		
	}

}
