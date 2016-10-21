package com.dscalzi.obsidianbot;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.entities.Game;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.UserImpl;

public class ConsoleUser extends UserImpl{

	private static final String _id;
	private static final String _username;
    private static final String _discriminator;
    private static final String _avatarId;
    private static final Game _game;
    private static final OnlineStatus _onlineStatus;
    private static final PrivateChannel _privateChannel;
    private static final boolean _isBot;
	
	static {
		_id = "ObsidianBot_Console";
		_username = "ObsidianBot_Console";
		_discriminator = "ObsidianBot_Console#-1";
		_avatarId = null;
		_game = null;
		_onlineStatus = OnlineStatus.ONLINE;
		_privateChannel = null;
		_isBot = false;
	}
	
	public ConsoleUser(JDA api) {
		super(_id, (JDAImpl) api);
		this.setUserName(ConsoleUser._username);
		this.setDiscriminator(ConsoleUser._discriminator);
		this.setAvatarId(ConsoleUser._avatarId);
		this.setCurrentGame(ConsoleUser._game);
		this.setOnlineStatus(ConsoleUser._onlineStatus);
		this.setPrivateChannel(ConsoleUser._privateChannel);
		this.setIsBot(ConsoleUser._isBot);
	}

}
