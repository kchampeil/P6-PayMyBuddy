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

    public static final String GET_USER_INFO_OK = "User information retrieved for : ";
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
    public static final String HOME_REQUEST_RECEIVED = "GET request on endpoint / received";

    public static final String USER_REGISTRATION_SHOW_PAGE_RECEIVED = "GET request on endpoint /registerUser received";
    public static final String USER_REGISTRATION_REQUEST_RECEIVED =
            "POST request on endpoint /registerUser received for (email): ";
    public static final String USER_REGISTRATION_REQUEST_OK = "New user has been saved with id: ";
    public static final String USER_REGISTRATION_REQUEST_KO = "New user has not been added";
    public static final String USER_REGISTRATION_REQUEST_NOT_VALID = "User information not valid";

    public static final String USER_LOGIN_REQUEST_RECEIVED = "POST request on endpoint /login received for (email): ";

    public static final String GET_RELATIONSHIP_REQUEST_RECEIVED = "GET request on endpoint /contact received";
    public static final String ADD_RELATIONSHIP_REQUEST_RECEIVED =
            "POST request on endpoint /contact received for (friend email): ";
    public static final String ADD_RELATIONSHIP_REQUEST_OK = "New relationship has been saved with id: ";
    public static final String ADD_RELATIONSHIP_REQUEST_KO = "New relationship has not been added";
    public static final String ADD_RELATIONSHIP_REQUEST_NOT_VALID = "Relationship information not valid";

    public static final String GET_BANK_ACCOUNT_REQUEST_RECEIVED = "GET request on endpoint /addBankAccount received";
    public static final String ADD_BANK_ACCOUNT_REQUEST_RECEIVED =
            "POST request on endpoint /addBankAccount received for (iban/name): ";
    public static final String ADD_BANK_ACCOUNT_REQUEST_OK = "New bank account has been saved with id: ";
    public static final String ADD_BANK_ACCOUNT_REQUEST_KO = "New bank account has not been added";
    public static final String ADD_BANK_ACCOUNT_REQUEST_NOT_VALID = "Bank account information not valid";

    public static final String GET_BANK_TRANSFER_REQUEST_RECEIVED = "GET request on endpoint /profile received";
    public static final String ADD_BANK_TRANSFER_REQUEST_RECEIVED =
            "POST request on endpoint /profile received for (bankAccountId/description/type/amount): ";
    public static final String ADD_BANK_TRANSFER_REQUEST_OK = "New bank transfer has been saved with id: ";
    public static final String ADD_BANK_TRANSFER_REQUEST_KO = "New bank transfer has not been added";
    public static final String ADD_BANK_TRANSFER_REQUEST_NOT_VALID = "Bank transfer information not valid";

    public static final String GET_TRANSACTION_REQUEST_RECEIVED = "GET request on endpoint /transfer received";
    public static final String ADD_TRANSACTION_REQUEST_RECEIVED =
            "POST request on endpoint /transfer received for (relationshipId/description/amount): ";
    public static final String ADD_TRANSACTION_REQUEST_OK = "New transaction has been saved with id: ";
    public static final String ADD_TRANSACTION_REQUEST_KO = "New transaction has not been added";
    public static final String ADD_TRANSACTION_REQUEST_NOT_VALID = "Transaction information not valid";

    public static final String CURRENT_USER_UNKNOWN = "Current user unknown";
}
