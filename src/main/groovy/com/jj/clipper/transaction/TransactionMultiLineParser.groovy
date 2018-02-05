package com.jj.clipper.transaction

interface TransactionMultiLineParser {

    boolean isTransaction(String... lines)

    TransactionLine parse(String... lines)

}