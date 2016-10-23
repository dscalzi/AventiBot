package com.dscalzi.obsidianbot.console;

import com.dscalzi.obsidianbot.BotStatus;
import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandDispatcher;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.MessageImpl;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

public class CommandLine {

	private final SimpleLog LOG;
	@SuppressWarnings("unused")
	private TextField node;
	
	public CommandLine(TextField node){
		this.LOG = SimpleLog.getLog("Console");
		this.node = node;
		this.node.setOnKeyPressed((e) -> {
			if(e.getCode().equals(KeyCode.ENTER)){
				if(node.getText().equals(""))
					return;
				
				if(ObsidianBot.getStatus() != BotStatus.CONNECTED){
					LOG.info("Please launch ObsidianBot to use the command line!");
				} else {
					JDA api = ObsidianBot.getInstance().getJDA();
					Console console = ObsidianBot.getInstance().getConsole();
					
					LOG.info(node.getText());
					
					MessageImpl m = new MessageImpl("console", (JDAImpl) api);
					m.setContent(node.getText());
					m.setAuthor(console);
					m.setChannelId("consolepm");
					m.setIsPrivate(true);
					MessageReceivedEvent mre = new MessageReceivedEvent(api, -1, m);
					CommandDispatcher.dispatchCommand(mre, CommandDispatcher.parseMessage(mre));
				}
				node.setText(null);
			}
		});
	}
	
}
