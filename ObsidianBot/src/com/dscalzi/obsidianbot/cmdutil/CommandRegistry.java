package com.dscalzi.obsidianbot.cmdutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
		return registry.containsKey(cmd) ? Optional.of(registry.get(cmd)) : Optional.empty();
	}
	
	public List<String> getAllRegisteredNodes(){
		Set<Class<? extends CommandExecutor>> clzz = new HashSet<Class<? extends CommandExecutor>>();
		List<String> a = new ArrayList<String>();
		for(CommandExecutor e : registry.values()){
			if(!clzz.contains(e.getClass())){
				clzz.add(e.getClass());
				for(String s : e.getNodes())
					a.add(s);
			}
		}
		return a;
	}
	
	public boolean isCommandRegistered(String cmd){
		return registry.containsKey(cmd);
	}
	
	public Set<String> getRegisteredCommands(){
		return Collections.unmodifiableSet(registry.keySet());
	}
}
