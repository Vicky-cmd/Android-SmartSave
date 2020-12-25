package com.infotrends.in.smartsave.database;

import android.content.Context;
import android.database.Cursor;

public class LoginAdapter {

    private Context mContext;
    private Cursor mCursor;

    public LoginAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public class LoginHolder {

    }
}
