package com.infotrends.in.smartsave.orchestrator;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SendOrc {
    public void shareApp(Context context) {
        String appurl = "***";
        Intent sendInt = new Intent();
        sendInt.setAction(Intent.ACTION_SEND);
        sendInt.setType("text/plain");
        sendInt.putExtra(Intent.EXTRA_TEXT, "Download the latest version of the SmartSave App using the link: " + appurl);
        try {
            context.startActivity(Intent.createChooser(sendInt, "Share via..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
