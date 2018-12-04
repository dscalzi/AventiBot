/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2018 Daniel D. Scalzi
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

import java.util.Arrays;
import java.util.List;

import com.dscalzi.aventibot.cmdline.CommandLineExecutor;
import com.dscalzi.aventibot.ui.TerminalExecutor;

public class LaunchWrapper {

	public static void main(String[] args) {
		List<String> lstArgs = Arrays.asList(args);
		if(lstArgs.contains("--cmdline")){
			CommandLineExecutor.main(args);
		} else {
			TerminalExecutor.main(args);
		}
	}

}
