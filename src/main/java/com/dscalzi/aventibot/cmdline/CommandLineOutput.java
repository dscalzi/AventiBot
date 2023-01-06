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

package com.dscalzi.aventibot.cmdline;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MarkerFactory;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
public class CommandLineOutput extends OutputStream {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss").withLocale(Locale.US).withZone(ZoneId.systemDefault());

    private volatile boolean fileStreamClosed;

    private PrintStream oldOut;
    private FileOutputStream file;
    private File logFile;

    public CommandLineOutput() {
        this.logFile = new File("logs", "AventiBot-" + formatter.format(Instant.now()) + ".log");
        if (!logFile.exists()) {
            try {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            } catch (IOException e) {
                log.error(MarkerFactory.getMarker("FATAL"), "Could not create logging file.");
                e.printStackTrace();
            }
        }
        try {
            this.oldOut = System.out;
            this.file = new FileOutputStream(logFile);
            fileStreamClosed = false;
        } catch (FileNotFoundException e) {
            fileStreamClosed = true;
            log.error(MarkerFactory.getMarker("FATAL"), "Could not bind to logging file.");
            e.printStackTrace();
        }
    }

    public void closeLogger() throws IOException {
        file.close();
        fileStreamClosed = true;
    }

    @Override
    public void write(int i) throws IOException {
        if (!fileStreamClosed) {
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
