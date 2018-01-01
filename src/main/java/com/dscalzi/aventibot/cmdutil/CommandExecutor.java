/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.cmdutil;

import java.util.Set;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Interface representing a CommandExecutor object.
 * 
 * @author Daniel D. Scalzi
 */
public interface CommandExecutor {
	
	/**
	 * Called when the command is received.
	 * 
	 * @param e The MessageRecievedEvent that triggered the command.
	 * @param cmd The command string which was typed.
	 * @param args The message contents split into arguments.
	 * @param rawArgs The <strong>raw</strong> message contents split into arguments.
	 * @return The result of the command operation.
	 */
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs);
	
	/**
	 * This method should provide a Set of PermissionNode objects, which are the
	 * permissions used by this command executor. This is essentially registering
	 * those nodes. This method must not return null.
	 * 
	 * @return Never null Set of PermissionNode objects used by this command executor.
	 */
	public Set<PermissionNode> provideNodes();
	
}
