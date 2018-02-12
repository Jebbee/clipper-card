package com.jj.clipper.transaction

import com.jj.regex.RegexTextExtractor
import com.jj.regex.RegexTextExtractorImpl

/**
 * Parses lines that appear as follows for debit transactions:
 *
 * <pre>
 * 11/27/2017 07:03 AM Dual-tag entry transaction, maximum fare deducted (purse debit) Zone 3 (GGT) 18S Clipper Cash 6.60 58.04
 * </pre>
 *
 * And for a credit transactions:
 *
 * <pre>
 * 11/27/2017 07:57 AM Dual-tag exit transaction, fare adjustment (purse rebate) Zone 1 (GGT) 18S Clipper Cash 1.00 59.04
 * </pre>
 *
 * And for re-load:
 * <pre>
 * 12/14/2017 04:03 PM Remote re-load of existing purse GGB #972 Clipper Cash 125.00 132.84
 * </pre>
 *
 * Note that this handles the standard, downloadable reports.
 * The text parsed by PDFBox for these reports looks a lot like the text in the PDF files, just with some whitespace removed.
 */
class TransactionLineParserImpl implements TransactionLineParser {

    // TODO [high] Spring inject dependency
    private final RegexTextExtractor regexTextExtractor = new RegexTextExtractorImpl()

    boolean isTransactionLine(final String line) {
        return line ==~ /.* Clipper Cash \d+\.\d{2} \d+\.\d{2}$/
    }

    TransactionLine parse(final String line) {
        assert isTransactionLine(line)

        return new TransactionLine(
                balance: getBalance(line),
                adjustmentAmount: getAdjustmentAmount(line)
        )
    }

    private BigDecimal getAdjustmentAmount(final String line) {
        final List<String> listOfGroups = regexTextExtractor.extractGroups(line, ~/Clipper Cash (\d+\.\d{2}) (\d+\.\d{2})$/, 2)
        final BigDecimal adjustmentAmount = listOfGroups[1] as BigDecimal

        return isDebit(line) ? adjustmentAmount.negate() : adjustmentAmount
    }

    private static boolean isDebit(final String line) {
        return line ==~ /.+ \(purse debit\) .+/
    }

    private static BigDecimal getBalance(final String line) {
        return (line =~ /\d+\.\d{2}$/)[0] as BigDecimal
    }

}
