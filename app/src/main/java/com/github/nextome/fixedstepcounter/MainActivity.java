package com.github.nextome.fixedstepcounter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Math.abs;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mLinearAccelerationSensor;
    private Sensor mMagneticSensor;
    private Sensor mGravitySensor;
    private Sensor mStepDetectSensor;
    private TextView textView;

    private float[] gravityValues = null;
    private float[] magneticValues = null;
    private float[] earthAccValues = new float[3];

    @Override
    public void onSensorChanged(SensorEvent event) {
        if ((gravityValues != null) && (magneticValues != null)
                && (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)) {

            float[] deviceRelativeAcceleration = new float[4];
            deviceRelativeAcceleration[0] = event.values[0];
            deviceRelativeAcceleration[1] = event.values[1];
            deviceRelativeAcceleration[2] = event.values[2];
            deviceRelativeAcceleration[3] = 0;

            // Change the device relative acceleration values to earth relative values
            // X axis -> East
            // Y axis -> North Pole
            // Z axis -> Sky

            float[] R = new float[16], I = new float[16], earthAcc = new float[16];

            SensorManager.getRotationMatrix(R, I, gravityValues, magneticValues);

            float[] inv = new float[16];

            android.opengl.Matrix.invertM(inv, 0, R, 0);
            android.opengl.Matrix.multiplyMV(earthAcc, 0, inv, 0, deviceRelativeAcceleration, 0);

            earthAccValues[0] = abs(earthAcc[0]);
            earthAccValues[1] = abs(earthAcc[1]);
            earthAccValues[2] = abs(earthAcc[2]);

        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            gravityValues = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticValues = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            textView.setText(earthAccValues[0] + " " + earthAccValues[1] + " " + earthAccValues[2]);
            if (((earthAccValues[0] + earthAccValues[1]) >1.6 && earthAccValues[2] > 1) &&
                    ((earthAccValues[0] + earthAccValues[1]) < 6 && earthAccValues[2] < 11)){
                // Step detected
                Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(100);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.test_textview);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mLinearAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mStepDetectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
    }

    protected void onResume() {
        super.onResume();
        if (mSensorManager != null) {
            mSensorManager.registerListener(this, mLinearAccelerationSensor, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mGravitySensor,SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mStepDetectSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mLinearAccelerationSensor);
        mSensorManager.unregisterListener(this, mGravitySensor);
        mSensorManager.unregisterListener(this, mMagneticSensor);
        mSensorManager.unregisterListener(this, mStepDetectSensor);
    }


}