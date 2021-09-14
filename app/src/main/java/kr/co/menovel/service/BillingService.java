package kr.co.menovel.service;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import kr.co.menovel.MainActivity;
import kr.co.menovel.retrofit.RetrofitApi;
import kr.co.menovel.retrofit.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static kr.co.menovel.util.CommonUtil.showToast;

public class BillingService implements PurchasesUpdatedListener, BillingClientStateListener {

    private final String TAG = "BillingService";
    private final List<String> INAPP_LIST = Arrays.asList("coin_50_5000", "coin_100_10000", "coin_200_20000", "coin_300_30000", "coin_500_50000", "coin_1000_100000");
    private final List<String> SUBS_LIST = Arrays.asList("premium_auto_9900");

    public enum ConnectStatus {waiting, connected, fail, disconnected}

    private BillingClient billingClient;
//    private Activity activity;
    private MainActivity activity;
    private ConnectStatus connectStatus = ConnectStatus.waiting;
    private List<SkuDetails> skuDetailsList = new ArrayList<>();    // 제품 리스트

    private RetrofitApi retrofitApi = RetrofitClient.getRetrofitApi();
    private int inappIndex;
    private int purchaseSendCount = 0;
    private int acknowledgeCount = 0;

    public ConnectStatus getConnectStatus() {
        return connectStatus;
    }

    public BillingService(MainActivity activity) {
        this.activity = activity;

        billingClient = BillingClient.newBuilder(this.activity).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(this);
    }

    // 인앱결제 서버 접속 처리
    @Override
    public void onBillingSetupFinished(BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            connectStatus = ConnectStatus.connected;

            getSkuDetailList(true);
            getSkuDetailList(false);
        } else {
            connectStatus = ConnectStatus.fail;
            Log.d(TAG, "구글 결제 서버 접속에 실패하였습니다.\n오류코드:" + billingResult.getResponseCode());
        }
    }

    // 인앱결제 서버 접속 끊김
    @Override
    public void onBillingServiceDisconnected() {
        connectStatus = ConnectStatus.disconnected;
        Log.d(TAG, "구글 결제 서버와 접속이 끊어졌습니다.");
    }

    /** 상품 리스트 가져오기
     *
     * @param inapp : true - 일회성 상품, false - 정기구독
     */
    public void getSkuDetailList(boolean inapp) {
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        if (inapp) {
            params.setSkusList(INAPP_LIST).setType(BillingClient.SkuType.INAPP);
        } else {
            params.setSkusList(SUBS_LIST).setType(BillingClient.SkuType.SUBS);
        }

        billingClient.querySkuDetailsAsync(params.build(), (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                skuDetailsList.addAll(list);
            }
        });
    }

    // 실제 구입 처리를 하는 메소드
    public void purchase(int index, String sku) {
        if (connectStatus != ConnectStatus.connected) {
            showToast("구글 결제 서버와 접속이 끊어졌습니다");
            return;
        }

        SkuDetails skuDetails = null;
        for (SkuDetails details : skuDetailsList) {
            if (details.getSku().equals(sku)) {
                skuDetails = details;
                break;
            }
        }

        if (skuDetails != null) {
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            BillingResult result = billingClient.launchBillingFlow(activity, flowParams);

            if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                inappIndex = index;
            } else {
                showToast("상품을 찾을 수 없습니다");
            }
        } else {
            showToast("상품을 찾을 수 없습니다");
        }
    }

    // 결제 처리를 하는 메소드
    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        // 결제에 성공한 경우
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase pur : purchases) {
                Log.d(TAG, "결제 응답 데이터 :" + pur.getOriginalJson());
                try {
                    purchaseSendCount = 0;
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> dataMap = mapper.readValue(pur.getOriginalJson(), new TypeReference<Map<String, Object>>() {});
//                    dataMap.put("mode", pur.isAutoRenewing() ? "purchaseProc_sub" : "purchaseProc");
//                    dataMap.put("appType", "google");
//                    dataMap.put("inapp_idx", String.valueOf(inappIndex));

//                    sendPurchase(dataMap, pur);
                    sendResultInApp(pur.getOriginalJson());
                    acknowledgeCount = 0;
                    purchaseAcknowledge(pur);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "사용자에 의해 결제가 취소 되었습니다.");
        } else {
            Log.d(TAG, "결제가 취소 되었습니다. 종료코드 : " + billingResult.getResponseCode());
        }
    }

    private void sendResultInApp(String data) {
//        activity.sendToResultInApp(data);
    }

    // 구매내역 서버 전송
//    private void sendPurchase(Map<String, Object> dataMap, Purchase purchase) {
//        retrofitApi.setInappResult(dataMap).enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response) {
//                if (response.code() == 200) {
//                    acknowledgeCount = 0;
//                    purchaseAcknowledge(purchase);
//                } else if (purchaseSendCount < 5) {
//                    purchaseSendCount++;
//                    new Handler().postDelayed(() -> sendPurchase(dataMap, purchase), 2000);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t) {
//                t.printStackTrace();
//            }
//        });
//    }

    // 구매 확정처리
    private void purchaseAcknowledge(Purchase purchase) {
        if (!purchase.isAcknowledged() && acknowledgeCount < 5) {
            if (purchase.isAutoRenewing()) {    // 정기구독 상품
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        // TODO 구매처리 실패
                        acknowledgeCount++;
                        new Handler().postDelayed(() -> purchaseAcknowledge(purchase), 2000);
                    }
                });
            } else {
                ConsumeParams consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.consumeAsync(consumeParams, (billingResult, purchaseToken) -> {
                    if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        // TODO 구매처리 실패
                        acknowledgeCount++;
                        new Handler().postDelayed(() -> purchaseAcknowledge(purchase), 2000);
                    }
                });
            }
        }
    }
}
