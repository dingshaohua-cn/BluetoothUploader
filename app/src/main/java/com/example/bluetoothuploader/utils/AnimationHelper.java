package com.example.bluetoothuploader.utils;

import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

public class AnimationHelper {
    public static RotateAnimation refreshAnm;

    public static RotateAnimation getRefreshAnm() {
        RotateAnimation anm = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anm.setDuration(1000);//设置动画持续时间
        anm.setRepeatCount(Animation.INFINITE);
        anm.setInterpolator(new LinearInterpolator());
        refreshAnm = anm;
        return anm;
    }

    public static void stopRefreshAnm() {
        refreshAnm.cancel();
    }
}
