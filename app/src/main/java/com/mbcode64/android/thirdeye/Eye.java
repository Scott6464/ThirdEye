package com.mbcode64.android.thirdeye;

import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class Eye {


    ImageView eyeball;
    RotateAnimation rotate;


    Eye(ImageView eyeball) {
        this.eyeball = eyeball;
        rotate = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF,
                .5f);
        rotate.setDuration(2000);
        rotate.setInterpolator(new LinearInterpolator());
    }


    public void startAnimation() {

        rotate.setRepeatCount(Animation.INFINITE);
        eyeball.startAnimation(rotate);
    }

    public void stopAnimation() {

        rotate.setRepeatCount(0);
    }
}
