package com.infotrends.in.smartsave.ui.register;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.infotrends.in.smartsave.orchestrator.LoginOrc;
import com.infotrends.in.smartsave.orchestrator.OTPValidator;
import com.infotrends.in.smartsave.utils.LoadingBox;

public class SignUpFragment extends Fragment implements IOnBackPressed {

    View root;
    Context context;
    Activity mActivity;
    AppCompatActivity appCompatActivity;
    DrawerInterface drInterface;
    private NavController navController;
    private NavHostFragment navHostFragment;
    private Resources resources;
    private Spinner spinner;
    private ImageView errPopUp;
    private ColorStateList colorStateListGreen, colorStateListRed, colorStateListBlack;
    private UserModel userModel = new UserModel();
    private LoginOrc oLoginOrc = new LoginOrc();


    EditText email, password, phone, password_re, username, countryCode;
    Button loginbtn, signupbtn;
    private OTPValidator otpValidator;
    SharedPreferences sharedPref;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_signup, container, false);
        context = getActivity();
        mActivity = getActivity();
        resources = getResources();
        appCompatActivity = (AppCompatActivity) getActivity();

        navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        spinner = (Spinner) root.findViewById(R.id.inp_occupation);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.occ_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        try {
            drInterface = (DrawerInterface) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement MyInterface");
        }

        oLoginOrc = new LoginOrc();
        otpValidator = new OTPValidator();

        email = root.findViewById(R.id.inp_emailId);
        password = root.findViewById(R.id.inp_Password);
        phone = root.findViewById(R.id.inp_phNum);
        password_re = root.findViewById(R.id.inp_Passwordconf);
        username = root.findViewById(R.id.inp_name);
        errPopUp = root.findViewById(R.id.errPopUp);
        loginbtn = root.findViewById(R.id.loginbtn);
        signupbtn = root.findViewById(R.id.signupbtn);
        countryCode = root.findViewById(R.id.inp_country_code);

        loginbtn.setEnabled(false);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_SignUFragment_to_LoginFragment);
            }
        });

        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpAction();
            }
        });


        colorStateListGreen = ColorStateList.valueOf(resources.getColor(R.color.green));
        colorStateListRed = ColorStateList.valueOf(resources.getColor(R.color.red));
        colorStateListBlack = ColorStateList.valueOf(resources.getColor(R.color.black));


        username.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode()==event.KEYCODE_ENTER) {
                    if(validateUsername()) {
                        phone.requestFocus();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            username.setBackgroundTintList(colorStateListGreen);
                        }
                    } else {
                        username.requestFocus();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            username.setBackgroundTintList(colorStateListRed);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(validateUsername()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        username.setBackgroundTintList(colorStateListGreen);
                    }
                } else if (username.getText().length()==0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        username.setBackgroundTintList(colorStateListBlack);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        username.setBackgroundTintList(colorStateListRed);
                    }
                }
            }
        });

        phone.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode()==event.KEYCODE_ENTER) {
                    if(validatePhoneNumber()) {
                        email.requestFocus();
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

        email.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode()==event.KEYCODE_ENTER) {
                    if(validateEmail()) {
                        password.requestFocus();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            email.setBackgroundTintList(colorStateListGreen);
                        }
                    } else {
                        email.requestFocus();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            email.setBackgroundTintList(colorStateListRed);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(validateEmail()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        email.setBackgroundTintList(colorStateListGreen);
                    }
                } else if (email.getText().length()==0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        email.setBackgroundTintList(colorStateListBlack);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        email.setBackgroundTintList(colorStateListRed);
                    }
                }
            }
        });

        password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode()==event.KEYCODE_ENTER) {
                    if(validatePassword()) {
                        password_re.requestFocus();
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
                if(validatePassword()) {
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

        password_re.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode()==event.KEYCODE_ENTER) {
                    if(confirmPassword()) {
                        spinner.requestFocus();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            password_re.setBackgroundTintList(colorStateListGreen);
                            errPopUp.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        password_re.requestFocus();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            password_re.setBackgroundTintList(colorStateListRed);
                            errPopUp.setVisibility(View.VISIBLE);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        password_re.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(confirmPassword()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        password_re.setBackgroundTintList(colorStateListGreen);
                        errPopUp.setVisibility(View.INVISIBLE);
                    }
                } else if (password_re.getText().length()==0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        password_re.setBackgroundTintList(colorStateListBlack);
                        errPopUp.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        password_re.setBackgroundTintList(colorStateListRed);
                        errPopUp.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);

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

    public void signUpAction() {

        if(!(validateUsername() && validatePhoneNumber() && validateEmail() && validatePassword() && confirmPassword())) {
            return;
        }
        userModel = new UserModel();
        userModel.setUsername(username.getText().toString());
        userModel.setPhone_no(phone.getText().toString());
        userModel.setPassword(password.getText().toString());
        userModel.setOccupation("");
        userModel.setEmailId(email.getText().toString());
        userModel.setCountry_code(countryCode.getText().toString());

        LoadingBox.createInstance(mActivity).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                userModel = oLoginOrc.signUpAction(userModel);
                if(userModel.getStatusCode().equalsIgnoreCase("200")) {

                    mActivity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            otpValidator.otpCheckMethod("confirmNumber", userModel);
                        }
                    });
                }
            }
        }).start();
    }


    private void saveSignUpData(UserModel userModel){
        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_ActionType), "2");
        editor.putString(getString(R.string.saved_signup_phNo), userModel.getPhone_no());
        editor.putString(getString(R.string.saved_signup_usrname), userModel.getUsername());
        editor.putString(getString(R.string.saved_signup_password), userModel.getPassword());
        editor.apply();
    }

    private boolean validateUsername() {
        String name = username.getText().toString();
        if(name==null || name.length()==0) {
            Toast.makeText(context, "Username Is Required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(name.length()<=3) {
            Toast.makeText(context, "Username Must be Atleast 3 Characters Long", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validatePhoneNumber() {
        String phNo = phone.getText().toString();
        if(phNo==null || phNo.length()==0) {
            Toast.makeText(context, "Please Enter A Phone Number", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(phNo.length()!=10) {
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

    private boolean validateEmail() {
        String emailId = email.getText().toString();
        if(emailId.indexOf("@")>1 && emailId.indexOf(".")>(emailId.indexOf("@")+1) && emailId.indexOf(".")<(emailId.length()-1)) {
            return true;
        }
        Toast.makeText(context, "The Pasword Must Atleast contain three characters", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean confirmPassword() {
        String pass = password.getText().toString();
        String pass_re = password_re.getText().toString();
        if(pass.equalsIgnoreCase(pass_re)) {
            return true;
        }
        Toast.makeText(context, "The Paswords Don't Match", Toast.LENGTH_SHORT).show();
        return false;
    }
}
