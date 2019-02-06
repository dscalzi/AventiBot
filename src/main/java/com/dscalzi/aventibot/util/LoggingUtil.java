/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2019 Daniel D. Scalzi
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Work in progress
 *
 */
public class LoggingUtil {

	public static final Logger AVENTIBOT = LoggerFactory.getLogger("AventiBot");
	//public static final Logger JDA = LoggerFactory.getLogger("JDA");
	//public static final Logger CONSOLE = LoggerFactory.getLogger("Console");
	//public static final Logger LAUNCHER = LoggerFactory.getLogger("Launcher");
	//public static final Logger COMMANDISPATCHER = LoggerFactory.getLogger("CommandDispatcher");
	//public static final Logger PERMISSIONUTIL = LoggerFactory.getLogger("PermissionUtil");
	//public static final Logger ExtensionLoader = LoggerFactory.getLogger("ExtensionLoader");
	//Settings Manager
	
	public static final Marker FATAL = MarkerFactory.getMarker("FATAL");
	public static final Marker SEVERE = MarkerFactory.getMarker("SEVERE");
	
}
