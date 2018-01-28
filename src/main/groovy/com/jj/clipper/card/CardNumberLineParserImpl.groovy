package com.jj.clipper.card

import java.util.regex.Matcher

class CardNumberLineParserImpl implements CardNumberLineParser {

    boolean isCardNumberLine(final String line) {
        return line ==~ /^CARD \d{10}$/
    }

    String parse(final String line) {
        assert isCardNumberLine(line)

        final Matcher matcher = (line =~ /^CARD (\d{10})$/)
        assert matcher.hasGroup()
        assert 1L == matcher.size()

        final List<String> listOfGroups = matcher[0] as List<String>

        return listOfGroups[1]
    }

}
