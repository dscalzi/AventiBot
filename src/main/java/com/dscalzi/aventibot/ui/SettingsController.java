/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2018 Daniel D. Scalzi
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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.settings.GlobalConfig;
import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.OSUtil;

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

public class SettingsController implements Initializable, ChangeListener<String>{

	private static final Logger LOG = LoggerFactory.getLogger("Launcher");
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
			current = SettingsManager.getGlobalConfig();
			if(current == null){
				throw new IOException();
			}
		} catch (IOException e) {
			LOG.error(MarkerFactory.getMarker("FATAL"), "Unable to load global config. This error is FATAL, shutting down..");
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
			LOG.error(MarkerFactory.getMarker("FATAL"), "Failed to save global configuration settings..");
			e1.printStackTrace();
		}
	}
	
	@FXML
	private void handleShowFileButton(ActionEvent e){
		try {
			if(OSUtil.isWindows())
				Runtime.getRuntime().exec("explorer.exe /select," + SettingsManager.getGlobalConfigurationFile().getAbsolutePath());
			else if(OSUtil.isMac())
				Runtime.getRuntime().exec("open " + SettingsManager.getGlobalConfigurationFile().getAbsolutePath());
			else 
				LOG.error(MarkerFactory.getMarker("FATAL"), "Cannot open the configuration file location, unsupported OS. Path is " + SettingsManager.getGlobalConfigurationFile().getAbsolutePath());
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
