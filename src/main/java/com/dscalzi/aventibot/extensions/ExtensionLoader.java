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

package com.dscalzi.aventibot.extensions;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExtensionLoader {

    public static File getBaseExtensionsDirectory() {
        File f = new File("extensions");
        if (!f.exists()) {
            if (f.mkdirs()) return f;
            else {
                log.error(MarkerFactory.getMarker("FATAL"), "Unable to create settings directory!");
                return null;
            }
        }
        return f;
    }

    public static List<File> getCandidates() {
        List<File> candidates = new ArrayList<>();
        File fB = getBaseExtensionsDirectory();
        if (fB != null && fB.isDirectory()) {
            File[] allFiles = fB.listFiles();
            for (File f : allFiles) {
                if (!f.isDirectory() && f.getName().toLowerCase().endsWith(".jar")) {
                    candidates.add(f);
                }
            }
        }
        return candidates;
    }

}
