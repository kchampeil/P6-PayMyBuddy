package com.paymybuddy.webapp.constants;

public class PMBExceptionConstants {
    //Missing information
    public static final String MISSING_INFORMATION_NEW_USER = "All information must be specified for a new user";
    public static final String MISSING_INFORMATION_NEW_BANK_ACCOUNT = "All information must be specified for a new bank account";
    public static final String MISSING_INFORMATION_LIST_BANK_ACCOUNT = "A user id must be specified to get his list of bank accounts";

    //Invalid data
    public static final String INVALID_EMAIL = "Invalid email for user: ";
    public static final String INVALID_IBAN = "Invalid IBAN: ";

    //Already exists
    public static final String ALREADY_EXIST_USER = "There is already one user with email: ";
    public static final String ALREADY_EXIST_BANK_ACCOUNT = "This bank account (IBAN) already exists for user (ID): ";

    //Does not exists
    public static final String DOES_NOT_EXISTS_USER = "No user not exists for: ";

}
