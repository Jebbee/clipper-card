package com.jj.clipper

import static java.math.BigDecimal.ZERO

class ClipperCardParserContext {

    BigDecimal previousBalance
    BigDecimal expectedBalance
    BigDecimal totalDiscrepancyAmount = ZERO

}
