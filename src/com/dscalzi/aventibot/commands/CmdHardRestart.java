package com.dscalzi.aventibot.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.cmdline.CommandLineExecutor;
import com.dscalzi.aventibot.cmdutil.CommandDispatcher;
import com.dscalzi.aventibot.cmdutil.CommandExecutor;
import com.dscalzi.aventibot.cmdutil.CommandResult;
import com.dscalzi.aventibot.cmdutil.PermissionNode;
import com.dscalzi.aventibot.cmdutil.PermissionNode.NodeType;
import com.dscalzi.aventibot.cmdutil.PermissionUtil;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CmdHardRestart implements CommandExecutor{

	private final PermissionNode permHardRestart = PermissionNode.get(NodeType.COMMAND, "hardrestart");
	
	public final Set<PermissionNode> nodes;
	
	public CmdHardRestart(){
		nodes = new HashSet<PermissionNode>(Arrays.asList(
				permHardRestart
			));
	}
	
	@Override
	public CommandResult onCommand(MessageReceivedEvent e, String cmd, String[] args, String[] rawArgs) {
		if(!PermissionUtil.hasPermission(e.getAuthor(), permHardRestart, e.getGuild(), false)){
			return CommandResult.NO_PERMISSION;
		}
		
		try {
			ProcessBuilder builder = null;
			if(CommandLineExecutor.usingCmdLine()){
				if(!CommandLineExecutor.headless()){
					e.getChannel().sendMessage("Restarting is not supported for terminal based command line startup.").queue();
					return CommandResult.ERROR;
				} else {
					builder = new ProcessBuilder("java", "-jar", AventiBot.getDataPathFull(), "--cmdline", "--headless");
				}
			} else {
				builder = new ProcessBuilder("java", "-jar", AventiBot.getDataPathFull(), "--abNow");
			}
			e.getChannel().sendMessage("Restarting..").queue();
			builder.start();
			CommandDispatcher.displayResult(CommandResult.SUCCESS, e.getMessage(), v -> {
				try {
					if(AventiBot.getStatus() == BotStatus.CONNECTED){
						AventiBot.getInstance().shutdown();
					}
				} catch (Exception ex){
					//Shutdown
					Runtime.getRuntime().exit(0);
				}
				Runtime.getRuntime().exit(0);
			});
		} catch (IOException e1) {
			e.getChannel().sendMessage("Failed to restart..").queue();
			e1.printStackTrace();
			return CommandResult.ERROR;
		}
		
		
		return CommandResult.IGNORE;
	}

	@Override
	public Set<PermissionNode> getNodes() {
		return nodes;
	}

	
	
}
