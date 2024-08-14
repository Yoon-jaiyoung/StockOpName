package com.parkland.stockopname;

import android.text.InputFilter;
import android.text.Spanned;

public class MAT_InputFilter implements InputFilter {
    private int min, max;

    public MAT_InputFilter(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public MAT_InputFilter(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            Float input = Float.parseFloat(dest.toString() + source.toString());
            if (isInRange(min, max, input))
                return null;
        } catch (NumberFormatException nfe) { }
        return "";
    }

    private boolean isInRange(int a, int b, float c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
