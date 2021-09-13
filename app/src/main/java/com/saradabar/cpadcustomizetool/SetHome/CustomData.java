package com.saradabar.cpadcustomizetool.SetHome;

import android.graphics.drawable.Drawable;

class CustomData {
    private Drawable imageData_;
    private String textData_;

    void setImagaData(Drawable image) {
        imageData_ = image;
    }

    Drawable getImageData() {
        return imageData_;
    }

    void setTextData(String text) {
        textData_ = text;
    }

    String getTextData() {
        return textData_;
    }
}

