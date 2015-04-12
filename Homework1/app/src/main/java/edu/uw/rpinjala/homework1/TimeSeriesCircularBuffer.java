package edu.uw.rpinjala.homework1;

// Collects a fixed number of data points, and drops the oldest data when needed to make room
// for new data.
// Indexes are always in reverse chronological order, so that 0 is the newest data point, 1 is
// the second newest, etc.
public class TimeSeriesCircularBuffer {
    private long _timestamp[];
    private float _data[];
    private int _current;
    private int _count;

    public TimeSeriesCircularBuffer(int size) {
        _timestamp = new long[size];
        _data = new float[size];
        _current = size;
        _count = 0;
    }

    public int size() {
        return Math.min(_count, _timestamp.length);
    }

    public void add(long timestamp, float f) {
        _current++;
        if (_current >= size())
            _current = 0;

        _count++;
        _timestamp[_current] = timestamp;
        _data[_current] = f;
    }

    private int getIndex(int i) {
        if (i < 0 || i >= size())
            throw new Error("invalid index passed");

        if (i <= _current)
            return _current - i;

        return (_current + size()) - i;
    }

    public long getTimestamp(int i) {
        return _timestamp[getIndex(i)];
    }

    public float getData(int i) {
        return _data[getIndex(i)];
    }
}
