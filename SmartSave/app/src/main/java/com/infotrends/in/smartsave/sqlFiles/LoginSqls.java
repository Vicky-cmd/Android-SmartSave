package com.infotrends.in.smartsave.sqlFiles;

import com.infotrends.in.smartsave.database.DBContract;
import com.infotrends.in.smartsave.models.UserModel;

public class LoginSqls {

    public static String deleteAllLoginData() {
        return "delete from " + DBContract.LoginInfo.TABLE_NAME;
    }

    public static String insertLogin(UserModel oUserModel) {
        return "insert into " + DBContract.LoginInfo.TABLE_NAME + "(" + DBContract.LoginInfo.COLUMN_User_ID + " , " + DBContract.LoginInfo.COLUMN_Name + " , " + DBContract.LoginInfo.COLUMN_Email_ID + " , "
                + DBContract.LoginInfo.COLUMN_Occupation + " , " + DBContract.LoginInfo.COLUMN_Login_Time + " , " + DBContract.LoginInfo.COLUMN_Phone_Number + " , " + DBContract.LoginInfo.COLUMN_Phone_Verified + " , "
                + DBContract.LoginInfo.COLUMN_Login_Attempts + " , " + DBContract.LoginInfo.COLUMN_AUTH_TOKEN + " , " + DBContract.LoginInfo.COLUMN_PASSWORD + ")"
                + " values ( \"" + oUserModel.getUserId() + "\", \"" + oUserModel.getUsername() + "\", \""  + oUserModel.getEmailId() + "\", \""  + oUserModel.getOccupation() + "\", \""
                + oUserModel.getLoginTime() + "\", \""  + oUserModel.getPhone_no() + "\", \""  + oUserModel.getPhoneVerified() + "\", \""  + oUserModel.getLoginAttempts() + "\", \""
                + oUserModel.getAuthCode() + "\", \""  + oUserModel.getPassword()  + "\")";
    }
}
