package com.infotrends.in.smartsave.utils;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.infotrends.in.smartsave.R;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationMan {

    private static AppProps oAppProps = AppProps.getInstance();
    private static  Context context = oAppProps.getContext();
    private static NotificationCompat.Builder fProcessbuilder;
    private static NotificationManagerCompat fprocessnotificationManager;
    private static String fProcessStatus = "";



    public static void createNotificationForUploadDownloads(String type, String fileName, String fType) {

        final String CHANNEL_ID = "Files Upload/Download Status";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Files Upload/Download Status";
            String description = "Display the notification for File Upload/Download Status.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        String title = "";
        String content = "";
        String bigContent = "";

        if(type.equalsIgnoreCase("d")) {
            title = "DOWNLOADING";
            content = "Downloading the " + fType + " file";
            bigContent = "Downloading " + fileName;
            fProcessStatus = "Download Complete";
        } else {
            title = "UPLOADING";
            content = "Uploading the " + fType + " file";
            bigContent = "Uploading " + fileName;
            fProcessStatus = "Upload Complete";
        }

        fProcessbuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_app_name_v1)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigContent))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        fprocessnotificationManager = NotificationManagerCompat.from(context);
    }

    public static void startDisplayNotifForUploadDownload() {
        fProcessbuilder.setProgress(0,0, true);
        fprocessnotificationManager.notify(1, fProcessbuilder.build());
    }

    public static void endDisplayNotifForUploadDownload() {
        fProcessbuilder.setContentText(fProcessStatus)
                .setProgress(0,0, false);
        fprocessnotificationManager.notify(1, fProcessbuilder.build());
    }

    public static void endDisplayNotifForUploadDownloadFailed(String type) {
        if(type.equalsIgnoreCase("u")) {
            fProcessStatus = "Upload Failed!";
        } else {
            fProcessStatus = "Download Failed!";
        }
        fProcessbuilder.setContentText(fProcessStatus)
                .setProgress(0,0, false);
        fprocessnotificationManager.notify(1, fProcessbuilder.build());
    }
}
