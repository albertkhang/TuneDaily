package com.albertkhang.tunedaily.animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

public class RotateAnimation {
    private View view;

    public RotateAnimation(View view) {
        this.view = view;
    }

    public void start() {
        android.view.animation.RotateAnimation rotate = new android.view.animation.RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(13000);
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setRepeatCount(Animation.INFINITE);

        view.startAnimation(rotate);
    }

    public void stop(){
        view.clearAnimation();
    }
}
