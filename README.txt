Ravi Pinjala
Ubiquitous Computing
Assignment 1

For this assignment, I've implemented a simple step counter app. The step counting is implented using a basic zero-crossing strategy, where we count a step every time the acceleration goes further than a hardcoded threshold above the mean. The accelerometer data is stored in a circular buffer (implemented in TimeSeriesCircularBuffer.java). The step detection should work in any orientation, since I'm basing everything off the magnitude of the acceleration vector, rather than any one component. 

The plot is custom-made; I looked at the amount of boilerplate needed to use AndroidPlot, and thought "I could write this myself in less code than that!" The result isn't especially pretty or efficient, but it works nicely and is fast enough to keep up with the sensor data at SENSOR_DELAY_GAME on my device. The drawing code is in MainActivity.java, in the updatePlot function. 
