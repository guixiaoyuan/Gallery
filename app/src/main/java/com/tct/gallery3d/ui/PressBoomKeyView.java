package com.tct.gallery3d.ui;

import com.tct.gallery3d.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PressBoomKeyView extends RelativeLayout {
    
    private ImageView iv1 = null;
    
    private ImageView iv2 = null;
    
    public PressBoomKeyView(Context context) {
        this(context, null);
    }
    
    public PressBoomKeyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PressBoomKeyView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.setPadding(0, 0, 0, 0);
        iv1 = new ImageView(context);
        iv1.setImageResource(R.drawable.tip_pressboomkey_1);
        iv2 = new ImageView(context);
        iv2.setImageResource(R.drawable.tip_pressboomkey_2);
        this.addView(iv1);
        this.addView(iv2);

        RelativeLayout.LayoutParams lp1 = (RelativeLayout.LayoutParams)iv1.getLayoutParams();
        lp1.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

        RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams)iv2.getLayoutParams();
        lp2.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);


        Animation anim = AnimationUtils.loadAnimation(context, R.anim.anim_press_boom_point);
        iv2.startAnimation(anim);
    }

    public void recycle() {
        
    }
}
