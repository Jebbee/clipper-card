package com.jj.clipper

class ClipperCardParserContext {

    String cardNumber
    String customerServicePhoneNumber
    BigDecimal previousBalance
    BigDecimal expectedBalance
    List<BigDecimal> discrepancyAmounts = []
}
