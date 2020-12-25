package com.infotrends.in.smartsave.ui.register;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.infotrends.in.smartsave.R;
import com.infotrends.in.smartsave.interaces.DrawerInterface;
import com.infotrends.in.smartsave.interaces.IOnBackPressed;
import com.infotrends.in.smartsave.models.UserModel;
import com.infotrends.in.smartsave.orchestrator.ConnectionOrchestrator;
import com.infotrends.in.smartsave.orchestrator.LoginOrc;
import com.infotrends.in.smartsave.orchestrator.OTPValidator;
import com.infotrends.in.smartsave.utils.LoadingBox;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginFragment extends Fragment implements IOnBackPressed {

    View root;
    Context context;
    Activity mActivity;
    AppCompatActivity appCompatActivity;
    DrawerInterface drInterface;
    private NavController navController;
    private Resources resources;
    private NavHostFragment navHostFragment;
    private ColorStateList colorStateListGreen, colorStateListRed, colorStateListBlack;
    private ConnectionOrchestrator connOrc = new ConnectionOrchestrator();
    private LoginOrc oLoginOrc = new LoginOrc();
    private UserModel userModel = new UserModel();
    private int devWidth = 0;

    EditText phone, password;
    Button loginbtn, signupbtn;

    private OTPValidator otpValidator;
    SharedPreferences sharedPref;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_login, container, false);
        context = getActivity();
        mActivity = getActivity();
        resources = getResources();
        appCompatActivity = (AppCompatActivity) getActivity();

        navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        try {
            drInterface = (DrawerInterface) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement MyInterface");
        }

        oLoginOrc = new LoginOrc();
        otpValidator = new OTPValidator();
        phone = root.findViewById(R.id.inp_phNo);
        password = root.findViewById(R.id.inp_Password);
        loginbtn = root.findViewById(R.id.loginbtn);
        signupbtn = root.findViewById(R.id.signupbtn);

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAction();
            }
        });


        colorStateListGreen = ColorStateList.valueOf(resources.getColor(R.color.green));
        colorStateListRed = ColorStateList.valueOf(resources.getColor(R.color.red));
        colorStateListBlack = ColorStateList.valueOf(resources.getColor(R.color.black));
        phone.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode()==event.KEYCODE_ENTER) {
                    if(validatePhoneNumber()) {
                        password.requestFocus();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            phone.setBackgroundTintList(colorStateListGreen);
                        }
                    } else {
                        phone.requestFocus();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            phone.setBackgroundTintList(colorStateListRed);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(validatePhoneNumber()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        phone.setBackgroundTintList(colorStateListGreen);
                    }
                } else if (phone.getText().length()==0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        phone.setBackgroundTintList(colorStateListBlack);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        phone.setBackgroundTintList(colorStateListRed);
                    }
                }
            }
        });

        password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode()==event.KEYCODE_ENTER) {
                    if(validatePassword()) {
                        hideKeyboard();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            password.setBackgroundTintList(colorStateListGreen);
                        }
                    } else {
                        password.requestFocus();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            password.setBackgroundTintList(colorStateListRed);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(validatePhoneNumber()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        password.setBackgroundTintList(colorStateListGreen);
                    }
                } else if (password.getText().length()==0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        password.setBackgroundTintList(colorStateListBlack);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        password.setBackgroundTintList(colorStateListRed);
                    }
                }
            }
        });

        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_LoginFragment_to_SignUFragment);
            }
        });


        DisplayMetrics metrics = getResources().getDisplayMetrics();
        devWidth = metrics.widthPixels;

        sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);

        String actionType = sharedPref.getString(getString(R.string.saved_ActionType), "0");

        if(actionType!=null && actionType.equalsIgnoreCase("1")) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        userModel = new UserModel();
                        String phNo = sharedPref.getString(getString(R.string.saved_login_phNo), "");
                        String pwd = sharedPref.getString(getString(R.string.saved_login_password), "");
                        userModel.setPhone_no(phNo);
                        userModel.setPassword(pwd);
                        if(phNo!=null && !phNo.equalsIgnoreCase("") && pwd!=null && !pwd.equalsIgnoreCase("")) {
                            otpValidator.otpCheckMethod("loginAction", userModel);
                        }
                    }
                });
        }

        if(getArguments()!=null) {

            if(getArguments().getString("phone_no_via_SignUp")!=null
                    && !getArguments().getString("phone_no_via_SignUp").trim().toString().isEmpty()
                    && getArguments().getString("password_via_SignUp")!=null
                    && !getArguments().getString("password_via_SignUp").trim().toString().isEmpty()) {
                phone.setText(getArguments().getString("phone_no_via_SignUp").trim().toString());
                password.setText(getArguments().getString("password_via_SignUp").trim().toString());
                loginAction();
            }
        }

        return root;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        appCompatActivity.getSupportActionBar().show();
        drInterface.unlockDrawer();
    }

    @Override
    public void onResume() {
        appCompatActivity.getSupportActionBar().hide();
        drInterface.lockDrawer();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onBackPressed() {
        AlertDialog.Builder alertDb = new AlertDialog.Builder(context);
        alertDb.setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alert = alertDb.create();
        alert.show();
        return false;
    }


    public void loginAction() {
        if(!(validatePhoneNumber() && validatePassword())) {
            return;
        }
        userModel=new UserModel();
        userModel.setPhone_no(phone.getText().toString());
        userModel.setPassword(password.getText().toString());
        LoadingBox.createInstance(mActivity).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                userModel = oLoginOrc.loginAction(userModel);
                try {
                    if (userModel.getStatusCode().equalsIgnoreCase("200")) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                mActivity.recreate();
//                                Bundle args = new Bundle();
//                                args.putParcelable("userModel", userModel);
                                LoadingBox.getInstance().dismiss();
                                navController.navigateUp();
                                mActivity.recreate();
                            }
                        });

