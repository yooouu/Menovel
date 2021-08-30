package kr.co.menovel;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.kakao.auth.Session;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.kakaostory.KakaoStoryService;
import com.kakao.kakaostory.api.KakaoStoryApi;
import com.kakao.kakaostory.callback.StoryResponseCallback;
import com.kakao.kakaostory.response.model.MyStoryInfo;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.TextTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.KakaoParameterException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import kr.co.menovel.component.CustomChromeClient;
import kr.co.menovel.component.CustomWebViewClient;
import kr.co.menovel.kakao.KakaoLoginCallback;
import kr.co.menovel.kakao.KakaoSessionCallback;
import kr.co.menovel.kakao.KakaoStoryLink;
import kr.co.menovel.model.ReservePushData;
import kr.co.menovel.retrofit.RetrofitClient;
import kr.co.menovel.service.AlarmRecevier;
import kr.co.menovel.util.AndroidBridge;
import kr.co.menovel.util.HTTPUtil;
import kr.co.menovel.util.SharedPrefUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static kr.co.menovel.util.CommonUtil.SPLASH_TIME;
import static kr.co.menovel.util.CommonUtil.finishApp;
import static kr.co.menovel.util.CommonUtil.showToast;

public class MainActivity extends AppCompatActivity implements KakaoLoginCallback {

    private final String URL = HTTPUtil.ip;
    private WebView webView;
    private RelativeLayout layout_loading;
    private LinearLayout layout_error_page;

    private BroadcastReceiver receiver;

    public static final String WEB_LINK_ACTION = "web_link";
    public static boolean isRunning = false;

    // Property google login
    public static final int RC_SIGN_IN = 1;
    private GoogleSignInClient googleSignInClient;

    // Property kakao login
    private KakaoSessionCallback kakaoSessionCallback;

    private String loginResultUrl = "";

    // Property Local Notification
    private AlarmManager alarmManager;
    private GregorianCalendar calendar;
    private NotificationManager notificationManager;
    NotificationCompat.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver();
        isRunning = true;

        webView = findViewById(R.id.webview);
        layout_loading = findViewById(R.id.layout_loading);
        layout_error_page = findViewById(R.id.layout_error_page);

        webView.addJavascriptInterface(new AndroidBridge(this, webView), "AndroidBridge");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setTextZoom(100);
        webView.setWebViewClient(new CustomWebViewClient(webView, layout_loading, layout_error_page));
        webView.setWebChromeClient(new CustomChromeClient(this));
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        String url = getIntent().getStringExtra("url");
        if (url != null && !url.equals("")) {
            url = (!url.startsWith("https") ? "https://" : "") + url;
            webView.loadUrl(url);
        } else {
            webView.loadUrl(URL);
        }

        initGoogleSignIn();
        kakaoSessionCallback = new KakaoSessionCallback(this);

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        calendar = new GregorianCalendar();

