package edu.uw.rpinjala.homework1;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.util.*;

// Listens for accelerometer data and passes it on to the main activity
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

    private float computeMagnitude(float[] values) {
        float sum = 0.0f;
        for (float f : values) {
            sum += f * f;
        }
        return (float)Math.sqrt(sum);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float f = computeMagnitude(event.values);
        _mainActivity.onReceiveSensorData(f, event.timestamp);
        _mainActivity.updatePlot();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
