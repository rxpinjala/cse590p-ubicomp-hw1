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
    private long _updates;
    private long _initialTS;
    private TextView _stepCount;
    private TextView _updateCount;
    private TextView _dbgText;
    private SurfaceView _plot;
    private TimeSeriesCircularBuffer _sensorData;

    private Paint _paintGreenLine;
    private Paint _paintData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _listener = new MySensorListener(this, (SensorManager)getSystemService(SENSOR_SERVICE));

        _steps = 0;
        _lastStepTimestamp = 0;
        _updates = 0;
        _initialTS = 0;
        _sensorData = new TimeSeriesCircularBuffer(200);

        _stepCount = (TextView)findViewById(R.id.stepCount);
        _updateCount = (TextView)findViewById(R.id.updateCount);
        _dbgText = (TextView)findViewById(R.id.dbgText);
        _plot = (SurfaceView)findViewById(R.id.plot);

        _paintGreenLine = new Paint();
        _paintGreenLine.setARGB(255, 32, 255, 32);
        _paintData = new Paint();
        _paintData.setARGB(255, 255, 255, 255);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _listener.unregister((SensorManager) getSystemService(SENSOR_SERVICE));
        _listener = null;
        _stepCount = null;
        _dbgText = null;
        _plot = null;
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

    private static final float _displayCycleTime = 3.0f; // 3 seconds
    private static final long _displayCycleTimeNS = (long)(_displayCycleTime * 1000 * 1000 * 1000);

    // Given a timestamp, returns the position on the rotating display that we should draw at
    private float xPosFromTimestamp(long ts) {
        long elapsed = ts - _initialTS;
        elapsed = elapsed % _displayCycleTimeNS;
        float xPos = (float)elapsed / _displayCycleTimeNS;
        return _plot.getWidth() * xPos;
    }

    public void onReceiveSensorData(double d, long timestamp) {
        _updates++;
        if (_initialTS == 0)
            _initialTS = timestamp;

        _sensorData.add(timestamp, d);
        checkForStep();

        _stepCount.setText(Long.toString(_steps));
        _updateCount.setText(Long.toString(_updates));
        _dbgText.setText(Double.toString(d));
    }

    private static final double _plotYMin = 0.0f;
    private static final double _plotYMax = 20.0f;

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

            float xPos = xPosFromTimestamp(ts);
            float yPos = (float)yPosFromData(d);

            c.drawCircle(xPos, yPos, 2.0f, _paintData);
        }

        // Draw the green line that shows the current position
        float x = xPosFromTimestamp(tsMax);
        c.drawLine(x, 0, x, c.getHeight(), _paintGreenLine);

        sh.unlockCanvasAndPost(c);
    }

    private static final long _minStepIntervalNS = 300 * 1000 * 1000; // 200 msec
    private static final long _stepComparisonIntervalNS = 2 * 1000 * 1000 * 1000;
    private static final double _stepDetectionThreshold = 1.1; // 10% more than the mean over the interval

    // Records a step, if it's been long enough since the last one (to avoid jitter)
    private void tryRecordStep() {
        long timeSinceLastStep =_sensorData.getTimestamp(0) - _lastStepTimestamp;
        if (timeSinceLastStep < _minStepIntervalNS)
            return;

        _steps++;
        _lastStepTimestamp = _sensorData.getTimestamp(0);
    }

    private void checkForStep() {
        // take the mean over the last 2 seconds
        long tsMax = _sensorData.getTimestamp(0);
        double sum = 0.0;
        int count = 0;
        int max = _sensorData.size();
        for (int i = 0; i < max; i++) {
            long ts = _sensorData.getTimestamp(i);
            if (ts == 0)
                return; // not enough data

            if ((tsMax - ts) > _stepComparisonIntervalNS)
                break;

            double d = _sensorData.getData(i);
            sum += d;
            count++;
        }

        double mean = sum / count;
        if (_sensorData.getData(0) > mean * _stepDetectionThreshold)
            tryRecordStep();
    }
}
