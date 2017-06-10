package com.dscalzi.aventibot.cmdutil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.IconUtil;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;

public class PermissionResult {

	//Successes
	private final Set<Role> roles;
	private final Set<String> nodes;
	//Failed Nodes
	private final Set<String> failedNodes;
	//Invalids
	private final Set<String> invalidNodes;
	private final Set<String> invalidRoles;
	//Log
	private final List<String> log;
	private int pointer;
	
	private final Type type;
	private final Guild g;
	
	public PermissionResult(Type type, Guild g){
		this.type = type;
		this.g = g;
		this.roles = new HashSet<Role>();
		this.nodes = new HashSet<String>();
		this.failedNodes = new HashSet<String>();
		this.invalidNodes = new HashSet<String>();
		this.invalidRoles = new HashSet<String>();
		this.log = new ArrayList<String>();
		log.add(0, "");
		this.pointer = 0;
	}
	
	public void addRole(Role role){
		roles.add(role);
	}
	
	public void addNode(String node){
		nodes.add(node);
	}
	
	public void addFailedNode(String node){
		failedNodes.add(node);
	}
	
	public void addInvalidNodes(String s){
		invalidNodes.add(s);
	}
	
	public void addInvalidRole(String s){
		invalidRoles.add(s);
	}
	
	public void logResult(String s){
		if(log.get(pointer) == null || log.get(pointer).length()+s.length() > 1985){
			++pointer;
			log.add(pointer, "");
		}
		log.set(pointer, log.get(pointer) + s + "\n");
	}
	
	public MessageEmbed construct(){
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setAuthor(type.getTitle(), null, type.getURL());
		eb.setColor(SettingsManager.getColorAWT(g));
		
		//Construct pieces
		if(roles.size() > 0){
			String allRoles = "";
			for(Role r : roles) allRoles += r.getAsMention() + " ";
			eb.setDescription(allRoles.trim());
		}
		if(nodes.size() > 0){
			List<String> nCopy = new ArrayList<String>(nodes);
			nCopy.replaceAll(s -> "`" + s + "`");
			eb.addField(type.getSuccessMsg(), nCopy.toString(), true);
		}
		if(failedNodes.size() > 0){
			List<String> nCopy = new ArrayList<String>(failedNodes);
			nCopy.replaceAll(s -> "`" + s + "`");
			eb.addField(type.getFailedMsg(), nCopy.toString(), true);
		}
		if(invalidNodes.size() > 0){
			eb.addField("Invalid Node" + (invalidNodes.size() > 1 ? "s" : ""), invalidNodes.toString(), true);
		}
		if(invalidRoles.size() > 0){
			eb.addField("Invalid Role" + (invalidRoles.size() > 1 ? "s" : ""), invalidRoles.toString(), true);
		}
		
		return eb.build();
	}
	
	public boolean hasLog(){
		return log.size() >= 1 && log.get(pointer) != null && !log.get(pointer).isEmpty();
	}
	
	public List<String> constructLog(){
		List<String> lCopy = new ArrayList<String>(log);
		lCopy.replaceAll(s -> "```scheme\n" + s + "```");
		return lCopy;
	}
	
	public enum Type {
		GRANT("Permission Grant Results", 
				"Successfully Granted", 
				"Permissions Not Enabled", 
				IconUtil.ADD.getURL()),
		REVOKE("Permission Revoke Results", 
				"Successfully Revoked", 
				"Permissions Not Enabled", 
				IconUtil.REMOVE.getURL());
		
		private final String title;
		private final String success;
		private final String failed;
		private final String URL;
		
		Type(String title, String success, String failed, String URL){
			this.title = title;
			this.success = success;
			this.failed = failed;
			this.URL = URL;
		}
		
		public String getTitle(){
			return this.title;
		}
		
		public String getSuccessMsg(){
			return this.success;
		}
		
		public String getFailedMsg(){
			return this.failed;
		}
		
		public String getURL(){
			return this.URL;
		}
	}
}
