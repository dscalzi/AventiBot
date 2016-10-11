package com.dscalzi.obsidianbot.cmdutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class CommandRegistry {

	private Map<String, CommandExecutor> registry;
	
	public CommandRegistry(){
		this.registry = new HashMap<String, CommandExecutor>();
	}
	
	public boolean register(String cmd, CommandExecutor executor){
		if(registry.containsKey(cmd)) return false;
		
		registry.put(cmd, executor);
		return true;
	}
	
	public Optional<CommandExecutor> getExecutor(String cmd){
		if(registry.containsKey(cmd)){
			return Optional.of(registry.get(cmd));
		}
		return Optional.empty();
	}
	
	public boolean isCommandRegistered(String cmd){
		return registry.containsKey(cmd);
	}
	
	public List<String> getRegisteredCommands(){
		List<String> cmds = new ArrayList<String>();
		for(Entry<String, CommandExecutor> entry : registry.entrySet()){
			cmds.add(entry.getKey());
		}
		return cmds;
	}
}
