package com.infotrends.in.smartsave.orchestrator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.navigation.NavController;

import com.infotrends.in.smartsave.R;
import com.infotrends.in.smartsave.database.DBContract;
import com.infotrends.in.smartsave.database.DBHelper;
import com.infotrends.in.smartsave.models.UserModel;
import com.infotrends.in.smartsave.sqlFiles.LoginSqls;
import com.infotrends.in.smartsave.utils.AppProps;
import com.infotrends.in.smartsave.utils.IntegProperties;
import com.infotrends.in.smartsave.utils.LoadingBox;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class LoginOrc {
    private ConnectionOrchestrator connOrc = new ConnectionOrchestrator();
    private IntegProperties oInteg = IntegProperties.getInstance();
    DBHelper dbHelper = DBHelper.getInstance();
    private SQLiteDatabase mDatabase = dbHelper==null?null:dbHelper.getWritableDatabase();
    private AppProps oAppProps = AppProps.getInstance();
    private String ErrorString = "";
    private NavController  navController;

    public static Cursor getRecentLogin(Context context, SQLiteDatabase mDatabase) {
        return  mDatabase.query(
                DBContract.LoginInfo.TABLE_NAME, null, null,
                null, null, null, null
        );
    }

    public static UserModel validateLogin(Context context, SQLiteDatabase mDatabase) {
        UserModel userModel = new UserModel();
        Cursor mCursor = getRecentLogin(context, mDatabase);
        if((mCursor.moveToFirst()) || mCursor.getCount() !=0) {
            userModel = new UserModel(mCursor);
        }
        return userModel;
    }

    public boolean checkSignIn(Context context, SQLiteDatabase mDatabase) {

        boolean validate = false;
        Cursor cursor1 = getRecentLogin(context, mDatabase);
        if(cursor1!=null && ((cursor1.moveToFirst()) || cursor1.getCount() !=0)) {
            validate = true;
        }

        return validate;
    }

    public UserModel loginAction(UserModel oUserModel1) {
        JSONObject jsonReq =  jsonReqFormation("Login",oUserModel1);
        HashMap<String, Object> jsonResponse = new HashMap<String, Object>();
        jsonResponse = connOrc.executeMethod(oInteg.getString("login_url"), jsonReq);
        UserModel oUserModel = new UserModel();
        responseToUsrModMapping(oUserModel, jsonResponse);
        if(oUserModel.getStatusCode().equalsIgnoreCase("200")) {
            UserModel oUserModel2 = new UserModel();
            oUserModel2 = confirmLogin(oUserModel);
            if(oUserModel2.getStatusCode().equalsIgnoreCase("200")) {
                oUserModel.setActiveLogin("Y");
                oUserModel.setLoginTime(new SimpleDateFormat().format(new Date()));
                oUserModel.setPassword(oUserModel1.getPassword());
                insertLoginData(oUserModel);
            }
            else {
                return oUserModel2;
            }
        } else {
//            LoadingBox.getInstance().dismiss();
            if(oUserModel.getStatusCode().equalsIgnoreCase("100"))
                oUserModel.setStatusMessage("Error Connecting to the Server!");
            ErrorString=oUserModel.getStatusMessage();
//            Handler mHandler = AppProps.getInstance().getHandler();
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    AlertDialog.Builder alertDb = new AlertDialog.Builder(oAppProps.getContext());
//                    alertDb.setCancelable(true)
//                            .setMessage(ErrorString);
//                    try {
//                        AlertDialog alert = alertDb.create();
//                        alert.show();
//                        Thread.sleep(1000);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
            AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppProps.getInstance().getContext(), ErrorString, Toast.LENGTH_SHORT).show();
                }
            });


        }
        return oUserModel;
    }

    public UserModel confirmLogin(UserModel oUserModel1) {
        JSONObject jsonReq =  jsonReqFormation("confirmLogin",oUserModel1);
        HashMap<String, Object> jsonResponse = new HashMap<String, Object>();
        jsonResponse = connOrc.executeMethod(oInteg.getString("confirmLogin_url"), jsonReq);
        UserModel oUserModel = new UserModel();
        responseToUsrModMapping(oUserModel, jsonResponse);
        if(oUserModel.getStatusCode().equalsIgnoreCase("555")) {
            deleteLoginData(oUserModel);
        }
        return oUserModel;
    }

    public UserModel genLoginOTP(UserModel oUserModel1) {
        JSONObject jsonReq =  jsonReqFormation("sendOTP",oUserModel1);
        HashMap<String, Object> jsonResponse = new HashMap<String, Object>();
        jsonResponse = connOrc.executeMethod(oInteg.getString("genOtp_url"), jsonReq);
        UserModel oUserModel = new UserModel();
        responseToUsrModMapping(oUserModel, jsonResponse);
        if(oUserModel.getStatusCode().equalsIgnoreCase("555")) {
//            deleteLoginData(oUserModel);
        }
        return oUserModel;
    }

    public UserModel signUpAction(UserModel oUserModel) {
        JSONObject jsonReq =  jsonReqFormation("SignUp",oUserModel);
        HashMap<String, Object> jsonResponse = new HashMap<String, Object>();
        jsonResponse = connOrc.executeMethod(oInteg.getString("signUp_url"), jsonReq);
//        UserModel oUserModel = new UserModel();
        responseToUsrModMapping(oUserModel, jsonResponse);
        if(oUserModel.getStatusCode().equalsIgnoreCase("200")) {
            LoadingBox.getInstance().dismiss();
        } else if(oUserModel.getStatusCode().equalsIgnoreCase("201")) {

            final String phone_no_via_SignUp = oUserModel.getPhone_no();
            final String password_via_SignUp = oUserModel.getPassword();
            AlertDialog.Builder alertDb = new AlertDialog.Builder(oAppProps.getContext());
            alertDb.setCancelable(false)
                    .setMessage("Exit App?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle args = new Bundle();
                            args.putString("phone_no_via_SignUp", phone_no_via_SignUp);
                            args.putString("password_via_SignUp", password_via_SignUp);
                            navController = oAppProps.getNavController();
                            navController.navigateUp();
                            navController.navigate(R.id.action_HomeFragment_to_LoginFragment, args);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            AlertDialog alert = alertDb.create();
            alert.show();


        } else {
            LoadingBox.getInstance().dismiss();
            if(oUserModel.getStatusCode().equalsIgnoreCase("100"))
                oUserModel.setStatusMessage("Error Connecting to the Server!");
            ErrorString=oUserModel.getStatusMessage();
            AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppProps.getInstance().getContext(), ErrorString, Toast.LENGTH_SHORT).show();
                }
            });

        }
        return oUserModel;
    }


    public UserModel confirmNumber(UserModel oUserModel1) {
        JSONObject jsonReq =  jsonReqFormation("validateOTP",oUserModel1);
        HashMap<String, Object> jsonResponse = new HashMap<String, Object>();
        jsonResponse = connOrc.executeMethod(oInteg.getString("confirmNumber_url"), jsonReq);
        UserModel oUserModel = new UserModel();
        responseToUsrModMapping(oUserModel, jsonResponse);
        if(!oUserModel.getStatusCode().equalsIgnoreCase("200")) {
//            UserModel oUserModel2 = new UserModel();
//            oUserModel2 = confirmLogin(oUserModel);
//            if(oUserModel2.getStatusCode().equalsIgnoreCase("200")) {
//                oUserModel.setActiveLogin("Y");
//                oUserModel.setLoginTime(new SimpleDateFormat().format(new Date()));
//                insertLoginData(oUserModel);
//            }
//            else {
//                return oUserModel2;
//            }
//        } else {
            if(oUserModel.getStatusCode().equalsIgnoreCase("100"))
                oUserModel.setStatusMessage("Error Connecting to the Server!");
            ErrorString=oUserModel.getStatusMessage();
            AppProps.getInstance().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppProps.getInstance().getContext(), ErrorString, Toast.LENGTH_SHORT).show();
                }
            });


        }
        return oUserModel;
    }

    public JSONObject jsonReqFormation(String opType, UserModel oUserModel) {
        JSONObject jsonReq = new JSONObject();
        try {
//            jsonReq.put("opType", opType);
            switch (opType) {
                case "Login":
                    jsonReq.put("phone_no", oUserModel.getPhone_no());
                    jsonReq.put("password", oUserModel.getPassword());
                    jsonReq.put("auth_key", oUserModel.getAuthCode()!=null?oUserModel.getAuthCode():"");
                    break;
                case "confirmLogin":
                case "validateOTP":
                    jsonReq.put("phone_no", oUserModel.getPhone_no());
                    jsonReq.put("username", oUserModel.getUsername());
                    jsonReq.put("otp", oUserModel.getAuthCode());
                    break;
                case "getFilesList":
                    jsonReq.put("phone_no", oUserModel.getPhone_no());
                    jsonReq.put("username", oUserModel.getUsername());
                    jsonReq.put("otp", oUserModel.getAuthCode());
                    jsonReq.put("target_Dir", oUserModel.getCurrDir());
                    jsonReq.put("isFetchSharable", oUserModel.getIsSharedOrPubFile()!=null?oUserModel.getIsSharedOrPubFile():"N");
                    jsonReq.put("fileId", oAppProps.getCurrSharedFileID());
                    break;
                case "Logout":
                    jsonReq.put("userid", oUserModel.getUserId());
                    jsonReq.put("phone_no", oUserModel.getPhone_no());
                    jsonReq.put("username", oUserModel.getUsername());
                    jsonReq.put("otp", oUserModel.getAuthCode());
                    break;
                case "SignUp":
                    jsonReq.put("emailid", oUserModel.getEmailId());
                    jsonReq.put("phone_no", oUserModel.getPhone_no());
                    jsonReq.put("username", oUserModel.getUsername());
                    jsonReq.put("otp", oUserModel.getAuthCode());
                    jsonReq.put("password", oUserModel.getPassword());
                    jsonReq.put("country_code", oUserModel.getCountry_code());
                    jsonReq.put("occupation", oUserModel.getOccupation());
                    break;
                case "getFile":
                    jsonReq.put("phone_no", oUserModel.getPhone_no());
                    jsonReq.put("otp", oUserModel.getAuthCode());
                    jsonReq.put("file_name", oUserModel.getFileModel().getFileName());
                    jsonReq.put("isencode", "Y");
                    jsonReq.put("username", oUserModel.getUsername());
                    jsonReq.put("target_Dir", AppProps.getInstance().getCurrDir());
                    jsonReq.put("isFetchSharable", oUserModel.getIsSharedOrPubFile()!=null?oUserModel.getIsSharedOrPubFile():"N");
                    jsonReq.put("fileId", oAppProps.getCurrSharedFileID());
                    break;
                case "createFolder":
                    jsonReq.put("phone_no", oUserModel.getPhone_no());
                    jsonReq.put("otp", oUserModel.getAuthCode());
                    jsonReq.put("file_name", oUserModel.getFileModel().getFileName());
                    jsonReq.put("username", oUserModel.getUsername());
                    jsonReq.put("target_Dir", AppProps.getInstance().getCurrDir());
                    break;
                case "s3Connect":
                    jsonReq.put("phone_no", oUserModel.getPhone_no());
                    jsonReq.put("otp", oUserModel.getAuthCode());
                    jsonReq.put("file_name", oUserModel.getFileModel().getFileName());
                    jsonReq.put("isencode", "Y");
                    jsonReq.put("username", oUserModel.getUsername());
                    jsonReq.put("content", oUserModel.getFileModel().getFileContent());
                    jsonReq.put("emailid", oUserModel.getEmailId());
                    jsonReq.put("is_Image", oUserModel.getFileModel().getIsImage());
                    jsonReq.put("target_Dir", AppProps.getInstance().getCurrDir());
                    break;
                case "makePublic":
                    jsonReq.put("pubStat", oUserModel.getFileModel().getIsPublic());
                case "getSharableUrl":
                case "deleteFile":
                case "uploadFile":
                    jsonReq.put("phone_no", oUserModel.getPhone_no());
                    jsonReq.put("otp", oUserModel.getAuthCode());
                    jsonReq.put("file_name", oUserModel.getFileModel().getFileName());
                    jsonReq.put("username", oUserModel.getUsername());
                    jsonReq.put("target_Dir", AppProps.getInstance().getCurrDir());
                    break;
                case "updateSharedFileDtls":
                    jsonReq.put("phone_no", oUserModel.getPhone_no());
                    jsonReq.put("otp", oUserModel.getAuthCode());
                    jsonReq.put("file_name", oUserModel.getFileModel().getFileName());
                    jsonReq.put("username", oUserModel.getUsername());
                    jsonReq.put("target_Dir", AppProps.getInstance().getCurrDir());
                    jsonReq.put("shared_Folds", oUserModel.getFileModel().getSharedCont());
                    break;
                case "uploadFDtls":
                    jsonReq.put("phone_no", oUserModel.getPhone_no());
                    jsonReq.put("otp", oUserModel.getAuthCode());
                    jsonReq.put("file_name", oUserModel.getFileModel().getFileName());
                    jsonReq.put("username", oUserModel.getUsername());
                    jsonReq.put("emailid", oUserModel.getEmailId());
                    jsonReq.put("is_Image", oUserModel.getFileModel().getIsImage());
                    jsonReq.put("target_Dir", AppProps.getInstance().getCurrDir());
                    break;
                case "sendOTP":
                    jsonReq.put("phone_no", oUserModel.getPhone_no());
                    jsonReq.put("otptype", "smsLogin");
                    break;


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("The Request Generated is: " + jsonReq);
        return jsonReq;
    }

    public void responseToUsrModMapping(UserModel oUserModel, HashMap<String, Object> json) {
        try {
            if (json != null && json.size() > 0) {
                HashMap<String, Object> jsonResponse = (HashMap<String, Object>) json.get("body");
                if (jsonResponse != null && jsonResponse.size() > 0) {
                    if (jsonResponse.get("status") != null) {
                        oUserModel.setStatusCode(String.valueOf(jsonResponse.get("status")));
                    }
                    if (jsonResponse.get("id") != null) {
                        oUserModel.setUserId(String.valueOf(jsonResponse.get("id")));
                    }
                    if (jsonResponse.get("username") != null) {
                        oUserModel.setUsername((String) jsonResponse.get("username"));
                    }
                    if (jsonResponse.get("emailid") != null) {
                        oUserModel.setEmailId((String) jsonResponse.get("emailid"));
                    }
                    if (jsonResponse.get("occupation") != null) {
                        oUserModel.setOccupation((String) jsonResponse.get("occupation"));
                    }
                    if (jsonResponse.get("loginTime") != null) {
                        oUserModel.setLoginTime(String.valueOf(jsonResponse.get("loginTime") == null ? "" : jsonResponse.get("loginTime")));
                    }
                    if (jsonResponse.get("loginAttempt") != null) {
                        oUserModel.setLoginAttempts(String.valueOf(jsonResponse.get("loginAttempt")));
                    }
                    if (jsonResponse.get("phoneVerified") != null) {
                        oUserModel.setPhoneVerified((String) jsonResponse.get("phoneVerified"));
                    }
                    if (jsonResponse.get("active_login") != null) {
                        oUserModel.setActiveLogin((String) jsonResponse.get("active_login"));
                    }
                    if (jsonResponse.get("phoneNo") != null) {
                        oUserModel.setPhone_no(String.valueOf(jsonResponse.get("phoneNo")));
                    }
                    if (jsonResponse.get("password") != null) {
                        oUserModel.setPassword(String.valueOf(jsonResponse.get("password")));
                    }
                    if (jsonResponse.get("otp") != null) {
                        oUserModel.setAuthCode(String.valueOf(jsonResponse.get("otp")));
                    }
                    if (jsonResponse.get("error_msg") != null) {
                        oUserModel.setStatusMessage((String) jsonResponse.get("error_msg"));
                    }
                    if (jsonResponse.get("statusMsg") != null) {
                        oUserModel.setStatusMessage((String) jsonResponse.get("statusMsg"));
                    }
                } else {
                    oUserModel.setStatusCode("100");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertLoginData(UserModel oUserModel) {
//        ContentValues cv = new ContentValues();
//        cv.put(DBContract.LoginInfo.COLUMN_User_ID, oUserModel.getUserId());
//        cv.put(DBContract.LoginInfo.COLUMN_Name, oUserModel.getUsername());
//        cv.put(DBContract.LoginInfo.COLUMN_Email_ID, oUserModel.getEmailId());
//        cv.put(DBContract.LoginInfo.COLUMN_Phone_Number, oUserModel.getPhone_no());
//        cv.put(DBContract.LoginInfo.COLUMN_Occupation, oUserModel.getOccupation());
//        cv.put(DBContract.LoginInfo.COLUMN_Login_Time, oUserModel.getLoginTime()!=null?oUserModel.getLoginTime():"");
//        cv.put(DBContract.LoginInfo.COLUMN_Phone_Verified, oUserModel.getPhoneVerified());
//        cv.put(DBContract.LoginInfo.COLUMN_Login_Attempts, oUserModel.getLoginAttempts());
//        cv.put(DBContract.LoginInfo.COLUMN_AUTH_TOKEN, oUserModel.getAuthCode());
//        long val = mDatabase.insert(DBContract.LoginInfo.TABLE_NAME, null, cv);
//        return val;
        try {

            String delSQL = LoginSqls.deleteAllLoginData();
            mDatabase.execSQL(delSQL);
            String dbString = LoginSqls.insertLogin(oUserModel);
            mDatabase.execSQL(dbString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public UserModel signOutAction() {
        Cursor mCursor = LoginOrc.getRecentLogin(oAppProps.getContext(), mDatabase);
        UserModel oUserModel = new UserModel();
        if((mCursor.moveToFirst()) || mCursor.getCount() !=0) {
            oUserModel = new UserModel(mCursor);
        }
        JSONObject jsonReq =  jsonReqFormation("Logout",oUserModel);
        HashMap<String, Object> jsonResponse = new HashMap<String, Object>();
        jsonResponse = connOrc.executeMethod(oInteg.getString("logout_url"), jsonReq);
        responseToUsrModMapping(oUserModel, jsonResponse);
        deleteLoginData(oUserModel);
        return oUserModel;
    }

    public long deleteLoginData(UserModel oUserModel) {
        long val = mDatabase.delete(DBContract.LoginInfo.TABLE_NAME, null, null);
//        NavController navController = oAppProps.getNavController();
//        navController.navigateUp();
//        navController.navigate(R.id.action_HomeFragment_to_LoginFragment);
        return val;
    }
}
