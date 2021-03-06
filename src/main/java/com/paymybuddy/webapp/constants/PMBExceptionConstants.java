package com.paymybuddy.webapp.constants;

public class PMBExceptionConstants {
    //Missing information
    public static final String MISSING_INFORMATION_NEW_USER = "All information must be specified for a new user";
    public static final String MISSING_INFORMATION_GETTING_USER = "A user email must be specified to get user information";

    public static final String MISSING_INFORMATION_NEW_BANK_ACCOUNT = "All information must be specified for a new bank account";
    public static final String MISSING_INFORMATION_LIST_BANK_ACCOUNT = "A user id must be specified to get his list of bank accounts";

    public static final String MISSING_INFORMATION_NEW_BANK_TRANSFER = "All information must be specified for a new bank transfer";
    public static final String MISSING_INFORMATION_LIST_BANK_TRANSFER = "A user id must be specified to get his list of bank transfers";

    public static final String MISSING_INFORMATION_NEW_RELATIONSHIP = "All information must be specified for a new relationship";
    public static final String MISSING_INFORMATION_LIST_RELATIONSHIP = "A user id must be specified to get his list of relationships";

    public static final String MISSING_INFORMATION_NEW_TRANSACTION = "All information must be specified for a new transaction";
    public static final String MISSING_INFORMATION_LIST_TRANSACTION = "A user id must be specified to get his list of transactions";

    //Invalid data
    public static final String INVALID_USER_EMAIL = "Invalid email";
    public static final String INVALID_FRIEND_EMAIL = "Email of friend equals to current user email";
    public static final String INVALID_IBAN = "Invalid IBAN";
    public static final String INVALID_BANK_TRANSFER_TYPE = "Invalid bank transfer type";

    //Already exists
    public static final String ALREADY_EXIST_USER = "One user already exists with this email";
    public static final String ALREADY_EXIST_BANK_ACCOUNT = "This bank account (IBAN) already exists for this user";
    public static final String ALREADY_EXIST_RELATIONSHIP = "This relationship already exists";

    //Does not exists
    public static final String DOES_NOT_EXISTS_USER = "User does not exist";
    public static final String DOES_NOT_EXISTS_BANK_ACCOUNT = "Bank account does not exist";
    public static final String DOES_NOT_EXISTS_RELATIONSHIP = "Relationship does not exist";

    //Insufficient balance
    public static final String INSUFFICIENT_BALANCE = "Insufficient balance for user to proceed to transfer/transaction";
}
