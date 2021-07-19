package me.yarinlevi.qbansbotremastered.utilities;

import me.yarinlevi.qbansbotremastered.exceptions.DurationNotDetectedException;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author YarinQuapi
 */
public class StringUtils {
    private final static Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?"
            + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?"
            + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?"
            + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);

    public static long parseDuration(final String durationStr) throws IllegalArgumentException, DurationNotDetectedException {
        final Matcher m = timePattern.matcher(durationStr);
        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;
        boolean found = false;
        while (m.find()) {
            if (m.group() == null || m.group().isEmpty()) {
                continue;
            }
            for (int i = 0; i < m.groupCount(); i++) {
                if (m.group(i) != null && !m.group(i).isEmpty()) {
                    found = true;
                    break;
                }
            }
            if (found) {
                if (m.group(4) != null && !m.group(4).isEmpty()) {
                    days = Integer.parseInt(m.group(4));
                }
                if (m.group(5) != null && !m.group(5).isEmpty()) {
                    hours = Integer.parseInt(m.group(5));
                }
                if (m.group(6) != null && !m.group(6).isEmpty()) {
                    minutes = Integer.parseInt(m.group(6));
                }
                if (m.group(7) != null && !m.group(7).isEmpty()) {
                    seconds = Integer.parseInt(m.group(7));
                }
                break;
            }
        }
        if (!found) {
            throw new DurationNotDetectedException("No duration was detected.");
        }
        final Calendar c = new GregorianCalendar();
        if (days > 0) {
            c.add(Calendar.DAY_OF_MONTH, days);
        }
        if (hours > 0) {
            c.add(Calendar.HOUR_OF_DAY, hours);
        }
        if (minutes > 0) {
            c.add(Calendar.MINUTE, minutes);
        }
        if (seconds > 0) {
            c.add(Calendar.SECOND, seconds);
        }
        return c.getTimeInMillis();
    }

    public static String[] split(String reason) {
        String reason1 = reason;
        int i = 0;
        for (char c : reason1.toCharArray())
            if (c == ' ')
                i++;
        String[] args = new String[i + 1];
        i = 0;
        String word = "";
        reason1 += " ";
        for (char c : reason1.toCharArray())
        {
            if (c != ' ')
                word += c;
            else
            {
                args[i] = word;
                word = "";
                i++;
            }
        }
        if (i == 0)
            args[0] = word;
        return args;
    }
}
