package com.infotrends.in.smartsave.ui.register;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.infotrends.in.smartsave.R;
import com.infotrends.in.smartsave.interaces.DrawerInterface;
import com.infotrends.in.smartsave.models.UserModel;
import com.infotrends.in.smartsave.orchestrator.LoginOrc;
import com.infotrends.in.smartsave.utils.LoadingBox;

public class OtpFragment  extends Fragment {

    View root;
    Context context;
    AppCompatActivity appCompatActivity;
    DrawerInterface drInterface;
    private NavController navController;
    private NavHostFragment navHostFragment;
    private Activity mActivity;
    private Spinner spinner;
    private UserModel userModel = new UserModel();
    private LoginOrc oLoginOrc = new LoginOrc();

    private EditText otp;
    private Button verify, clear;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.otpview, container, false);

        if(getArguments()!=null) {
            if(getArguments().getParcelable("userModel")!=null) {
                userModel = getArguments().getParcelable("userModel");
            }
        }
        mActivity = getActivity();
        context = getActivity();
        appCompatActivity = (AppCompatActivity) getActivity();

        navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        oLoginOrc = new LoginOrc();

        otp = root.findViewById(R.id.otp_inp);
        verify = root.findViewById(R.id.otp_verify);
        clear = root.findViewById(R.id.otp_clear);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAction();
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear.setText("");
            }
        });

        try {
            drInterface = (DrawerInterface) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement MyInterface");
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

    private void loginAction() {
//        userModel=new UserModel();
        userModel.setAuthCode(otp.getText().toString());
        new Thread(new Runnable() {
            @Override
            public void run() {
                userModel = oLoginOrc.loginAction(userModel);
                if(userModel.getStatusCode().equalsIgnoreCase("200")) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LoadingBox.getInstance().dismiss();
                            navController.navigateUp();
                            mActivity.recreate();
                        }
                    });

                }
            }
        }).start();

    }
}
