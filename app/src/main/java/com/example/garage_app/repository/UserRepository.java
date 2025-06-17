package com.example.garage_app.repository;

import android.util.Log;

import com.example.garage_app.api.UserApi;
import com.example.garage_app.model.User;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserRepository {
    private static final String BASE_URL = "https://garage-backend-vwe3.onrender.com";
    private static UserApi userApi;

    static {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        userApi = retrofit.create(UserApi.class);
    }

    public static void syncUser(User user) {
        Call<User> call = userApi.syncUser(user);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d("UserRepository", "User sync: " + response.body().getMail());
                } else {
                    Log.e("UserRepository", "Sync error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("UserRepository", "Network error: " + t.getMessage());
            }
        });
    }

    public static void getUserIdByEmail(String email, UserIdCallback callback) {
        Call<List<User>> call = userApi.getUsersByEmail(email);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User user = response.body().get(0);
                    if (user.getId() != null) {  // Verificare null pentru siguranță
                        callback.onUserIdReceived(user.getId().toString());
                    } else {
                        callback.onUserIdReceived(null);
                        Log.e("UserRepository", "User ID is null for email: " + email);
                    }
                } else {
                    callback.onUserIdReceived(null);
                    Log.e("UserRepository", "User not found or error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onUserIdReceived(null);
                Log.e("UserRepository", "Network error: " + t.getMessage());
            }
        });
    }
    public interface UserIdCallback {
        void onUserIdReceived(String userId);
    }

    public static void getUserByEmail(String email, UserCallback callback) {
        Call<List<User>> call = userApi.getUsersByEmail(email);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User user = response.body().get(0);
                    if (user != null) {
                        callback.onUserReceived(user);
                    } else {
                        callback.onUserReceived(null);
                        Log.e("UserRepository", "User object is null for email: " + email);
                    }
                } else {
                    callback.onUserReceived(null);
                    Log.e("UserRepository", "User not found or error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                callback.onUserReceived(null);
                Log.e("UserRepository", "Network error: " + t.getMessage());
            }
        });
    }
    public interface UserCallback {
        void onUserReceived(User user);
    }
}

