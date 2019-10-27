package com.github.qompute.boxify.data;

import android.graphics.Bitmap;

public class BitmapLabelPair {

    private Bitmap bitmap;
    private String label;

    public BitmapLabelPair(Bitmap bitmap, String label) {
        this.bitmap = bitmap;
        this.label = label;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getLabel() {
        return label;
    }
}
