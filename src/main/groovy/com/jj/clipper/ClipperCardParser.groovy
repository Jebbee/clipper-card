package com.jj.clipper

import com.jj.clipper.card.CardNumberLineParser
import com.jj.clipper.card.CardNumberLineParserImpl
import com.jj.clipper.contact.ContactInfoLineParser
import com.jj.clipper.contact.ContactInfoLineParserImpl
import com.jj.clipper.transaction.*
import com.jj.pdf.PdfToTextService
import com.jj.pdf.PdfToTextServiceImpl
import groovy.util.logging.Slf4j

import static java.math.BigDecimal.ZERO

@Slf4j
class ClipperCardParser {

    // TODO Spring inject delegate services
    private final PdfToTextService pdfToTextService = new PdfToTextServiceImpl()
    private final CardNumberLineParser cardNumberLineParser = new CardNumberLineParserImpl()
    private final TransactionLineParser singleTagLineParser = new SingleTagLineParserImpl()
    private final TransactionLineParser transactionLineParser = new TransactionLineParserImpl()
    private final TransactionMultiLineParser transactionMultiLineParser = new TransactionMultiLineParserImpl()
    private final ContactInfoLineParser contactInfoLineParser = new ContactInfoLineParserImpl()

    void parsePdfFile(final File pdfFile) {
        final String pdfText = pdfToTextService.toText(pdfFile)

        final ClipperCardParserContext context = new ClipperCardParserContext()

        pdfText.eachLine { final String line ->
            log.debug "line = $line"

            context.linesSinceLastTransaction.push(line)

            if (cardNumberLineParser.isCardNumberLine(line)) {
                context.cardNumber = cardNumberLineParser.parse(line)
                context.linesSinceLastTransaction.clear()
            } else if (singleTagLineParser.isTransactionLine(line)) {
                final TransactionLine transactionLine = singleTagLineParser.parse(line)
                handleTransactionLine(context, transactionLine, line)
                context.linesSinceLastTransaction.clear()
            } else if (transactionLineParser.isTransactionLine(line)) {
                final TransactionLine transactionLine = transactionLineParser.parse(line)
                handleTransactionLine(context, transactionLine, line)
                context.linesSinceLastTransaction.clear()
            } else if (transactionMultiLineParser.isTransaction(context.linesSinceLastTransaction as String[])) {
                final TransactionLine transactionLine = transactionMultiLineParser.parse(context.linesSinceLastTransaction as String[])
                handleTransactionLine(context, transactionLine, line)
                context.linesSinceLastTransaction.clear()
            } else if (contactInfoLineParser.isContactInfoLine(line)) {
                context.customerServicePhoneNumber = contactInfoLineParser.parse(line)
                context.linesSinceLastTransaction.clear()
            }
        }

        if (context.totalDiscrepancyAmount > ZERO) {
            // TODO Add report date

            println ''
            80.times { print '=' }
            println ''
            println "TOTAL DISCREPANCY AMOUNT: \$${context.totalDiscrepancyAmount}"
            println "Call ${context.customerServicePhoneNumber}"
            println "Card Number: ${context.cardNumber}"
            80.times { print '=' }
            println ''
        }
    }

    private static void handleTransactionLine(final ClipperCardParserContext context, final TransactionLine transactionLine, final String... lines) {
        log.debug('')

        if (isFirstTransactionLine(context.previousBalance)) {
            context.previousBalance = transactionLine.balance
            context.expectedBalance = transactionLine.balance
        } else {
            context.expectedBalance = context.previousBalance + transactionLine.adjustmentAmount
        }

        if (transactionLine.balance != context.expectedBalance) {
            final BigDecimal discrepancyAmount = context.expectedBalance - transactionLine.balance
            context.totalDiscrepancyAmount += discrepancyAmount

            // TODO Add page number

            println ''
            80.times { print '-' }
            println ''
            println 'INVALID LINES:'
            println lines
            println "Balance should be \$${context.expectedBalance}"
            println "Off by \$$discrepancyAmount"
            80.times { print '-' }
            println ''
        }

        context.previousBalance = transactionLine.balance
    }

    private static boolean isFirstTransactionLine(final BigDecimal previousBalance) {
        return !previousBalance
    }

}