        removeAlarm();
    }

    @Override
    public void onBackPressed() {
        if (layout_loading.getVisibility() == View.GONE) {
            String url = webView.getUrl();
            if (!webView.canGoBack() || url.endsWith("menovel.com/")) {
                finishApp(MainActivity.this);
            } else {
                webView.goBack();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (kakaoSessionCallback != null) {
            if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
                return;
            }
        }

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_refresh:
                webView.reload();
                break;
            default:
        }
    }

    // SNS Login
    public void loginSNS(String type, String url) {
        loginResultUrl = url;
        switch (type) {
            case "google":
                signIn();
                break;

            case "kakao":
                kakaoLogin();
                break;

            case "naver":
                break;

            case "facebook":
                break;
        }
    }

    // SNS Share
    public void shareSNS(String type, String title, String url) {
        switch (type) {
            case "1":   // 트위터
                shareToTwitter(title, url);
                break;

            case "2":   // 페이스북

                break;

            case "3":   // 네이버밴드
                shareToBand(title, url);
                break;

            case "5":   // 카카오스토리
                shareToKakaoStory(title, url);
                break;

            case "6":   // 카카오링크
                shareToKakao(title, url);
                break;
        }
    }

    // Google Login
    private void initGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    public void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            String id = account.getId();
            String email = account.getEmail();
            String familyName = account.getFamilyName();
            String givenName = account.getGivenName();
            String displayName = account.getDisplayName();

            // Send google login data to webView
            sendToSnsLoginData(displayName, email, id);

        } catch (ApiException e) {
            showToast(R.string.toast_error_server);
        }
    }

    // Kakao Login
    public void kakaoLogin() {
        Session session = Session.getCurrentSession();
        session.addCallback(kakaoSessionCallback);
        session.checkAndImplicitOpen();
    }

    @Override
    public void kakaoLoginSuccess(MeV2Response profile) {
        if (profile != null) {
            final String userId = String.valueOf(profile.getId());
            final String name = profile.getKakaoAccount().getProfile().getNickname();
            final String email = String.valueOf(profile.getKakaoAccount().getEmail());

            // send login data to webView
            sendToSnsLoginData(name, email, userId);
        }
    }

    @Override
    public void kakaoLoginFailed(String errorString) {
        //TODO Fail to kakao login
        Log.e("Kakao Login", "Fail to kakao login : " + errorString);
    }

    // App => WebView
    private void sendToSnsLoginData(String name, String email, String id) {
        webView.evaluateJavascript("javascript:sns_login_ok('"+ id +"','"+ email +"', '" + name +"', '" + loginResultUrl +"');", null);
    }

    // 카카오톡 공유하기
    private void shareToKakao(String title, String url) {
        // 기본 텍스트 템플릿 생성
        TextTemplate params = TextTemplate.newBuilder(
                title, // 텍스트값
                LinkObject.newBuilder() // 링크 오브젝트
                        .setWebUrl(url)
                        .setMobileWebUrl(url)
                        .build())

                // 버튼
                .addButton(new ButtonObject("자세히 보기", LinkObject.newBuilder()
                        .setWebUrl(url)
                        .setMobileWebUrl(url)
                        .setAndroidExecutionParams("key1=value1") // JSON -> P-> DID 주소 뒤에 담김.
                        .setIosExecutionParams("key1=value1")
                        .build()))

                .build();

        Map<String, String> serverCallbackArgs = new HashMap<String, String>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");
        KakaoLinkService.getInstance().sendDefault(this, params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.e("Kakao Share", "Fail to kakao share : " + errorResult.toString());
            }

            @Override
            public void onSuccess(KakaoLinkResponse kakaoLinkResponse) {
                //TODO send success data to webView
            }
        });
    }
    // 카카오스토리 공유하기
    private void shareToKakaoStory(String title, String url) {
        Map<String, Object> urlInfoAndroid = new Hashtable<String, Object>(1);
        urlInfoAndroid.put("title", title);
//        urlInfoAndroid.put("desc", title);
        urlInfoAndroid.put("type", "article");

        // Recommended: Use application context for parameter.
        KakaoStoryLink storyLink = KakaoStoryLink.getLink(getApplicationContext());

        // check, intent is available.
        if (!storyLink.isAvailableIntent()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.kakao.story"));
            startActivity(intent);
            return;
        }

        storyLink.openKakaoLink(this,
                url,
                getPackageName(),
                "1.0",
                "미노벨",
                "UTF-8",
                urlInfoAndroid);
    }
    // 네이버밴드 공유하기
    private void shareToBand(String title, String url) {
        try {
            String decodeURL = URLDecoder.decode(url, "UTF-8");
            PackageManager manager = getPackageManager();
            Intent i = manager.getLaunchIntentForPackage("com.nhn.android.band");
            startActivity(i);
        } catch (Exception e) {
            // not launched band app
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.band"));
            startActivity(intent);
            return;
        }
        String serviceDomain = url;
        Uri uri = Uri.parse("bandapp://create/post?text=" + title + "\n" + url + "&route=" + serviceDomain);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    // 트위터 공유하기
    private void shareToTwitter(String title, String url) {
        try {
            String sharedText = String.format("http://twitter.com/intent/tweet?text="+ title + "\n%s", URLEncoder.encode(url, "utf-8"));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharedText));
            startActivity(intent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    // 외부링크 공유하기
    public void shareToLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    // App => WebView
    public void sendToFcmToken() {
        String fcmToken = SharedPrefUtil.getString(SharedPrefUtil.FCM_TOKEN, "");

        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript("javascript:setFcmToken('"+ fcmToken +"');", null);
            }
        });
    }
    // App => WebView
    // 예약 푸시 시간 설정 테스트용
    public void sendToReserveTime() {
        webView.post(new Runnable() {
            @Override
            public void run() {
                String reserveTime = "2021-08-24 18:13:00";
                webView.evaluateJavascript("javascript:setReservePushTime('"+ reserveTime +"');", null);
            }
        });
    }
    // App => WebView
    // 푸시 발송 테스트용
    public void sendPush() {
        String fcmToken = SharedPrefUtil.getString(SharedPrefUtil.FCM_TOKEN, "");
        String title = "푸시 테스트";
        String msg = "푸시 테스트 입니다.";

        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript("javascript:push_test('"+ fcmToken +"', '" + title + "', '" + msg + "');", null);
            }
        });
    }

    // Get Reserve Time For Local Notification Reg
    private void requestPushTime() {
        RetrofitClient.getRetrofitApi().getReservePushData().enqueue(new Callback<ReservePushData>() {
            @Override
            public void onResponse(Call<ReservePushData> call, Response<ReservePushData> response) {
                if (response.body() != null) {
                    String title = response.body().getTitle();
                    String msg = response.body().getBody();
                    String sendDate = response.body().getSenddate();
                    String url = response.body().getUrl();
                    String imgUrl = response.body().getImg_url();
                    Log.e("aaa", "title: " + title + " msg: " + msg + " sendDate: " + sendDate + " url: " + url + " img_url: " + imgUrl);

                    setAlarm(title, msg, sendDate, url, imgUrl);
                } else {

                }
            }

            @Override
            public void onFailure(Call<ReservePushData> call, Throwable t) {
                showToast(R.string.toast_error_server);
            }
        });
    }
    // 예약 푸시 등록
    private void setAlarm(String title, String msg, String send_date, String url, String imgUrl) {
        Intent receiverIntent = new Intent(MainActivity.this, AlarmRecevier.class);
        receiverIntent.putExtra("title", title);
        receiverIntent.putExtra("msg", msg);
        receiverIntent.putExtra("url", url);
        receiverIntent.putExtra("img_url", imgUrl);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 1, receiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateTime = null;
        try {
            dateTime = dateFormat.parse(send_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);

        Calendar nowCalendar = Calendar.getInstance();
        if (calendar.after(nowCalendar) && nowCalendar.getTime() != calendar.getTime()) {
            alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        }
    }
    // 등록된 예약 푸시 제거
    private void removeAlarm() {
        Intent intent = new Intent(MainActivity.this, AlarmRecevier.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 1, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }

        requestPushTime();
    }

    private void registerReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals(WEB_LINK_ACTION)) {
                    String url = intent.getStringExtra("url");
                    if (url != null && !url.equals("")) {
                        url = (!url.startsWith("https") ? "https://" : "") + url;
                        webView.loadUrl(url);
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(WEB_LINK_ACTION));
    }
}