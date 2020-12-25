package com.infotrends.in.smartsave.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import com.infotrends.in.smartsave.R;

public class LoadingBox {

    public static AlertDialog alert;

    LoadingBox(Activity mActivity) {
        AlertDialog.Builder alertDb = new AlertDialog.Builder(mActivity);
        final View loadingPopUp = mActivity.getLayoutInflater().inflate(R.layout.user_input, null);

        alertDb.setCancelable(false)
                .setView(loadingPopUp)
                .setIcon(R.drawable.loading)
                .setTitle("Loading...");
        alert = alertDb.create();
    }

    public static AlertDialog createInstance(Activity mActivity) {
        if(alert==null) {
            new LoadingBox(mActivity);
        }
        return alert;
    }

    public static AlertDialog getInstance() {
        return alert;
    }
}
