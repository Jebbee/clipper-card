package com.jj.clipper

import com.jj.clipper.card.CardNumberLineParser
import com.jj.clipper.card.CardNumberLineParserImpl
import com.jj.clipper.contact.ContactInfoLineParser
import com.jj.clipper.contact.ContactInfoLineParserImpl
import com.jj.clipper.transaction.TransactionLine
import com.jj.clipper.transaction.TransactionLineParser
import com.jj.clipper.transaction.TransactionLineParserImpl
import com.jj.pdf.PdfToTextService
import com.jj.pdf.PdfToTextServiceImpl

import static java.math.BigDecimal.ZERO

class ClipperCardParser {

    // TODO Spring inject delegate services
    private final PdfToTextService pdfToTextService = new PdfToTextServiceImpl()
    private final CardNumberLineParser cardNumberLineParser = new CardNumberLineParserImpl()
    private final TransactionLineParser transactionLineParser = new TransactionLineParserImpl()
    private final ContactInfoLineParser contactInfoLineParser = new ContactInfoLineParserImpl()

    // TODO Print Clipper card number (for reference when calling in)

    void parsePdfFile(final File pdfFile) {
        final String pdfText = pdfToTextService.toText(pdfFile)

        String customerServiceCenterPhoneNumber = '877-878-8883'

        final ClipperCardParserContext context = new ClipperCardParserContext()

        pdfText.eachLine { final String line ->
            if (cardNumberLineParser.isCardNumberLine(line)) {
                context.cardNumber = cardNumberLineParser.parse(line)
            } else if (transactionLineParser.isTransactionLine(line)) {
                final TransactionLine transactionLine = transactionLineParser.parse(line)

                if (isFirstTransactionLine(context.previousBalance)) {
                    context.previousBalance = transactionLine.balance
                    context.expectedBalance = transactionLine.balance
                } else {
                    context.expectedBalance = context.previousBalance + transactionLine.adjustmentAmount
                }

                if (transactionLine.balance != context.expectedBalance) {
                    final BigDecimal discrepancyAmount = context.expectedBalance - transactionLine.balance
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

                context.previousBalance = transactionLine.balance
            } else if (contactInfoLineParser.isContactInfoLine(line)) {
                customerServiceCenterPhoneNumber = contactInfoLineParser.parse(line)
            }
        }

        if (context.totalDiscrepancyAmount > ZERO) {
            println ''
            80.times { print '=' }
            println ''
            println "TOTAL DISCREPANCY AMOUNT: \$${context.totalDiscrepancyAmount}"
            println "Call $customerServiceCenterPhoneNumber"
            println "Card number: ${context.cardNumber}"
            80.times { print '=' }
            println ''
        }
    }

    private static boolean isFirstTransactionLine(final BigDecimal previousBalance) {
        return !previousBalance
    }

}
