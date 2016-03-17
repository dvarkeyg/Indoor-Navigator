package com.example.lab1_204_12;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;


public class RotationVector implements SensorEventListener {
	TextView output;
	double [] rotateVals = new double[3];
	double [] maxVals = {0.0,0.0,0.0};
	
	public RotationVector(TextView outputView){
		output=outputView;
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
			for(int i = 0; i<3; i++){
				rotateVals[i] = event.values[i];
				if(Math.abs(rotateVals[i])> Math.abs(maxVals[i]))
					maxVals[i] = rotateVals[i];
			}
		}
		output.setText(String.format("Rotation Vector:\n Current Reading: x = %2f , y = %2f , z = %2f \n MaxReading: x = %2f, y = %2f, z = %2f", rotateVals[0], rotateVals[1], rotateVals[2],maxVals[0],maxVals[1],maxVals[2]));
	}

}
