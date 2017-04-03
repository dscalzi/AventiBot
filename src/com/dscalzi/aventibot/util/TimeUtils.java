/*
 * AventiBot
 * Copyright (C) 2016-2017 Daniel D. Scalzi
 * See LICENSE.txt for license information.
 */
package com.dscalzi.aventibot.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

	private static Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);
	
	// Copyright essentials, all credits to them for this.
	/**
	 * Convert time input to Unix time in seconds. The returned time is the instant
	 * either in the past or future by adding/subtracting the time period given to
	 * the current time.
	 * The time variable is formatted in the following way:
	 * <p>
	 * <strong>{#}y</strong> - # Years
	 * <br><strong>{#}mo</strong> - # Months
	 * <br><strong>{#}w</strong> - # Weeks
	 * <br><strong>{#}d</strong> - # Days
	 * <br><strong>{#}h</strong> - # Hours
	 * <br><strong>{#}m</strong> - # Minutes
	 * <br><strong>{#}s</strong> - # Seconds
	 * <p>
	 * An example string would be <code>1mo7d5h3s</code><br>
	 * This would be read as one month, seven days, five hours, three seconds.
	 * 
	 * @param time The time String
	 * @param future If true, the Unix time will be the time in the future, if false it will be the past.
	 * @return The Unix time in seconds of the instant x time in the past or future.
	 * @throws Exception Thrown if illegal date, or if the time exceeds the limit set by the method.
	 */
	public static long parseDateDiff(String time, boolean future) throws Exception {
		Matcher m = timePattern.matcher(time);
	    int years = 0, months = 0, weeks = 0, days = 0, hours = 0, minutes = 0, seconds = 0;
	    boolean found = false;
	    while(m.find()){
	    	if(m.group() == null || m.group().isEmpty())
	    		continue;
	    	for(int i = 0; i < m.groupCount(); i++){
	    		if(m.group(i) != null && !m.group(i).isEmpty()){
	    			found = true;
	    			break;
	    		}
	    	}
	    	if(found){
	    		if(m.group(1) != null && !m.group(1).isEmpty())
	    			years = Integer.parseInt(m.group(1));	
	    		if(m.group(2) != null && !m.group(2).isEmpty())
	    			months = Integer.parseInt(m.group(2));
	    		if(m.group(3) != null && !m.group(3).isEmpty())
	    			weeks = Integer.parseInt(m.group(3));
	    		if(m.group(4) != null && !m.group(4).isEmpty())
	    			days = Integer.parseInt(m.group(4));
	    		if(m.group(5) != null && !m.group(5).isEmpty())
	    			hours = Integer.parseInt(m.group(5));
	    		if(m.group(6) != null && !m.group(6).isEmpty())
	    			minutes = Integer.parseInt(m.group(6));
	    		if(m.group(7) != null && !m.group(7).isEmpty())
	    			seconds = Integer.parseInt(m.group(7));
	    		break;
	    	}
	    }
	    if(!found)
	    	throw new Exception("Illegal Date");

	    if(years > 20)
	    	throw new Exception("Illegal Date");

	    Calendar c = new GregorianCalendar();
	    if(years > 0)
	    	c.add(Calendar.YEAR, years * (future ? 1 : -1));
	    if(months > 0)
	    	c.add(Calendar.MONTH, months * (future ? 1 : -1));
	    if(weeks > 0)
	    	c.add(Calendar.WEEK_OF_YEAR, weeks * (future ? 1 : -1));
	    if(days > 0)
	    	c.add(Calendar.DAY_OF_MONTH, days * (future ? 1 : -1));
	    if(hours > 0)
	    	c.add(Calendar.HOUR_OF_DAY, hours * (future ? 1 : -1));
	    if(minutes > 0)
	    	c.add(Calendar.MINUTE, minutes * (future ? 1 : -1));
	    if(seconds > 0)
	    	c.add(Calendar.SECOND, seconds * (future ? 1 : -1));
	    return c.getTimeInMillis() / 1000L;
	}
	
	public static String formatTrackDuration(long d){
		int hours = (int) Math.floorDiv(d, 3600000L);
		d -= hours > 0 ? 3600000*hours : 0;
		int minutes = (int)Math.floorDiv(d, 60000L);
		d -= minutes > 0 ? 60000*minutes : 0;
		int seconds = (int)Math.floorDiv(d, 1000L);
		return (hours > 0 ? hours + ":" : "") + (minutes > 0 ? ((hours > 0 && minutes/10 == 0) ? "0" + minutes : minutes) + ":" : "0:") + (seconds/10 == 0 ? "0" + seconds : seconds);
	}
	
}