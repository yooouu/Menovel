package kr.co.menovel.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtil {
    private static SharedPreferences preferences;
    private final static String PREF_NAME = "kr.co.menovel";
    public final static String FCM_TOKEN = "fcm_token";

    public static void init(Context context) {
        if (preferences == null) {
            preferences = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        if (preferences == null) return defaultValue;
        return preferences.getBoolean(key, defaultValue);
    }

    public static void putBoolean(String key, boolean Value) {
        if (preferences == null) return;
        SharedPreferences.Editor mEditer = preferences.edit();
        mEditer.putBoolean(key, Value);
        mEditer.commit();
    }

    public static String getString(String key, String defaultValue) {
        if (preferences == null) return defaultValue;
        return preferences.getString(key, defaultValue);
    }

    public static void putString(String key, String value) {
        if (preferences == null) return;
        SharedPreferences.Editor mEditer = preferences.edit();
        mEditer.putString(key, value);
        mEditer.commit();
    }

    public static int getInt(String key, int defaultValue) {
        if (preferences == null) return defaultValue;
        return preferences.getInt(key, defaultValue);
    }

    public static void putInt(String key, int value) {
        if (preferences == null) return;
        SharedPreferences.Editor mEditer = preferences.edit();
        mEditer.putInt(key, value);
        mEditer.commit();
    }

    public static long getLong(String key, long defaultValue) {
        if (preferences == null) return defaultValue;
        return preferences.getLong(key, defaultValue);
    }

    public static void putLong(String key, long value) {
        if (preferences == null) return;
        SharedPreferences.Editor mEditer = preferences.edit();
        mEditer.putLong(key, value);
        mEditer.commit();
    }

    public static float getFloat(String key, float defaultValue) {
        if (preferences == null) return defaultValue;
        return preferences.getFloat(key, defaultValue);
    }

    public static void putFloat(String key, float value) {
        if (preferences == null) return;
        SharedPreferences.Editor mEditer = preferences.edit();
        mEditer.putFloat(key, value);
        mEditer.commit();
    }
}
