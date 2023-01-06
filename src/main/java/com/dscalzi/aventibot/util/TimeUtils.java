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

package com.dscalzi.aventibot.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtils {

    private static Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);

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
     * @param time   The time String
     * @param future If true, the Unix time will be the time in the future, if false it will be the past.
     * @return The Unix time in seconds of the instant x time in the past or future.
     * @throws Exception Thrown if illegal date, or if the time exceeds the limit set by the method.
     */
    public static long parseDateDiff(String time, boolean future) throws Exception {
        // Copyright essentials, all credits to them for this.
        Matcher m = timePattern.matcher(time);
        int years = 0, months = 0, weeks = 0, days = 0, hours = 0, minutes = 0, seconds = 0;
        boolean found = false;
        while (m.find()) {
            if (m.group() == null || m.group().isEmpty())
                continue;
            for (int i = 0; i < m.groupCount(); i++) {
                if (m.group(i) != null && !m.group(i).isEmpty()) {
                    found = true;
                    break;
                }
            }
            if (found) {
                if (m.group(1) != null && !m.group(1).isEmpty())
                    years = Integer.parseInt(m.group(1));
                if (m.group(2) != null && !m.group(2).isEmpty())
                    months = Integer.parseInt(m.group(2));
                if (m.group(3) != null && !m.group(3).isEmpty())
                    weeks = Integer.parseInt(m.group(3));
                if (m.group(4) != null && !m.group(4).isEmpty())
                    days = Integer.parseInt(m.group(4));
                if (m.group(5) != null && !m.group(5).isEmpty())
                    hours = Integer.parseInt(m.group(5));
                if (m.group(6) != null && !m.group(6).isEmpty())
                    minutes = Integer.parseInt(m.group(6));
                if (m.group(7) != null && !m.group(7).isEmpty())
                    seconds = Integer.parseInt(m.group(7));
                break;
            }
        }
        if (!found)
            throw new Exception("Illegal Date");

        if (years > 20)
            throw new Exception("Illegal Date");

        Calendar c = new GregorianCalendar();
        if (years > 0)
            c.add(Calendar.YEAR, years * (future ? 1 : -1));
        if (months > 0)
            c.add(Calendar.MONTH, months * (future ? 1 : -1));
        if (weeks > 0)
            c.add(Calendar.WEEK_OF_YEAR, weeks * (future ? 1 : -1));
        if (days > 0)
            c.add(Calendar.DAY_OF_MONTH, days * (future ? 1 : -1));
        if (hours > 0)
            c.add(Calendar.HOUR_OF_DAY, hours * (future ? 1 : -1));
        if (minutes > 0)
            c.add(Calendar.MINUTE, minutes * (future ? 1 : -1));
        if (seconds > 0)
            c.add(Calendar.SECOND, seconds * (future ? 1 : -1));
        return c.getTimeInMillis() / 1000L;
    }

    public static String formatTrackDuration(long d) {
        int hours = (int) Math.floorDiv(d, 3600000L);
        d -= hours > 0 ? 3600000 * hours : 0;
        int minutes = (int) Math.floorDiv(d, 60000L);
        d -= minutes > 0 ? 60000 * minutes : 0;
        int seconds = (int) Math.floorDiv(d, 1000L);
        return (hours > 0 ? hours + ":" : "") + (minutes > 0 ? ((hours > 0 && minutes / 10 == 0) ? "0" + minutes : minutes) + ":" : "0:") + (seconds / 10 == 0 ? "0" + seconds : seconds);
    }

    /**
     * Converts Unix time to:<br><br>
     * <strong>hh:mm:ss</strong><br><br>
     * Each unit will be shown even if the value is zero, (ex. 00:00:00).
     *
     * @param d
     * @return
     */
    private static String formatUptimeShort(long d) {
        int hours = (int) Math.floorDiv(d, 3600000L);
        d -= hours > 0 ? 3600000 * hours : 0;
        int minutes = (int) Math.floorDiv(d, 60000L);
        d -= minutes > 0 ? 60000 * minutes : 0;
        int seconds = (int) Math.floorDiv(d, 1000L);
        return ((hours / 10 == 0 ? "0" : "") + hours + ":" + (minutes / 10 == 0 ? "0" : "") + minutes + ":" + (seconds / 10 == 0 ? "0" : "") + seconds);
    }

    /**
     * Formats uptime from Unix time. The format will be:
     * <br>
     * <strong>x Days, hh:mm:ss</strong>
     * <br><br>
     * Ex.<br>
     * <strong>10 Days, 07:18:27</strong>
     * <br>
     * Each field will always be present, even if the value is zero.
     *
     * @param d
     * @return
     */
    public static String formatUptime(long d) {
        int days = (int) Math.floorDiv(d, 86400000L);
        d -= 86400000L * days;
        return ((days + " Day" + (days != 1 ? "s" : "") + ", ") +
                TimeUtils.formatUptimeShort(d));
    }

}
