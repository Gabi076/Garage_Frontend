package com.example.garage_app.api;

import com.example.garage_app.model.Car;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CarApi {

    @POST("/api/cars/add")
    Call<Void> addCar(@Body Car car);

    @GET("/api/cars/user/{userId}")
    Call<List<Car>> getCarsByUser(@Path("userId") Long userId);

    @DELETE("/api/cars/delete/{carId}")
    Call<Void> deleteCar(@Path("carId") Long carId);

}
