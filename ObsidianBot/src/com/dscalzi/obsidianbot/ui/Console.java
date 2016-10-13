package com.dscalzi.obsidianbot.ui;

import java.io.IOException;
import java.io.OutputStream;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class Console extends OutputStream {

	private TextArea node;
	
	public Console(TextArea textArea){
		this.node = textArea;
	}
	
	@Override
	public void write(int i) throws IOException {
		synchronized(this){
			Platform.runLater(() -> {
				node.appendText(String.valueOf((char) i));
			});
		}
		
	}

}
