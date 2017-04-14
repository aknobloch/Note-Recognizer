package com.aarondevelops.noterecognizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

public class NoteDisplayActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback
{
    final int RECORD_AUDIO = 001;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_display);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        beginAudio();
    }

    private void beginAudio()
    {
        if(hasAudioPermission())
        {
            startAudioRecording();
        }
        else
        {
            getAudioPermission();
        }
    }

    private boolean hasAudioPermission()
    {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void getAudioPermission()
    {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                RECORD_AUDIO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if(requestCode != RECORD_AUDIO)
        {
            return;
        }

        if(grantResults[0] == PackageManager.PERMISSION_DENIED)
        {
            MessageHelper.makeToast(this,
                    "Audio permission is required to run Note Recognizer",
                    Toast.LENGTH_LONG);
            this.finishAndRemoveTask();
        }
        else
        {
            beginAudio();
        }
    }

    private void startAudioRecording()
    {
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);
        dispatcher.addAudioProcessor(new PitchProcessor(PitchEstimationAlgorithm.FFT_YIN, 22050, 1024,
                new PitchDetectionHandler() {

                    @Override
                    public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                            AudioEvent audioEvent) {

                        final float pitchInHz = pitchDetectionResult.getPitch();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Note capturedNote = NoteFactory.createNote(pitchInHz);
                                TextView tvNote = (TextView) findViewById(R.id.tvBaseNote);
                                TextView tvPitch = (TextView) findViewById(R.id.tvPitch);
                                TextView tvOctave = (TextView) findViewById(R.id.tvOctave);

                                tvNote.setText("" + capturedNote.getNote());
                                tvPitch.setText("" + capturedNote.getPitch());
                                tvOctave.setText("" + capturedNote.getOctave());

                                double range = 70;
                                float normalizedDegree = (float)
                                        ((capturedNote.getNormalizedValue() * range) - 35);

                                ImageView needle = (ImageView) findViewById(R.id.imageView);
                                needle.setPivotX(needle.getWidth() / 2);
                                needle.setPivotY(needle.getHeight() - (needle.getHeight() / 10));
                                needle.setRotation(normalizedDegree);

                            }
                        });
            }
        }));
        new Thread(dispatcher,"Audio Dispatcher").start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
