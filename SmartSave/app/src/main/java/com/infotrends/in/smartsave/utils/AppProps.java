package com.infotrends.in.smartsave.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.infotrends.in.smartsave.models.FileModel;

import java.util.ArrayList;
import java.util.List;

import androidx.navigation.NavController;

public class AppProps {

    private Activity activity=null;
    private Context context=null;
    private NavController navController=null;
    private Handler handler = null;
    private List<FileModel> fileModelLst = new ArrayList<FileModel>();
    private List<String> filesNames = new ArrayList<String>();
    private String currDir;
    private String currSharedFileID;

    private static AppProps appProps=null;


    AppProps(Context mContext, Activity mActivity, NavController mNavController) {
        context = mContext;
        activity = mActivity;
        navController = mNavController;
        handler = new Handler();
    }
    public AppProps() {

    }

    public static AppProps getInstance() {
        if(appProps==null) {
            appProps = new AppProps();
        }
        return appProps;
    }

    public void setFieldValues(Context mContext, Activity mActivity, NavController mNavController) {
        context = mContext;
        activity = mActivity;
        navController = mNavController;
        handler = new Handler();
    }

    public static void setProperties(Context mContext, Activity mActivity, NavController mNavController) {
        if(appProps==null) {
            appProps = new AppProps();
        }
        appProps.setFieldValues(mContext, mActivity, mNavController);
    }

//    public static AppProps getInstance() {
//        return appProps;
//    }

    public Activity getActivity() {
        return activity;
    }

    public Context getContext() {
        return context;
    }

    public NavController getNavController() {
        return navController;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setFilesList(List<FileModel> fileModelLst) {
        this.fileModelLst = fileModelLst;
        filesNames = new ArrayList<String>();
        if(fileModelLst!=null) {
            for(int i=0; i<fileModelLst.size(); i++) {
                filesNames.add(fileModelLst.get(i).getFileName());
            }
        }
    }

    public List<FileModel> getFilesList() {
        return fileModelLst;
    }

    public List<String> getFileNamesLst() {
        return filesNames;
    }

    public String getCurrDir() {
        return currDir;
    }

    public void setCurrDir(String currDir) {
        this.currDir = currDir;
    }

    public String getCurrSharedFileID() {
        return currSharedFileID;
    }

    public void setCurrSharedFileID(String currSharedFileID) {
        this.currSharedFileID = currSharedFileID;
    }
}
