package com.example.garage_app.repository;

import com.example.garage_app.adapter.LocalDateAdapter;
import com.example.garage_app.api.MaintenanceApi;
import com.example.garage_app.model.Maintenance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MaintenanceRepository {
    private static final String BASE_URL = "https://garage-backend-vwe3.onrender.com";
    private static final MaintenanceApi api;

    static {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        api = retrofit.create(MaintenanceApi.class);
    }

    public static void getMaintenancesByCar(Long carId, Callback<List<Maintenance>> callback) {
        api.getMaintenancesByCar(carId).enqueue(callback);
    }

    public static void addMaintenance(Maintenance m, Callback<Void> callback) {
        api.addMaintenance(m).enqueue(callback);
    }

    public static void deleteMaintenance(Long maintenanceId, Callback<Void> callback) {
        api.deleteMaintenance(maintenanceId).enqueue(callback);
    }

    public static void updateMaintenance(Long maintenanceId, Maintenance maintenance, Callback<Void> callback) {
        api.updateMaintenance(maintenanceId, maintenance).enqueue(callback);
    }

    public static void exportMaintenances(Long carId, Callback<List<Maintenance>> callback) {
        api.exportMaintenances(carId).enqueue(callback);
    }

    public static void importMaintenances(Long carId, List<Maintenance> maintenances, Callback<Void> callback) {
        api.importMaintenances(carId, maintenances).enqueue(callback);
    }
}