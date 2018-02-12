package com.jj.clipper

import com.jj.clipper.card.CardNumberLineParser
import com.jj.clipper.card.CardNumberLineParserImpl
import com.jj.clipper.contact.ContactInfoLineParser
import com.jj.clipper.contact.ContactInfoLineParserImpl
import com.jj.clipper.transaction.AlternateTransactionLineParserImpl
import com.jj.clipper.transaction.TransactionLine
import com.jj.clipper.transaction.TransactionLineParser
import com.jj.clipper.transaction.TransactionLineParserImpl
import com.jj.pdf.PdfToTextService
import com.jj.pdf.PdfToTextServiceImpl
import groovy.util.logging.Slf4j

import static java.math.BigDecimal.ZERO

@Slf4j
class ClipperCardParser {

    // TODO Spring inject delegate services
    private final PdfToTextService pdfToTextService = new PdfToTextServiceImpl()
    private final CardNumberLineParser cardNumberLineParser = new CardNumberLineParserImpl()
    private final TransactionLineParser transactionLineParser = new TransactionLineParserImpl()
    private final TransactionLineParser alternateTransactionLineParser = new AlternateTransactionLineParserImpl()
    private final ContactInfoLineParser contactInfoLineParser = new ContactInfoLineParserImpl()

    void parsePdfFile(final File pdfFile, final BigDecimal discrepancyAmountLimit) {
        final String pdfText = pdfToTextService.toText(pdfFile)

        final ClipperCardParserContext context = new ClipperCardParserContext()

        pdfText.eachLine { final String line ->
            log.debug "line = $line"

            context.linesSinceLastTransaction.push(line)

            if (cardNumberLineParser.isCardNumberLine(line)) {
                context.cardNumber = cardNumberLineParser.parse(line)
            } else if (transactionLineParser.isTransactionLine(line)) {
                final TransactionLine transactionLine = transactionLineParser.parse(line)
                handleTransactionLine(context, transactionLine, line, discrepancyAmountLimit)
                context.linesSinceLastTransaction.clear()
            } else if (alternateTransactionLineParser.isTransactionLine(line)) {
                final TransactionLine transactionLine = alternateTransactionLineParser.parse(line)
                handleTransactionLine(context, transactionLine, line, discrepancyAmountLimit)
                context.linesSinceLastTransaction.clear()
            } else if (contactInfoLineParser.isContactInfoLine(line)) {
                context.customerServicePhoneNumber = contactInfoLineParser.parse(line)
                context.linesSinceLastTransaction.clear()
            }
        }

        final BigDecimal totalDiscrepancyAmount = context.discrepancyAmounts.sum() as BigDecimal

        if (totalDiscrepancyAmount != ZERO) {
            // TODO Add report date

            println ''
            80.times { print '=' }
            println ''
            println "TOTAL DISCREPANCY AMOUNT: \$${totalDiscrepancyAmount}"
            println "Call ${context.customerServicePhoneNumber}"
            println "Card Number: ${context.cardNumber}"
            80.times { print '=' }
            println ''
            context.discrepancyAmounts.each {
                log.debug "$it"
            }
        }
    }

    private static void handleTransactionLine(final ClipperCardParserContext context,
                                              final TransactionLine transactionLine,
                                              final String line,
                                              final BigDecimal discrepancyAmountLimit) {
        log.debug('')

        if (isFirstTransactionLine(context.previousBalance)) {
            context.previousBalance = transactionLine.balance
            context.expectedBalance = transactionLine.balance
        } else {
            context.expectedBalance = context.previousBalance + transactionLine.adjustmentAmount
        }

        if (transactionLine.balance != context.expectedBalance) {
            final BigDecimal discrepancyAmount = context.expectedBalance - transactionLine.balance

            // Deal with large discrepancies due to report errors (i.e. missing reloads)
            if (discrepancyAmount.abs() < discrepancyAmountLimit) {
                context.discrepancyAmounts << discrepancyAmount

                // TODO Add page number

                println ''
                80.times { print '-' }
                println ''
                println 'INVALID LINE:'
                println line
                println "Balance should be \$${context.expectedBalance}"
                println "Off by \$$discrepancyAmount"
                80.times { print '-' }
                println ''
            }
        }

        context.previousBalance = transactionLine.balance
    }

    private static boolean isFirstTransactionLine(final BigDecimal previousBalance) {
        return !previousBalance
    }

}
