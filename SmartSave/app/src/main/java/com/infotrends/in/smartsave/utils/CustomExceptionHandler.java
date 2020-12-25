package com.infotrends.in.smartsave.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import androidx.annotation.NonNull;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {

        IntegProperties oInteg = IntegProperties.getInstance();
        File logFile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + oInteg.getString("app_base_dir") + oInteg.getString("lod_crash_dir") + "/crashlog.txt");


        File logDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + oInteg.getString("app_base_dir") + oInteg.getString("lod_crash_dir"));

        if(!logDir.exists()) {
            logDir.mkdirs();
        }

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();

        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(throwable.toString());
            buf.newLine();
            buf.newLine();
            buf.newLine();
            buf.append("-----------------------------------------------------------------------------------------------------------------------------------");
            buf.newLine();
            buf.newLine();
            buf.newLine();
            buf.append(stacktrace);
            buf.close();


        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
