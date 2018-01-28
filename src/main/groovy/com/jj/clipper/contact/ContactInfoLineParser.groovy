package com.jj.clipper.contact

interface ContactInfoLineParser {

    boolean isContactInfoLine(String line)

    String parse(String line)

}
