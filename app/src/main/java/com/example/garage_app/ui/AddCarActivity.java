package com.example.garage_app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.garage_app.R;
import com.example.garage_app.adapter.CarResultAdapter;
import com.example.garage_app.util.CarApiService;
import com.example.garage_app.model.Car;
import com.example.garage_app.repository.CarRepository;
import com.example.garage_app.repository.UserRepository;
import com.example.garage_app.util.TranslationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCarActivity extends AppCompatActivity {

    private EditText makeInput, modelInput, yearInput;
    private TextView noResultsText;
    private Car selectedCar;
    private String userId;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        makeInput = findViewById(R.id.input_make);
        modelInput = findViewById(R.id.input_model);
        yearInput = findViewById(R.id.input_year);
        noResultsText = findViewById(R.id.no_results_text);
        MaterialButton searchButton = findViewById(R.id.search_button);

        recyclerView = findViewById(R.id.recycler_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            UserRepository.getUserIdByEmail(firebaseUser.getEmail(), id -> userId = id);
        }

        searchButton.setOnClickListener(v -> {
            String make = makeInput.getText().toString().trim();
            String model = modelInput.getText().toString().trim();
            String year = yearInput.getText().toString().trim();

            CarApiService.searchCarDetails(make, model, year, results -> runOnUiThread(() -> {
                if (results != null && !results.isEmpty()) {
                    recyclerView.setAdapter(new CarResultAdapter(results, car -> {
                        selectedCar = car;
                        selectedCar.setOwnerId(Long.valueOf(userId));

                        TranslationHelper translator = new TranslationHelper();
                        translator.translateCarFields(selectedCar, new TranslationHelper.CarTranslationCallback() {
                            @Override
                            public void onSuccess(Car translatedCar) {
                                CarRepository.addCar(translatedCar, new Callback<>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        Toast.makeText(AddCarActivity.this, "Mașina a fost adăugată", Toast.LENGTH_SHORT).show();
                                        translator.cleanup();
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        Toast.makeText(AddCarActivity.this, "Eroare la salvare", Toast.LENGTH_SHORT).show();
                                        translator.cleanup();
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(AddCarActivity.this, "Eroare la traducere: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                translator.cleanup();
                            }
                        });
                    }));
                    recyclerView.setVisibility(View.VISIBLE);
                    noResultsText.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    noResultsText.setVisibility(View.VISIBLE);
                }
            }));
        });
    }
}
