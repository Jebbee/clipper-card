package com.jj.clipper.transaction

import java.util.regex.Matcher

class TransactionLineParserImpl implements TransactionLineParser {

    boolean isTransactionLine(final String line) {
        // Date (MM/dd/yyyy)
        // Time (HH:mm)
        // "AM" or "PM"
        // Space
        // One or more characters
        // Space
        // Either:
        //    "(purse debit)" or "(purse rebate)"
        //    or
        //    "re-load of existing purse"
        // One or more characters
        // Space
        // One or more digits + dot + two digits
        // Space
        // One or more digits + dot + two digits
        return line ==~ /\d{2}\/\d{2}\/\d{4} \d{2}:\d{2} (A|P)M .+ (\(purse (debit|rebate)\)|re-load of existing purse).+ \d+\.\d{2} \d+\.\d{2}/
    }

    TransactionLine parse(final String line) {
        assert isTransactionLine(line)

        final TransactionLine transactionLine = new TransactionLine()
        transactionLine.balance = getBalance(line)
        transactionLine.adjustmentAmount = getAdjustmentAmountWithProperSign(line)

        return transactionLine
    }

    private static BigDecimal getBalance(final String transactionLine) {
        return (transactionLine =~ /\d+\.\d{2}$/)[0] as BigDecimal
    }

    private static BigDecimal getAdjustmentAmountWithProperSign(String line) {
        BigDecimal adjustmentAmount = getAdjustmentAmount(line)

        if (isDebitLine(line)) {
            adjustmentAmount = adjustmentAmount.negate()
        }

        return adjustmentAmount
    }

    private static BigDecimal getAdjustmentAmount(final String transactionLine) {
        // With grouping we get a multidimensional array
        final Matcher matcher = (transactionLine =~ /(\d+\.\d{2}) (\d+\.\d{2})$/)
        assert matcher.hasGroup()
        assert 1L == matcher.size()

        /*
         * The first element of the matcher (matcher[0]) is the list of groups.
         * The first element of the list of groups (listOfGroups[0]) is both decimal strings.
         * The second element of the list of groups (listOfGroups[1]) is the first matched group.
         * The third element of the list of groups (listOfGroups[2]) is the second matched group.
         *
         * For example, matching the string "...Clipper Cash 1.00 38.24" with the above regex, we get:
         * listOfGroups = ["1.00 38.24", "1.00", "38.24"]
         */
        final List<String> listOfGroups = matcher[0] as List<String>

        return listOfGroups[1] as BigDecimal
    }

    private static boolean isDebitLine(final String transactionLine) {
        return transactionLine ==~ /.+ \(purse debit\) .+/
    }

}
