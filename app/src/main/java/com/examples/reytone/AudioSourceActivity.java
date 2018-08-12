package com.examples.reytone;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class AudioSourceActivity extends AppCompatActivity {
    private ImageButton play, stop, record;
    private Button next;
    private TextView timerText;
    private MediaRecorder audioRecorder;
    private String audioSourcePath;
    private long mStartTime = 0;
    private MediaPlayer mediaPlayer;

    private int[] amplitudes = new int[100];
    private int i = 0;

    private Handler mHandler = new Handler();
    private Runnable mTickExecutor = new Runnable() {
        @Override
        public void run() {
            tick();
            mHandler.postDelayed(mTickExecutor,100);
        }
    };
    final static int RQS_OPEN_AUDIO_MP3 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_source);
        GeneralUtils.checkForPermissionsMAndAbove(AudioSourceActivity.this, false);

        play        = (ImageButton) findViewById(R.id.btnPlay);
        stop        = (ImageButton) findViewById(R.id.btnStop);
        record      = (ImageButton) findViewById(R.id.btnRecord);

        next        = (Button) findViewById(R.id.btnNext);

        timerText   = (TextView) findViewById(R.id.textViewTimer);

        mediaPlayer = new MediaPlayer();

        stop.setEnabled(false);
        play.setEnabled(false);
        next.setEnabled(false);

        File sourcePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/.reytones");
        File audioSource = new File (sourcePath, "/voice.wav");

        audioSourcePath = audioSource.getAbsolutePath();
        try {
            if (!audioSource.exists())
                sourcePath.mkdir();
                audioSource.createNewFile();
        } catch (IOException e) {

        }

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartTime = SystemClock.elapsedRealtime();
                mHandler.postDelayed(mTickExecutor, 100);
                onRecordClicked();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(mTickExecutor);
                stopRecord();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(audioSourcePath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();

//                    Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.w("WKWKWKWK", e);
                }


            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.release();
                Intent intent = new Intent(AudioSourceActivity.this, AudioGeneratorActivity.class);
                intent.putExtra("AUDIO_SOURCE_PATH", audioSourcePath);
                finish();
                startActivity(intent);
            }
        });
    }
    private void onRecordClicked() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO },
                    10);
        } else {
            recordAudio();
            startRecord();
        }
    }
    private void recordAudio() {
        audioRecorder = new MediaRecorder();
        audioRecorder.reset();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        audioRecorder.setOutputFile(audioSourcePath);
    }
    private void startRecord() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    10);
            record.setEnabled(false);
        } else {
            try {
                audioRecorder.prepare();
                audioRecorder.start();
//                Toast.makeText(getApplicationContext(), "Audio Recorder starting", Toast.LENGTH_LONG).show();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            stop.setEnabled(true);
            record.setEnabled(false);
        }
    }
    private void stopRecord() {
        try {
            audioRecorder.stop();
            audioRecorder.release();
            next.setEnabled(true);
//            Toast.makeText(getApplicationContext(), "Audio Recorder successfully", Toast.LENGTH_LONG).show();
        } catch (IllegalStateException e) {
            Log.w("WKWKWKWK", e);
        }

        audioRecorder = null;
        record.setEnabled(true);
        stop.setEnabled(false);
        play.setEnabled(true);
    }
    private void tick() {
        long time = (mStartTime < 0) ? 0 : (SystemClock.elapsedRealtime() - mStartTime);
        int minutes = (int) (time / 60000);
        int seconds = (int) (time / 1000) % 60;
        int milliseconds = (int) (time / 100) % 10;
        timerText.setText( minutes+":"+(seconds < 10 ? "0" + seconds : seconds)+ "." +milliseconds );
        if (audioRecorder != null) {
            amplitudes[i] = audioRecorder.getMaxAmplitude();
            //Log.d("Voice Recorder","amplitude: "+(amplitudes[i] * 100 / 32767));
            if (i >= amplitudes.length -1) {
                i = 0;
            } else {
                ++i;
            }
        }
    }
 }
