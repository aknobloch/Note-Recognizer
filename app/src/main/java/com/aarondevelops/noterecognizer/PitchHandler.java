package com.aarondevelops.noterecognizer;

import android.app.Activity;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class PitchHandler implements PitchDetectionHandler
{
    Activity mContextActivity;
    AudioDispatcher mDispatcher;
    PitchCallback mListener;

    public  PitchHandler(Activity contextActivity, PitchCallback pitchListener)
    {
        this.mContextActivity = contextActivity;
        this.mListener = pitchListener;
    }

    public void startPitchDetection()
    {
        if(mDispatcher != null)
        {
            throw new IllegalStateException("Cannot run more than one PitchHandler.");
        }

        mDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);
        mDispatcher.addAudioProcessor(new PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                22050,
                1024,
                this));

        new Thread(mDispatcher, "Audio Dispatcher").start();
    }


    @Override
    public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent)
    {
        final float pitchInHz = pitchDetectionResult.getPitch();
        mContextActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Note capturedNote = NoteFactory.createNote(pitchInHz);
                mListener.onPitchDetected(capturedNote);

                TextView tvNote = (TextView) mContextActivity.findViewById(R.id.tvBaseNote);
                TextView tvPitch = (TextView) mContextActivity.findViewById(R.id.tvPitch);
                TextView tvOctave = (TextView) mContextActivity.findViewById(R.id.tvOctave);

                tvNote.setText("" + capturedNote.getNote());
                tvPitch.setText("" + capturedNote.getPitch());
                tvOctave.setText("" + capturedNote.getOctave());
            }
        });
    }
}
