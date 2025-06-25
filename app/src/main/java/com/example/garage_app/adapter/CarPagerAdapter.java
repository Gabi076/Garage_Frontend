package com.example.garage_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.garage_app.R;
import com.example.garage_app.model.Car;

import java.util.List;

public class CarPagerAdapter extends RecyclerView.Adapter<CarPagerAdapter.CarViewHolder> {

    private List<Car> cars;
    private Context context;
    private OnCarActionListener listener;

    public interface OnCarActionListener {
        void onDelete(Car car);
        void onViewMaintenance(Car car);
    }

    public CarPagerAdapter(List<Car> cars, Context context, OnCarActionListener listener) {
        this.cars = cars;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car_card, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = cars.get(position);

        holder.makeModel.setText(String.format("%s %s", car.getMake(), car.getModel()));
        holder.yearTrim.setText(String.format("%s %s", car.getYear(), car.getTrim()));
        holder.driveType.setText(car.getDrive());
        holder.transmission.setText(car.getTransmission());
        holder.engineType.setText(car.getEngineType());
        holder.cylinders.setText(car.getCylinders());
        holder.displacement.setText(String.format("%s cc", car.getDisplacement()));
        holder.fuel.setText(car.getFuelType());
        holder.doors.setText(car.getDoors());

        Glide.with(context)
                .load("https://i.ibb.co/JwVN05ng/car.png")
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(holder.carImage);

        holder.deleteBtn.setOnClickListener(v -> listener.onDelete(car));
        holder.maintenanceBtn.setOnClickListener(v -> listener.onViewMaintenance(car));
    }

    @Override
    public int getItemCount() {
        return cars.size();
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView makeModel, yearTrim, driveType, transmission, engineType, cylinders, displacement, fuel, doors;
        ImageView carImage;
        Button deleteBtn, maintenanceBtn;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.car_card);
            makeModel = itemView.findViewById(R.id.car_make_model);
            yearTrim = itemView.findViewById(R.id.car_year_trim);
            driveType = itemView.findViewById(R.id.drive_type);
            transmission = itemView.findViewById(R.id.transmission);
            engineType = itemView.findViewById(R.id.engine_type);
            cylinders = itemView.findViewById(R.id.cylinders);
            displacement = itemView.findViewById(R.id.displacement);
            fuel = itemView.findViewById(R.id.fuel_type);
            doors = itemView.findViewById(R.id.doors);
            deleteBtn = itemView.findViewById(R.id.btn_delete_car);
            maintenanceBtn = itemView.findViewById(R.id.btn_maintenance);
            carImage = itemView.findViewById(R.id.car_image);
        }
    }
}