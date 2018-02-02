package com.jj.clipper

import static java.math.BigDecimal.ZERO

class ClipperCardParserContext {

    String cardNumber
    String customerServicePhoneNumber
    BigDecimal previousBalance
    BigDecimal expectedBalance
    BigDecimal totalDiscrepancyAmount = ZERO

}
