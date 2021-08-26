package com.bad.studios.tellerbot.utils;

import java.util.regex.Pattern;

public class MentionMatcher {

    private static final Pattern mentionRegex = Pattern.compile("<+([@!&#]*)([0-9]+)>+");
    private static final Pattern fullNameRegex = Pattern.compile("(.+)(#)([0-9]{4})");

    public static boolean matchMention(String mention) {
        return mentionRegex.matcher(mention).matches();
    }

    public static boolean matchFullName(String fullName) {
        return fullNameRegex.matcher(fullName).matches();
    }

    public static boolean matchMentionOrFullName(String term) {
        return fullNameRegex.matcher(term).matches() || mentionRegex.matcher(term).matches();
    }
}
