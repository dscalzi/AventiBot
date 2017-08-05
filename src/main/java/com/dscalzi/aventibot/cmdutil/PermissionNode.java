/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
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
			if (other.nodeText != null) return false;
		} else if (!nodeText.equals(other.nodeText)) return false;
		return true;
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
