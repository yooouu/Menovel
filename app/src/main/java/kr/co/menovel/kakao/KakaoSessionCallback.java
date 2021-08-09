package kr.co.menovel.kakao;

import android.util.Log;

import com.kakao.auth.ISessionCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;

import java.util.ArrayList;
import java.util.List;

public class KakaoSessionCallback implements ISessionCallback {

    private KakaoLoginCallback mCallback;

    public KakaoSessionCallback(KakaoLoginCallback callback){
        this.mCallback = callback;
    }

    @Override
    public void onSessionOpened() {
        requestMe();
    }

    @Override
    public void onSessionOpenFailed(KakaoException exception) {
        Log.d("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
    }

    public void requestMe(){
        List<String> keys = new ArrayList<>();
        keys.add("kakao_account.email");

        UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                if(mCallback!=null)mCallback.kakaoLoginFailed(errorResult.toString());
                Log.d("SessionCallback :: ", "onSessionClosed : " + errorResult.getErrorMessage());
            }

            @Override
            public void onSuccess(MeV2Response result) {
                Log.e("aaa", "result: " + result);
                if(mCallback!=null)mCallback.kakaoLoginSuccess(result);
            }

            @Override
            public void onFailure(ErrorResult errorResult) {
                super.onFailure(errorResult);
                Log.e("onFailuer", errorResult.getErrorMessage());
            }
        });
    }
}
