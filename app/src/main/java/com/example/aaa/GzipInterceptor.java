package com.example.aaa;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GzipInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Добавляем заголовок Accept-Encoding, если его нет
        if (originalRequest.header("Accept-Encoding") == null) {
            originalRequest = originalRequest.newBuilder()
                    .header("Accept-Encoding", "gzip")
                    .build();
        }

        Response response = chain.proceed(originalRequest);

        // Если ответ сжат - распаковываем
        if ("gzip".equalsIgnoreCase(response.header("Content-Encoding"))) {
            assert response.body() != null;
            String content = decompressGzip(response.body().bytes());
            return response.newBuilder()
                    .body(ResponseBody.create(
                            response.body().contentType(),
                            content
                    ))
                    .removeHeader("Content-Encoding")
                    .build();
        }

        return response;
    }

    private String decompressGzip(byte[] compressed) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(bis);
        BufferedReader reader = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }
        reader.close();
        gis.close();
        bis.close();
        return output.toString();
    }
}