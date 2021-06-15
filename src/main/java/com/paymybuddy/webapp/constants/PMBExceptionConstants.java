package com.paymybuddy.webapp.constants;

public class PMBExceptionConstants {
    //Missing information
    public static final String MISSING_INFORMATION_NEW_USER = "All information must be specified for a new user";

    public static final String MISSING_INFORMATION_NEW_BANK_ACCOUNT = "All information must be specified for a new bank account";
    public static final String MISSING_INFORMATION_LIST_BANK_ACCOUNT = "A user id must be specified to get his list of bank accounts";

    public static final String MISSING_INFORMATION_NEW_BANK_TRANSFER = "All information must be specified for a new bank transfer";
    public static final String MISSING_INFORMATION_LIST_BANK_TRANSFER = "A user id must be specified to get his list of bank transfers";

    public static final String MISSING_INFORMATION_NEW_RELATIONSHIP = "All information must be specified for a new relationship";
    public static final String MISSING_INFORMATION_LIST_RELATIONSHIP = "A user id must be specified to get his list of relationships";

    public static final String MISSING_INFORMATION_NEW_TRANSACTION = "All information must be specified for a new transaction";
    public static final String MISSING_INFORMATION_LIST_TRANSACTION = "A user id must be specified to get his list of transactions";

    //Invalid data
    public static final String INVALID_EMAIL = "Invalid email for user: ";
    public static final String INVALID_IBAN = "Invalid IBAN: ";
    public static final String INVALID_BANK_TRANSFER_TYPE = "Invalid bank transfer type: ";

    //Already exists
    public static final String ALREADY_EXIST_USER = "One user already exists with email: ";
    public static final String ALREADY_EXIST_BANK_ACCOUNT = "This bank account (IBAN) already exists for user (ID): ";
    public static final String ALREADY_EXIST_RELATIONSHIP = "This relationship already exists for users (IDs): ";

    //Does not exists
    public static final String DOES_NOT_EXISTS_USER = "No user not exists for: ";
    public static final String DOES_NOT_EXISTS_BANK_ACCOUNT = "No bank account exists for: ";
    public static final String DOES_NOT_EXISTS_RELATIONSHIP = "No relationship exists for: ";

    //Insufficient balance
    public static final String INSUFFICIENT_BALANCE = "Insufficient balance for user (ID): ";
}
