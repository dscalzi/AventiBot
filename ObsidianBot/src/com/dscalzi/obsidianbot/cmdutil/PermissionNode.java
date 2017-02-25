package com.dscalzi.obsidianbot.cmdutil;

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
	public boolean equals(Object o){
		if(this == o) return true;
		if(o == null) return false;
		if(o.getClass() != this.getClass()) return false;
		PermissionNode n = (PermissionNode)o;
		if(!n.toString().equals(this.toString())) return false;
		//Ignores op value.
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
