package com.dscalzi.obsidianbot;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.MessageHistory;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.PrivateChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.SimpleLog;

public class ConsolePrivateChannel implements PrivateChannel{
	
	public static final String RATE_LIMIT_IDENTIFIER = "GLOBAL_PRIV_CHANNEL_RATELIMIT";
    private final String id;
    private final User user;
    private final JDA api;

    protected ConsolePrivateChannel(User user, JDA api) {
        this.user = user;
        this.api = api;
        this.id = user.getId();
    }
	
	@Override
	public Message sendMessage(String text) {
		SimpleLog.getLog("??? -> Me").info(text);
		return null;
	}

	@Override
	public Message sendMessage(Message msg) {
		SimpleLog.getLog(msg.getAuthor().getDiscriminator() + " -> Me").info(msg.getRawContent());
		return null;
	}

	@Override
	public void sendMessageAsync(String msg, Consumer<Message> callback) {
		// TODO Implement
		
	}

	@Override
	public void sendMessageAsync(Message msg, Consumer<Message> callback) {
		// TODO Implement
		
	}

	/**
	 * Currently not possible for Console.
	 */
	@Override
	public Message sendFile(File file, Message message) { return null; }

	/**
	 * Currently not possible for Console.
	 */
	@Override
	public void sendFileAsync(File file, Message message, Consumer<Message> callback) {	}

	/**
	 * Not possible for Console.
	 */
	@Override
	public Message getMessageById(String messageId) { return null; }

	/**
	 * Not possible for Console.
	 */
	@Override
	public boolean deleteMessageById(String messageId) { return false; }

	/**
	 * Currently not possible for Console.
	 */
	@Override
	public MessageHistory getHistory() { return null; }

	/**
	 * Not possible for Console.
	 */
	@Override
	public void sendTyping() { }

	/**
	 * Not possible for Console.
	 */
	@Override
	public boolean pinMessageById(String messageId) { return false; }

	/**
	 * Not possible for Console.
	 */
	@Override
	public boolean unpinMessageById(String messageId) {	return false; }

	/**
	 * Not possible for Console.
	 */
	@Override
	public List<Message> getPinnedMessages() {
		return null;
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

	/**
	 * Not possible for Console.
	 */
	@Override
	public void close() { }

}
