package com.example.lab1_204_12;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;


public class MagneticField implements SensorEventListener {
	TextView output;
	double [] magVals = new double[3];
	double [] maxVals = {0.0, 0.0 , 0.0};
	
	public MagneticField(TextView outputView){
		output = outputView;
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
			for(int i = 0; i<3; i++){
				magVals[i] = event.values[i];
				if(Math.abs(magVals[i]) > Math.abs(maxVals[i]))
					maxVals[i] = magVals[i];
			}
			output.setText(String.format("Magnetic Field:\n Current Reading: x = %2f , y = %2f , z = %2f \n MaxReading: x = %2f, y = %2f, z = %2f", magVals[0], magVals[1], magVals[2],maxVals[0],maxVals[1],maxVals[2]));
		}

	}

}
