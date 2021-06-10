package com.paymybuddy.webapp.testconstants;

import java.math.BigDecimal;

public class BankTransferTestConstants {
    public static final Long NEW_BANK_TRANSFER_ID = 100L;
    public static final BigDecimal NEW_BANK_TRANSFER_AMOUNT = BigDecimal.valueOf(500).setScale(2);
    public static final String NEW_BANK_TRANSFER_DESCRIPTION = "Transfert supplémentaire depuis compte principal Picsou";

    public static final String EXISTING_BANK_TRANSFER_DESCRIPTION = "Transfert depuis compte principal Picsou";
    public static final BigDecimal EXISTING_BANK_TRANSFER_AMOUNT = BigDecimal.valueOf(2021).setScale(2);
}
