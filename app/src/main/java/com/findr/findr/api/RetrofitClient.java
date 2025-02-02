package com.findr.findr.api;

import java.io.IOException;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://172.20.16.1:8080/api/";
    private static Retrofit retrofit;
    private static String currentUsername;
    private static String currentPassword;

    public static Retrofit getInstance(String username, String password) {
        // Reset retrofit if credentials change
        if (retrofit == null || !username.equals(currentUsername) || !password.equals(currentPassword)) {
            currentUsername = username;
            currentPassword = password;

            // Create an Interceptor to add the Basic Authentication header
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();
                    Request.Builder builder = originalRequest.newBuilder()
                            .header("Authorization", Credentials.basic(username, password));
                    Request authenticatedRequest = builder.build();
                    return chain.proceed(authenticatedRequest);
                }
            };

            // Build the OkHttpClient with the Interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();

            // Create the Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
