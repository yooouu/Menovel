package kr.co.menovel.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.UUID;

import kr.co.menovel.AppApplication;
import kr.co.menovel.R;

public class CommonUtil {
    public static final int SPLASH_TIME = 2000;
    private static final int FINISH_TIMER = 2000;
    private static boolean isFinish = false;
    private static Toast toast;

    // 앱 종료
    public static void finishApp(Activity act) {
        if (isFinish) {
            act.finishAffinity();
        } else {
            showToast(R.string.app_finish);
            isFinish = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isFinish = false;
                }
            }, FINISH_TIMER);
        }
    }

    // Toast 출력
    public static void showToast(String msg) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(AppApplication.getContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }
    public static void showToast(int msgId) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(AppApplication.getContext(), msgId, Toast.LENGTH_SHORT);
        toast.show();
    }

    // 키보드 숨김
    public static void hideKeyboard(Activity act) {
        InputMethodManager imm = (InputMethodManager) act.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = act.getCurrentFocus();
        if (view == null) {
            view = new View(act);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // 권한 체크
    public enum PermissionCheck {
        PERMISSION_Y,
        PERMISSION_N,
        PERMISSION_DENY
    }
    public static PermissionCheck permissionResultCallBack(Activity act, int requestCode, String[] permissions, int[] grantResults) {
        boolean isGranted = false;
        for (int result : grantResults) {
            if (!(isGranted = (result == PackageManager.PERMISSION_GRANTED))) {
                break;
            }
        }
        if (grantResults.length == 0 || !isGranted) {
            boolean isAlwaysDeny = true;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED && ActivityCompat.shouldShowRequestPermissionRationale(act, permissions[i])) {
                    isAlwaysDeny = false;
                    break;
                }
            }
            if (isAlwaysDeny) {
                return PermissionCheck.PERMISSION_DENY;
            } else {
                return PermissionCheck.PERMISSION_N;
            }
        } else {
            return PermissionCheck.PERMISSION_Y;
        }
    }

    // 디바이스 고유 아이디
    @SuppressLint("HardwareIds")
    public static String getDevicesUUID(Activity act) {
        if (ActivityCompat.checkSelfPermission(act, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        String tmDevice, tmSerial, androidId;
        TelephonyManager tm = (TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tmDevice = "" + tm.getImei();
        } else {
            tmDevice = "" + tm.getDeviceId();
        }
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(act.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        return deviceUuid.toString();
    }
}
