package com.aarondevelops.noterecognizer;

import android.widget.TextView;

/**
 * Created by Aaron K on 4/12/2017.
 */

public interface Note
{
    enum RelativePitch
    {
        HIGH, LOW, OK;
    }

    public char getNote();

    public char getPitch();

    public char getOctave();

    public RelativePitch getRelativePitch();
}
