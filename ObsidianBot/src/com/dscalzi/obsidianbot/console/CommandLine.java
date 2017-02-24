package com.dscalzi.obsidianbot.console;

import com.dscalzi.obsidianbot.BotStatus;
import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandDispatcher;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.MessageImpl;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class CommandLine {

	private final SimpleLog LOG;
	private TextField node;
	
	public CommandLine(TextField node){
		this.LOG = SimpleLog.getLog("Console");
		this.node = node;
		this.node.setOnKeyPressed((e) -> {
			if(e.getCode().equals(KeyCode.ENTER)){
				if(node.getText() == null || node.getText().isEmpty())
					return;
				
				if(ObsidianBot.getStatus() == BotStatus.SHUTDOWN){
					LOG.info("ObsidianBot has been shutdown, no further commands will be received.");
				} else if(ObsidianBot.getStatus() != BotStatus.CONNECTED){
					LOG.info("Please launch ObsidianBot to use the command line!");
				} else {
					JDA api = ObsidianBot.getInstance().getJDA();
					ConsoleUser console = ObsidianBot.getInstance().getConsole();
					
					LOG.info(node.getText());
					
					MessageImpl m = new MessageImpl("console", ((JDAImpl)api).getPrivateChannelById("consolepm"), false);
					m.setContent(node.getText());
					m.setAuthor(console);
					MessageReceivedEvent mre = new MessageReceivedEvent(api, -1, m);
					CommandDispatcher.dispatchCommand(mre, CommandDispatcher.parseMessage(mre));
				}
				node.setText(null);
			}
		});
	}
	
}
