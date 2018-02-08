package com.jj.clipper.transaction

import com.jj.regex.RegexTextExtractor
import com.jj.regex.RegexTextExtractorImpl

import java.util.regex.Pattern

/**
 * Parses lines that appear (from Apache PDFBox) as follows for debit transactions:
 *
 * <pre>
 * Single-tag fare payment Embarcadero (Muni) NONE1/6/17   4:10 pm  204.74  0.00  2.25 Clipper Cash
 * </pre>
 *
 * or
 *
 * <pre>
 * Single-tag fare payment Church NONE1/6/17   4:52 pm  204.74  0.00  0.00 Clipper Cash
 * </pre>
 */
class SingleTagLineParserImpl implements TransactionLineParser {

    // TODO [high] Spring inject dependency
    private final RegexTextExtractor regexTextExtractor = new RegexTextExtractorImpl()

    boolean isTransactionLine(final String line) {
        // TODO [low] Could write a more specific regex
        return line ==~ /^Single-tag fare payment .+ Clipper Cash/
    }

    TransactionLine parse(final String line) {
        assert isTransactionLine(line)

        return new TransactionLine(
                balance: getBalance(line),
                adjustmentAmount: getAdjustmentAmount(line)
        )
    }

    private BigDecimal getBalance(final String line) {
        final Pattern pattern = ~/(\d+\.\d{2})\s+\d+\.\d{2}\s+\d+\.\d{2} Clipper Cash$/
        final List<String> listOfGroups = regexTextExtractor.extractGroups(line, pattern, 1)

        return listOfGroups[1] as BigDecimal
    }

    private BigDecimal getAdjustmentAmount(final String line) {
        final Pattern pattern = ~/(\d+\.\d{2}) Clipper Cash$/
        final List<String> listOfGroups = regexTextExtractor.extractGroups(line, pattern, 1)
        final BigDecimal adjustmentAmount = listOfGroups[1] as BigDecimal

        return adjustmentAmount.negate()
    }
}