//                        Intent intent = new Intent();
//                        mActivity.finish();
//                        startActivity(intent);
//                        Bundle args = new Bundle();
//                        args.putParcelable("userModel", userModel);
//                        navController.navigate(R.id.action_LoginFragment_to_HomeFragment, args);

                    } else if (userModel.getStatusCode().equalsIgnoreCase("201")) {
                        LoadingBox.getInstance().dismiss();
                        userModel = new UserModel();
                        userModel.setPhone_no(phone.getText().toString());
                        userModel.setPassword(password.getText().toString());
//                        Bundle args = new Bundle();
//                        args.putParcelable("userModel", userModel);
//                        navController.navigate(R.id.action_LoginFragment_to_OTPFragment, args);

                        mActivity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                saveLoginData(userModel);
                                otpValidator.otpCheckMethod("loginAction", userModel);
                            }
                        });

                    } else {
                        LoadingBox.getInstance().dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();



    }


    private void saveLoginData(UserModel userModel){
        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_ActionType), "1");
        editor.putString(getString(R.string.saved_login_phNo), userModel.getPhone_no());
        editor.putString(getString(R.string.saved_login_password), userModel.getPassword());
        editor.apply();
    }

    private boolean validatePhoneNumber() {
        String phNo = phone.getText().toString();
        if(phNo==null || phNo.length()==0) {
            Toast.makeText(context, "Please Enter A Phone Number", Toast.LENGTH_SHORT).show();
            return false;
        }
        Pattern noPattern = Pattern.compile("^\\d{10}$");
        Matcher matcher = noPattern.matcher(phNo);
        if(phNo.length()!=10 || !matcher.matches()) {
            Toast.makeText(context, "Please Enter A Valid Phone Number", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private boolean validatePassword() {
        String pass = password.getText().toString();
        if(pass==null || pass.length()==0) {
            Toast.makeText(context, "Password Cannot be blank", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(pass.length()<3) {
            Toast.makeText(context, "The Pasword Must Atleast contain three characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = mActivity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(mActivity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
//