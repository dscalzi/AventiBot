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

package com.dscalzi.aventibot.cmdutil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dscalzi.aventibot.settings.SettingsManager;
import com.dscalzi.aventibot.util.IconUtil;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PermissionResult {

	//Successes
	private final Set<IMentionable> entities;
	private final Set<String> nodes;
	//Failed Nodes
	private final Set<String> failedNodes;
	//Invalids
	private final Set<String> invalidNodes;
	private final Set<String> invalidRoles;
	private final Set<String> invalidUsers;
	//Log
	private final List<String> log;
	private int pointer;
	
	private final Type type;
	private final Guild g;
	
	public PermissionResult(Type type, Guild g){
		this.type = type;
		this.g = g;
		this.entities = new HashSet<IMentionable>();
		this.nodes = new HashSet<String>();
		this.failedNodes = new HashSet<String>();
		this.invalidNodes = new HashSet<String>();
		this.invalidRoles = new HashSet<String>();
		this.invalidUsers = new HashSet<String>();
		this.log = new ArrayList<String>();
		log.add(0, "");
		this.pointer = 0;
	}
	
	public void addMentionable(IMentionable entity){
		entities.add(entity);
	}
	
	public void addNode(String node){
		nodes.add(node);
	}
	
	public void addFailedNode(String node){
		failedNodes.add(node);
	}
	
	public void addInvalidNode(String s){
		invalidNodes.add(s);
	}
	
	public void addInvalidRole(String s){
		invalidRoles.add(s);
	}
	
	public void addInvalidUser(String s){
		invalidUsers.add(s);
	}
	
	public void logResult(String s){
		if(log.get(pointer) == null || log.get(pointer).length()+s.length() > 1985){
			++pointer;
			log.add(pointer, "");
		}
		log.set(pointer, log.get(pointer) + s + "\n");
	}
	
	public MessageEmbed construct(){
		return construct(false);
	}
	
	public MessageEmbed construct(boolean withLogFooter){
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.setAuthor(type.getTitle(), null, type.getURL());
		eb.setColor(SettingsManager.getColorAWT(g));
		
		if(withLogFooter && hasLog()){
			eb.setFooter("Process returned output, pasting below.", IconUtil.INFO.getURL());
		}
		
		//Construct pieces
		if(entities.size() > 0){
			String allEntities = "";
			for(IMentionable m : entities) allEntities += m.getAsMention() + " ";
			eb.setDescription(allEntities.trim());
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
		if(invalidUsers.size() > 0){
			eb.addField("Invalid User" + (invalidRoles.size() > 1 ? "s" : ""), invalidUsers.toString(), true);
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
				"Processed Nodes", 
				"Permissions Not Enabled", 
				IconUtil.ADD.getURL()),
		REVOKE("Permission Revoke Results", 
				"Processed Nodes", 
				"Permissions Not Enabled", 
				IconUtil.REMOVE.getURL()),
		BLACKLIST("Blacklist Results", 
				"Processed Nodes", 
				"", 
				IconUtil.ADD.getURL()),
		UNBLACKLIST("Unblacklist Results", 
				"Processed Nodes", 
				"", 
				IconUtil.REMOVE.getURL()),
		NODE_ENABLE("Node Enable Results", 
				"Nodes Enabled", 
				"Already Enabled", 
				IconUtil.ADD.getURL()),
		NODE_DISABLE("Node Disable Results", 
				"Nodes Disabled", 
				"Already Disabled", 
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
