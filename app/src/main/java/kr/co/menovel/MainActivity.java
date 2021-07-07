package kr.co.menovel;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.kakao.sdk.user.UserApiClient;

import kr.co.menovel.component.CustomWebViewClient;
import kr.co.menovel.util.AndroidBridge;

import static kr.co.menovel.util.CommonUtil.finishApp;

public class MainActivity extends AppCompatActivity {

    private final String URL = "https://menovel.com";
    private WebView webView;
    private RelativeLayout layout_loading;
    private LinearLayout layout_error_page;

    public static boolean isRunning = false;

    // Property google login
    public static final int RC_SIGN_IN = 1;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isRunning = true;

        webView = findViewById(R.id.webview);
        layout_loading = findViewById(R.id.layout_loading);
        layout_error_page = findViewById(R.id.layout_error_page);

        webView.addJavascriptInterface(new AndroidBridge(this, webView), "Android");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setTextZoom(100);
        webView.setWebViewClient(new CustomWebViewClient(webView, layout_loading, layout_error_page));
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.loadUrl(URL);

        initGoogleSignIn();
    }

    @Override
    public void onBackPressed() {
        if (layout_loading.getVisibility() == View.GONE) {
            // 사이드 메뉴 Visible 체크
            webView.evaluateJavascript("javascript:$('#header').hasClass('active')", value -> {
                try {
                    boolean sideActive = Boolean.parseBoolean(value);
                    if (sideActive) {
                        // 사이드 메뉴 닫기
                        webView.evaluateJavascript("javascript:$('.dimmed').eq(0).trigger('click')", null);
                    } else {
                        String url = webView.getUrl();
                        if (!webView.canGoBack() || url.endsWith("menovel.com/")) {
                            finishApp(MainActivity.this);
                        } else {
                            webView.goBack();
                        }
                    }
                } catch (Exception e) {
                    String url = webView.getUrl();
                    if (!webView.canGoBack() || url.endsWith("menovel.com/")) {
                        finishApp(MainActivity.this);
                    } else {
                        webView.goBack();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
    // TODO send google login data to webview
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            String email = account.getEmail();
            String familyName = account.getFamilyName();
            String givenName = account.getGivenName();
            String displayName = account.getDisplayName();


        } catch (ApiException e) {

        }
    }

    // Kakao Login
    public void kakaoLogin() {
        UserApiClient.getInstance().loginWithKakaoTalk(MainActivity.this, (oAuthToken, error) -> {
            if (error != null) {
                Log.e("Kakao", "kakao login fail", error);
            } else if (oAuthToken != null) {
                Log.i("Kakao", "kakao login success token : " + oAuthToken.getAccessToken());
                getKakaoUserData();
            }
            return null;
        });
    }
    // TODO send kakao login data to webview
    private void getKakaoUserData() {
        UserApiClient.getInstance().me((user, meError) -> {
            if (meError != null) {
                Log.e("Kakao", "fail to get user data", meError);
            } else {
                Log.i("Kakao", "kakao user data : " + user);
                int id = (int) user.getId();
                String email = user.getKakaoAccount().getEmail();
            }
            return null;
        });
    }
}