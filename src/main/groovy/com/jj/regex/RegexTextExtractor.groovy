package com.jj.regex

import java.util.regex.Pattern

interface RegexTextExtractor {

    /**
     * Returns a list of groups, from the given string, with the given pattern.
     * Assumes that the matcher size is always one.
     *
     * @param text String to match against.
     * @param pattern Matching pattern to use.
     * @param expectedGroupCount Expect group count.
     *
     * @return List of matched groups, as Strings.
     */
    List<String> extractGroups(String text, Pattern pattern, int expectedGroupCount)

}