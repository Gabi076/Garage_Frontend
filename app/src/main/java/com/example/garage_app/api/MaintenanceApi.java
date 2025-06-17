package com.example.garage_app.api;

import com.example.garage_app.model.Maintenance;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface MaintenanceApi {

    @GET("/api/maintenances/car/{carId}")
    Call<List<Maintenance>> getMaintenancesByCar(@Path("carId") Long carId);

    @POST("/api/maintenances/add")
    Call<Void> addMaintenance(@Body Maintenance maintenance);

    @PUT("/api/maintenances/update/{maintenanceId}")
    Call<Void> updateMaintenance(@Path("maintenanceId") Long id, @Body Maintenance maintenance);

    @DELETE("/api/maintenances/delete/{maintenanceId}")
    Call<Void> deleteMaintenance(@Path("maintenanceId") Long id);

    @GET("/api/maintenances/export/{carId}")
    Call<List<Maintenance>> exportMaintenances(@Path("carId") Long carId);

    @POST("/api/maintenances/import/{carId}")
    Call<Void> importMaintenances(@Path("carId") Long carId, @Body List<Maintenance> maintenances);
}
