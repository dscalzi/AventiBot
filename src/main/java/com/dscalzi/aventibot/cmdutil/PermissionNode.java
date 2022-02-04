/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2022 Daniel D. Scalzi
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

public class PermissionNode {

	private final String nodeText;
	private final boolean op;
	
	private PermissionNode(String nodeText){
		this(nodeText, false);
	}
	
	private PermissionNode(String nodeText, boolean op){
		this.nodeText = nodeText;
		this.op = op;
	}
	
	public boolean isOp(){
		return this.op;
	}
	
	@Override
	public String toString(){
		return this.nodeText;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeText == null) ? 0 : nodeText.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		PermissionNode other = (PermissionNode) obj;
		if (nodeText == null) {
			return other.nodeText == null;
		} else {
			return nodeText.equals(other.nodeText);
		}
	}
	
	/**
	 * <b>Only use for input, DOES NOT VALIDATE</b>
	 * 
	 * @input
	 * 
	 * @param s String representation of this PermissionNode.
	 * @return Permission node with the value of the given String.
	 */
	public static PermissionNode get(String s){
		if(s == null || s.trim().isEmpty())
			throw new IllegalArgumentException();
		
		return new PermissionNode(s.toLowerCase());
	}
	
	public static PermissionNode get(NodeType type, String topLevel, String... sub){
		return PermissionNode.get(type, topLevel, false, sub);
	}
	
	public static PermissionNode get(NodeType type, String topLevel, boolean op, String... sub){
		if(type == null || topLevel == null || topLevel.trim().isEmpty()) 
			throw new IllegalArgumentException();
		
		String builtNode = type.getWritable() + "." + topLevel.trim().toLowerCase();
		
		if(sub.length > 0){
			String joined = String.join(".", sub).toLowerCase();
			builtNode += "." + joined;
		}
		
		return new PermissionNode(builtNode, op);
	}
	
	public enum NodeType {
		COMMAND(),
		SUBCOMMAND(),
		ACCESS();
		
		public String getWritable(){
			return this.name().toLowerCase();
		}
	}
	
}
