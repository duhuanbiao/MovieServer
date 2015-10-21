package com.person.tools;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

public class WifiApManager {
	 
    public static final int WIFI_AP_STATE_FAILED= 4;

    private final WifiManager mWifiManager;

    public WifiApManager(Context context) {
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
    }

    public boolean setWifiApEnabled(WifiConfiguration config,
            boolean enabled) {

        try {
            if (enabled) { // disable WiFi in any case
                mWifiManager.setWifiEnabled(false);
            }

            Method method = mWifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class,
                    boolean.class);
            return (Boolean) method.invoke(mWifiManager, config, enabled);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getWifiApState() {
        try {
            Method method = mWifiManager.getClass().getMethod(
                    "getWifiApState");
            return (Integer) method.invoke(mWifiManager);
        } catch(Exception e) {
            e.printStackTrace();
            return WIFI_AP_STATE_FAILED;
        }
    }
   
    public WifiConfiguration getWifiApConfiguration() {
        try {
            Method method = mWifiManager.getClass().getMethod(
                    "getWifiApConfiguration");
            return (WifiConfiguration) method.invoke(mWifiManager);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
   
    public boolean setWifiApConfiguration(WifiConfiguration config) {
        try {
            Method method = mWifiManager.getClass().getMethod(
                    "setWifiApConfiguration",WifiConfiguration.class);
            return (Boolean) method.invoke(mWifiManager, config);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public int setWifiApConfig(WifiConfiguration config) {
        try {
            Method method = mWifiManager.getClass().getMethod(
                    "setWifiApConfig",WifiConfiguration.class);
            return (Integer) method.invoke(mWifiManager, config);
        } catch(Exception e) {
            e.printStackTrace();
            return WIFI_AP_STATE_FAILED;
        }
    }
}
