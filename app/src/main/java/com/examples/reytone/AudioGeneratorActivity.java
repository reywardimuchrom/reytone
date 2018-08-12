package com.examples.reytone;

import android.content.Intent;
import android.content.res.AssetManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;

public class AudioGeneratorActivity extends AppCompatActivity {

    private ImageButton
       audio1,
       audio2,
       audio3,
       audio4,
       audio5,
       audio6,
       audio7,
       audio8,
    space, hapus;
    private Button btnMerge;
    private TextView durationText;
    private ProgressBar progressBar;
    private MediaPlayer mediaPlayer;
    private String recordPath;
    private String dataPath;

    private String ffmpegConcatedOutput;
    private String ffmpegFinal;
    private Integer audioSourceDuration;
    private Integer audioToneDuration = 0;
    private List<String> ffmpegConcatInput = new ArrayList<>();
    private String ffmpegConcatArgument = "";

    private Integer ffmpegConcatOutputCount    = 0;

    DelayedProgressDialog progressDialog = new DelayedProgressDialog();
    private List<String> sounds      = new ArrayList<String>();

    private boolean commandValidationFailedFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_generator);
        this.copyAssets();

        dataPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.reytones/";

        audio1       = (ImageButton) findViewById(R.id.button5);
        audio2       = (ImageButton) findViewById(R.id.button6);
        audio3       = (ImageButton) findViewById(R.id.button7);
        audio4       = (ImageButton) findViewById(R.id.button8);
        audio5       = (ImageButton) findViewById(R.id.button9);
        audio6       = (ImageButton) findViewById(R.id.button10);
        audio7       = (ImageButton) findViewById(R.id.button11);
        audio8       = (ImageButton) findViewById(R.id.button12);
        space        = (ImageButton) findViewById(R.id.btnSpace);
