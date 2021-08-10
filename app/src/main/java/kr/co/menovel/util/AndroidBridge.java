package kr.co.menovel.util;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.net.URL;

import kr.co.menovel.MainActivity;

public class AndroidBridge {
    private MainActivity act;
    private WebView webView;

    public AndroidBridge(MainActivity act, WebView webView) {
        this.act = act;
        this.webView = webView;
    }

    // Bridge function
    @JavascriptInterface
    public void appSnsLogin(String type, String url) {
        Log.e("bridge","appSnsLogin!!\ntype : " + type + " url : " + url);
        act.loginSNS(type, url);
    }

    @JavascriptInterface
    public void app_svr_share(String type, String title, String url) {
        Log.e("bridge", "app_svr_share!!\ntype: " + type + " title: " + title + " url: " + url);
        act.shareSNS(type, title, url);
    }

    @JavascriptInterface
    public void getFcmToken() {
        Log.e("bridge", "getFcmToken!!");
        act.sendToFcmToken();
    }

    @JavascriptInterface
    public void getNowDateTime() {
        Log.e("bridge", "getNowDateTime!!");
        act.sendToReserveTime();
    }
}
