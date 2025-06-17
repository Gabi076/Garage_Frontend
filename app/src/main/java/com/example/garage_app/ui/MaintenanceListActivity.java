package com.example.garage_app.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.garage_app.R;
import com.example.garage_app.adapter.MaintenanceAdapter;
import com.example.garage_app.model.Maintenance;
import com.example.garage_app.repository.MaintenanceRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaintenanceListActivity extends AppCompatActivity {
    private List<Maintenance> maintenanceList = new ArrayList<>();
    private MaintenanceAdapter adapter;
    private long carId;

    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int IMPORT_REQUEST_CODE = 101;

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Permisiune refuzată, exportul nu poate fi realizat", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_list);

        carId = getIntent().getLongExtra("carId", -1);
        RecyclerView recyclerView = findViewById(R.id.maintenance_recycler);
        adapter = new MaintenanceAdapter(maintenanceList, this, new MaintenanceAdapter.OnMaintenanceActionListener() {
            @Override
            public void onEdit(Maintenance m) {
                Intent intent = new Intent(MaintenanceListActivity.this, AddMaintenanceActivity.class);
                intent.putExtra("carId", carId);
                intent.putExtra("maintenance", m);
                startActivity(intent);
            }

            @Override
            public void onDelete(Maintenance m) {
                new AlertDialog.Builder(MaintenanceListActivity.this)
                        .setTitle("Confirmare ștergere")
                        .setMessage("Sigur doriți să ștergeți această întreținere?")
                        .setPositiveButton("Da", (dialog, which) -> {
                            MaintenanceRepository.deleteMaintenance(m.getId(), new Callback<>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        maintenanceList.remove(m);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(MaintenanceListActivity.this, "Șters cu succes", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MaintenanceListActivity.this, "Eroare la ștergere", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(MaintenanceListActivity.this, "Eroare: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Nu", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_add_maintenance).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMaintenanceActivity.class);
            intent.putExtra("carId", carId);
            startActivity(intent);
        });

        findViewById(R.id.btn_export_csv).setOnClickListener(v -> exportMaintenances());
        findViewById(R.id.btn_import_csv).setOnClickListener(v -> importMaintenances());

        loadMaintenances();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMaintenances();
    }

    private void loadMaintenances() {
        MaintenanceRepository.getMaintenancesByCar(carId, new Callback<>() {
            @Override
            public void onResponse(Call<List<Maintenance>> call, Response<List<Maintenance>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    maintenanceList.clear();
                    maintenanceList.addAll(response.body());
                    adapter.sortMaintenancesByDate();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Maintenance>> call, Throwable t) {
                Log.e("Maintenance", "Eroare la încărcare", t);
            }
        });
    }

    private void exportMaintenances() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            checkStoragePermission();
        }

        MaintenanceRepository.exportMaintenances(carId, new Callback<>() {
            @Override
            public void onResponse(Call<List<Maintenance>> call, Response<List<Maintenance>> response) {
                if (response.isSuccessful()) {
                    List<Maintenance> maintenances = response.body();
                    String json = new Gson().toJson(maintenances);
                    saveJsonToDownloads(json, "maintenances_export.json");
                } else {
                    Toast.makeText(MaintenanceListActivity.this, "Eroare la export", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Maintenance>> call, Throwable t) {
                Toast.makeText(MaintenanceListActivity.this, "Eroare: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ExportError", "Failed to export", t);
            }
        });
    }
    private void saveJsonToDownloads(String json, String fileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveJsonUsingMediaStore(json, fileName);
        } else {
            saveJsonLegacy(json, fileName);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void saveJsonUsingMediaStore(String json, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/json");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver resolver = getContentResolver();
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream outputStream = resolver.openOutputStream(uri)) {
                outputStream.write(json.getBytes());
                Toast.makeText(this, "Export realizat în Downloads!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Eroare la salvarea fișierului", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @SuppressWarnings("deprecation")
    private void saveJsonLegacy(String json, String fileName) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, fileName);

        if (file.exists()) {
            new AlertDialog.Builder(this)
                    .setTitle("Fișier existent")
                    .setMessage("Suprascriem fișierul existent?")
                    .setPositiveButton("Da", (d, w) -> writeFile(file, json))
                    .setNegativeButton("Nu", null)
                    .show();
        } else {
            writeFile(file, json);
        }
    }
    private void writeFile(File file, String json) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(json.getBytes());
            Toast.makeText(this, "Export realizat în Downloads!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Eroare la salvare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importMaintenances() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, IMPORT_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            readJsonFromUri(uri);
        }
    }
    private void readJsonFromUri(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            try (InputStream inputStream = resolver.openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String json = stringBuilder.toString();

                Gson gson = new Gson();
                Type maintenanceListType = new TypeToken<List<Maintenance>>() {
                }.getType();
                List<Maintenance> maintenances = gson.fromJson(json, maintenanceListType);

                if (maintenances == null || maintenances.isEmpty()) {
                    Toast.makeText(this, "Fișierul nu conține date valide", Toast.LENGTH_SHORT).show();
                    return;
                }

                MaintenanceRepository.importMaintenances(carId, maintenances, new Callback<>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(MaintenanceListActivity.this, "Import realizat cu succes!", Toast.LENGTH_SHORT).show();
                            loadMaintenances();
                        } else {
                            Toast.makeText(MaintenanceListActivity.this, "Eroare la import", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(MaintenanceListActivity.this, "Eroare: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("ImportError", "Failed to import", t);
                    }
                });
            }
        } catch (IOException e) {
            Toast.makeText(this, "Eroare la citirea fișierului: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ImportError", "File read error", e);
        } catch (Exception e) {
            Toast.makeText(this, "Format fișier invalid", Toast.LENGTH_SHORT).show();
            Log.e("ImportError", "JSON parse error", e);
        }
    }
}