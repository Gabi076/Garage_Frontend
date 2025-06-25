package com.example.garage_app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.garage_app.R;
import com.example.garage_app.model.Maintenance;
import com.example.garage_app.model.MaintenanceType;
import com.example.garage_app.repository.MaintenanceRepository;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMaintenanceActivity extends AppCompatActivity {

    private EditText descriptionEditText, mileageEditText, costEditText, dateEditText;
    private AutoCompleteTextView titleAutoComplete;
    private MaterialButton saveButton;
    private Maintenance existingMaintenance;
    private LocalDate selectedDate = LocalDate.now();
    private Long carId;
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_maintenance);

        titleAutoComplete = findViewById(R.id.spinner_maintenance_title);

        descriptionEditText = findViewById(R.id.edit_maintenance_description);
        mileageEditText = findViewById(R.id.edit_maintenance_mileage);
        costEditText = findViewById(R.id.edit_maintenance_cost);
        dateEditText = findViewById(R.id.edit_maintenance_date);
        saveButton = findViewById(R.id.btn_save_maintenance);

        existingMaintenance = (Maintenance) getIntent().getSerializableExtra("maintenance");

        String[] types = {"Ulei", "Revizie", "Anvelope", "Piese", "Altele"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, types);
        titleAutoComplete.setAdapter(typeAdapter);
        titleAutoComplete.setThreshold(1);

        if (existingMaintenance != null) {
            String existingTitle = existingMaintenance.getTitle().name();
            titleAutoComplete.setText(existingTitle, false);
            titleAutoComplete.setEnabled(false);

            descriptionEditText.setText(existingMaintenance.getDescription());
            mileageEditText.setText(String.valueOf(existingMaintenance.getMileage()));
            costEditText.setText(String.valueOf(existingMaintenance.getCost()));
            selectedDate = existingMaintenance.getDate();
            updateDateEditText();
        }

        carId = getIntent().getLongExtra("carId", -1);
        if (carId == -1) {
            Toast.makeText(this, "Mașină invalidă", Toast.LENGTH_SHORT).show();
            finish();
        }

        dateEditText.setOnClickListener(v -> showDatePicker());

        saveButton.setOnClickListener(v -> {
            if (existingMaintenance != null) {
                updateExistingMaintenance();
            } else {
                createNewMaintenance();
            }
        });

        updateDateEditText();
    }

    private void updateExistingMaintenance() {
        existingMaintenance.setDescription(descriptionEditText.getText().toString());
        existingMaintenance.setMileage(Integer.parseInt(mileageEditText.getText().toString()));
        existingMaintenance.setCost(Double.parseDouble(costEditText.getText().toString()));
        existingMaintenance.setDate(selectedDate);

        MaintenanceRepository.updateMaintenance(existingMaintenance.getId(), existingMaintenance, new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddMaintenanceActivity.this, "Actualizat cu succes", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddMaintenanceActivity.this, "Eroare la actualizare", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddMaintenanceActivity.this, "Eroare: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewMaintenance() {
        String titleString = titleAutoComplete.getText().toString().toUpperCase(Locale.ROOT);
        MaintenanceType title = MaintenanceType.valueOf(titleString);
        String description = descriptionEditText.getText().toString();
        String mileageText = mileageEditText.getText().toString();
        String costText = costEditText.getText().toString();

        if (mileageText.isEmpty() || costText.isEmpty()) {
            Toast.makeText(this, "Completează numărul de kilometri și costul", Toast.LENGTH_SHORT).show();
            return;
        }

        double cost = Double.parseDouble(costText);
        int mileage = mileageText.isEmpty() ? 0 : Integer.parseInt(mileageText);
        Maintenance maintenance = new Maintenance(null, title, description, selectedDate, cost, mileage, carId);
        MaintenanceRepository.addMaintenance(maintenance, new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddMaintenanceActivity.this, "Întreținere salvată", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddMaintenanceActivity.this, "Eroare la salvare", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AddMaintenanceActivity.this, "Eroare: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
            updateDateEditText();
        },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDateEditText() {
        dateEditText.setText(selectedDate.format(displayFormatter));
    }
}