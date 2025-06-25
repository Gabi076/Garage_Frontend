package com.example.garage_app.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.garage_app.R;
import com.example.garage_app.model.Maintenance;
import com.example.garage_app.model.MaintenanceType;
import com.example.garage_app.repository.MaintenanceRepository;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaintenanceStatsActivity extends AppCompatActivity {

    private LineChart chart;
    private long carId;
    private List<Maintenance> maintenances = new ArrayList<>();

    private boolean showPrice = true;
    private View toggleIndicator;
    private TextView togglePrice, toggleKm;
    private FrameLayout toggleContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_stats);

        chart = findViewById(R.id.maintenance_chart);
        toggleIndicator = findViewById(R.id.toggle_indicator);
        togglePrice = findViewById(R.id.toggle_price);
        toggleKm = findViewById(R.id.toggle_km);
        toggleContainer = findViewById(R.id.y_axis_toggle);

        carId = getIntent().getLongExtra("carId", -1);

        loadMaintenances();

        toggleContainer.setOnClickListener(v -> {
            showPrice = !showPrice;
            animateToggle(showPrice);
            if (!maintenances.isEmpty()) {
                generateChart();
            }
        });

        toggleContainer.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            animateToggle(showPrice);
        });
    }

    private void animateToggle(boolean showPrice) {
        int width = toggleContainer.getWidth() / 2;
        toggleIndicator.animate()
                .x(showPrice ? 0 : width)
                .setDuration(200)
                .start();
    }

    private void loadMaintenances() {
        MaintenanceRepository.getMaintenancesByCar(carId, new Callback<List<Maintenance>>() {
            @Override
            public void onResponse(Call<List<Maintenance>> call, Response<List<Maintenance>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    maintenances = response.body();
                    generateChart();
                }
            }

            @Override
            public void onFailure(Call<List<Maintenance>> call, Throwable t) {
                Toast.makeText(MaintenanceStatsActivity.this, "Eroare la încărcare date", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateChart() {
        String selectedYAxis = showPrice ? "Preț" : "Kilometrii";

        List<YearMonth> lastSixMonths = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            lastSixMonths.add(currentMonth.minusMonths(i));
        }

        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM yy", Locale.getDefault());
        List<String> xAxisLabels = new ArrayList<>();
        for (YearMonth ym : lastSixMonths) {
            xAxisLabels.add(ym.format(labelFormatter));
        }

        Map<MaintenanceType, Map<YearMonth, Float>> dataMap = new LinkedHashMap<>();
        for (MaintenanceType type : MaintenanceType.values()) {
            Map<YearMonth, Float> monthMap = new LinkedHashMap<>();
            for (YearMonth ym : lastSixMonths) {
                monthMap.put(ym, 0f);
            }
            dataMap.put(type, monthMap);
        }

        for (Maintenance m : maintenances) {
            YearMonth ym = YearMonth.from(m.getDate());
            if (!lastSixMonths.contains(ym)) continue;

            MaintenanceType type = m.getTitle();
            float value = selectedYAxis.equals("Preț") ? m.getCost().floatValue() : m.getMileage().floatValue();

            Map<YearMonth, Float> monthMap = dataMap.get(type);
            monthMap.put(ym, monthMap.get(ym) + value);
        }

        List<ILineDataSet> dataSets = new ArrayList<>();
        int[] colors = getResources().getIntArray(R.array.chart_colors);
        int colorIndex = 0;

        for (MaintenanceType type : MaintenanceType.values()) {
            Map<YearMonth, Float> monthMap = dataMap.get(type);
            List<Entry> entries = new ArrayList<>();

            // Gestionez 0-urile la început: păstrez doar ultima valoare 0 din secvența inițială
            int lastZeroIndex = -1;
            for (int i = 0; i < lastSixMonths.size(); i++) {
                float y = monthMap.getOrDefault(lastSixMonths.get(i), 0f);
                if (y == 0f) lastZeroIndex = i;
                else break;  // se oprește secvența inițială de zero-uri
            }

            for (int i = 0; i < lastSixMonths.size(); i++) {
                float y = monthMap.getOrDefault(lastSixMonths.get(i), 0f);
                if (y > 0f) {
                    entries.add(new Entry(i, y));
                } else if (i == lastZeroIndex) {
                    // păstrăm doar ultima valoare 0 din secvența inițială
                    entries.add(new Entry(i, 0f));
                }
                // restul valorilor zero sunt ignorate
            }

            LineDataSet dataSet;
            if (!entries.isEmpty()) {
                dataSet = new LineDataSet(entries, type.name());
                dataSet.setDrawCircles(false);
                dataSet.setDrawValues(false);
                dataSet.setLineWidth(3f);
                dataSet.setColor(colors[colorIndex % colors.length]);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
            } else {
                List<Entry> dummyEntries = Collections.singletonList(new Entry(-1, 0f));
                dataSet = new LineDataSet(dummyEntries, type.name());
                dataSet.setColor(colors[colorIndex % colors.length]);
                dataSet.setDrawCircles(false);
                dataSet.setDrawValues(false);
                dataSet.setLineWidth(0f);
                dataSet.setMode(LineDataSet.Mode.LINEAR);
            }

            colorIndex++;
            dataSets.add(dataSet);
        }

        LineData lineData = new LineData(dataSets);
        chart.setData(lineData);

        // Setări legendă - poziționată în partea de sus, fără să se suprapună cu axa X
        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        legend.setForm(Legend.LegendForm.LINE);
        legend.setFormSize(12f);

        chart.setExtraTopOffset(30f);// ca să dea loc legendei sus
        chart.setExtraBottomOffset(29f);
        chart.setExtraRightOffset(20f);
        chart.setExtraLeftOffset(10f);
        chart.setDrawBorders(true);
        chart.setBorderWidth(1f);
        chart.setBorderColor(getResources().getColor(R.color.black));

        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < xAxisLabels.size()) ? xAxisLabels.get(index) : "";
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        // Tooltip personalizat (marker)
        MarkerView markerView = new MarkerView(chart.getContext(), R.layout.custom_marker_view) {
            @Override
            public void refreshContent(Entry e, Highlight highlight) {
                int xIndex = (int) e.getX();
                String monthLabel = (xIndex >= 0 && xIndex < xAxisLabels.size()) ? xAxisLabels.get(xIndex) : "";
                String label = "";
                // Găsește dataset-ul curent
                LineDataSet ds = (LineDataSet) chart.getData().getDataSetForEntry(e);
                if (ds != null) {
                    label = ds.getLabel();
                }
                // Actualizează textul în marker (în layout trebuie să ai un TextView cu id tvContent)
                TextView tvContent = findViewById(R.id.tvContent);
                tvContent.setText(label + "\n" + monthLabel + "\nValoare: " + e.getY());
                super.refreshContent(e, highlight);
            }

            @Override
            public MPPointF getOffset() {
                // poziționare marker deasupra punctului
                return new MPPointF(-(getWidth() / 2f), -getHeight());
            }
        };
        chart.setMarker(markerView);

        chart.getDescription().setText("Statistici întrețineri - " + selectedYAxis);
        chart.invalidate();
    }
}
