package com.dscalzi.obsidianbot.console;

import net.dv8tion.jda.client.entities.Call;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.SimpleLog;

public class ConsolePrivateChannel implements PrivateChannel{
	
    private final String id;
    private final User user;
    private final JDA api;

    protected ConsolePrivateChannel(User user, JDA api) {
        this.user = user;
        this.api = api;
        this.id = user.getId();
    }
	
    @Override
	public RestAction<Message> sendMessage(String text) {
    	SimpleLog.getLog("??? -> Me").info(text);
    	return new RestAction.EmptyRestAction<Message>(new MessageBuilder().append(text).build());
	}

	@Override
	public RestAction<Message> sendMessage(Message msg) {
		SimpleLog.getLog(msg.getAuthor().getDiscriminator() + " -> Me").info(msg.getRawContent());
		return new RestAction.EmptyRestAction<Message>(msg);
	}
	
	@Override
	public RestAction<Message> sendMessage(MessageEmbed embed) {
		// Not supported
		return new RestAction.EmptyRestAction<Message>(new MessageBuilder().setEmbed(embed).build());
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public User getUser() {
		return this.user;
	}

	@Override
	public JDA getJDA() {
		return this.api;
	}

	@Override
	public String getName() {
		return getUser().getName();
	}

	@Override
	public ChannelType getType() {
		return ChannelType.PRIVATE;
	}

	@Override
	public RestAction<Void> close() {
		// Not Supported
		return new RestAction.EmptyRestAction<Void>(null);
	}

	@Override
	public RestAction<Call> startCall() {
		// Not supported
		return null;
	}

	@Override
	public Call getCurrentCall() {
		// Not supported
		return null;
	}

	@Override
	public boolean isFake() {
		// Not supported
		return false;
	}

}
