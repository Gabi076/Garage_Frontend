package com.example.garage_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.garage_app.R;
import com.example.garage_app.model.Maintenance;

import java.util.List;
import java.time.format.DateTimeFormatter;

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.MaintenanceViewHolder> {

    public interface OnMaintenanceActionListener {
        void onEdit(Maintenance m);
        void onDelete(Maintenance m);
    }

    private List<Maintenance> maintenances;
    private Context context;
    private OnMaintenanceActionListener listener;

    public MaintenanceAdapter(List<Maintenance> maintenances, Context context, OnMaintenanceActionListener listener) {
        this.maintenances = maintenances;
        this.context = context;
        this.listener = listener;
        sortMaintenancesByDate();
    }

    @NonNull
    @Override
    public MaintenanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_maintenance, parent, false);
        return new MaintenanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaintenanceViewHolder holder, int position) {
        Maintenance m = maintenances.get(position);
        holder.title.setText(m.getTitle().toString());
        holder.description.setText(m.getDescription());
        holder.mileage.setText("Odometru: " + m.getMileage() + " km");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        holder.date.setText(m.getDate().format(formatter));
        holder.cost.setText("Cost: " + m.getCost() + " RON");

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(m));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(m));
    }

    @Override
    public int getItemCount() {
        return maintenances.size();
    }

    static class MaintenanceViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, mileage, date, cost;
        ImageButton btnEdit, btnDelete;

        public MaintenanceViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.maintenance_title);
            description = itemView.findViewById(R.id.maintenance_description);
            mileage = itemView.findViewById(R.id.maintenance_mileage);
            date = itemView.findViewById(R.id.maintenance_date);
            cost = itemView.findViewById(R.id.maintenance_cost);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    public void sortMaintenancesByDate() {
        if (maintenances != null && !maintenances.isEmpty()) {
            maintenances.sort((m1, m2) -> m2.getDate().compareTo(m1.getDate()));
            notifyDataSetChanged();
        }
    }

    public void updateList(List<Maintenance> newList) {
        this.maintenances.clear();
        this.maintenances.addAll(newList);
        sortMaintenancesByDate();
        notifyDataSetChanged();
    }
}

