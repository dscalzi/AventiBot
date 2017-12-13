/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.console;

import com.dscalzi.aventibot.BotStatus;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.cmdutil.CommandDispatcher;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message.Attachment;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.SystemMessage;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandLine {

	private final Logger LOG;
	private TextField node;
	
	public CommandLine(TextField node){
		this.LOG = LoggerFactory.getLogger("Console");
		this.node = node;
		this.node.setOnKeyPressed((e) -> {
			if(e.getCode().equals(KeyCode.ENTER)){
				if(node.getText() == null || node.getText().isEmpty())
					return;
				
				if(AventiBot.getStatus() == BotStatus.SHUTDOWN){
					LOG.info("AventiBot has been shutdown, no further commands will be received.");
				} else if(AventiBot.getStatus() != BotStatus.CONNECTED){
					LOG.info("Please launch AventiBot to use the command line!");
				} else {
					JDA api = AventiBot.getInstance().getJDA();
					ConsoleUser console = AventiBot.getInstance().getConsole();
					
					LOG.info(node.getText());
					
					SystemMessage m = new SystemMessage(-1L,
							((JDAImpl)api).getPrivateChannelById(-1L),
							MessageType.DEFAULT,
							false,
							false,
							false,
							false,
							node.getText(),
							"-1",
							console,
							OffsetDateTime.now(),
							new ArrayList<MessageReaction>(),
							new ArrayList<Attachment>(),
							new ArrayList<MessageEmbed>());
					MessageReceivedEvent mre = new MessageReceivedEvent(api, -1, m);
					CommandDispatcher.dispatchCommand(mre, CommandDispatcher.parseMessage(mre));
				}
				node.setText(null);
			}
		});
	}
	
}
