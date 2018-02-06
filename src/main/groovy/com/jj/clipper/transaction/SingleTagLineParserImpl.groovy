package com.jj.clipper.transaction

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

    boolean isTransactionLine(final String line) {
        return line ==~ /^Single-tag fare payment .+ Clipper Cash/
    }

    TransactionLine parse(final String line) {
        assert isTransactionLine(line)

        return new TransactionLine(
                balance: getBalance(line),
                adjustmentAmount: getAdjustmentAmount(line)
        )
    }

    private static BigDecimal getBalance(final String line) {
        null
    }

    private static BigDecimal getAdjustmentAmount(final String line) {
        null
    }
}
