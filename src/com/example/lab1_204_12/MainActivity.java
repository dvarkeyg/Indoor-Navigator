package com.example.lab1_204_12;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import ca.uwaterloo.lab1_204_12.LineGraphView;
import ca.uwaterloo.lab1_204_12.MapLoader;
import ca.uwaterloo.lab1_204_12.MapView;
import ca.uwaterloo.lab1_204_12.NavigationalMap;
import ca.uwaterloo.lab1_204_12.PositionListener;
import ca.uwaterloo.lab1_204_12.VectorUtils;

public class MainActivity extends ActionBarActivity {

	static LineGraphView graph;
	static MapView mapView;
	static SensorEventListener accel;
	static PointF pointOrigin, pointEnd, pointUser = null;
	static NavigationalMap checkMap;
	static TextView directionsOut;
	static float orientation = 0.0f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}

		graph = new LineGraphView(getApplicationContext(), 100, Arrays.asList(
				"x", "y", "z"));
		mapView = new MapView(getApplicationContext(), 800, 800, 32, 35);
		registerForContextMenu(mapView);
		NavigationalMap map = MapLoader.loadMap(getExternalFilesDir(null),
				"E2-3344.svg");
		checkMap = map;
		mapView.setMap(map);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		mapView.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item)
				|| mapView.onContextItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_main,
					container, false);
			TextView tv1 = new TextView(rootView.getContext());
			LinearLayout rv = (LinearLayout) rootView.findViewById(R.id.label);
			rv.addView(tv1);

			rv.setOrientation(LinearLayout.VERTICAL);
			Position pl = new Position();
			directionsOut = new TextView(rootView.getContext());
			TextView lightOut = new TextView(rootView.getContext());
			// rv.addView(lightOut);
			// rv.addView(graph);
			rv.addView(mapView);
			graph.setVisibility(View.VISIBLE);

			SensorManager sensorManager = (SensorManager) rootView.getContext()
					.getSystemService(SENSOR_SERVICE);

			TextView accelOut = new TextView(rootView.getContext());
			TextView orientOut = new TextView(rootView.getContext());
			rv.addView(accelOut);
			rv.addView(orientOut);
			Sensor accelSensor = sensorManager
					.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); // adds
																		// the
																		// accelerometer
																		// event
																		// listener

			Sensor orientSensor = sensorManager
					.getDefaultSensor(Sensor.TYPE_ORIENTATION);
			accel = new Accelerometer(accelOut, orientOut, graph, pl, mapView,
					checkMap);
			sensorManager.registerListener(accel, orientSensor,
					SensorManager.SENSOR_DELAY_GAME);
			sensorManager.registerListener(accel, accelSensor,
					SensorManager.SENSOR_DELAY_GAME); // the game delay gives a
														// smoother graph
														// (probably adds a
														// filter)
			Button reset = (Button) rootView.findViewById(R.id.resetButton); // reset
																				// button,
																				// to
																				// reset
																				// the
																				// steps
			reset.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					((Accelerometer) accel).resetSteps(); // reset method as
															// part of the
															// accelerometer
				}
			});
			final Button calibrate = new Button(rootView.getContext());
			calibrate.setText("Calibrate direction");

			calibrate.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((Accelerometer) accel).calibrate();

				}
			});
			rv.addView(calibrate);
			rv.addView(directionsOut);
			mapView.addListener(pl);
			return rootView;

		}

		class Position implements PositionListener {
			@Override
			public void originChanged(MapView source, PointF loc) {
				// TODO Auto-generated method stub
				source.setUserPoint(loc);
				pointOrigin = loc;
				pointUser = pointOrigin;
				Log.d("Point", "User " + loc.x + " " + loc.y);
				if (pointEnd != null) {
					buildPath(pointUser);
				}

			}

			@Override
			public void destinationChanged(MapView source, PointF dest) {
				pointEnd = dest;
				Log.d("Point", "End " + dest.x + " " + dest.y);
				if (pointOrigin != null) {
					buildPath(pointUser);
				}

			}

			public PointF getUser() {
				return pointUser;
			}

			public PointF getOrigin() {
				return pointOrigin;
			}

			public PointF getEnd() {
				return pointEnd;
			}

			public void setUser(PointF user) {
				pointUser = user;
			}

			public void buildPath(PointF user) {
				PointF[] myList = { new PointF(6.4f, 18.4f),
						new PointF(12.65f, 18.4f), new PointF(20.4f, 18.4f),
						new PointF(20.125f, 6.4f) };
				Boolean build = true;
				List<PointF> lineList = new ArrayList<PointF>();
				lineList.add(user);
				if (build = true)
					if (checkDirect(user)) {
						lineList.add(new PointF(pointEnd.x, pointEnd.y));
					} else if (checkPerpendicular(user) == 0) {
						lineList.add(new PointF(user.x, pointEnd.y));
						lineList.add(new PointF(pointEnd.x, pointEnd.y));
					} else if (checkPerpendicular(user) == 1) {
						lineList.add(new PointF(pointEnd.x, user.y));
						lineList.add(new PointF(pointEnd.x, pointEnd.y));
					} else if (user.x > 22.7f && user.y < 7.0f && user.y > 4.0f) {
						lineList.add(myList[3]);
						PointF next = new PointF(myList[3].x, 18.4f);
						lineList.add(next);
						if (checkPerpendicular(next) == 0) {
							lineList.add(new PointF(next.x, pointEnd.y));
							lineList.add(new PointF(pointEnd.x, pointEnd.y));
						} else if (checkPerpendicular(next) == 1) {
							lineList.add(new PointF(pointEnd.x, next.y));
							lineList.add(new PointF(pointEnd.x, pointEnd.y));
						}
					} else if (pointEnd.x > 22.7f && pointEnd.y < 7.0f
							&& pointEnd.y > 4.0f) {
						PointF next = new PointF(user.x, 18.4f);
						lineList.add(next);
						if (checkPerpendicularPoints(next, myList[3]) == 0) {
							lineList.add(new PointF(next.x, myList[3].y));
							lineList.add(new PointF(myList[3].x, myList[3].y));
						} else if (checkPerpendicularPoints(next, myList[3]) == 1) {
							lineList.add(new PointF(myList[3].x, 18.4f));
							lineList.add(new PointF(myList[3].x, myList[3].y));
						}
						lineList.add(new PointF(pointEnd.x, pointEnd.y));
					} else {
						Log.d("Point", "Complicated");
						PointF next = new PointF(user.x, 18.4f);
						lineList.add(next);
						if (checkPerpendicular(next) == 0) {
							lineList.add(new PointF(next.x, pointEnd.y));
							lineList.add(new PointF(pointEnd.x, pointEnd.y));
						} else if (checkPerpendicular(next) == 1) {
							lineList.add(new PointF(pointEnd.x, next.y));
							lineList.add(new PointF(pointEnd.x, pointEnd.y));
						}

					}
				mapView.setUserPath(lineList);
				directionsOut.setText(generateDirections(lineList));

			}

			private String generateDirections(List<PointF> lineList) {
				List<PointF> directions = lineList;
				String listDir = "";
				Log.d("Point", "Generating Directions");
				if (checkAtEnd(directions)) {
					listDir += "You have reached your destination!";
					return listDir;
				}
				float angle = 0.0f;
				orientation = ((Accelerometer) accel).getOrient();
				if (directions.size() == 2) {
					Log.d("Points", " angle check");
					if ((orientation > 0 && orientation < 45)
							|| (orientation < 360 && orientation > 315)) {
						Log.d("Points" ,"" +orientation);
						PointF inter = new PointF(directions.get(0).x,
								directions.get(0).y + 1);
						angle = VectorUtils.angleBetween(inter,
								directions.get(0), directions.get(1));
						if(angle < 0.0f){
							angle =(float) Math.toDegrees(angle);
						}
						listDir += "Turn "
								+ angle 
								+ ((directions.get(1).x - directions.get(0).x > 0) ? "º to the left"
										: "º to the right");
						listDir += ". Walk forward "
								+ (int) (VectorUtils.distance(
										directions.get(0), directions.get(1)) / 0.8)
								+ " steps.";
					} else if (orientation > 45 && orientation < 135) {
						PointF inter = new PointF(directions.get(0).x - 1,
								directions.get(0).y);
						 angle = VectorUtils.angleBetween(inter,
								directions.get(0), directions.get(1));
							if(angle < 0.0f){
								angle =(float) Math.toDegrees(angle);
							}
						listDir += "Turn "
								+angle
								+ ((directions.get(1).x - directions.get(0).x > 0) ? "º to the left"
										: "º to the right");
						listDir += ". Walk forward "
								+ (int) (VectorUtils.distance(
										directions.get(0), directions.get(1)) / 0.8)
								+ " steps.";
					} else if (orientation > 135 && orientation < 225) {
						PointF inter = new PointF(directions.get(0).x,
								directions.get(0).y - 1);
						angle = VectorUtils.angleBetween(inter,
								directions.get(0), directions.get(1));
						if(angle < 0.0f){
							angle =(float) Math.toDegrees(angle);
						}

						listDir += "Turn "
								+ angle
								+ ((directions.get(1).x - directions.get(0).x > 0) ? "º to the left"
										: "º to the right");
						listDir += ". Walk forward "
								+ (int) (VectorUtils.distance(
										directions.get(0), directions.get(1)) / 0.8)
								+ " steps.";
					} else if (orientation > 225 && orientation < 315) {
						PointF inter = new PointF(directions.get(0).x + 1,
								directions.get(0).y);
						angle = VectorUtils.angleBetween(inter,
								directions.get(0), directions.get(1));
						if(angle < 0.0f){
							angle = (float) Math.toDegrees(angle);
						}
						listDir += "Turn "
								+ angle
								+ ((directions.get(1).x - directions.get(0).x > 0) ? "º to the left"
										: "º to the right");
						listDir += ". Walk forward "
								+ (int) (VectorUtils.distance(
										directions.get(0), directions.get(1)) / 0.8)
								+ " steps.";
					}
					// if (directions.get(1).y - directions.get(0).y > 0.0f) {
					// PointF inter = new PointF(directions.get(0).x,
					// directions.get(0).y + 1);
					// float angle = VectorUtils.angleBetween(inter,
					// directions.get(0), directions.get(1));
					//
					// listDir += "Turn "
					// + Math.toDegrees(angle)
					// + ((directions.get(1).x - directions.get(0).x > 0) ?
					// "º to the left"
					// : "º to the right");
					// listDir += ". Walk forward "
					// + (int)(VectorUtils.distance(directions.get(0),
					// directions.get(1))/0.8) +" steps.";
					// } else {
					// PointF inter = new PointF(directions.get(0).x,
					// directions.get(0).y - 1);
					// float angle = VectorUtils.angleBetween(inter,
					// directions.get(0), directions.get(1));
					// listDir += "Turn "
					// + angle
					// + ((directions.get(1).x - directions.get(0).x > 0) ?
					// "º to the left"
					// : "º to the right");
					// listDir += ". Walk forward "
					// + (int)(VectorUtils.distance(directions.get(0),
					// directions.get(1)) / 0.8 )+ " steps.";

					// }
				} else if (directions.size() == 3) {
					listDir += "Walk forward "
							+ (int) (VectorUtils.distance(directions.get(0),
									directions.get(1)) / 0.8) + " steps.";
					listDir += "Turn 90º "
							+ ((directions.get(2).x - directions.get(0).x > 0) ? "to the left "
									: "to the right");
					listDir += ". Then walk forward "
							+ (int) (VectorUtils.distance(directions.get(1),
									directions.get(2)) / 0.8) + " steps.";
				} else if (directions.size() == 4) {
					listDir += "Walk forward "
							+ (int) (VectorUtils.distance(directions.get(0),
									directions.get(1)) / 0.8) + " steps.";
					listDir += "Turn 90º "
							+ ((directions.get(3).x - directions.get(0).x > 0) ? "to the left "
									: "to the right");
					listDir += ". Then walk forward "
							+ (int) (VectorUtils.distance(directions.get(1),
									directions.get(2)) / 0.8) + " steps.";
					listDir += "Turn 90º "
							+ ((directions.get(3).y - directions.get(2).y > 0) ? "to the left "
									: "to the right");
					listDir += ". Then walk forward "
							+ (int) (VectorUtils.distance(directions.get(2),
									directions.get(3)) / 0.8) + " steps.";
				} else {
					listDir += "Walk forward "
							+ (int) (VectorUtils.distance(directions.get(0),
									directions.get(1)) / 0.8) + " steps.";
					listDir += "Turn 90º "
							+ ((directions.get(4).x - directions.get(0).x > 0) ? "to the left "
									: "to the right");
					listDir += ". Then walk forward "
							+ (int) VectorUtils.distance(directions.get(1),
									directions.get(2));
					listDir += ". Turn 90º "
							+ ((directions.get(3).y - directions.get(2).y > 0) ? "to the left "
									: "to the right");
				}
				return listDir;

			}

			private int checkPerpendicularPoints(PointF next, PointF myList) {
				if (checkMap.calculateIntersections(next,
						new PointF(next.x, myList.y)).isEmpty()
						&& checkMap.calculateIntersections(myList,
								new PointF(next.x, myList.y)).isEmpty()) {
					Log.d("Point", "Perpendicular");
					return 0;
				} else if (checkMap.calculateIntersections(next,
						new PointF(myList.x, next.y)).isEmpty()
						&& checkMap.calculateIntersections(myList,
								new PointF(myList.x, next.y)).isEmpty()) {
					Log.d("Point", "Perpendicular");
					return 1;
				}
				return 3;
			}

			private int checkPerpendicular(PointF user) {
				if (checkMap.calculateIntersections(user,
						new PointF(user.x, pointEnd.y)).isEmpty()
						&& checkMap.calculateIntersections(pointEnd,
								new PointF(user.x, pointEnd.y)).isEmpty()) {
					Log.d("Point", "Perpendicular");
					return 0;
				} else if (checkMap.calculateIntersections(user,
						new PointF(pointEnd.x, user.y)).isEmpty()
						&& checkMap.calculateIntersections(pointEnd,
								new PointF(pointEnd.x, user.y)).isEmpty()) {
					Log.d("Point", "Perpendicular");
					return 1;
				}
				return 3;
			}

			private boolean checkDirect(PointF user) {
				if (checkMap.calculateIntersections(user, pointEnd).isEmpty()) {
					Log.d("Point", "direct");
					return true;
				}
				return false;
			}

			private boolean checkAtEnd(List<PointF> directions) {
				if (directions.size() > 2)
					return false;
				else {
					if (VectorUtils.distance(directions.get(0),
							directions.get(1)) < 1.0) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
	}

}
