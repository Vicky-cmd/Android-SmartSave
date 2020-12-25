package com.infotrends.in.smartsave.database;

import android.provider.BaseColumns;

public class DBContract {

    private DBContract() {

    }

    public static final class LoginInfo implements BaseColumns {
        public static final String TABLE_NAME = "logininfo";
        public static final String COLUMN_User_ID = "user_id";
        public static final String COLUMN_Name = "username";
        public static final String COLUMN_Email_ID = "email_id";
        public static final String COLUMN_Occupation = "occupation";
        public static final String COLUMN_Login_Time = "login_time";
        public static final String COLUMN_Login_Attempts = "login_attempts";
        public static final String COLUMN_Phone_Number = "phone_number";
        public static final String COLUMN_Phone_Verified = "phone_verified";
        public static final String COLUMN_AUTH_TOKEN = "auth_token";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_PASSWORD = "password";
    }


    public static final class NoGenerator implements BaseColumns {
        public static final String TABLE_NAME = "nogenerator";
        public static final String COLUMN_NO_TYPE = "notype";
        public static final String COLUMN_NO_GENERATED = "nogenerated";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}
