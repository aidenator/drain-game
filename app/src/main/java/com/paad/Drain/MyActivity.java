package com.paad.Drain;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class MyActivity extends Activity {
  /** Called when the activity is first created. */
  //Global vars...
  float accelx = 0;
  float accely = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

  ((SensorManager) getSystemService(Context.SENSOR_SERVICE)).registerListener(
          new SensorEventListener() {
              @Override
              public void onSensorChanged(SensorEvent event) {
                  // I hope I got the signs right.  If not, experiment with this.
                  accelx = -event.values[0];
                  accely = event.values[1];
                  tomatoPosition(findViewById(R.id.mazeView), accelx, accely);
              }

              @Override
              public void onAccuracyChanged(Sensor sensor, int accuracy) {
              } //ignore
          },
          ((SensorManager) getSystemService(Context.SENSOR_SERVICE))
                  .getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_GAME);
  }

  public void tomatoPosition(View v, float x, float y){
      MazeView maze = (MazeView) findViewById(R.id.mazeView);
      maze.setAccel(x, y);
  }


}