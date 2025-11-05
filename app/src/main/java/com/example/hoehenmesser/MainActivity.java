package com.example.hoehenmesser;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final double SEA_LEVEL_PRESSURE_HPA = 1013.25;
    private static final double ALTITUDE_CONSTANT = 44330.0;
    private static final double EXPONENT = 0.1903;
    private static final int STEP_HEIGHT = 10;
    private SensorManager sensorManager;
    private Sensor pressureSensor;
    private double currentAltitude = 0.0;
    private double offset = 0.0;
    private TextView resultDisplay;
    private EditText calibrationTextField;
    private Button addButton, subtractButton;
    private boolean hasSensorReading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultDisplay = findViewById(R.id.resultDisplay);
        calibrationTextField = findViewById(R.id.calibrationTextField);
        addButton = findViewById(R.id.additionButton);
        subtractButton = findViewById(R.id.subtractButton);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        if (pressureSensor == null) {
            Log.d("SensorCheck", "Kein Drucksensor");
            resultDisplay.setText("Kein Drucksensor gefunden!");
        }

        calibrationTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    offset = 0.0;
                    updateDisplay();
                    return;
                }
                try {
                    offset = Double.parseDouble(s.toString());
                    updateDisplay();
                } catch (NumberFormatException e) {}
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        addButton.setOnClickListener(v -> {
            offset += STEP_HEIGHT;
            calibrationTextField.setText(String.valueOf(offset));
            updateDisplay();
        });

        subtractButton.setOnClickListener(v -> {
            offset -= STEP_HEIGHT;
            calibrationTextField.setText(String.valueOf(offset));
            updateDisplay();
        });
    }

    private void updateDisplay() {
        if (!hasSensorReading) {
            return;
        }
        double displayAltitude = currentAltitude + offset;
        resultDisplay.setText(String.format("%.1f m", displayAltitude));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        hasSensorReading = true;
        float pressure = event.values[0];
        currentAltitude = ALTITUDE_CONSTANT * (1.0 - Math.pow(pressure / SEA_LEVEL_PRESSURE_HPA, EXPONENT));
        updateDisplay();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pressureSensor != null) {
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
