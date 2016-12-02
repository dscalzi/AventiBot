package com.dscalzi.obsidianbot.console;

import com.dscalzi.obsidianbot.ObsidianBot;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.MemberImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;

public class Console extends MemberImpl{

	private static final OnlineStatus _onlineStatus;
	
	static {
		_onlineStatus = OnlineStatus.ONLINE;
	}
	
	public Console(JDA api){
		super(api.getGuildById(ObsidianBot.guildId), new ConsoleUser(api));
		this.setOnlineStatus(_onlineStatus);
	}
	
	public static class ConsoleUser extends UserImpl {

		private static final String _id;
		private static final String _username;
	    private static final String _discriminator;
	    private static final String _avatarId;
	    private static final boolean _isBot;
		
		static {
			_id = "ObsidianBot_Console";
			_username = "ObsidianBot_Console";
			_discriminator = "ObsidianBot_Console#-1";
			_avatarId = null;
			_isBot = false;
		}
		
		private ConsoleUser(JDA api) {
			super(_id, (JDAImpl) api);
			this.setName(ConsoleUser._username);
			this.setDiscriminator(ConsoleUser._discriminator);
			this.setAvatarId(ConsoleUser._avatarId);
			this.setPrivateChannel(new ConsolePrivateChannel(this, api));
			this.setBot(ConsoleUser._isBot);
		}
		
	}

}
