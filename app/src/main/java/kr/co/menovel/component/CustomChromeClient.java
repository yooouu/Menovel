package kr.co.menovel.component;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.content.FileProvider;

import java.io.File;

import kr.co.menovel.MainActivity;

public class CustomChromeClient extends WebChromeClient {
    private MainActivity act;


    public CustomChromeClient(MainActivity act) {
        this.act = act;
    }

    // Input태그 파일 선택
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        if (!act.requestPermission()) {
            return false;
        }
        if (act.getFilePathCallback() != null) {
            act.getFilePathCallback().onReceiveValue(null);
            act.setFilePathCallback(null);
        }
        act.setFilePathCallback(filePathCallback);
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType(MediaStore.Images.Media.CONTENT_TYPE);
        i.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/menovel");
        if (!path.exists()) {
            path.mkdir();
        }
        File file = new File(path, "IMG_" + System.currentTimeMillis() + ".png");
        // File 객체의 URI 를 얻는다.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            act.setCameraImageUri(FileProvider.getUriForFile(act, act.getPackageName() + ".fileprovider", file));
        } else {
            act.setCameraImageUri(Uri.fromFile(file));
        }
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, act.getCameraImageUri());

        Intent chooserIntent = Intent.createChooser(i, "파일 선택");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{intentCamera});
        act.startActivityForResult(chooserIntent, MainActivity.FILE_CHOOSER_REQ_CODE);
        return true;
    }

    // 새창 외부 브라우저 열기
    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        WebView newWebView = new WebView(act);
        view.addView(newWebView);
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(newWebView);
        resultMsg.sendToTarget();

        newWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(url));
                act.startActivity(browserIntent);
                return true;
            }
        });
        return true;
    }
}
