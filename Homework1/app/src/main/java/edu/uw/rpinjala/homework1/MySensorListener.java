package edu.uw.rpinjala.homework1;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.*;

public class MySensorListener implements SensorEventListener
{
    private MainActivity _mainActivity;
    public MySensorListener(MainActivity mainActivity, SensorManager sensorManager) {
        _mainActivity = mainActivity;

        registerForAccelEvents(sensorManager);
    }

    private void registerForAccelEvents(SensorManager sensorManager) {
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() == 0)
            return;
        Sensor sensor = sensors.get(0);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregister(SensorManager sensorManager) {
        sensorManager.unregisterListener(this);
    }

    private double computeMagnitude(float[] values) {
        float sum = 0.0f;
        for (float f : values) {
            sum += f * f;
        }
        return Math.sqrt(sum);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double d = computeMagnitude(event.values);
        _mainActivity.onReceiveSensorData(d, event.timestamp);
        _mainActivity.updatePlot();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
