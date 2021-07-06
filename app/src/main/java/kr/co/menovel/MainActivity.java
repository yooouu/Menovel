package kr.co.menovel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import kr.co.menovel.component.CustomWebViewClient;
import kr.co.menovel.util.AndroidBridge;

import static kr.co.menovel.util.CommonUtil.finishApp;

public class MainActivity extends AppCompatActivity {

    private final String URL = "https://menovel.com";
    private WebView webView;
    private RelativeLayout layout_loading;
    private LinearLayout layout_error_page;

    public static boolean isRunning = false;

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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_refresh:
                webView.reload();
                break;
            default:
        }
    }
}