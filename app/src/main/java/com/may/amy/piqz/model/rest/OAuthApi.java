package com.may.amy.piqz.model.rest;

import android.content.SharedPreferences;
import android.util.Log;

import com.loopj.android.http.Base64;
import com.may.amy.piqz.model.AuthResponseBody;
import com.may.amy.piqz.util.AppConstants;
import com.may.amy.piqz.util.AppUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

import javax.crypto.spec.OAEPParameterSpec;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by kuhnertj on 15.04.2016.
 */
public class OAuthApi {
    private static final String BASE_URL = "https://ssl.reddit.com";
    private static final String GRANT_TYPE = "https://oauth.reddit.com/grants/installed_client";
    private static final String TAG = OAuthApi.class.getSimpleName();

    private final AuthHelper mAuthHelper;

    public OAuthApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .client(getClient())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();

        mAuthHelper = retrofit.create(AuthHelper.class);
    }

    private OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        //Create the class AppConstants or simply put your client id:
                        String credentials = AppConstants.CLIENT_ID + ":" + "";
                        final String basic =
                                "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Authorization", basic)
                                .header("Accept", "application/json")
                                .method(original.method(), original.body());

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                })
                .build();
    }

    public void auth() {
        Call<AuthResponseBody> responseCall = mAuthHelper.auth(GRANT_TYPE, getDeviceId());
        responseCall.enqueue(responseCallback);
    }

    private String getDeviceId() {
        if (AppUtil.getInstance().getAppPreferences().getString(AppUtil.KEY_DEVICE_ID, null) == null) {
            String uuid = UUID.randomUUID().toString();
            AppUtil.getInstance().getAppPreferences().edit().putString(AppUtil.KEY_DEVICE_ID, uuid).apply();
        }
        return AppUtil.getInstance().getAppPreferences().getString(AppUtil.KEY_DEVICE_ID, null);
    }

    public void refreshTokenIfExpired() {
        int expiresIn = Integer.parseInt(AppUtil.getInstance().getAppPreferences().getString(AppUtil.KEY_EXPIRES_IN, "0"));
        if (expiresIn < Calendar.getInstance().getTimeInMillis()) {
            Call<AuthResponseBody> responseCall = mAuthHelper.auth(GRANT_TYPE, getDeviceId());
            responseCall.enqueue(responseCallback);
        }
    }

    Callback<AuthResponseBody> responseCallback = new Callback<AuthResponseBody>() {
        @Override
        public void onResponse(Call<AuthResponseBody> call, retrofit2.Response<AuthResponseBody> response) {
            Log.d(TAG, "onResponse - Call: " + call.toString() + "\nResponse: " + response.raw().toString());
            if (response.isSuccessful()) {
                if (response.body().getAccessToken() == null && response.errorBody() != null) {
                    Log.d(TAG, response.errorBody().toString());
                    return;
                }

                Log.d(TAG, "access token: " + response.body().getAccessToken() + "\nExpires in: " + response.body().getExpiresIn());
                SharedPreferences.Editor editor = AppUtil.getInstance().getAppPreferences().edit();
                editor.putString(AppUtil.KEY_TOKEN, response.body().getAccessToken());
                editor.putString(AppUtil.KEY_TOKEN_TYPE, response.body().getTokenType());
                editor.putString(AppUtil.KEY_EXPIRES_IN, response.body().getExpiresIn());
                editor.putString(AppUtil.KEY_SCOPE, response.body().getScope());
                editor.apply();
            }
        }

        @Override
        public void onFailure(Call<AuthResponseBody> call, Throwable t) {
            Log.e(TAG, t.toString());
        }
    };
}