package com.example.aaa;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkHelper {
    private static OkHttpClient client;
    private static final int CONNECT_TIMEOUT = 15;
    private static final int READ_TIMEOUT = 30;
    private static final int WRITE_TIMEOUT = 15;
    private final String baseUrl = "https://finalprojectbackend-oi8b.onrender.com";
    private static NetworkHelper instance;

    public NetworkHelper(Context context) {
        // Настройка OkHttp клиента
        client = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized NetworkHelper getInstance(Context context, String baseUrl) {
        if (instance == null) {
            instance = new NetworkHelper(context);
        }
        return instance;
    }

    public void get(String endpoint, JsonApiCallback callback) {
        Request.Builder builder = new Request.Builder()
                .url(baseUrl + endpoint)
                .get();

        Request request = builder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        JSONObject json = new JSONObject(responseBody);
                        callback.onSuccess(json);
                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                } else {
                    callback.onError(response.code(), response.message());
                }
            }
        });
    }

    void onError(int code, String message) {
    }

    void onFailure(Exception e) {
    }

    public void onSuccess(JSONObject json) {
    }

    public interface JsonApiCallback {
        void onSuccess(JSONObject response);
        void onError(int statusCode, String message);
        void onFailure(Throwable t);
    }
}
