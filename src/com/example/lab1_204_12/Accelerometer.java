package com.example.lab1_204_12;

import ca.uwaterloo.lab1_204_12.LineGraphView;
import ca.uwaterloo.lab1_204_12.MapView;
import ca.uwaterloo.lab1_204_12.NavigationalMap;
import ca.uwaterloo.lab1_204_12.PositionListener;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.TextView;

public class Accelerometer implements SensorEventListener {
	TextView output;
	LineGraphView graphOutput;
	float[] prevVals = { 0.0f, 0.0f, 0.0f };
	float[] accelVals = { 0.0f, 0.0f, 0.0f };
	float[] tempVals = { 0.0f, 0.0f, 0.0f };
	double[] maxVals = { 0.0, 0.0, 0.0 };
	float[] lowVals = { 0.0f, 0.0f, 0.0f };
	float[] avgVals = new float[20];
	float orientation = 0.0f;
	float offsetAzimuth = 0.0f;
	float azimuth = 0.0f;
	float displaceNS = 0.0f, displaceEW = 0.0f;
	FSM fsm = new FSM();
	int state = 0;
	int stepCount = 0;
	State finiteState = State.STABLE;
	TextView output2;
	PositionListener pl;
	NavigationalMap nm;
	MapView mv;
	float stepSize = 1.0f;

	public enum State {
		STABLE, RISING, FALLING, NEGATIVE;
	}

	public Accelerometer(TextView outputView, TextView outputView2,
			LineGraphView grapher, PositionListener pl, MapView map,
			NavigationalMap checkMap) {
		output = outputView;
		output2 = outputView2;
		graphOutput = grapher;
		this.pl = pl;
		mv = map;
		nm = checkMap;
	}
	public float getOrient(){
		return orientation;
	}

	public void resetSteps() {
		stepCount = 0;
		displaceNS = 0.0f;
		displaceEW = 0.0f;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			prevVals[2] = accelVals[2];

			if (event.values[2] > maxVals[2])
				maxVals[2] = event.values[2];
			else if (event.values[2] < lowVals[2])
				lowVals[2] = event.values[2];

			tempVals[2] = lowPass(event.values[2]);
			accelVals[2] = tempVals[2];

		} else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			azimuth = event.values[0];
			orientation = azimuth - offsetAzimuth;
			if (orientation < 0) {
				orientation += 360;
			}
		}

		graphOutput.addPoint(accelVals);

		if (finiteState == State.STABLE) { // beginning of the Finite State
											// machine, stable state
			if (accelVals[2] > 1.0 && accelVals[2] < 2.5) {
				finiteState = State.RISING;
			} else if (accelVals[2] > 2.5) {
				finiteState = State.STABLE;
			}
		}
		if (finiteState == State.RISING) { // rising state
			if (accelVals[2] > 1.5 && accelVals[2] < 2.5)
				finiteState = State.FALLING;
			else if (accelVals[2] < 1.5 && accelVals[2] > 0.5)
				finiteState = State.RISING;
			else if (accelVals[2] > 2.5 || accelVals[2] < 0.5)
				finiteState = State.STABLE;
		}
		if (finiteState == State.FALLING) { // falling state
			if (accelVals[2] > 0.0 && accelVals[2] < 2.5) {
				finiteState = State.NEGATIVE;
			} else if (accelVals[2] > 1.5 && accelVals[2] < 2.5) {
				finiteState = State.FALLING;
			} else if (accelVals[2] < 0.0) {
				finiteState = State.STABLE;
			}
		}
		if (finiteState == State.NEGATIVE) { // negative state
			if (accelVals[2] > -2.0 && accelVals[2] < 0.0) {
				stepCount++;
				displaceNS += Math.cos(orientation * Math.PI / 180);
				displaceEW += Math.sin(orientation * Math.PI / 180);
				finiteState = State.STABLE;
				if (pl.getEnd() != null && pl.getOrigin() != null
						&& pl.getUser() != null) {
					PointF userNext = new PointF(pl.getUser().x - displaceEW
							* stepSize, pl.getUser().y + displaceNS * stepSize);
					if (!nm.calculateIntersections(pl.getUser(), userNext)
							.isEmpty())
						userNext = pl.getUser();
					Log.d("Point", "stepped, building path " + userNext.x + " "
							+ userNext.y);
					pl.setUser(userNext);
					mv.setUserPoint(userNext);
					pl.buildPath(userNext);
					displaceNS = 0.0f;
					displaceEW = 0.0f;
				} else if (accelVals[2] > 0.0 && accelVals[2] < 2.5)
					finiteState = State.NEGATIVE;
				else if (accelVals[2] > 2.5 || accelVals[2] < -2.0)
					finiteState = State.STABLE;
			}
		}
		output.setText(String
				.format("Accelerometer:\n StepCount = %d , State = %d, z = %2f \n MaxReading: z = %2f LowReading: z= %2f",
						stepCount, state, accelVals[2], maxVals[2], lowVals[2]));
		output2.setText("Orientation angle :" + orientation
				+ " \n Displacement North :" + displaceNS
				+ ", Displacement East :" + displaceEW);
	}

	public float lowPass(float in) { // lowPass filter for the input values
		float out = accelVals[2];
		float a = 0.25f;
		out = a * in + (1 - a) * out;
		accelVals[2] = out;
		return out;
	}

	public void calibrate() {
		offsetAzimuth = azimuth;
	}
}
