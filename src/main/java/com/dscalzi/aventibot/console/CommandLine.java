/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2018 Daniel D. Scalzi
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

package com.dscalzi.aventibot.console;

import com.dscalzi.aventibot.BotStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.cmdutil.CommandDispatcher;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
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
					
					ConsoleMessage m = new ConsoleMessage(((JDAImpl)api).getPrivateChannelById(-1L), node.getText(), console);
					MessageReceivedEvent mre = new MessageReceivedEvent(api, -1, m);
					CommandDispatcher.dispatchCommand(mre, CommandDispatcher.parseMessage(mre));
				}
				node.setText(null);
			}
		});
	}
	
}
