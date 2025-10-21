package com.example.ma2025.boss;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {

    public interface OnShakeListener {
        void onShake();
    }

    private static final float SHAKE_THRESHOLD_GRAVITY = 2.3F; // osetljivost
    private static final int SHAKE_SLOP_TIME_MS = 2000; // minimalni razmak između 2 shake-a

    private OnShakeListener listener;
    private long lastShakeTime;

    public ShakeDetector(OnShakeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // gravitacija normalizovana (9.8 ~ 1G)
        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        // ukupna sila gravitacije
        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            final long now = System.currentTimeMillis();
            if (lastShakeTime + SHAKE_SLOP_TIME_MS > now) {
                return; // ignorisi ako je preblizu prethodnom
            }

            lastShakeTime = now;
            if (listener != null) listener.onShake();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nije potrebno koristiti
    }

    public void stop() {
        listener = null;
    }
}
