package com.example.video;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

public class AccelerometerAppActivity extends AppCompatActivity implements SensorEventListener {


    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private FrameLayout mainContainer;
    private ImageView targetIconImage; // single target icon

    private Vibrator vibrator;
    private MediaPlayer winSound;
    private long lastVibrationTime = 0;


    private Random random = new Random();
    private float targetAngle;
    private static final float WIN_THRESHOLD = 5f;
    private boolean gameStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer_app);

        mainContainer = findViewById(R.id.main);
        ImageView arrowImage = findViewById(R.id.arrowImage);

        targetIconImage = new ImageView(this);
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.star);
        int newWidth = 100;
        int newHeight = 100;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
        targetIconImage.setImageBitmap(scaledBitmap);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        targetIconImage.setLayoutParams(params);
        mainContainer.addView(targetIconImage);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        winSound = MediaPlayer.create(this, R.raw.win_sound);

        // Setup the sensor.
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
        }
        generateNewTarget();
        updateTargetIconPosition();
    }

    public void startGameClick(View view){
        startGame();
    }

    private void startGame() {
        gameStarted = true;
        lastVibrationTime = System.currentTimeMillis();
        generateNewTarget();
        updateTargetIconPosition();
    }


    private void generateNewTarget() {
        targetAngle = random.nextFloat() * 360;
    }
    private void updateTargetIconPosition() {
        int containerWidth = mainContainer.getWidth();
        int containerHeight = mainContainer.getHeight();
        float centerX = containerWidth / 2f;
        float centerY = containerHeight / 2f;
        float distance = 300f;


        float screenAngle = (float) Math.toRadians(targetAngle - 90);
        float targetX = centerX + distance * (float) Math.cos(screenAngle);
        float targetY = centerY + distance * (float) Math.sin(screenAngle);
        targetIconImage.setX(targetX - targetIconImage.getWidth() / 2f);
        targetIconImage.setY(targetY - targetIconImage.getHeight() / 2f);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!gameStarted) {
            return;
        }
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(rotationMatrix, orientation);
            float azimuth = (float) Math.toDegrees(orientation[0]);
            if (azimuth < 0) {
                azimuth += 360;
            }

            // Så vi undviker negativa vinklar och skillnande kan inte vara större än 180 grader.
            float angleDiff = Math.abs(azimuth - targetAngle);
            if (angleDiff > 180) {
                angleDiff = 360 - angleDiff;
            }

            float relativeAngle = targetAngle - azimuth;
            if (relativeAngle < -180) {
                relativeAngle += 360;
            }
            if (relativeAngle > 180) {
                relativeAngle -= 360;
            }


            int containerWidth = mainContainer.getWidth();
            int containerHeight = mainContainer.getHeight();
            float centerX = containerWidth / 2f;
            float centerY = containerHeight / 2f;
            float distance = 400f;

            // Eftersom att i matte är 0 grader år höger, men i kooridnater är 0 grader uppåt behöver vi subtrahera 90 grader.

            float screenAngle = (float) Math.toRadians(relativeAngle - 90);
            float targetX = centerX + distance * (float) Math.cos(screenAngle);
            float targetY = centerY + distance * (float) Math.sin(screenAngle);
            targetIconImage.setX(targetX - targetIconImage.getWidth() / 2f);
            targetIconImage.setY(targetY - targetIconImage.getHeight() / 2f);

            // Bestämmer hur snabbt vi ska vibrera
            long vibrationInterval = vibrationInterval(angleDiff);
            if(angleDiff < WIN_THRESHOLD) {
                vibrationInterval = 0;
            }

            long currentTime = System.currentTimeMillis();
            if (vibrationInterval > 0) {
                if (currentTime - lastVibrationTime >= vibrationInterval) {
                    if (vibrator != null && vibrator.hasVibrator()) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(50);
                        }
                    }
                    lastVibrationTime = currentTime;
                }
            } else if (vibrationInterval == 0) {
                if (winSound != null && !winSound.isPlaying()) {
                    winSound.start();
                }
                gameStarted = false;
                showGameWonDialog();
            }

        }
    }


    long vibrationInterval(float angleDiff) {

        long minInterval = 50;
        long maxInterval = 1000;
        float factor = angleDiff / 180.0f;

        return (long) (minInterval + (maxInterval - minInterval) * factor);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (winSound != null) {
            winSound.release();
            winSound = null;
        }
    }

    private void showGameWonDialog() {
        gameStarted = false;
        new AlertDialog.Builder(this)
                .setTitle("Game Won!")
                .setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startGame();  // Restart the game
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }


}