package com.infotrends.in.smartsave.services;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Toast;

import com.infotrends.in.smartsave.MainActivity;
import com.infotrends.in.smartsave.models.FileModel;
import com.infotrends.in.smartsave.orchestrator.FilesModOrc;
import com.infotrends.in.smartsave.utils.LoggerFile;
import com.infotrends.in.smartsave.utils.NotificationMan;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import androidx.annotation.Nullable;

public class DownloadFile extends Service {

    protected Handler handler;
    protected Toast mToast;
    private FileModel fModel;
    private FilesModOrc oFilesModOrc = new FilesModOrc();
    private LoggerFile ologger = LoggerFile.createInstance(DownloadFile.class);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            ologger.info("Inside Start Command Method");
            Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
            fModel = new FileModel();
            fModel.setFileName(intent.getStringExtra("fModelName"));
            fModel.setFileType(intent.getStringExtra("fModelType"));
            final String curSharedFileStatus = intent.getStringExtra("curSharedFileStatus");
            fModel.setIsSharedStatus(curSharedFileStatus!=null?curSharedFileStatus:"N");

            if (intent.getStringExtra("fModelName") != null) {
                ologger.info(intent.getStringExtra("fModelName"));
//                Toast.makeText(this, intent.getStringExtra("fModelName"), Toast.LENGTH_LONG).show();
            } else {
                ologger.info("No File!");
//                Toast.makeText(this, "null!", Toast.LENGTH_LONG).show();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        ologger.info("Going to Start Download Job!");
                        boolean bool = oFilesModOrc.downloadSelectedFile(fModel);
                        ologger.info("Download Finished!");
                        if(bool) {
                            Bundle bundle = new Bundle();
                            bundle.putString("opType","upload");
                            bundle.putString("fileName",fModel.getFileName());
                            NotificationMan.endDisplayNotifForUploadDownload();
                            MainActivity.responseProcessingOfServies(bundle);
                        } else {
                            NotificationMan.endDisplayNotifForUploadDownloadFailed("d");
                        }
                    } catch (Exception e) {
                        ologger.error(e.getMessage());
                        ologger.error(e.toString());
                        NotificationMan.endDisplayNotifForUploadDownloadFailed("d");
                    }
                }
            }).start();
        } catch (Exception e) {
            ologger.error(e.getMessage());
            ologger.error(e.toString());
            NotificationMan.endDisplayNotifForUploadDownloadFailed("d");
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Toast.makeText(this, "Service Task removed by user.", Toast.LENGTH_LONG).show();

        try {
            ologger.info("Inside Task Removed Method");
            Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
            restartServiceTask.putExtra("fModelName", fModel.getFileName());
            restartServiceTask.putExtra("fModelType", fModel.getFileType());

            restartServiceTask.setPackage(getPackageName());
            ologger.info("Creating PendingIntent");
            PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
            ologger.info("Creating AlarmManager");
            AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            myAlarmService.set(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 1000,
                    restartPendingIntent);
            ologger.info("Done!");
        } catch(Exception e) {
            ologger.error(e.toString());
            NotificationMan.endDisplayNotifForUploadDownloadFailed("d");
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service destroyed by user.", Toast.LENGTH_LONG).show();
    }
}
