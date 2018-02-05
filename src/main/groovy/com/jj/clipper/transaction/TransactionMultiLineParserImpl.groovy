package com.jj.clipper.transaction

import java.util.regex.Matcher

/**
 * Parses lines that appear (from Apache PDFBox) as follows for debit transactions:
 *
 * <pre>
 * Dual-tag entry transaction, maximum fare deducted
 * (purse debit)
 * Zone 3 (GGT) 18S11/27/17   7:03 am  58.04  0.00  6.60 Clipper Cash
 * </pre>
 *
 * And for a credit transactions:
 *
 * <pre>
 * Dual-tag exit transaction, fare adjustment (purse rebate) Zone 1 (GGT) 18S11/27/17   7:57 am  59.04  1.00  0.00 Clipper Cash
 * </pre>
 */
class TransactionMultiLineParserImpl implements TransactionMultiLineParser {

    // TODO [now] Handle credits

    boolean isTransaction(final String... lines) {
        return isDebit(lines) //|| isCredit(lines)
    }

    TransactionLine parse(final String... lines) {
        println "***************** TransactionMultiLineParserImpl"
        assert isDebit(lines)

        final String debitAmountLine = lines[lines.length - 1]

        final TransactionLine transactionLine = new TransactionLine()
        transactionLine.balance = getBalance(debitAmountLine)
        transactionLine.adjustmentAmount = getAdjustmentAmount(debitAmountLine)

        // TODO temp
        transactionLine.adjustmentAmount = transactionLine.adjustmentAmount.negate()

        return transactionLine
    }

    private static boolean isDebit(final String... lines) {
        final int linesLength = lines.length

        if (linesLength < 3) {
            return false
        }

        final boolean hasDebitAmountLine = isDebitAmountLine(lines[linesLength - 1])
        final boolean hasPurseDebitLine = isPurseDebitLine(lines[linesLength - 2])
        final boolean hasDualTagEntryLine = isDualTagEntryLine(lines[linesLength - 3])

        return hasDebitAmountLine && hasPurseDebitLine && hasDualTagEntryLine
    }

    private static BigDecimal getBalance(final String line) {
        // With grouping we get a multidimensional array
        final Matcher matcher = (line =~ /\s+(\d+\.\d{2})\s+\d+\.\d{2}\s+\d+\.\d{2} Clipper Cash/)
        assert matcher.hasGroup()
        assert 1L == matcher.size()

        // TODO [now] Fix comments (and in method below)

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

    private static BigDecimal getAdjustmentAmount(final String line) {
        // With grouping we get a multidimensional array
        final Matcher matcher = (line =~ /\s+\d+\.\d{2}\s+\d+\.\d{2}\s+(\d+\.\d{2}) Clipper Cash/)
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

    private static boolean isDebitAmountLine(final String line) {
        return line ==~ /Zone \d+ \(.+\) \d+[NSEW]\d{1,2}\/\d{1,2}\/\d{2}\s+\d{1,2}:\d{2} (a|p)m\s+\d+\.\d{2}\s+\d+\.\d{2}\s+\d+\.\d{2} Clipper Cash/
    }

    private static boolean isPurseDebitLine(final String line) {
        return line ==~ /\(purse debit\)/
    }

    private static boolean isDualTagEntryLine(final String line) {
        return line ==~ /Dual-tag entry transaction, maximum fare deducted\s*/
    }

    private static boolean isCredit(final String... lines) {
        return lines[0] ==~ /.+ fare adjustment \(purse debit\) .+/
    }

}
