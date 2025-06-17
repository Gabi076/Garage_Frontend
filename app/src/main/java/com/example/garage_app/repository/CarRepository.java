package com.example.garage_app.repository;

import com.example.garage_app.api.CarApi;
import com.example.garage_app.model.Car;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CarRepository {
    private static final String BASE_URL = "https://garage-backend-vwe3.onrender.com";
    private static final CarApi api;

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
        api = retrofit.create(CarApi.class);
    }

    public static void addCar(Car car, Callback<Void> callback) {
        api.addCar(car).enqueue(callback);
    }

    public static void getCarsByUser(Long userId, Callback<List<Car>> callback) {
        api.getCarsByUser(userId).enqueue(callback);
    }

    public static void deleteCar(Long carId, Callback<Void> callback) {
        api.deleteCar(carId).enqueue(callback);
    }
}