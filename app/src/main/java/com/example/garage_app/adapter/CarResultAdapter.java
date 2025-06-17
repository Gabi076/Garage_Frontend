package com.example.garage_app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.garage_app.R;
import com.example.garage_app.model.Car;

import java.util.List;
import java.util.function.Consumer;

public class CarResultAdapter extends RecyclerView.Adapter<CarResultAdapter.CarViewHolder> {
    private final List<Car> cars;
    private final Consumer<Car> onClick;

    public CarResultAdapter(List<Car> cars, Consumer<Car> onClick) {
        this.cars = cars;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car_result, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = cars.get(position);

        String makeModelYear = String.format("%s %s %s",
                car.getYear(),
                car.getMake(),
                car.getModel());
        holder.makeModelYear.setText(makeModelYear);

        // Format fuel type and displacement
        String fuelType = String.format("Fuel: %s", car.getFuelType());
        String trim = String.format("%s", car.getTrim());

        holder.fuelType.setText(fuelType);
        holder.trim.setText(trim);

        holder.itemView.setOnClickListener(v -> onClick.accept(car));
    }

    @Override
    public int getItemCount() {
        return cars.size();
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView makeModelYear, fuelType, trim;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            makeModelYear = itemView.findViewById(R.id.text_make_model_year);
            fuelType = itemView.findViewById(R.id.text_fuel_type);
            trim = itemView.findViewById(R.id.text_trim);
        }
    }
}

