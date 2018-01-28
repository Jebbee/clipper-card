package com.jj.clipper.transaction

import com.jj.clipper.ClipperCardParserContext

interface TransactionLineParser {

    void parseLine(String line, ClipperCardParserContext context)

}