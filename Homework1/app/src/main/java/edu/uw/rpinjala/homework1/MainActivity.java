package edu.uw.rpinjala.homework1;

import android.graphics.*;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
    private MySensorListener _listener;

    private long _steps;
    private long _lastStepTimestamp;
    private long _updates; // number of sensor updates
    private TimeSeriesCircularBuffer _sensorData;
    private boolean _wasPositiveAccel; // used for zero-crossing step detection

    private TextView _stepCount;
    private TextView _updateCount;
    private TextView _currentAccel;
    private SurfaceView _plot;
    private Paint _paintData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _listener = new MySensorListener(this, (SensorManager)getSystemService(SENSOR_SERVICE));

        _steps = 0;
        _lastStepTimestamp = 0;
        _updates = 0;
        _sensorData = new TimeSeriesCircularBuffer(1000);
        _wasPositiveAccel = false;

        _stepCount = (TextView)findViewById(R.id.stepCount);
        _updateCount = (TextView)findViewById(R.id.updateCount);
        _currentAccel = (TextView)findViewById(R.id.dbgText);
        _plot = (SurfaceView)findViewById(R.id.plot);

        _paintData = new Paint();
        _paintData.setARGB(255, 255, 255, 255);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _listener.unregister((SensorManager) getSystemService(SENSOR_SERVICE));
        _listener = null;
        _stepCount = null;
        _updateCount = null;
        _currentAccel = null;
        _plot = null;
        _paintData = null;
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

    // the plot shows the last 3 seconds of data
    private static final long _displayCycleTimeNS = (long)(3.0 * 1000 * 1000 * 1000);

    public void onReceiveSensorData(float accel, long timestamp) {
        _updates++;

        _sensorData.add(timestamp, accel);
        checkForStep();

        // Update UI elements
        _stepCount.setText(Long.toString(_steps));
        _updateCount.setText(Long.toString(_updates));
        _currentAccel.setText(Float.toString(accel));
    }

    private static final double _plotYMin = 0.0f;
    private static final double _plotYMax = 20.0f;

    // Given a timestamp, returns the position on the X axis to draw at
    private float xPosFromTimestamp(long ts, long tsLatest) {
        long elapsed = tsLatest - ts;
        float xPos = (float)elapsed / _displayCycleTimeNS;
        return _plot.getWidth() * xPos;
    }

    // Given a data point, returns the position on the Y axis to draw at
    private double yPosFromData(double d) {
        if (d < _plotYMin || d > _plotYMax)
            return 0.0f;

        float height = _plot.getHeight();
        return height - (height * (d - _plotYMin) / (_plotYMax - _plotYMin));
    }

    public void updatePlot() {
        SurfaceHolder sh = _plot.getHolder();
        Canvas c = sh.lockCanvas();
        if (c == null)
            return;
        long tsMax = _sensorData.getTimestamp(0);

        // Blank the canvas
        c.drawARGB(255, 0, 0, 0);

        // Draw the sensor data
        int max = _sensorData.size();
        for (int i = 0; i < max; i++) {
            long ts = _sensorData.getTimestamp(i);
            double d = _sensorData.getData(i);
            if (ts == 0 || ts < (tsMax - _displayCycleTimeNS))
                break;

            float xPos = xPosFromTimestamp(ts, tsMax);
            float yPos = (float)yPosFromData(d);

            c.drawCircle(xPos, yPos, 2.0f, _paintData);
        }

        sh.unlockCanvasAndPost(c);
    }

    // Compute the mean of the data over the last (interval) ns
    private float computeMeanOverInterval(long interval) {
        int size = _sensorData.size();
        long tsLatest = _sensorData.getTimestamp(0);

        if (size == 0)
            throw new Error(); // let's just not do this

        int i = 0;
        float sum = 0.0f;
        while (i < size) {
            long ts = _sensorData.getTimestamp(i);
            if ((tsLatest - ts) > interval)
                break;

            sum += _sensorData.getData(i);
            i++;
        }

        return sum / i;
    }

    private static final long _zeroAccelIntervalNS = 5 * 1000 * 1000 * 1000; // use the mean over the last 5 seconds as "zero"
    private static final long _currentAccelIntervalNS = 50 * 1000 * 1000; // use the mean of the last 50 ms of data
    private static final long _minStepInterval = 200 * 1000 * 1000; // count a step at most every 200 ms
    private static final float _accelThreshold = 1.0f; // the current acceleration must be this far above the mean to count a step

    // Use a simple zero-crossing algorithm to detect steps
    private void checkForStep() {
        long tsNewest = _sensorData.getTimestamp(0);
        long tsOldest = _sensorData.getTimestamp(_sensorData.size() - 1);
        if (tsNewest - tsOldest < _zeroAccelIntervalNS) {
            if (_sensorData.size() == 1000)
                throw new Error(); // this would mean that our buffer is too small, and can't hold enough data to compute the mean
            return; // not enough data yet
        }

        float zeroAccel = computeMeanOverInterval(_zeroAccelIntervalNS);
        float currentAccel = computeMeanOverInterval(_currentAccelIntervalNS);
        float delta = currentAccel - zeroAccel; // acceleration relative to mean
        if (delta > _accelThreshold && !_wasPositiveAccel && (tsNewest - _lastStepTimestamp) > _minStepInterval) {
            // record a step!
            _steps++;
            _lastStepTimestamp = tsNewest;
        }

        _wasPositiveAccel = delta > _accelThreshold;
    }
}
