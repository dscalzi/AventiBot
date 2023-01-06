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

package com.dscalzi.aventibot.cmdline;

import com.dscalzi.aventibot.AventiBot;
import com.dscalzi.aventibot.BotStatus;
import com.dscalzi.aventibot.cmdutil.CommandDispatcher;
import com.dscalzi.aventibot.console.ConsoleMessage;
import com.dscalzi.aventibot.console.ConsoleUser;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Scanner;

@Slf4j
public class CommandLineConsole {

    private final Scanner input;
    private volatile boolean control;

    public CommandLineConsole() {
        this.input = new Scanner(System.in);
        this.control = false;
    }

    public void start() {
        if (control) return;
        control = true;
        while (control) {
            String line = input.nextLine();

            if (AventiBot.getStatus() == BotStatus.SHUTDOWN) {
                log.info("AventiBot has been shutdown, no further commands will be received.");
            } else if (AventiBot.getStatus() != BotStatus.CONNECTED) {
                log.info("Please launch AventiBot to use the command line!");
            } else {
                JDA api = AventiBot.getInstance().getJDA();
                ConsoleUser console = AventiBot.getInstance().getConsole();

                log.info(line);

                ConsoleMessage m = new ConsoleMessage(api.getPrivateChannelById(-1L), line, console);
                MessageReceivedEvent mre = new MessageReceivedEvent(api, -1, m);
                CommandDispatcher.dispatchCommand(mre, CommandDispatcher.parseMessage(mre));
            }
        }
        input.close();
    }

    public void shutdown() {
        control = false;
        //input.close();
    }

    public boolean isShutdown() {
        return control;
    }

}
