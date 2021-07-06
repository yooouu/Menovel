package kr.co.menovel.util;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import kr.co.menovel.MainActivity;

public class AndroidBridge {
    private MainActivity act;
    private WebView webView;

    public AndroidBridge(MainActivity act, WebView webView) {
        this.act = act;
        this.webView = webView;
    }

    // TODO Bridge method
    @JavascriptInterface
    public void googleLogin() {
//        act.
    }
}
