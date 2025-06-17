package com.example.garage_app.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.garage_app.R;
import com.example.garage_app.adapter.CarPagerAdapter;
import com.example.garage_app.model.Car;
import com.example.garage_app.repository.CarRepository;
import com.example.garage_app.repository.UserRepository;
import com.example.garage_app.ui.AddCarActivity;
import com.example.garage_app.ui.MaintenanceListActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarListFragment extends Fragment {
    private ViewPager2 viewPager;
    private TextView noCarsText;
    private CarPagerAdapter adapter;
    private List<Car> carList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_list, container, false);
        MaterialButton addCarButton = view.findViewById(R.id.add_car_fab);
        viewPager = view.findViewById(R.id.car_view_pager);
        noCarsText = view.findViewById(R.id.no_cars_text);

        adapter = new CarPagerAdapter(carList, getContext(), new CarPagerAdapter.OnCarActionListener() {
            @Override
            public void onDelete(Car car) {
                new AlertDialog.Builder(CarListFragment.this.getContext())
                        .setTitle("Confirmare ștergere")
                        .setMessage("Sigur doriți să ștergeți această mașină?")
                        .setPositiveButton("Da", (dialog, which) -> {
                            CarRepository.deleteCar(car.getId(), new Callback<>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        carList.remove(car);
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Log.e("DeleteCar", "Eroare la ștergerea mașinii", t);
                                }
                            });
                        })
                        .setNegativeButton("Nu", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

            @Override
            public void onViewMaintenance(Car car) {
                Intent intent = new Intent(getActivity(), MaintenanceListActivity.class);
                intent.putExtra("carId", car.getId());
                startActivity(intent);
            }

        });
        viewPager.setOffscreenPageLimit(3);
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40)); // spațiu între carduri
        transformer.addTransformer((page, position) -> {
            float scale = 0.85f + (1 - Math.abs(position)) * 0.15f;
            page.setScaleY(scale);
            page.setAlpha(0.6f + (1 - Math.abs(position)) * 0.4f);
        });

        viewPager.setPageTransformer(transformer);


        viewPager.setAdapter(adapter);

        addCarButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddCarActivity.class);
            startActivity(intent);
        });

        loadCarsForUser();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCarsForUser();
    }

    private void loadCarsForUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return;

        String email = firebaseUser.getEmail();
        UserRepository.getUserIdByEmail(email, userId -> {
            if (userId != null) {
                CarRepository.getCarsByUser(Long.valueOf(userId), new Callback<>() {
                    @Override
                    public void onResponse(Call<List<Car>> call, Response<List<Car>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            carList.clear();
                            carList.addAll(response.body());
                            adapter.notifyDataSetChanged();

                            if (carList.isEmpty()) {
                                noCarsText.setVisibility(View.VISIBLE);
                                viewPager.setVisibility(View.GONE);
                            } else {
                                noCarsText.setVisibility(View.GONE);
                                viewPager.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Car>> call, Throwable t) {
                        Log.e("CarList", "Eroare la încărcarea mașinilor", t);
                        noCarsText.setVisibility(View.VISIBLE);
                        viewPager.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
}


