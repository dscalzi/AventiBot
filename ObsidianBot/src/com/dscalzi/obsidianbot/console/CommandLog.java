package com.dscalzi.obsidianbot.console;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.dscalzi.obsidianbot.ObsidianBot;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import net.dv8tion.jda.core.utils.SimpleLog;

public class CommandLog extends OutputStream {
	
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss").withLocale(Locale.US).withZone(ZoneId.systemDefault());

	private volatile boolean fileStreamClosed;
	
	private TextArea node;
	private FileOutputStream file;
	private File logFile;
	
	public CommandLog(TextArea textArea){
		this.node = textArea;
		this.logFile = new File(ObsidianBot.getDataPath() + File.separator + "logs", "ObsidianBot-" + formatter.format(Instant.now()) + ".log");
		if(!logFile.exists()){ 
			try {
				logFile.getParentFile().mkdirs();
				logFile.createNewFile();
			} catch (IOException e) {
				SimpleLog.getLog("ObsidianBot").fatal("Could not create logging file.");
				e.printStackTrace();
			}
		}
		try {
			this.file = new FileOutputStream(logFile);
			fileStreamClosed = false;
		} catch (FileNotFoundException e) {
			fileStreamClosed = true;
			SimpleLog.getLog("ObsidianBot").fatal("Could not bind to logging file.");
			e.printStackTrace();
		}
	}
	
	public void closeLogger() throws IOException{
		file.close();
		fileStreamClosed = true;
	}
	
	@Override
	public void write(int i) throws IOException {
		synchronized(this){
			Platform.runLater(() -> {
				node.appendText(String.valueOf((char) i));
			});
		}
		if(!fileStreamClosed)
			file.write(i);
	}
	
	@Override
    public void close() throws IOException {
        super.close();
		file.close();
    }
	
	@Override
    public void flush() throws IOException {
        super.flush();
		file.flush();
    }

}
