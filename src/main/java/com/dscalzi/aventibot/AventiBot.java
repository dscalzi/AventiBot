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

package com.dscalzi.aventibot;

import com.dscalzi.aventibot.cmdutil.CommandListener;
import com.dscalzi.aventibot.cmdutil.CommandRegistry;
import com.dscalzi.aventibot.commands.*;
import com.dscalzi.aventibot.console.ConsoleUser;
import com.dscalzi.aventibot.music.LavaWrapper;
import com.dscalzi.aventibot.settings.GlobalConfig;
import com.dscalzi.aventibot.settings.SettingsManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.internal.utils.UnlockHook;
import net.dv8tion.jda.internal.utils.cache.SnowflakeCacheViewImpl;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.net.URISyntaxException;

@Slf4j
public class AventiBot {

    private static BotStatus status;
    private static AventiBot instance;

    static {
        status = BotStatus.NULL;
    }

    private JDA jda;
    private ConsoleUser console;
    private CommandRegistry registry;
    private Long launchTime = null;

    private AventiBot() {
        this.registry = new CommandRegistry();
        if (!this.connect()) return;
		/*try {
			jda.getSelfUser().getManager().setAvatar(Icon.from(new File("C:/Users/Asus/Desktop/HgXD7h2O.jpg"))).queue();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
    }

    public static boolean launch() {
        if (status == BotStatus.NULL) {
            status = BotStatus.LAUNCHED;
            instance = new AventiBot();
            return true;
        }
        return false;
    }

    private void postConnectionSetup(GlobalConfig g) {
        registerCommands();
        registerListeners();
        LavaWrapper.initialize(g);
        this.console = ConsoleUser.build(jda);
        SnowflakeCacheViewImpl<PrivateChannel> pcCache = ((SnowflakeCacheViewImpl<PrivateChannel>) jda.getPrivateChannelCache());
        try (UnlockHook ignored = pcCache.writeLock()) {
            pcCache.getMap().put(console.getIdLong(), console.getPrivateChannel());
        }
        log.info("Connected to {} servers.", jda.getGuilds().size());
        launchTime = System.currentTimeMillis();
    }

    private void registerCommands() {
        CmdMusicControl mcc = new CmdMusicControl();
        CmdUrbanDictionary ud = new CmdUrbanDictionary();
        this.registry.register("play", mcc);
        this.registry.register("shuffleplay", mcc);
        this.registry.register("playlist", mcc);
        this.registry.register("skip", mcc);
        this.registry.register("cancelskip", mcc);
        this.registry.register("forceskip", mcc);
        this.registry.register("pause", mcc);
        this.registry.register("stop", mcc);
        this.registry.register("resume", mcc);
        this.registry.register("shuffle", mcc);
        this.registry.register("say", new CmdSay());
        this.registry.register("help", new CmdHelp());
        this.registry.register("helloworld", new CmdHelloWorld());
        this.registry.register("author", new CmdAuthor());
        this.registry.register("clear", new CmdClear());
        this.registry.register("shutdown", new CmdShutdown());
        this.registry.register("roleid", new CmdRoleId());
        this.registry.register("permissions", new CmdPermissionsControl());
        this.registry.register("settings", new CmdSettingsControl());
        this.registry.register("gsettings", new CmdGSettings());
        this.registry.register("softreload", new CmdSoftReload());
        this.registry.register("hardrestart", new CmdHardRestart());
        this.registry.register("urbandictionary", ud);
        this.registry.register("define", ud);
        this.registry.register("sheik", new CmdSheik());
    }

    private void registerListeners() {
        jda.addEventListener(new CommandListener());
        jda.addEventListener(new MessageListener());
    }

    public boolean connect() {
        try {
            GlobalConfig g = SettingsManager.loadGlobalConfig();
            JDABuilder jdaBuilder = JDABuilder.createDefault(g.getToken())
                    .setAutoReconnect(true)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .setToken(g.getToken());
            if (!g.getCurrentGame().isEmpty())
                jdaBuilder.setActivity(Activity.playing(g.getCurrentGame()));
            jda = jdaBuilder.build().awaitReady();
            status = BotStatus.CONNECTED;
            postConnectionSetup(g);
        } catch (IllegalArgumentException | InterruptedException e) {
            status = BotStatus.LAUNCHED;
            log.error(MarkerFactory.getMarker("FATAL"), "Failed to connect to Discord!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void shutdown() {
        AventiBot.status = BotStatus.SHUTDOWN;
        jda.shutdownNow();
        LavaWrapper.getInstance().getAudioPlayerManager().shutdown();
    }

    public static AventiBot getInstance() {
        return AventiBot.instance;
    }

    public static BotStatus getStatus() {
        return AventiBot.status;
    }

    public static void setCurrentGame(String name) {
        if (AventiBot.getStatus() == BotStatus.CONNECTED && getInstance() != null)
            getInstance().getJDA().getPresence().setActivity(name != null && !name.isEmpty() ? Activity.playing(name) : null);
    }


    public CommandRegistry getCommandRegistry() {
        return this.registry;
    }

    public ConsoleUser getConsole() {
        return this.console;
    }

    public long getUptime() {
        return launchTime == null ? 0 : System.currentTimeMillis() - launchTime;
    }

    public long getLaunchTime() {
        return launchTime == null ? 0 : launchTime;
    }

    public JDA getJDA() {
        return this.jda;
    }

    public static String getDataPath() {
        String pth;
        try {
            pth = AventiBot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replace("/", File.separator);
        } catch (URISyntaxException e) {
            log.error(MarkerFactory.getMarker("FATAL"), "The paths on your machine cannot be converted to URIs, I am speechless.");
            e.printStackTrace();
            return null;
        }
        return pth.substring(1, pth.lastIndexOf(File.separator));
    }

    public static String getDataPathFull() {
        return AventiBot.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(1);
    }

    public static String getVersion() {
        String a = AventiBot.class.getPackage().getSpecificationVersion();
        String v = AventiBot.class.getPackage().getImplementationVersion();
        return v == null ? a == null ? "Debug" : a : v;
    }

}