/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE for license information.
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
