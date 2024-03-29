/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.filtershow.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.android.gallery3d.R;
import com.android.gallery3d.filtershow.FilterShowActivity;

public class ColorPickerDialog extends Dialog   {
    ToggleButton mSelectedButton;
    ColorHueView mColorHueView;
    ColorSVRectView mColorSVRectView;
    ColorOpacityView mColorOpacityView;
    ColorCompareView mColorCompareView;

    float[] mHSVO = new float[4]; // hue=0..360, sat & val opacity = 0...1

    public ColorPickerDialog(Context context, final ColorListener cl) {
        super(context);
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm =  (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels*8/10;
        int width = metrics.widthPixels*8/10;
        getWindow().setLayout(width, height);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.filtershow_color_picker);
        mColorHueView = findViewById(R.id.ColorHueView);
        mColorSVRectView = findViewById(R.id.colorRectView);
        mColorOpacityView = findViewById(R.id.colorOpacityView);
        mColorCompareView = findViewById(R.id.btnSelect);

        float[] hsvo = {123, .9f, 1, 1 };

        ImageButton apply = findViewById(R.id.applyColorPick);
        ImageButton cancel = findViewById(R.id.cancelColorPick);

        apply.setOnClickListener(v -> {
            cl.setColor(mHSVO);
            ColorPickerDialog.this.dismiss();
        });
        cancel.setOnClickListener(v -> ColorPickerDialog.this.dismiss());
        ColorListener [] c = {mColorCompareView,mColorSVRectView,mColorOpacityView,mColorHueView};
        for (int i = 0; i < c.length; i++) {
            c[i].setColor(hsvo);
            for (int j = 0; j < c.length; j++) {
                if (i==j) {
                     continue;
                }
               c[i].addColorListener(c[j]);
            }
        }

        ColorListener colorListener = new ColorListener(){
            @Override
            public void setColor(float[] hsvo) {
                System.arraycopy(hsvo, 0, mHSVO, 0, mHSVO.length);
                int color = Color.HSVToColor(hsvo);
                setButtonColor(mSelectedButton, hsvo);
            }

            @Override
            public void addColorListener(ColorListener l) {
            }
        };

        for (ColorListener listener : c) {
            listener.addColorListener(colorListener);
        }
        setOnShowListener((FilterShowActivity) context);
        setOnDismissListener((FilterShowActivity) context);
    }

    void toggleClick(ToggleButton v, int[] buttons, boolean isChecked) {
        int id = v.getId();
        if (!isChecked) {
            mSelectedButton = null;
            return;
        }
        for (int button : buttons) {
            if (id != button) {
                ToggleButton b = findViewById(button);
                b.setChecked(false);
            }
        }
        mSelectedButton = v;

        float[] hsv = (float[]) v.getTag();

        ColorHueView csv = findViewById(R.id.ColorHueView);
        ColorSVRectView cwv = findViewById(R.id.colorRectView);
        ColorOpacityView cvv = findViewById(R.id.colorOpacityView);
        cwv.setColor(hsv);
        cvv.setColor(hsv);
        csv.setColor(hsv);
    }

    public void setOrigColor(float[] hsvo) {
        mColorCompareView.setOrigColor(hsvo);
    }

    public void setColor(float[] hsvo) {
        mColorOpacityView.setColor(hsvo);
        mColorHueView.setColor(hsvo);
        mColorSVRectView.setColor(hsvo);
        mColorCompareView.setColor(hsvo);
    }

    private void setButtonColor(ToggleButton button, float[] hsv) {
        if (button == null) {
            return;
        }
        int color = Color.HSVToColor(hsv);
        button.setBackgroundColor(color);
        float[] fg = {
                (hsv[0] + 180) % 360,
                hsv[1],
                (hsv[2] > .5f) ? .1f : .9f
        };
        button.setTextColor(Color.HSVToColor(fg));
        button.setTag(hsv);
    }

}
