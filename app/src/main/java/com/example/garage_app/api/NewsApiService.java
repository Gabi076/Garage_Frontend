package com.example.garage_app.api;

import com.example.garage_app.model.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {
    @GET("v2/everything")
    Call<NewsResponse> getCarNews(
            @Query("q") String query,
            @Query("language") String language,
            @Query("apiKey") String apiKey
    );
}
