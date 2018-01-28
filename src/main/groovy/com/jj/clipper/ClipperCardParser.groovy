package com.jj.clipper

import com.jj.clipper.transaction.TransactionLineParser
import com.jj.clipper.transaction.TransactionLineParserImpl
import com.jj.pdf.PdfToTextService
import com.jj.pdf.PdfToTextServiceImpl

import java.util.regex.Matcher

import static java.math.BigDecimal.ZERO

class ClipperCardParser {

    // TODO Spring inject services
    private final PdfToTextService pdfToTextService = new PdfToTextServiceImpl()
    private final TransactionLineParser transactionLineParser = new TransactionLineParserImpl()

    // TODO Print Clipper card number (for reference when calling in)

    void parsePdfFile(final File pdfFile) {
        final String pdfText = pdfToTextService.toText(pdfFile)

        String customerServiceCenterPhoneNumber = '877-878-8883'

        final ClipperCardParserContext context = new ClipperCardParserContext()

        pdfText.eachLine { final String line ->
            transactionLineParser.parseLine(line, context)

            if (isCustomerServiceCenterPhoneNumberLine(line)) {
                customerServiceCenterPhoneNumber = getCustomerServiceCenterPhoneNumber(line)
            }
        }

        if (context.totalDiscrepancyAmount > ZERO) {
            println ''
            80.times { print '=' }
            println ''
            println "TOTAL DISCREPANCY AMOUNT: \$${context.totalDiscrepancyAmount}"
            println "Call $customerServiceCenterPhoneNumber"
            80.times { print '=' }
            println ''
        }
    }

    private static boolean isCustomerServiceCenterPhoneNumberLine(final String line) {
        return line ==~ /.*Customer Service Center at \d{3}-\d{3}-\d{4}.*/
    }

    private static String getCustomerServiceCenterPhoneNumber(final String line) {
        final Matcher matcher = (line =~ /.*Customer Service Center at (\d{3}-\d{3}-\d{4}).*/)
//    assert matcher.hasGroup()
//    assert 1L == matcher.size()

        final List<String> listOfGroups = matcher[0] as List<String>

        return listOfGroups[1]
    }

}
