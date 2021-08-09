package kr.co.menovel.kakao;

import com.kakao.usermgmt.response.MeV2Response;

public interface KakaoLoginCallback {
    void kakaoLoginSuccess(MeV2Response profile);
    void kakaoLoginFailed(String errorString);
}
