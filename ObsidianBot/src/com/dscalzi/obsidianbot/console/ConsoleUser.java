package com.dscalzi.obsidianbot.console;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;

public class ConsoleUser extends UserImpl {

	private static final String _id;
	private static final String _username;
	private static final String _discriminator;
	private static final String _avatarId;
	private static final boolean _isBot;
	
	private static boolean limit;
	
	static {
		_id = "ObsidianBot_Console";
		_username = "ObsidianBot_Console";
		_discriminator = "ObsidianBot_Console#-1";
		_avatarId = null;
		_isBot = false;
		
		limit = false;
	}
		
	private ConsoleUser(JDA api) {
		super(_id, (JDAImpl) api);
		this.setName(ConsoleUser._username);
		this.setDiscriminator(ConsoleUser._discriminator);
		this.setAvatarId(ConsoleUser._avatarId);
		this.setPrivateChannel(new ConsolePrivateChannel(this, api));
		this.setBot(ConsoleUser._isBot);
	}
	
	public static ConsoleUser build(JDA api){
		if(!limit){
			limit = true;
			return new ConsoleUser(api);
		}
		throw new IllegalStateException("Cannot build more than one ConsoleUser!");
	}

}
