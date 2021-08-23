package kr.co.menovel.retrofit;

import kr.co.menovel.model.ReservePushData;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RetrofitApi {
    @FormUrlEncoded
    @POST("/api/app/fcmMember.php")
    Call<Void> updateToken(@Field("token") String fcm_token, @Field("packageName") String package_name,
                           @Field("appVersion") String app_version, @Field("uuid") String uuid);


    @GET("/api/reservePushInfo.php")
    Call<ReservePushData> getReservePushData();
}
