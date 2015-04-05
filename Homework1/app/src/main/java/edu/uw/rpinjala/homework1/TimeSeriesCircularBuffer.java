package edu.uw.rpinjala.homework1;

public class TimeSeriesCircularBuffer {
    private long _timestamp[];
    private double _data[];
    private int _current;

    public TimeSeriesCircularBuffer(int size) {
        _timestamp = new long[size];
        _data = new double[size];
        _current = size;
    }

    public int size() {
        return _timestamp.length;
    }

    public void add(long timestamp, double d) {
        _current++;
        if (_current >= size())
            _current = 0;

        _timestamp[_current] = timestamp;
        _data[_current] = d;
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

    public double getData(int i) {
        return _data[getIndex(i)];
    }
}
