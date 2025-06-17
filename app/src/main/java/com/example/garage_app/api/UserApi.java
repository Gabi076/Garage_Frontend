package com.example.garage_app.api;

import com.example.garage_app.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserApi {
    @POST("/api/user/sync")
    Call<User> syncUser(@Body User user);

    @GET("/api/user/users")
    Call<List<User>> getUsersByEmail(@Query("email") String email);
}