/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE for license information.
 */
package com.dscalzi.aventibot.cmdline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

public class CommandLineOutput extends OutputStream {

	public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss").withLocale(Locale.US).withZone(ZoneId.systemDefault());

	private volatile boolean fileStreamClosed;
	
	private PrintStream oldOut;
	private FileOutputStream file;
	private File logFile;
	
	public CommandLineOutput(){
		this.logFile = new File("logs", "AventiBot-" + formatter.format(Instant.now()) + ".log");
		if(!logFile.exists()){ 
			try {
				logFile.getParentFile().mkdirs();
				logFile.createNewFile();
			} catch (IOException e) {
				LoggerFactory.getLogger("AventiBot").error(MarkerFactory.getMarker("FATAL"), "Could not create logging file.");
				e.printStackTrace();
			}
		}
		try {
			this.oldOut = System.out;
			this.file = new FileOutputStream(logFile);
			fileStreamClosed = false;
		} catch (FileNotFoundException e) {
			fileStreamClosed = true;
			LoggerFactory.getLogger("AventiBot").error(MarkerFactory.getMarker("FATAL"), "Could not bind to logging file.");
			e.printStackTrace();
		}
	}
	
	public void closeLogger() throws IOException{
		file.close();
		fileStreamClosed = true;
	}
	
	@Override
	public void write(int i) throws IOException {
		if(!fileStreamClosed){
			oldOut.write(i);
			file.write(i);
		}
	}
	
	@Override
    public void close() throws IOException {
        super.close();
        oldOut.close();
		file.close();
    }
	
	@Override
    public void flush() throws IOException {
        super.flush();
        oldOut.flush();
		file.flush();
    }
}
