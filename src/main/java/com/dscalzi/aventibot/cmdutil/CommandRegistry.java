/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE for license information.
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
		this.registry = new HashMap<String, CommandExecutor>();
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
		Set<PermissionNode> a = new HashSet<PermissionNode>();
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
