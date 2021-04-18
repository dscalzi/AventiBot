/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2021 Daniel D. Scalzi
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

package com.dscalzi.aventibot.cmdutil;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CommandRegistry {

	private Map<String, CommandExecutor> registry;
	
	public CommandRegistry(){
		this.registry = new HashMap<>();
	}
	
	public boolean register(String cmd, CommandExecutor executor){
		cmd = cmd.toLowerCase();
		if(registry.containsKey(cmd)) return false;
		
		registry.put(cmd, executor);
		return true;
	}
	
	public Optional<CommandExecutor> getExecutor(String cmd){
		cmd = cmd.toLowerCase();
		return registry.containsKey(cmd) ? Optional.of(registry.get(cmd)) : Optional.empty();
	}
	
	public Set<PermissionNode> getAllRegisteredNodes(){
		Set<PermissionNode> a = new HashSet<>();
		for(CommandExecutor e : registry.values()){
			for(PermissionNode pn : e.provideNodes()) a.add(pn);
		}
		return a;
	}
	
	public boolean isCommandRegistered(String cmd){
		cmd = cmd.toLowerCase();
		return registry.containsKey(cmd);
	}
	
	public Set<String> getRegisteredCommands(){
		return Collections.unmodifiableSet(registry.keySet());
	}
}
