package com.jj.clipper.transaction

interface TransactionLineParser {

    boolean isTransactionLine(String line)

    TransactionLine parse(String line)

}