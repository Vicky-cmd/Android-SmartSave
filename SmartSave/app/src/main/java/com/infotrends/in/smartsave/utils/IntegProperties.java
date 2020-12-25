package com.infotrends.in.smartsave.utils;

import java.util.ResourceBundle;

public class IntegProperties {

    private static IntegProperties oInteg = null;
    private ResourceBundle resBundle=null;
    IntegProperties() {
        resBundle = ResourceBundle.getBundle("integration");
    }
    public static IntegProperties getInstance() {
        if(oInteg==null) {
            oInteg = new IntegProperties();
        }
        return oInteg;
    }

    public String getString(String key) {
        return resBundle.getString(key);
    }
    public int getInt(String key) {
        return Integer.getInteger(getString(key));
    }
    public boolean getBoolean(String key) {
        return Boolean.getBoolean(getString(key));
    }
}
