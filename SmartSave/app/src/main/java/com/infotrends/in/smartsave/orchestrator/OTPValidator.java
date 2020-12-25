package com.infotrends.in.smartsave.orchestrator;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.navigation.NavController;

import com.infotrends.in.smartsave.R;
import com.infotrends.in.smartsave.models.UserModel;
import com.infotrends.in.smartsave.utils.AppProps;
import com.infotrends.in.smartsave.utils.LoadingBox;

import java.util.regex.Pattern;

public class OTPValidator {
    private AppProps oAppProps = AppProps.getInstance();
    Context context = oAppProps.getContext();
    Activity mActivity = oAppProps.getActivity();
    private UserModel userModel = new UserModel();
    private LoginOrc oLoginOrc = new LoginOrc();
    private NavController navController = oAppProps.getNavController();
    private int devWidth = 0;
    private String mActionType = "";
    private Resources resources;
    private ColorStateList colorStateListGreen, colorStateListRed, colorStateListBlack;
    SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);

    public OTPValidator() {
        resources = mActivity.getResources();
        colorStateListGreen = ColorStateList.valueOf(resources.getColor(R.color.green));
        colorStateListRed = ColorStateList.valueOf(resources.getColor(R.color.red));
        colorStateListBlack = ColorStateList.valueOf(resources.getColor(R.color.black));
    }
    public void otpCheckMethod(String actionType, UserModel oUserModel) {

        mActionType = actionType;
        userModel = oUserModel;
        DisplayMetrics metrics = mActivity.getResources().getDisplayMetrics();
        devWidth = metrics.widthPixels;

//        LayoutInflater layoutInflater = LayoutInflater.from(context);
//        View promptView = layoutInflater.inflate(R.layout.otpview, null);
        try {
            final Dialog alertD = new Dialog(context);
            alertD.setCancelable(false);
            alertD.setCanceledOnTouchOutside(false);
            alertD.setContentView(R.layout.otpview);

            final EditText otpInp = (EditText) alertD.findViewById(R.id.otp_inp);
            Button btnOK = (Button) alertD.findViewById(R.id.otp_verify);
            Button btnNo = (Button) alertD.findViewById(R.id.otp_clear);

            btnOK.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    String otpStr = otpInp.getText().toString();
                    Pattern dPattern = Pattern.compile("\\d{4}");
                    if (!dPattern.matcher(otpStr).matches()) {
                        Toast.makeText(context, "Only Numeric 4 Digit OTPs are Accepted!", Toast.LENGTH_SHORT).show();
//                    alertD.dismiss();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            otpInp.setBackgroundTintList(colorStateListRed);
                        }
                        otpInp.requestFocus();
                        return;
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            otpInp.setBackgroundTintList(colorStateListGreen);
                        }
                    }

                    LoadingBox.createInstance(mActivity).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {


//                        userModel=new UserModel();
//                        userModel.setPhone_no(phone.getText().toString());
//                        userModel.setPassword(password.getText().toString());
                            userModel.setAuthCode(otpInp.getText().toString());

                            if (mActionType.equalsIgnoreCase("loginAction")) {
                                userModel = oLoginOrc.loginAction(userModel);
                            } else if (mActionType.equalsIgnoreCase("confirmNumber")) {
                                UserModel userModel1 = oLoginOrc.confirmNumber(userModel);
                                if (userModel1 != null && userModel1.getStatusCode() != null && userModel1.getStatusCode().equalsIgnoreCase("200")) {
                                    userModel.setAuthCode("");
                                    userModel = oLoginOrc.loginAction(userModel);
                                } else {
                                    return;
                                }
                            }
//
                            LoadingBox.getInstance().dismiss();
                            if (userModel.getStatusCode().equalsIgnoreCase("200")) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                mActivity.recreate();
//                                Bundle args = new Bundle();
//                                args.putParcelable("userModel", userModel);
                                        LoadingBox.getInstance().dismiss();
                                        alertD.dismiss();
                                        deleteKeys();
//                                        navController.navigateUp();
//                                        mActivity.recreate();
                                        Intent intent = mActivity.getIntent();
                                        mActivity.finish();
                                        mActivity.startActivity(intent);
                                    }
                                });
                            }
                        }
                    }).start();
                    ;


                }
            });

            btnNo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    otpInp.setText("");
                    alertD.dismiss();
                    deleteKeys();

                }
            });

//        alertD.setView(promptView);
            alertD.show();
            alertD.getWindow().setLayout((8 * devWidth / 9), LinearLayout.LayoutParams.WRAP_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteKeys() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(context.getString(R.string.saved_login_phNo));
        editor.remove(context.getString(R.string.saved_login_password));
        editor.remove(context.getString(R.string.saved_signup_phNo));
        editor.remove(context.getString(R.string.saved_signup_usrname));
        editor.remove(context.getString(R.string.saved_signup_password));
        editor.remove(context.getString(R.string.saved_ActionType));
        editor.apply();
    }

}

