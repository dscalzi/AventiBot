/*
 * This file is part of AventiBot.
 * Copyright (C) 2016-2023 Daniel D. Scalzi
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

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.cmdutil.CommandDispatcher;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@Slf4j
public class CommandLine {

    private TextField node;

    public CommandLine(TextField node) {
        this.node = node;
        this.node.setOnKeyPressed((e) -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                if (node.getText() == null || node.getText().isEmpty())
                    return;

                if (AventiBot.getStatus() == BotStatus.SHUTDOWN) {
                    log.info("AventiBot has been shutdown, no further commands will be received.");
                } else if (AventiBot.getStatus() != BotStatus.CONNECTED) {
                    log.info("Please launch AventiBot to use the command line!");
                } else {
                    JDA api = AventiBot.getInstance().getJDA();
                    ConsoleUser console = AventiBot.getInstance().getConsole();

                    log.info(node.getText());

                    ConsoleMessage m = new ConsoleMessage(console.getPrivateChannel(), node.getText(), console);
                    MessageReceivedEvent mre = new MessageReceivedEvent(api, -1, m);
                    CommandDispatcher.dispatchCommand(mre, CommandDispatcher.parseMessage(mre));
                }
                node.setText(null);
            }
        });
    }

}
