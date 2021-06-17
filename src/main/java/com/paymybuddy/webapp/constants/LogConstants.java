package com.paymybuddy.webapp.constants;

public class LogConstants {

    /* Service */
    public static final String CREATE_USER_OK = "User added with id: ";
    public static final String CREATE_USER_ERROR = "Error when saving user: ";

    public static final String CREATE_BANK_ACCOUNT_OK = "Bank account added with id: ";
    public static final String CREATE_BANK_ACCOUNT_ERROR = "Error when saving bank account: ";

    public static final String CREATE_BANK_TRANSFER_OK = "Bank transfer added with id: ";
    public static final String CREATE_BANK_TRANSFER_ERROR = "Error when saving bank transfer: ";

    public static final String CREATE_RELATIONSHIP_OK = "Relationship added with id: ";
    public static final String CREATE_RELATIONSHIP_ERROR = "Error when saving relationship: ";

    public static final String CREATE_TRANSACTION_OK = "Transaction added with id: ";
    public static final String CREATE_TRANSACTION_ERROR = "Error when saving transaction: ";

    public static final String GET_USER_INFO_OK = "User information retrieved (ID): ";
    public static final String GET_USER_INFO_ERROR = "Error when getting the user informations: ";

    public static final String LIST_BANK_ACCOUNT_OK = "List of bank accounts retrieved with () values: ";
    public static final String LIST_BANK_ACCOUNT_ERROR = "Error when getting the list of bank accounts: ";

    public static final String LIST_BANK_TRANSFER_OK = "List of bank transfers retrieved with () values: ";
    public static final String LIST_BANK_TRANSFER_ERROR = "Error when getting the list of bank transfers: ";

    public static final String LIST_RELATIONSHIP_OK = "List of relationships retrieved with () values: ";
    public static final String LIST_RELATIONSHIP_ERROR = "Error when getting the list of relationships: ";

    public static final String LIST_TRANSACTION_OK = "List of transactions retrieved with () values: ";
    public static final String LIST_TRANSACTION_ERROR = "Error when getting the list of transactions: ";

    /* Controller */
    public static final String USER_REGISTRATION_REQUEST_RECEIVED = "POST request on endpoint /registerUser received for (email): ";
    public static final String USER_REGISTRATION_REQUEST_OK = "New user has been saved with id: ";
    public static final String USER_REGISTRATION_REQUEST_KO = "New user has not been added";
    public static final String USER_REGISTRATION_REQUEST_NOT_VALID = "User information not valid";

    public static final String USER_PROFILE_REQUEST_RECEIVED = "GET request on endpoint /userProfile received for (ID): ";
    public static final String USER_LOGIN_REQUEST_RECEIVED = "POST request on endpoint /logUser received for (email): ";
}
