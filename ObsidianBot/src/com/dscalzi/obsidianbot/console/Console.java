package com.dscalzi.obsidianbot.console;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.entities.Game;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.UserImpl;

public class Console extends UserImpl{

	private static final String _id;
	private static final String _username;
    private static final String _discriminator;
    private static final String _avatarId;
    private static final Game _game;
    private static final OnlineStatus _onlineStatus;
    private static final boolean _isBot;
	
	static {
		_id = "ObsidianBot_Console";
		_username = "ObsidianBot_Console";
		_discriminator = "ObsidianBot_Console#-1";
		_avatarId = null;
		_game = null;
		_onlineStatus = OnlineStatus.ONLINE;
		_isBot = false;
	}
	
	public Console(JDA api) {
		super(_id, (JDAImpl) api);
		this.setUserName(Console._username);
		this.setDiscriminator(Console._discriminator);
		this.setAvatarId(Console._avatarId);
		this.setCurrentGame(Console._game);
		this.setOnlineStatus(Console._onlineStatus);
		this.setPrivateChannel(new ConsolePrivateChannel(this, api));
		this.setIsBot(Console._isBot);
	}

}
