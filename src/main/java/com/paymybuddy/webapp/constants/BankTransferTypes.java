package com.paymybuddy.webapp.constants;

/**
 * type de transfert bancaire vu du compte utilisateur
 * DEBIT : du compte utilisateur vers le compte bancaire
 * CREDIT : du compte bancaire vers le compte utilisateur
 */
public enum BankTransferTypes {
    DEBIT("From PMB to Bank"),
    CREDIT("From Bank to PMB");

    private final String displayValue;

    BankTransferTypes(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
