package edu.uw.rpinjala.homework1;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
    private MySensorListener _listener;

    private long _updates;
    private TextView _stepCount;
    private SurfaceView _plot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _listener = new MySensorListener(this, (SensorManager)getSystemService(SENSOR_SERVICE));

        _updates = 0;
        _stepCount = (TextView)findViewById(R.id.stepCount);
        _plot = (SurfaceView)findViewById(R.id.plot);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void plotSensorData(double d, long timestamp) {
        _updates++;
        _stepCount.setText(Long.toString(_updates));


    }
}
