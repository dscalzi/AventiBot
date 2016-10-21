package com.dscalzi.obsidianbot.ui;

import com.dscalzi.obsidianbot.ConsoleUser;
import com.dscalzi.obsidianbot.ObsidianBot;
import com.dscalzi.obsidianbot.cmdutil.CommandDispatcher;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
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
		node.setOnKeyPressed((e) -> {
			if(e.getCode().equals(KeyCode.ENTER)){
				System.out.println(node.getText());
				MessageImpl m = new MessageImpl("console", (JDAImpl) ObsidianBot.getInstance().getJDA());
				m.setContent(node.getText());
				ConsoleUser u = new ConsoleUser(ObsidianBot.getInstance().getJDA());
				m.setAuthor(u);
				m.setIsPrivate(false);
				m.setChannelId("211524927831015424");
				MessageReceivedEvent mre = new MessageReceivedEvent(ObsidianBot.getInstance().getJDA(), -1, m);
				CommandDispatcher.dispatchCommand(mre, CommandDispatcher.parseMessage(mre));
				LOG.info(node.getText());
				node.setText("");
			}
		});
	}
	
}
