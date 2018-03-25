/*
 * AventiBot
 * Copyright (C) 2016-2018 Daniel D. Scalzi
 * See LICENSE for license information.
 */
package com.dscalzi.aventibot.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.dscalzi.aventibot.AventiBot;

import javafx.util.Pair;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class InputUtils {

	private static final Pattern RAW_ROLE = Pattern.compile("<@&\\d+>");
	private static final Pattern ROLE_EXCESS = Pattern.compile("[<@&>]");
	private static final Pattern RAW_USER = Pattern.compile("<@!?\\d+>");
	private static final Pattern USER_EXCESS = Pattern.compile("[<@!?>]");
	
	public static TextChannel parseChannel(Message m, String reference){
		TextChannel chnl = null;
		try { chnl = AventiBot.getInstance().getJDA().getTextChannelById(reference); } catch (NumberFormatException e){ chnl = null; }
		if(chnl != null) return chnl;
		List<TextChannel> channels = m.getMentionedChannels();
		for(TextChannel tc : channels){
			if(tc.getName().equals(reference.replace("#", ""))){
				return tc;
			}
		}
		return null;
	}
	
	public static User parseUser(Message m, String reference){
		User usr = null;
		try { usr = AventiBot.getInstance().getJDA().getUserById(reference); } catch (NumberFormatException e) { usr = null; }
		if(usr != null) return usr;
		List<User> users = m.getMentionedUsers();
		for(User u : users){
			if(u.getAsMention().equals(reference.replace("!", ""))){
				return u;
			}
		}
		return null;
	}
	
	/**
	 * Too lazy to document fully so here's a brief explanation to avoid future
	 * confusion. This can parse members out of three types of input. The first is
	 * a snowflake id, which is the ONLY way to identify users who are not members
	 * of the specified guild. The second is by EFFECTIVE name of the user. If it contains
	 * spaces you can delimit it by encapsulating it with double or single quotes. Finally,
	 * it parses members from mentions using the raw content.
	 * <br><br>
	 * If you do not pass raw message content this will not work properly.
	 * <br><br>
	 * This method behaves similarly to {@link #parseBulkRoles(String[], Guild) parseBulkRoles} method, only for users/members.
	 * 
	 * 
	 * @param rawArgs Message contents converted to arguments.
	 * @param g The guild to parse against.
	 * @return A pair of data, the key will be a set of matching Users, the value will be a set of failed terms (no matches).
	 */
	public static Pair<Set<User>, Set<String>> parseBulkMembers(String[] rawArgs, Guild g){
		if(g == null) return null;
		if(rawArgs.length == 0) return null;
		Set<User> fMembers = new LinkedHashSet<User>();
		Set<String> failedTerms = new LinkedHashSet<String>();
		for(int i=0; i<rawArgs.length; ++i){
			String s = rawArgs[i];
			if(RAW_USER.matcher(s).matches()){
				String id = USER_EXCESS.matcher(s).replaceAll("");
				Member m = null;
				try{ m = g.getMemberById(id); } catch (NumberFormatException e) { m = null; }
				if(m != null) fMembers.add(m.getUser());
			} else {
				String arg;
				if(s.substring(0, 1).matches("['\"]")){
					Pair<Integer, String> loopResults = InputUtils.parseFullTerm(rawArgs, i);
					arg = loopResults.getValue();
					i = loopResults.getKey();
				} else {
					arg = s;
				}
				List<Member> result = g.getMembersByEffectiveName(arg, true);
				if(result.size() == 0) {
					Member m = null;
					try{ m = g.getMemberById(arg); } catch (NumberFormatException e) { m = null; }
					User u = null;
					if(m == null) 
						try { 
							u = AventiBot.getInstance().getJDA().getUserById(arg); 
						} catch(NumberFormatException e) { 
							u = null; 
						}
					if(u == null && m == null) failedTerms.add(arg);
					else fMembers.add(u != null ? u : m.getUser());
				} else result.forEach(m -> fMembers.add(m.getUser()));
			}
		}
		return new Pair<Set<User>, Set<String>>(fMembers, failedTerms);
	}
	
	/**
	 * Convenience method for parsing a role from a String value which matches
	 * the criteria listed below. If the term you are expecting to parse may have a space
	 * in it, the space should already be included in the reference String.
	 * 
	 * For example if there was a role called "My Role" with id "123", the following
	 * input would result in a successful parse.
	 * <ul>
	 * <li>Reference String with the value "<strong>my role</strong>" (name check, case insensitive).
	 * <ul><li>If more than one role has the same name of "<strong>my role</strong>", the first result will be returned.</li></ul>
	 * </li>
	 * <li>Reference String with the value "<strong>123</strong>" (id check).</li>
	 * <li>Reference String with the value "<strong><&@123></strong>" (raw value of a mention).</li>
	 * </ul>
	 * 
	 * <strong>This method should only be passed the <em>raw</em> contents of a message.</strong>
	 * <br>
	 * <br>
	 * @param reference The String to parse.
	 * @param g The guild to parse against.
	 * @return A <code>Role</code> object if the reference matches the above criteria and an existing role in 
	 * <code>Guild</code> g, otherwise null.
	 */
	public static Role parseRole(String reference, Guild g){
		Role r = null;
		try{ r = g.getRoleById(reference); } catch (NumberFormatException e) { r = null; }
		if(r != null) return r;
		if(RAW_ROLE.matcher(reference).matches()){
			String id = ROLE_EXCESS.matcher(reference).replaceAll("");
			try{ r = g.getRoleById(id); } catch (NumberFormatException e) { r = null; }
			if(r != null) return r;
		}
		List<Role> roles = g.getRolesByName(reference, true);
		if(roles.size() > 0) return roles.get(0);
		return null;
	}
	
	/**
	 * Takes a String array of arguments in which you know one of the arguments spans multiple
	 * indices, in other words if the arguments contains a space. To indicate the beginning and end of the
	 * term it will start/end with a delimiter value of either a double quote or single quote. For example:
	 * 
	 * <ul>
	 * <li><strong>"some term"</strong> --> <em>some term</em></li>
	 * <li><strong>"some' term"</strong> --> <em>some' term</em></li>
	 * <li><strong>'some" term'</strong> --> <em>some" term</em></li>
	 * <li><strong>"some" term"</strong> --> <em>some</em></li>
	 * <li><strong>"so"me term"</strong> --> <em>so"me term</em></li>
	 * <li><strong>"someterm"</strong> --> <em>someterm</em></li>
	 * </ul>
	 * 
	 * @param args Arguments to parse
	 * @param startAt Index to begin looking at.
	 * @return A pair with the key being the index the parse finished at, and the value being the String term
	 * without the delimiter.
	 */
	public static Pair<Integer, String> parseFullTerm(String[] args, int startAt){
		if(startAt >= args.length) return null;
		String delimiter = args[startAt].substring(0, 1).equals("'") ? "'" : "\"";
		String arg = args[startAt].substring(1);
		if(arg.substring(arg.length()-1).equals(delimiter))
			return new Pair<Integer, String>(startAt, arg.substring(0, arg.length()-1));
		int finishedAt = startAt;
		for(int i=startAt+1; i<args.length; ++i){
			finishedAt = i;
			if(args[i].substring(args[i].length()-1).equals(delimiter)){
				arg = arg + " " + args[i].substring(0, args[i].length()-1);
				break;
			}
			arg = arg + " " + args[i];
		}
		
		return new Pair<Integer, String>(finishedAt, arg);
	}
	
	/**
	 * Parses an array of raw message arguments for multiple roles. If a role name contains
	 * a space it must be passed with quotes as such <code><strong>"my role"</strong></code>.
	 * If an argument starts with a single quote <code>'</code> or double quote <code>"</code>,
	 * a nested parse will begin. Each subsequent argument will be considered one term until an
	 * argument is reached that ends with a single or double quote. For each term, the following
	 * criteria must be met.
	 * 
	 * <ul>
	 * <li>Raw Mention String.</li>
	 * <li>Role id.</li>
	 * <li>String matching the name of the role(s).</li>
	 * </ul>
	 * 
	 * For example if there was a role called "My Role" with id "123", the following
	 * input would result in a successful parse.
	 * 
	 * <ul>
	 * <li>Term with the value <strong>"my role"</strong> (name check, case insensitive).
	 * <ul><li>If more than one role has the same name of "<strong>my role</strong>", all matches will be returned.</li></ul>
	 * </li>
	 * <li>Term with the value "<strong>123</strong>" (id check).</li>
	 * <li>Term with the value "<strong><&@123></strong>" (raw value of a mention).</li>
	 * </ul>
	 * 
	 * <strong>This method should only be passed the <em>raw</em> arguments of a message.</strong>
	 * <br>
	 * <br>
	 * 
	 * @param rawArgs Message contents converted to arguments.
	 * @param g The guild to parse against.
	 * @return A pair of data, the key will be a set of matching Roles, the value will be a set of failed terms (no matches).
	 */
	public static Pair<Set<Role>, Set<String>> parseBulkRoles(String[] rawArgs, Guild g){
		if(g == null) return null;
		if(rawArgs.length == 0) return null;
		Set<Role> fRoles = new LinkedHashSet<Role>();
		Set<String> failedTerms = new LinkedHashSet<String>();
		for(int i=0; i<rawArgs.length; ++i){
			String s = rawArgs[i];
			if(RAW_ROLE.matcher(s).matches()){
				String id = ROLE_EXCESS.matcher(s).replaceAll("");
				Role r = null;
				try{ r = g.getRoleById(id); } catch (NumberFormatException e) { r = null; }
				if(r != null) fRoles.add(r);
			} else {
				String arg;
				if(s.substring(0, 1).matches("['\"]")){
					Pair<Integer, String> loopResults = InputUtils.parseFullTerm(rawArgs, i);
					arg = loopResults.getValue();
					i = loopResults.getKey();
				} else {
					arg = s;
				}
				List<Role> result = g.getRolesByName(arg, true);
				if(result.size() == 0) {
					Role r = null;
					try{ r = g.getRoleById(arg); } catch (NumberFormatException e) { r = null; }
					if(r == null) failedTerms.add(arg);
					else fRoles.add(r);
				} else fRoles.addAll(result);
			}
		}
		return new Pair<Set<Role>, Set<String>>(fRoles, failedTerms);
	}
	
}
