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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultDisplay = findViewById(R.id.resultDisplay);
        calibrationTextField = findViewById(R.id.calibrationTextField);
        calibrationTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    offset = Double.parseDouble(s.toString());
                } catch (NumberFormatException e) {
                    offset = 0.0;
                }
                updateDisplay();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        addButton = findViewById(R.id.additionButton);
        subtractButton = findViewById(R.id.subtractButton);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        if (pressureSensor != null) {
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        addButton.setOnClickListener(v -> adjustOffset(STEP_HEIGHT));
        subtractButton.setOnClickListener(v -> adjustOffset(-STEP_HEIGHT));
    }

    private void adjustOffset(int step) {
        String input = calibrationTextField.getText().toString();
        if (!TextUtils.isEmpty(input)) {
            try {
                offset = Double.parseDouble(input);
            } catch (NumberFormatException e) {
                offset = 0.0;
            }
        } else {
            offset += step;
        }
        updateDisplay();
    }

    private void updateDisplay() {
        double displayAltitude = currentAltitude + offset;
        resultDisplay.setText(String.format("%.1f m", displayAltitude));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float pressure = event.values[0];
        currentAltitude = ALTITUDE_CONSTANT * (1.0 - Math.pow(pressure / SEA_LEVEL_PRESSURE_HPA, EXPONENT));
        updateDisplay();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}