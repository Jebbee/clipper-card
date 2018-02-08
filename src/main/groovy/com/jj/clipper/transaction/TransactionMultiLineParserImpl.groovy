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
 *
 * Note that this handles the reports that come from Clipper's customer support.
 * The text parsed by PDFBox for these reports looks a lot different than the text in the PDF files,
 * with text out of order and text mashed together.
 */
class TransactionMultiLineParserImpl implements TransactionMultiLineParser {

    boolean isTransaction(final String... lines) {
        return isDebit(lines) || isCredit(lines)
    }

    TransactionLine parse(final String... lines) {
        final boolean isDebit = isDebit(lines)
        final boolean isCredit = isCredit(lines)

        assert isDebit || isCredit

        final String transactionAmountLine = lines[lines.length - 1]

        return new TransactionLine(
                balance: getBalance(transactionAmountLine),
                adjustmentAmount: getAdjustmentAmount(transactionAmountLine, isDebit)
        )
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

    private static boolean isCredit(final String... lines) {
        return lines[0] ==~ /.+ fare adjustment \(purse rebate\) .+/
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
         *
         * For example, matching the string "...Clipper Cash 1.00 38.24" with the above regex, we get:
         * listOfGroups = ["1.00 38.24", "1.00", "38.24"]
         */
        final List<String> listOfGroups = matcher[0] as List<String>

        return listOfGroups[1] as BigDecimal
    }

    private static BigDecimal getAdjustmentAmount(final String line, final boolean isDebit) {
        // With grouping we get a multidimensional array
        final Matcher matcher = (line =~ /\s+\d+\.\d{2}\s+(\d+\.\d{2})\s+(\d+\.\d{2}) Clipper Cash/)
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

        final BigDecimal adjustmentAmount = listOfGroups[isDebit ? 2 : 1] as BigDecimal

        return isDebit ? adjustmentAmount.negate() : adjustmentAmount
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

}
