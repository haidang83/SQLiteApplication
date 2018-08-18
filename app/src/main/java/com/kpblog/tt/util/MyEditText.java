package com.kpblog.tt.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.EditText;

@SuppressLint("AppCompatCustomView")
public class MyEditText extends EditText {

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setError(CharSequence error, Drawable icon) {
        setCompoundDrawables(null, null, icon, null);
    }
}