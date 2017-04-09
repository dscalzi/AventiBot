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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import net.dv8tion.jda.core.utils.SimpleLog;

public class SettingsController implements Initializable, ChangeListener<String>{

	private static final int COLOR_MAX_LENGTH = 7;
	private static final SimpleLog LOG = SimpleLog.getLog("Launcher");
	private GlobalConfig current;
	
	@FXML private Label status_text;
	@FXML private Button save_button;
	
	@FXML private TextField apikey_settings_field;
	@FXML private TextField currentgame_settings_field;
	@FXML private TextField color_settings_field;
	
	@FXML private Rectangle color_settings_display;
	
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
	}
	
	private void bindTextFields(){
		if(AventiBot.getStatus() == BotStatus.CONNECTED){
			apikey_settings_field.setDisable(true);
		}
		apikey_settings_field.textProperty().addListener(this);
		apikey_settings_field.setText(current.getAPIKey());
		currentgame_settings_field.textProperty().addListener(this);
		currentgame_settings_field.setText(current.getCurrentGame());
		color_settings_field.textProperty().addListener(this);
		color_settings_field.lengthProperty().addListener((o, oV, nV) -> {
			if(nV.intValue() > oV.intValue() && nV.intValue() > COLOR_MAX_LENGTH)
				color_settings_field.setText(color_settings_field.getText(0, COLOR_MAX_LENGTH));
			if(color_settings_field.getText().length() == COLOR_MAX_LENGTH){
				try {
					Color p = Color.web(color_settings_field.getText());
					color_settings_display.setFill(p);
					color_settings_field.getStyleClass().remove("textfieldinvalid");
				} catch (IllegalArgumentException e){
					color_settings_field.getStyleClass().add(0, "textfieldinvalid");
				}
			}
		});
		color_settings_field.setText(current.getBotColorHex());
	}

	@FXML
	private void handleSaveButton(ActionEvent e){
		GlobalConfig g = new GlobalConfig(apikey_settings_field.getText(),
				currentgame_settings_field.getText(),
				color_settings_field.getText());
		try {
			SettingsManager.saveGlobalConfig(g);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		StringProperty textProperty = (StringProperty) observable ;
		TextField source = (TextField) textProperty.getBean();
		final String notsaved = "Status: Not Saved!";
		if(source == apikey_settings_field && !newValue.equals(current.getAPIKey())){
			status_text.setText(notsaved);
			return;
		}
		if(source == currentgame_settings_field && !newValue.equals(current.getCurrentGame())){
			status_text.setText(notsaved);
			return;
		}
		if(source == color_settings_field && !newValue.equals(current.getBotColorHex())){
			status_text.setText(notsaved);
			return;
		}
		
		status_text.setText("Status: Saved");
	}

}
