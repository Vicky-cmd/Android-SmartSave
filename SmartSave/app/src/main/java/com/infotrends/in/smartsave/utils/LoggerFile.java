package com.infotrends.in.smartsave.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerFile {

    private IntegProperties oInteg = IntegProperties.getInstance();
    private File logFile = new File(Environment.getExternalStorageDirectory()
            .getAbsolutePath() + oInteg.getString("app_base_dir") + oInteg.getString("lod_crash_dir") + "/log.txt");

    private SimpleDateFormat sf = new SimpleDateFormat("MM-dd-yyyy, H:mm:ss");
    private static LoggerFile ologger;
    private Class classname;

    LoggerFile(Class classname) {

        File logDir = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + oInteg.getString("app_base_dir") + oInteg.getString("lod_crash_dir"));

        if(!logDir.exists()) {
            logDir.mkdirs();
        }

        this.classname = classname;
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
    }

    public static LoggerFile createInstance(Class classname) {
        ologger = new LoggerFile(classname);
        return ologger;
    }

    public void info(String text) {
        String fContent = "INFO  " + classname.getName() + "  - " + sf.format(new Date()) + "  --> " + text;
        appendLog(fContent);
    }

    public void error(String text) {
        String fContent = "ERROR  " + classname.getName() + "  - " + sf.format(new Date()) + "  --> " + text;
        appendLog(fContent);

    }

    public void debug(String text) {
        String fContent = "DEBUG  " + classname.getName() + "  - " + sf.format(new Date()) + "  --> " + text;
        appendLog(fContent);

    }

    private void appendLog(String text)
    {

        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
