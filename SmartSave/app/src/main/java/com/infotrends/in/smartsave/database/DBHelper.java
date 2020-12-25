package com.infotrends.in.smartsave.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME ="smartsave.db";
    public static final int DATABASE_VERSION = 3;
    public static DBHelper dBHelper = null;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DBHelper ctreateInstance(Context context) {
        if(dBHelper==null) {
            dBHelper =  new DBHelper(context);
        }
        return dBHelper;
    }

    public static DBHelper getInstance() {
        return dBHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_GAMELIST_TABLE="CREATE TABLE " +
                DBContract.LoginInfo.TABLE_NAME + " (" + DBContract.LoginInfo._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DBContract.LoginInfo.COLUMN_User_ID + " TEXT NOT NULL, " +
                DBContract.LoginInfo.COLUMN_Name + " TEXT NOT NULL, " +
                DBContract.LoginInfo.COLUMN_Email_ID + " TEXT NOT NULL, " +
                DBContract.LoginInfo.COLUMN_PASSWORD + " TEXT NOT NULL, " +
                DBContract.LoginInfo.COLUMN_Occupation + " TEXT NOT NULL, " +
                DBContract.LoginInfo.COLUMN_Login_Time + " TEXT NOT NULL, " +
                DBContract.LoginInfo.COLUMN_Phone_Number + " TEXT NOT NULL, " +
                DBContract.LoginInfo.COLUMN_Phone_Verified + " TEXT NOT NULL, " +
                DBContract.LoginInfo.COLUMN_Login_Attempts + " INTEGER NOT NULL, " +
                DBContract.LoginInfo.COLUMN_AUTH_TOKEN + " TEXT NOT NULL, " +
                DBContract.LoginInfo.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
        db.execSQL(SQL_CREATE_GAMELIST_TABLE);

        final String SQL_CREATE_GAMELIST_TABLE3="CREATE TABLE " +
                DBContract.NoGenerator.TABLE_NAME + " (" + DBContract.NoGenerator._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DBContract.NoGenerator.COLUMN_NO_GENERATED + " INTEGER NOT NULL, " +
                DBContract.NoGenerator.COLUMN_NO_TYPE + " TEXT NOT NULL, " +
                DBContract.NoGenerator.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";
        db.execSQL(SQL_CREATE_GAMELIST_TABLE3);

        ContentValues cv = new ContentValues();
        cv.put(DBContract.NoGenerator.COLUMN_NO_GENERATED, 1);
        cv.put(DBContract.NoGenerator.COLUMN_NO_TYPE, "Game_No");
        db.insert(DBContract.NoGenerator.TABLE_NAME, null, cv);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.LoginInfo.TABLE_NAME);
//        db.execSQL("DROP TABLE IF EXISTS " + DBContract.LastGameEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.NoGenerator.TABLE_NAME);
        onCreate(db);
    }
}
