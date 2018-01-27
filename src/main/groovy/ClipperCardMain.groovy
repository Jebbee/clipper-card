import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

import java.util.regex.Matcher

import static java.math.BigDecimal.ZERO

class Globals {
    // TODO Set debug via command line argument
    static boolean DEBUG = false
}

// TODO Set path to PDF via command line argument
final File pdfFile = new File('/Users/jeff/Desktop/clipper-card-ridehistory-60-day.pdf')
final PDDocument pdfDocument = PDDocument.load(pdfFile)

final PDFTextStripper pdfTextStripper = new PDFTextStripper()
final String pdfText = pdfTextStripper.getText(pdfDocument)

String customerServiceCenterPhoneNumber = '877-878-8883'

BigDecimal previousBalance
BigDecimal expectedBalance
BigDecimal totalDiscrepancyAmount = ZERO

pdfText.eachLine { final String line ->
    debug "\nline = $line"

    if (isTransactionLine(line)) {
        final BigDecimal lineBalance = getBalance(line)
        debug "\tpreviousBalance = $previousBalance"

        if (isFirstTransactionLine(previousBalance)) {
            previousBalance = lineBalance
            expectedBalance = lineBalance
        } else {
            BigDecimal adjustmentAmount = getAdjustmentAmount(line)

            if (isDebitLine(line)) {
                adjustmentAmount = adjustmentAmount.negate()
            }

            // TODO assert isRebateOrReloadLine

            debug "\tadjustmentAmount = $adjustmentAmount"

            expectedBalance = previousBalance + adjustmentAmount
        }

        debug "\texpectedBalance = $expectedBalance"
        debug "\tlineBalance = $lineBalance"

        if (lineBalance != expectedBalance) {
            BigDecimal discrepancyAmount = expectedBalance - lineBalance
            totalDiscrepancyAmount += discrepancyAmount

            println ''
            80.times { print '-' }
            println ''
            println "INVALID LINE: $line"
            println "Balance should be \$$expectedBalance"
            println "Off by \$$discrepancyAmount"
            80.times { print '-' }
            println ''
        }

        previousBalance = lineBalance
    } else if (isCustomerServiceCenterPhoneNumberLine(line)) {
        customerServiceCenterPhoneNumber = getCustomerServiceCenterPhoneNumber(line)
    }
}

if (totalDiscrepancyAmount > ZERO) {
    println ''
    80.times { print '=' }
    println ''
    println "TOTAL DISCREPANCY AMOUNT: \$$totalDiscrepancyAmount"
    println "Call $customerServiceCenterPhoneNumber"
    80.times { print '=' }
    println ''
}

private static boolean isFirstTransactionLine(final BigDecimal previousBalance) {
    return !previousBalance
}

private static boolean isTransactionLine(final String line) {
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

private static boolean isDebitLine(final String transactionLine) {
    return transactionLine ==~ /.+ \(purse debit\) .+/
}

private static boolean isCustomerServiceCenterPhoneNumberLine(final String line) {
    return line ==~ /.*Customer Service Center at \d{3}-\d{3}-\d{4}.*/
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

private static String getCustomerServiceCenterPhoneNumber(final String line) {
    final Matcher matcher = (line =~ /.*Customer Service Center at (\d{3}-\d{3}-\d{4}).*/)
//    assert matcher.hasGroup()
//    assert 1L == matcher.size()

    final List<String> listOfGroups = matcher[0] as List<String>

    return listOfGroups[1]
}

private static void debug(final String message) {
    if (Globals.DEBUG) {
        println message
    }
}
