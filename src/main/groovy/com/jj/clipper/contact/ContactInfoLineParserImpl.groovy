package com.jj.clipper.contact

import java.util.regex.Matcher

class ContactInfoLineParserImpl implements ContactInfoLineParser {

    boolean isContactInfoLine(final String line) {
        return line ==~ /.*Customer Service Center at \d{3}-\d{3}-\d{4}.*/
    }

    String parse(final String line) {
        assert isContactInfoLine(line)

        final Matcher matcher = (line =~ /.*Customer Service Center at (\d{3}-\d{3}-\d{4}).*/)
        assert matcher.hasGroup()
        assert 1L == matcher.size()

        final List<String> listOfGroups = matcher[0] as List<String>

        return listOfGroups[1]
    }

}
