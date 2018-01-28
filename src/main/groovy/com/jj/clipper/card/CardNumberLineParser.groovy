package com.jj.clipper.card

interface CardNumberLineParser {

    boolean isCardNumberLine(String line)

    String parse(String line)

}