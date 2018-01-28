package com.jj.clipper.transaction

import com.jj.clipper.ClipperCardParserContext

import java.util.regex.Matcher

class TransactionLineParserImpl implements TransactionLineParser {

    void parseLine(final String line, final ClipperCardParserContext context) {
        if (isTransactionLine(line)) {
            final BigDecimal lineBalance = getBalance(line)

            if (isFirstTransactionLine(context.previousBalance)) {
                context.previousBalance = lineBalance
                context.expectedBalance = lineBalance
            } else {
                BigDecimal adjustmentAmount = getAdjustmentAmount(line)

                if (isDebitLine(line)) {
                    adjustmentAmount = adjustmentAmount.negate()
                }

                // TODO assert isRebateOrReloadLine

                context.expectedBalance = context.previousBalance + adjustmentAmount
            }

            if (lineBalance != context.expectedBalance) {
                final BigDecimal discrepancyAmount = context.expectedBalance - lineBalance
                context.totalDiscrepancyAmount += discrepancyAmount

                println ''
                80.times { print '-' }
                println ''
                println "INVALID LINE: $line"
                println "Balance should be \$${context.expectedBalance}"
                println "Off by \$$discrepancyAmount"
                80.times { print '-' }
                println ''
            }

            context.previousBalance = lineBalance
        }
    }

    static boolean isTransactionLine(final String line) {
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

    private static boolean isFirstTransactionLine(final BigDecimal previousBalance) {
        return !previousBalance
    }

    private static boolean isDebitLine(final String transactionLine) {
        return transactionLine ==~ /.+ \(purse debit\) .+/
    }

    private static BigDecimal getBalance(final String transactionLine) {
        return (transactionLine =~ /\d+\.\d{2}$/)[0] as BigDecimal
    }

    private static BigDecimal getAdjustmentAmount(final String transactionLine) {
        // With grouping we get a multidimensional array
        final Matcher matcher = (transactionLine =~ /(\d+\.\d{2}) (\d+\.\d{2})$/)
//    assert matcher.hasGroup()
//    assert 1L == matcher.size()

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

}
