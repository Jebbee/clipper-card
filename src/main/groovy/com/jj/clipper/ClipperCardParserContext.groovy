package com.jj.clipper

import static java.math.BigDecimal.ZERO

class ClipperCardParserContext {

    String cardNumber
    BigDecimal previousBalance
    BigDecimal expectedBalance
    BigDecimal totalDiscrepancyAmount = ZERO

}
