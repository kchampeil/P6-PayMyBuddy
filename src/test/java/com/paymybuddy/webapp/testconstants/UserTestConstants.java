package com.paymybuddy.webapp.testconstants;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class UserTestConstants {
    public static final Long NEW_USER_ID = 100L;
    public static final String NEW_USER_EMAIL = "Richie.Rich@pmb.com";
    public static final String NEW_USER_FIRSTNAME = "Richie";
    public static final String NEW_USER_LASTNAME = "RICH";
    public static final String NEW_USER_PASSWORD = "RR2021!";
    public static final BigDecimal NEW_USER_BALANCE = BigDecimal.valueOf(0);

    public static final String NEW_USER_INVALID_EMAIL = "Richie.Rich.at.pmb.com";

    public static final Long EXISTING_USER_ID = 1L;
    public static final String EXISTING_USER_EMAIL = "Balthazar.Picsou@pmb.com";
    public static final String EXISTING_USER_FIRSTNAME = "Balthazar";
    public static final String EXISTING_USER_LASTNAME = "PICSOU";
    public static final String EXISTING_USER_PASSWORD = "BP2021!";
    public static final BigDecimal EXISTING_USER_WITH_HIGH_BALANCE = BigDecimal.valueOf(10000).setScale(2,RoundingMode.HALF_UP);
    public static final BigDecimal EXISTING_USER_WITH_LOW_BALANCE = BigDecimal.valueOf(1).setScale(2,RoundingMode.HALF_UP);

    public static final Long UNKNOWN_USER_ID = 666L;
    public static final String UNKNOWN_USER_EMAIL = "john.doe@pmb.com";

    public static final Long EXISTING_USER_AS_FRIEND_ID = 2L;
    public static final String EXISTING_USER_AS_FRIEND_EMAIL = "Fifi.Picsou@pmb.com";
    public static final String EXISTING_USER_AS_FRIEND_FIRSTNAME = "Fifi";
    public static final String EXISTING_USER_AS_FRIEND_LASTNAME = "PICSOU";
    public static final String EXISTING_USER_AS_FRIEND_PASSWORD = "FP2021!";
    public static final BigDecimal EXISTING_USER_AS_FRIEND_BALANCE = BigDecimal.ZERO;

}
