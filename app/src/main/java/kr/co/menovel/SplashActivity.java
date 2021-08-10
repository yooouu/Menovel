package kr.co.menovel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.security.MessageDigest;
import java.util.UUID;

import kr.co.menovel.component.ConfirmDialog;
import kr.co.menovel.retrofit.RetrofitClient;
import kr.co.menovel.util.CommonUtil;
import kr.co.menovel.util.SharedPrefUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static kr.co.menovel.util.CommonUtil.SPLASH_TIME;
import static kr.co.menovel.util.CommonUtil.showToast;

public class SplashActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };

        requestPermission();
//        getKeyHash(this);
    }

    @Override
    public void onBackPressed() {
        handler.removeCallbacks(runnable);
        finish();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 권한을 획득하지 않았다면
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_PHONE_STATE
                        },
                        123);
            } else {
                startApp();
            }
        }else{
            startApp();
        }
    }

    //권한체크 후
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = false;
        for (int result : grantResults) {
            if (!(isGranted = (result == PackageManager.PERMISSION_GRANTED))) {
                break;
            }
        }
        if (grantResults.length == 0 || !isGranted) {
            final ConfirmDialog dialog = new ConfirmDialog(this);
            dialog.setTitle(R.string.dialog_permission_title);
            dialog.setContent(R.string.dialog_permission_content);
            dialog.setBtnCancelText(R.string.finish);
            dialog.setConfirmListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestPermission();
                    dialog.dismiss();
                }
            });
            dialog.setCancelListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishAffinity();
                }
            });
            dialog.show();
        } else {
            startApp();
        }
    }

    private void startApp() {
        String packageName = getApplicationContext().getPackageName();
        String appVersion = BuildConfig.VERSION_NAME;
        String uuid = CommonUtil.getDevicesUUID(this);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        //TODO save fcm token data
                        String fcmToken = task.getResult().getToken();
                        SharedPrefUtil.putString(SharedPrefUtil.FCM_TOKEN, fcmToken);
                        Log.e("fcm", "token: " + task.getResult().getToken() + " packageName: " + packageName + " version: " + appVersion + " uuid: " + uuid);

                        RetrofitClient.getRetrofitApi().updateToken(fcmToken, packageName, appVersion, uuid).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                handler.postDelayed(runnable, SPLASH_TIME);
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                showToast(R.string.toast_error_server);
                            }
                        });
                    }
                });
    }

    // For Kakao Login
    public static void getKeyHash(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String keyHash = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.e("KeyHash", keyHash);
            }

            String sha1_str = "A9:31:E0:BC:B7:E9:C5:08:9B:3D:5A:BE:96:57:1E:08:D9:B8:19:4F";
            sha1_str = sha1_str.replaceAll(":", "");
            byte[] sha1 = hexStringToByteArray(sha1_str);
            Log.e("keyHash Release", Base64.encodeToString(sha1, Base64.NO_WRAP));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}