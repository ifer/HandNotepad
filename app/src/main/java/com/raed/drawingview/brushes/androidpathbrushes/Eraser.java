package com.raed.drawingview.brushes.androidpathbrushes;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;


public class Eraser extends PathBrush {

    private static final String TAG = "Eraser";

    public Eraser(int minSizePx, int maxSizePx) {
        super(minSizePx, maxSizePx);

        //ifer: use eraser as a white brush. Mode PorterDuff.Mode.CLEAR not working on Android API 19

//        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mPaint.setColor(0xFFFFFFFF);
        mPaint.setStrokeWidth(3);

    }

    @Override
    public void setColor(int color) {
        Log.w(TAG,"Eraser does not has a color");
        //Erasers do not have a color
    }

}
