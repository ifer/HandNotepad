package com.raed.drawingview;

import android.graphics.Bitmap;
import android.graphics.Rect;


public class DrawingAction {

    public Bitmap mBitmap;
    public Rect mRect;

    public DrawingAction(Bitmap bitmap, Rect rect){
        mBitmap = bitmap;
        mRect = new Rect(rect);
    }

    //The size is needed so we do not get an OutOfMemoryError
    public int getSize() {
        return mBitmap.getAllocationByteCount();
    }

}
