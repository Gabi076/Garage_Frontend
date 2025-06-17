package com.example.garage_app.util;

import com.example.garage_app.model.Car;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CarApiService {
    private static final String API_KEY = "319254b6damshb55d49c1338511ep1e195fjsnbe89cfa5f398";
    private static final String BASE_URL = "https://cars-api10.p.rapidapi.com/trims";

    public static void searchCarDetails(String make, String model, String year, Consumer<List<Car>> callback) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL).newBuilder();

        if (!make.isEmpty()) urlBuilder.addQueryParameter("make", make);
        if (!model.isEmpty()) urlBuilder.addQueryParameter("model", model);
        if (!year.isEmpty()) urlBuilder.addQueryParameter("year", year);

        HttpUrl url = urlBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .header("x-rapidapi-key", API_KEY)
                .header("x-rapidapi-host", "cars-api10.p.rapidapi.com")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject root = new JSONObject(responseBody);
                        JSONObject data = root.getJSONObject("data");
                        JSONArray array = data.getJSONArray("Trims");

                        List<Car> cars = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            Car car = new Car();
                            car.setMake(obj.optString("model_make_display"));
                            car.setModel(obj.optString("model_name"));
                            car.setYear(obj.optString("model_year"));
                            car.setTrim(obj.optString("model_trim"));
                            car.setDoors(obj.optString("model_doors"));
                            car.setDisplacement(obj.optString("model_engine_cc"));
                            car.setEngineType(obj.optString("model_engine_type"));
                            car.setCylinders(obj.optString("model_engine_cyl"));
                            car.setFuelType(obj.optString("model_engine_fuel"));
                            car.setTransmission(obj.optString("model_transmission_type"));
                            car.setDrive(obj.optString("model_drive"));
                            cars.add(car);
                        }

                        callback.accept(cars);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        callback.accept(null);
                    }
                } else {
                    callback.accept(null);
                }
            }


            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                callback.accept(null);
            }
        });
    }
}