//        hapus        = (ImageButton) findViewById(R.id.btnHapus);
        durationText = (TextView) findViewById(R.id.textViewDuration);
        btnMerge     = (Button) findViewById(R.id.buttonMerge);
        progressBar  = (ProgressBar) findViewById(R.id.progressBarDuration);

        btnMerge.setEnabled(false);
        Bundle bundle = getIntent().getExtras();
        recordPath    = bundle.getString("AUDIO_SOURCE_PATH");

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(recordPath);
        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        mmr.release();
        audioSourceDuration = Integer.valueOf(duration);

        new Thread(new Runnable() {
            @Override
            public void run() {
                progressBar.setMax(audioSourceDuration);
            }
        }).start();

        if (FFmpeg.getInstance(AudioGeneratorActivity.this).isSupported()) {
            // ffmpeg is supported
            // Toast.makeText(AudioGeneratorActivity.this, "FFmpeg Supported", Toast.LENGTH_SHORT).show();
        } else {
            // ffmpeg is not supported
            Toast.makeText(AudioGeneratorActivity.this, "FFmpeg Not Supported", Toast.LENGTH_LONG).show();
        }

        durationText.setText(getDurationBreakdown(new Long(duration)));

        btnMerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File output = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),  "/.reytones/concatedTones.mp3");
                ffmpegConcatedOutput = output.getAbsolutePath();

                try {
                    deleteFile(output);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                StringBuilder concatInput = new StringBuilder();

                for (String input : ffmpegConcatInput)
                    concatInput.append(input);

                while (audioToneDuration < audioSourceDuration) {
                    concatInput.append(concatInput);
                    audioToneDuration += audioToneDuration;
                }

                String command  = concatInput + "-filter_complex ";
                command += ffmpegConcatArgument + "concat=n=" + ffmpegConcatOutputCount.toString() + ":v=0:a=1[out] ";
                command += "-map ['out'] " + ffmpegConcatedOutput;
                Log.w("REYTONE", command);
                ffmpegConcat(GeneralUtils.utilConvertToComplex(command));

            }
        });
        mediaPlayer = new MediaPlayer();

        space.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( audioToneDuration < audioSourceDuration ) {
                    try {
                        addArgumentInputFFMPEG(sounds.get(8));

                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(sounds.get(8));
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        mmr.release();

                        audioToneDuration += Integer.valueOf(duration);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(audioToneDuration);
                            }
                        }).start();
                    } catch (Exception e) {
                        Log.w(Prefs.TAG, e);
                    }
                } else {
                   btnMerge.setEnabled(true);
                    Toast.makeText(AudioGeneratorActivity.this, "Complete", Toast.LENGTH_SHORT).show();
                }
            }
        });

        audio1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( audioToneDuration < audioSourceDuration ) {
                    try {
                        addArgumentInputFFMPEG(sounds.get(0));
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(sounds.get(0));
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(sounds.get(0));
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        mmr.release();

                        audioToneDuration += Integer.valueOf(duration);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(audioToneDuration);
                            }
                        }).start();
                    } catch (Exception e) {
                        Log.w(Prefs.TAG, e);
                    }
                } else {
                   btnMerge.setEnabled(true);
                    Toast.makeText(AudioGeneratorActivity.this, "Complete", Toast.LENGTH_SHORT).show();
                }
            }
        });

        audio1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( audioToneDuration < audioSourceDuration ) {
                    try {
                        addArgumentInputFFMPEG(sounds.get(0));
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(sounds.get(0));
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(sounds.get(0));
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        mmr.release();

                        audioToneDuration += Integer.valueOf(duration);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(audioToneDuration);
                            }
                        }).start();
                    } catch (Exception e) {
                        Log.w(Prefs.TAG, e);
                    }
                } else {
                   btnMerge.setEnabled(true);
                    Toast.makeText(AudioGeneratorActivity.this, "Complete", Toast.LENGTH_SHORT).show();
                }
            }
        });
        audio2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( audioToneDuration < audioSourceDuration ) {
                    try {
                        addArgumentInputFFMPEG(sounds.get(1));
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(sounds.get(1));
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(sounds.get(1));
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        mmr.release();

                        audioToneDuration += Integer.valueOf(duration);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(audioToneDuration);
                            }
                        }).start();
                    } catch (Exception e) {
                        Log.w(Prefs.TAG, e);
                    }
                } else {
                   btnMerge.setEnabled(true);
                    Toast.makeText(AudioGeneratorActivity.this, "Complete", Toast.LENGTH_SHORT).show();
                }
            }
        });
        audio3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( audioToneDuration < audioSourceDuration ) {
                    try {
                        addArgumentInputFFMPEG(sounds.get(2));
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(sounds.get(2));
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(sounds.get(2));
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        mmr.release();

                        audioToneDuration += Integer.valueOf(duration);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(audioToneDuration);
                            }
                        }).start();
                    } catch (Exception e) {
                        Log.w(Prefs.TAG, e);
                    }
                } else {
                   btnMerge.setEnabled(true);
                    Toast.makeText(AudioGeneratorActivity.this, "Complete", Toast.LENGTH_SHORT).show();
                }
            }
        });
        audio4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( audioToneDuration < audioSourceDuration ) {
                    try {
                        addArgumentInputFFMPEG(sounds.get(3));
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(sounds.get(3));
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(sounds.get(3));
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        mmr.release();

                        audioToneDuration += Integer.valueOf(duration);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(audioToneDuration);
                            }
                        }).start();
                    } catch (Exception e) {
                        Log.w(Prefs.TAG, e);
                    }
                } else {
                   btnMerge.setEnabled(true);
                    Toast.makeText(AudioGeneratorActivity.this, "Complete", Toast.LENGTH_SHORT).show();
                }
            }
        });
        audio5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( audioToneDuration < audioSourceDuration ) {
                    try {
                        addArgumentInputFFMPEG(sounds.get(4));
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(sounds.get(4));
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(sounds.get(4));
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        mmr.release();

                        audioToneDuration += Integer.valueOf(duration);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(audioToneDuration);
                            }
                        }).start();
                    } catch (Exception e) {
                        Log.w(Prefs.TAG, e);
                    }
                } else {
                   btnMerge.setEnabled(true);
                    Toast.makeText(AudioGeneratorActivity.this, "Complete", Toast.LENGTH_SHORT).show();
                }
            }
        });
        audio6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( audioToneDuration < audioSourceDuration ) {
                    try {
                        addArgumentInputFFMPEG(sounds.get(5));
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(sounds.get(5));
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(sounds.get(5));
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        mmr.release();

                        audioToneDuration += Integer.valueOf(duration);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(audioToneDuration);
                            }
                        }).start();
                    } catch (Exception e) {
                        Log.w(Prefs.TAG, e);
                    }
                } else {
                   btnMerge.setEnabled(true);
                    Toast.makeText(AudioGeneratorActivity.this, "Complete", Toast.LENGTH_SHORT).show();
                }
            }
        });
        audio7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( audioToneDuration < audioSourceDuration ) {
                    try {
                        addArgumentInputFFMPEG(sounds.get(6));
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(sounds.get(6));
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(sounds.get(6));
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        mmr.release();

                        audioToneDuration += Integer.valueOf(duration);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(audioToneDuration);
                            }
                        }).start();
                    } catch (Exception e) {
                        Log.w(Prefs.TAG, e);
                    }
                } else {
                   btnMerge.setEnabled(true);
                    Toast.makeText(AudioGeneratorActivity.this, "Complete", Toast.LENGTH_SHORT).show();
                }
            }
        });
        audio8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( audioToneDuration < audioSourceDuration ) {
                    try {
                        addArgumentInputFFMPEG(sounds.get(7));
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(sounds.get(7));
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(sounds.get(7));
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        mmr.release();

                        audioToneDuration += Integer.valueOf(duration);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(audioToneDuration);
                            }
                        }).start();
                    } catch (Exception e) {
                        Log.w(Prefs.TAG, e);
                    }
                } else {
                   btnMerge.setEnabled(true);
                    Toast.makeText(AudioGeneratorActivity.this, "Complete", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void ffmpegConcat(String [] command) {
        FFmpeg.getInstance(this).execute(command, new ExecuteBinaryResponseHandler() {

            @Override
            public void onStart() {
                progressDialog.show(getSupportFragmentManager(), "tag");
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }

            @Override
            public void onSuccess(String message) {
                Toast.makeText(AudioGeneratorActivity.this, "Success: ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(String message) {

                Log.w("REYTONE", "Progress: " + message);
            }

            @Override
            public void onFailure(String message) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(AudioGeneratorActivity.this, "Gagal: " + message, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFinish() {

                File directory = Environment.getExternalStorageDirectory();
                File folder  = new File( directory + "/Reytones" );
                if (!folder.exists()) {
                    folder.mkdir();
                    folder  = new File( directory + "/Reytones" );
                }

                String now = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date());
                File ffmpegFinalOutput = new File(folder.getAbsolutePath(),  "/reytone-" + now + ".mp3");

                try {
                    deleteFile(ffmpegFinalOutput);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String command  = "-i " + recordPath + " ";
                command += "-i " + ffmpegConcatedOutput + " ";
                command += "-filter_complex amerge -ac 2 -c:a libmp3lame -q:a 4 ";
                command += ffmpegFinalOutput.getAbsolutePath();

                Log.w("REYTONE", command);

                ffmpegCombine(GeneralUtils.utilConvertToComplex(command));
            }
        });

    }
    private void ffmpegCombine(String [] command) {
        FFmpeg.getInstance(this).execute(command, new ExecuteBinaryResponseHandler() {
            @Override
            public void onSuccess(String message) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                finish();
                startActivity(new Intent(AudioGeneratorActivity.this, ResultActivity.class));
            }

            @Override
            public void onProgress(String message) {
                Log.w("REYTONE", "Progress: " + message);
            }

            @Override
            public void onFailure(String message) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast.makeText(AudioGeneratorActivity.this, "Gagal: " + message, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFinish() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressDialog.cancel();
                Toast.makeText(AudioGeneratorActivity.this, "Finish" , Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addArgumentInputFFMPEG(String path) {

        String count               = ffmpegConcatOutputCount.toString();
        this.ffmpegConcatInput.add("-i " + path + " ");
        this.ffmpegConcatArgument += "[" + count + ":0]";
        this.ffmpegConcatOutputCount++;
    }

    private void copyAssets() {
        AssetManager assetsManager = getAssets();
        String[] files = null;
        try {
            files = assetsManager.list("tones");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                String dataPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.reytones/";
                in = assetsManager.open("tones/" + filename);
                File assetToneFile = new File(dataPath, filename);
                if (assetToneFile.exists()) {
                    in.close();
                } else {
                    new File(dataPath).mkdir();

                    assetToneFile.createNewFile();

                    out = new FileOutputStream(assetToneFile);
                    copyFile(in, out);
                    in.close();
                    in = null;
                    out.flush();
                    out.close();
                    out = null;
                }

                sounds.add(assetToneFile.toString());
            } catch (IOException e) {
                Log.e(Prefs.TAG, "Failed to copy asset file: " + filename, e);
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
    public boolean deleteFile(File file) throws IOException {
        if (file != null) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();

                for (File f: files) {
                    deleteFile(f);
                }
            }
            return file.delete();
        }
        return false;
    }
    public static String getDurationBreakdown(long millis) {
        if( millis < 0 ) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long milliss = millis % 100;

        StringBuilder sb = new StringBuilder(64);
        sb.append(minutes);
        sb.append(":");
        sb.append(seconds);
        sb.append(".");
        sb.append(milliss);

        return(sb.toString());
    }
}
