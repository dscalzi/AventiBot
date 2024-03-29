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

package com.dscalzi.aventibot.util;

public enum IconUtil {

    INFO("http://i.imgur.com/ccX8Pvi.png"),
    CLOCK("http://i.imgur.com/Y3rbhFt.png"),
    PLAY("http://i.imgur.com/nEw5Gsk.png"),
    ADD("http://i.imgur.com/7OfFSFx.png"),
    REMOVE("http://i.imgur.com/voGutMQ.png"),
    VOTE("http://i.imgur.com/saXkgYz.png"),
    URBAN_DICTIONARY("https://i.imgur.com/quPH023.png");

    private final String URL;

    IconUtil(String URL) {
        this.URL = URL;
    }

    public String getURL() {
        return this.URL;
    }

}
