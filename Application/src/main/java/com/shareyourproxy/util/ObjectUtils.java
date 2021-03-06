package com.shareyourproxy.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static java.lang.Character.toTitleCase;
import static java.lang.String.valueOf;

/**
 * Helper class for formatting objects.
 */
public class ObjectUtils {

    /**
     * Private Constructor.
     */
    private ObjectUtils() {
        super();
    }

    public static String buildFullName(String firstName, String lastName) {
        return new StringBuilder(capitalize(firstName))
            .append(" ")
            .append(capitalize(lastName))
            .toString().trim();
    }

    /**
     * Compare two ints.
     *
     * @param lhs left item
     * @param rhs right item
     * @return left right or equal
     */
    public static int compare(int lhs, int rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    public static String capitalize(String string) {
        if (string == null || string.length() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(valueOf(toTitleCase(string.charAt(0))))
            .append(string.substring(1));
        return sb.toString();
    }

    public static SimpleDateFormat getTwitterDateFormat() {
        return new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.US);
    }

    /**
     * Get a formatted TAG string.
     *
     * @param klass the class to copy a TAG for
     * @return return the TAG String
     */
    public static String getSimpleName(Class klass) {
        return klass.getSimpleName();
    }
}
