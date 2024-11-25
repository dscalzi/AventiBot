/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2023 Daniel D. Scalzi
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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class SettingsController implements Initializable, ChangeListener<String> {

    private GlobalConfig current;
    private SettingsState state;

    @FXML
    private Label status_text;
    @FXML
    private Button save_button;

    @FXML
    private TextField apikey_settings_field;
    @FXML
    private TextField currentgame_settings_field;
    @FXML
    private ColorPicker color_settings_picker;
    @FXML
    private TextField commandprefix_settings_field;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            current = SettingsManager.getGlobalConfig();
            if (current == null) {
                throw new IOException();
            }
        } catch (IOException e) {
            log.error(MarkerFactory.getMarker("FATAL"), "Unable to load global config. This error is FATAL, shutting down..");
            e.printStackTrace();
            if (AventiBot.getStatus() == BotStatus.CONNECTED)
                AventiBot.getInstance().shutdown();
            else
                TerminalController.markSoftShutdown = true;
        }
        bindTextFields();
        setState(SettingsState.SAVED);
    }

    private void bindTextFields() {
        if (AventiBot.getStatus() == BotStatus.CONNECTED) {
            apikey_settings_field.setDisable(true);
        }
        //Bind token
        apikey_settings_field.setText(current.getToken());
        apikey_settings_field.textProperty().addListener(this);
        //Bind current game
        currentgame_settings_field.setText(current.getCurrentGame());
        currentgame_settings_field.textProperty().addListener(this);
        //Bind command prefix
        commandprefix_settings_field.setText(current.getDefaultCommandPrefix());
        commandprefix_settings_field.textProperty().addListener(this);
        //Bind color
        //color_settings_picker.setValue(Color.web("#0f579d"));
        color_settings_picker.setValue(Color.web(current.getDefaultColorHex()));
        color_settings_picker.valueProperty().addListener((o, oV, nV) -> {
            if (!nV.equals(current.getDefaultColorJFX()))
                setState(SettingsState.NOT_SAVED);
            else
                setState(SettingsState.SAVED);
        });
    }

    @FXML
    private void handleSaveButton(ActionEvent e) {
        GlobalConfig g = new GlobalConfig(apikey_settings_field.getText(),
                currentgame_settings_field.getText(),
                toRGBCode(color_settings_picker.getValue()),
                commandprefix_settings_field.getText(),
                current.getSpotifyConfig(),
                current.getYoutubeConfig());
        try {
            AventiBot.setCurrentGame(currentgame_settings_field.getText());
            SettingsManager.saveGlobalConfig(g);
            current = g;
            setState(SettingsState.SAVED);
        } catch (IOException e1) {
            log.error(MarkerFactory.getMarker("FATAL"), "Failed to save global configuration settings..");
            e1.printStackTrace();
        }
    }

    @FXML
    private void handleShowFileButton(ActionEvent e) {
        try {
            String pth = SettingsManager.getGlobalConfigurationFile().getAbsolutePath();
            if (OSUtil.isWindows())
                Runtime.getRuntime().exec("explorer.exe /select," + pth);
            else if (OSUtil.isMac())
                Runtime.getRuntime().exec("open " + pth);
            else
                log.error(MarkerFactory.getMarker("FATAL"), "Cannot open the configuration file location, unsupported OS. Path is " + pth);
        } catch (IOException e1) {
            log.warn("Error while opening file explorer:");
            e1.printStackTrace();
        }
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        TextField source = (TextField) ((StringProperty) observable).getBean();
        if ((source == apikey_settings_field && !newValue.equals(current.getToken())) ||
                (source == currentgame_settings_field && !newValue.equals(current.getCurrentGame())))
            setState(SettingsState.NOT_SAVED);
        else
            setState(SettingsState.SAVED);
    }

    private void setState(SettingsState state) {
        switch (state) {
            case SAVED:
                status_text.setText("Status: Saved");
                break;
            case NOT_SAVED:
                status_text.setText("Status: Not Saved!");
                break;
        }
        this.state = state;
    }

    public SettingsState getState() {
        return this.state;
    }

    public Button getSaveButton() {
        return this.save_button;
    }

    public enum SettingsState {

        SAVED(),
        NOT_SAVED()

    }

    public static String toRGBCode(Color color) {
        return String.format("#%02x%02x%02x",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255));
    }

}
