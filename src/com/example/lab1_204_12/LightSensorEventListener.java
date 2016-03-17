package com.example.lab1_204_12;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;



public class LightSensorEventListener implements SensorEventListener {
	TextView output;
	double senseVal;
	double maxVal = 0.0;

	public LightSensorEventListener(TextView outputView){
		output=outputView;
	}
	public void onAccuracyChanged(Sensor s, int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent se) {
		if(se.sensor.getType() == Sensor.TYPE_LIGHT){
			senseVal=se.values[0];
			if(Math.abs(senseVal) > Math.abs(maxVal))
				maxVal = senseVal;
		}
		output.setText(String.format("Light Sensor:\n Current Reading = %2f\nMax Reading = %2f", senseVal, maxVal));

	}

}
