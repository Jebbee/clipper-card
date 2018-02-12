package com.jj.clipper.transaction

import com.jj.regex.RegexTextExtractor
import com.jj.regex.RegexTextExtractorImpl

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
 * or (note balance goes negative);
 *
 * <pre>
 * Dual-tag entry transaction, maximum fare deducted
 * (purse debit)
 * Zone 1 (GGT) 18N2/28/17   4:02 pm (8.06) 0.00  10.40 Clipper Cash
 * </pre>
 *
 * And for a credit transactions:
 *
 * <pre>
 * Dual-tag exit transaction, fare adjustment (purse rebate) Zone 1 (GGT) 18S11/27/17   7:57 am  59.04  1.00  0.00 Clipper Cash
 * </pre>
 *
 * And for re-loads:
 *
 * <pre>
 * Remote re-load of existing purse GGB #9481/5/17   7:42 am  221.19  125.00  0.00 Clipper Cash
 * </pre>
 *
 * or
 *
 * <pre>
 * Remote re-load of existing purse GOLDEN GATE FERRY SAN
 * FRANCISCO FERRY PL
 * 5/2/17   1:14 pm  191.29  150.00  0.00 Clipper Cash
 * </pre>
 *
 * Note that this handles the reports that come from Clipper's customer support.
 * The text parsed by PDFBox for these reports looks a lot different than the text in the PDF files,
 * with text out of order and text mashed together.
 */
class AlternateTransactionLineParserImpl implements TransactionLineParser {

    // TODO [high] Spring inject dependency
    private final RegexTextExtractor regexTextExtractor = new RegexTextExtractorImpl()

    boolean isTransactionLine(final String line) {
        return line ==~ /.*\d{1,2}:\d{2} (a|p)m\s+\(?\d+\.\d{2}\)?\s+\d+\.\d{2}\s+\d+\.\d{2} Clipper Cash$/
    }

    TransactionLine parse(final String line) {
        assert isTransactionLine(line)

        return new TransactionLine(
                balance: getBalance(line),
                adjustmentAmount: getAdjustmentAmount(line)
        )
    }

    private BigDecimal getAdjustmentAmount(final String line) {
        final boolean isDebit = isDebit(line)
        final List<String> listOfGroups = regexTextExtractor.extractGroups(line, ~/.*\s+\(?\d+\.\d{2}\)?\s+(\d+\.\d{2})\s+(\d+\.\d{2}) Clipper Cash$/, 2)
        final BigDecimal adjustmentAmount = listOfGroups[isDebit ? 2 : 1] as BigDecimal

        return isDebit ? adjustmentAmount.negate() : adjustmentAmount
    }

    private static boolean isDebit(final String line) {
        return line ==~ /.*\s+0.00\s+\d+.\d{2} Clipper Cash$/
    }

    private static BigDecimal getBalance(final String line) {
        // With grouping we get a multidimensional array
        final Matcher matcher = (line =~ /.*\s+(\(?)(\d+\.\d{2})(\)?)\s+\d+\.\d{2}\s+\d+\.\d{2} Clipper Cash$/)
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

        final boolean isNegative = (listOfGroups[1] == '(') && (listOfGroups[3] == ')')
        final BigDecimal balance = listOfGroups[2] as BigDecimal

        return isNegative ? balance.negate() : balance
    }

}
