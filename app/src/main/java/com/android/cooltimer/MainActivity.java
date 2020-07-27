package com.android.cooltimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    SeekBar timerSeekBar;
    TextView timerView;
    Button btnStart;
    MediaPlayer mediaPlayer;
    boolean timerIsActive;
    CountDownTimer timer;
    int defaultInterval;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerSeekBar = findViewById(R.id.timeSeekBar);
        timerView = findViewById(R.id.timeTextView);
        btnStart = findViewById(R.id.startButton);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        timerSeekBar.setMax(600);
        timerIsActive = false;
        setIntervalFromSharedPreferences(sharedPreferences);

        timerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long progressInMillis = progress*1000;
                updateTimer(progressInMillis);
                }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void start(View view) {
        if (!timerIsActive) {
            btnStart.setText("Stop");
            timerSeekBar.setEnabled(false);
            timerIsActive = true;

            timer = new CountDownTimer(timerSeekBar.getProgress()*1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    updateTimer(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    if (sharedPreferences.getBoolean("enable_sound", true)) {
                        String soundName = sharedPreferences.getString("timer_sound", "gong");
                        if (soundName.equals("gong")) {
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.gong);
                            mediaPlayer.start();
                        }
                        else if (soundName.equals("bip")) {
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bip_sound);
                            mediaPlayer.start();
                        }
                        else if (soundName.equals("bell")) {
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bell_sound);
                            mediaPlayer.start();
                        }
                    }

                    resetTimer();
                }
            }.start();

        } else {
            resetTimer();
        }
    }
    private void updateTimer(long time) {
        int minutes = (int)time /1000 / 60;
        int seconds = (int)time/1000 - (minutes * 60);
        String minutesString = "";
        String secondsString = "";
        if (minutes<10) {
            minutesString = "0" + minutes;
        }
        else {
            minutesString = String.valueOf(minutes);
        }
        if (seconds<10) {
            secondsString = "0" + seconds;
        }
        else {
            secondsString = String.valueOf(seconds);
        }
        timerView.setText(minutesString + ":" + secondsString);
    }
    private void resetTimer () {
        timer.cancel();
        btnStart.setText("Start");
        timerSeekBar.setEnabled(true);
        timerIsActive = false;
        setIntervalFromSharedPreferences(sharedPreferences);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       int id = item.getItemId();
       if (id == R.id.action_settings) {
           Intent openSettings = new Intent(this, Settings_Activity.class);
           startActivity(openSettings);
           return true;
       }
       else if (id == R.id.action_about) {
            Intent openAbout = new Intent(this, About_Activity.class);
            startActivity(openAbout);
            return true;
        }
       return super.onOptionsItemSelected(item);
    }
    private void setIntervalFromSharedPreferences(SharedPreferences sharedPreferences) {

        defaultInterval = Integer.parseInt(sharedPreferences.getString("default_interval", "30"));
        long defaultIntervalInMillis = defaultInterval * 1000;
        updateTimer(defaultIntervalInMillis);
        timerSeekBar.setProgress(defaultInterval);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("default_interval")) {
            setIntervalFromSharedPreferences(sharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
