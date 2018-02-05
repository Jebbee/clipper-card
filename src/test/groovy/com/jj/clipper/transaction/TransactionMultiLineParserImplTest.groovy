package com.jj.clipper.transaction

import org.junit.Test

class TransactionMultiLineParserImplTest {

    private final TransactionMultiLineParser transactionMultiLineParser = new TransactionMultiLineParserImpl()

    // TODO Unit tests!!!

    @Test
    void testIsTransaction() {
        assert transactionMultiLineParser.isTransaction('(purse debit)')
    }

}
