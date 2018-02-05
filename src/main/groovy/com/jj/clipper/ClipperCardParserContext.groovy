package com.jj.clipper

import static java.math.BigDecimal.ZERO

class ClipperCardParserContext {

    /**
     * Stack of lines, since last transaction, where the top of the stack is the last line
     */
    List<String> linesSinceLastTransaction = []

    String cardNumber
    String customerServicePhoneNumber
    BigDecimal previousBalance
    BigDecimal expectedBalance
    BigDecimal totalDiscrepancyAmount = ZERO

}
