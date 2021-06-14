package com.paymybuddy.webapp.testconstants;

import com.paymybuddy.webapp.constants.TransactionConstants;

import java.math.BigDecimal;

public class TransactionTestConstants {
    public static final Long NEW_TRANSACTION_ID = 100L;
    public static final BigDecimal NEW_TRANSACTION_AMOUNT_FEE_EXCLUDED = BigDecimal.valueOf(800).setScale(2);
    public static final BigDecimal NEW_TRANSACTION_FEE_AMOUNT
            = NEW_TRANSACTION_AMOUNT_FEE_EXCLUDED.multiply(TransactionConstants.FEE_PERCENTAGE.divide(BigDecimal.valueOf(100))).setScale(2);
    public static final String NEW_TRANSACTION_DESCRIPTION = "Nouveau transfert de Balthazar vers Fifi";

    public static final String EXISTING_TRANSACTION_DESCRIPTION = "Transfert depuis compte principal Picsou";
    public static final BigDecimal EXISTING_TRANSACTION_AMOUNT = BigDecimal.valueOf(2021).setScale(2);
}
