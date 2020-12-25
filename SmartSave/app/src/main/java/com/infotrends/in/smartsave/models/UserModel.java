package com.infotrends.in.smartsave.models;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.infotrends.in.smartsave.database.DBContract;

public class UserModel implements Parcelable, Cloneable {

    private String userId;
    private String username;
    private String emailId;
    private String occupation;
    private String authCode;
    private String phone_no;
    private String phoneVerified;
    private String loginAttempts;
    private String loginTime;
    private String activeLogin;
    private String country_code;

    private String password;
    private String statusCode;
    private String statusMessage;
    private String currDir;
    private String isSharedOrPubFile;

    public UserModel() {

    }

    public UserModel(Cursor in) {
        userId = in.getString(in.getColumnIndex(DBContract.LoginInfo.COLUMN_User_ID));
        username = in.getString(in.getColumnIndex(DBContract.LoginInfo.COLUMN_Name));
        emailId = in.getString(in.getColumnIndex(DBContract.LoginInfo.COLUMN_Email_ID));
        occupation = in.getString(in.getColumnIndex(DBContract.LoginInfo.COLUMN_Occupation));
        authCode = in.getString(in.getColumnIndex(DBContract.LoginInfo.COLUMN_AUTH_TOKEN));
        phone_no = in.getString(in.getColumnIndex(DBContract.LoginInfo.COLUMN_Phone_Number));
        phoneVerified = in.getString(in.getColumnIndex(DBContract.LoginInfo.COLUMN_Phone_Verified));
        loginAttempts = in.getString(in.getColumnIndex(DBContract.LoginInfo.COLUMN_Login_Attempts));
        loginTime = in.getString(in.getColumnIndex(DBContract.LoginInfo.COLUMN_Login_Time));
        password = in.getString(in.getColumnIndex(DBContract.LoginInfo.COLUMN_PASSWORD));
        activeLogin = "";

    }

    protected UserModel(Parcel in) {
        userId = in.readString();
        username = in.readString();
        emailId = in.readString();
        occupation = in.readString();
        authCode = in.readString();
        phone_no = in.readString();
        phoneVerified = in.readString();
        loginAttempts = in.readString();
        loginTime = in.readString();
        activeLogin = in.readString();
        password = in.readString();
        statusCode = in.readString();
        statusMessage = in.readString();
        country_code = in.readString();
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    public String getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(String phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public String getLoginAttempts() {
        return loginAttempts;
    }

    public void setLoginAttempts(String loginAttempts) {
        this.loginAttempts = loginAttempts;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getActiveLogin() {
        return activeLogin;
    }

    public void setActiveLogin(String activeLogin) {
        this.activeLogin = activeLogin;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    private FileModel fileModel = new FileModel();

    public FileModel getFileModel() {
        return fileModel;
    }

    public void setFileModel(FileModel fileModel) {
        this.fileModel = fileModel;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public String getCurrDir() {
        return currDir;
    }

    public void setCurrDir(String currDir) {
        this.currDir = currDir;
    }

    public String getIsSharedOrPubFile() {
        return isSharedOrPubFile;
    }

    public void setIsSharedOrPubFile(String isSharedOrPubFile) {
        this.isSharedOrPubFile = isSharedOrPubFile;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(username);
        dest.writeString(emailId);
        dest.writeString(occupation);
        dest.writeString(authCode);
        dest.writeString(phone_no);
        dest.writeString(phoneVerified);
        dest.writeString(loginAttempts);
        dest.writeString(loginTime);
        dest.writeString(activeLogin);
        dest.writeString(password);
        dest.writeString(statusCode);
        dest.writeString(statusMessage);
        dest.writeString(country_code);
    }
}
