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

package com.dscalzi.aventibot;

import info.debatty.java.stringsimilarity.JaroWinkler;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

public class DevTests {

    @Test
    public void testStringSimilarity() {
        JaroWinkler jw = new JaroWinkler();
        System.out.println(jw.similarity(sanitize("Prayer - Original Extended mix "), sanitize("Prayer (Extended mix)")));
        System.out.println(jw.similarity("Stop Teasing My Heart", "Stop Teasing my Soul"));
        System.out.println(jw.similarity("Stop Teasing My Heart", "YES I WILL"));
    }

    public String sanitize(String s) {
        Pattern x = Pattern.compile("[^a-zA-Z0-9 ]");
        Pattern spaces = Pattern.compile(" +");
        return spaces.matcher(x.matcher(s).replaceAll("")).replaceAll(" ");
    }

    @Test
    public void colorTest() {
        System.out.println(toRGBCode(Color.BLACK));
        System.out.println(toRGBCode(Color.BLUE));
    }

    public static String toRGBCode(Color color) {
        return String.format("#%02x%02x%02x",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255));
    }

}
