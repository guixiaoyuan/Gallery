/* 06/03/2015|    jialiang.ren     |      PR-937067       |[5.0][Gallery] pitch-to-zoom is not correctly 2x max */
/* ----------|---------------------|----------------------|---------------------------------------------------- */
/* 31/03/2015|    jialiang.ren     |      PR-962959       |[Android5.0][Gallery_v5.1.9.1.0109.0]    */
/*                                                         The picture will zoom out when zooming in*/
/* ----------|---------------------|----------------------|-----------------------------------------*/
/* 17/04/2015|    qiang.ding1       |      PR-959021       | [Android5.0][Gallery_v5.1.9.1.0107.0][REG][Monitor]There is display empty in title bar*/
/* ----------|--------------------- |----------------------|----------------------------------------------*/
/* 13/05/2015 |    jialiang.ren     |      PR-995626       |[Android][Gallery_v5.1.13.1.0201.0]The */
/*                                                          delete notice box is not in the middle */
/*------------|---------------------|----------------------|---------------------------------------*/
/* 18/06/2015 |    su.jiang         |      PR-1025516      |[Android 5.1][Gallery_v5.1.13.1.0208.0]The operation bar is not in */
/*------------|---------------------|-------------------   |the middle when playing the video----------------------------------*/
/* 05/10/2015|dongliang.feng        |PR512437              |[Android 5.1][Gallery_v5.2.0.1.1.0303.0]The icons disappeared after stopping slideshow */
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toolbar;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.GalleryActivity;

import java.lang.reflect.Method;

public class ScreenUtils {

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-06,PR937067
    public static final String TAG = "ScreenUtils";

    public static int STATUSBAR_HEIGHT = 0;

    public static int ACTIONBAR_HEIGHT = 0;

    public static int TABBAR_HEIGHT = 0;

    public static int ALBUM_MIN_SCROLL = 0;

    public static int ALBUMSET_MIN_SCROLL = 0;

    public static float SCALE_FIX = 0f;

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-31,PR962959
    public static float MAX_LIMIT = 0f;

    public static final int ANIM_TIME = 280;// MODIFIED by jian.pan1, 2016-03-28,BUG-1865173

    public static void initData(Context context) {
        getStatusBarHeight(context);
        getActionBarHeight(context);
        getTabBarHeight(context);
        getScaleFix((Activity)context);

        ALBUM_MIN_SCROLL = -STATUSBAR_HEIGHT - ACTIONBAR_HEIGHT;
        ALBUMSET_MIN_SCROLL = -STATUSBAR_HEIGHT - ACTIONBAR_HEIGHT - TABBAR_HEIGHT;
    }

    public static void getStatusBarHeight(Context context) {
        STATUSBAR_HEIGHT = (int)context.getResources().getDimension(R.dimen.status_bar_height);
    }

    public static void getActionBarHeight(Context context) {
        ACTIONBAR_HEIGHT = context.getResources().getDimensionPixelSize(R.dimen.action_bar_height);
    }

    public static void getTabBarHeight(Context context) {
        TABBAR_HEIGHT = context.getResources().getDimensionPixelSize(R.dimen.tab_height);
    }
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }

        return hasNavigationBar;

    }
    public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && checkDeviceHasNavigationBar(context)) {
            navigationBarHeight = rs.getDimensionPixelSize(id);
        }
        return navigationBarHeight;
    }

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-18,PR1025516 begin
    public static boolean isNavigationAtBottom(Activity activity) {
        boolean isAtBottom = false;
        DisplayMetrics realM = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(realM);
        DisplayMetrics actM = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(actM);
        if(realM.heightPixels > actM.heightPixels) {
            isAtBottom = true;
        }
        return isAtBottom;
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-06-18,PR1025516 end
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-06,PR937067 begin
    public static void getScaleFix(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        SCALE_FIX = 18 / metric.density;
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-31,PR962959 begin
        if(metric.density == 2) {
            MAX_LIMIT = 720 * 2.0f / 2304;
        } else {
            MAX_LIMIT = 1080 * 2.0f / 2304;
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-31,PR962959 end
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-06,PR937067 end

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-22,PR904487 begin
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-11-05, PR512437 begin
    /* MODIFIED-BEGIN by jian.pan1, 2016-03-28,BUG-1865173 */
    public static void showSystemUI(AbstractGalleryActivity activity, long animateDuration) {
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-21,PR955623 begin
        try {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
//            animatorShowToolbar(true, activity.getToolbar(), animateDuration);
        } catch(Exception e) {
            e.printStackTrace();
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-21,PR955623 end
    }

    public static void hideSystemUI(AbstractGalleryActivity activity, long animateDuration) {
    /* MODIFIED-END by jian.pan1,BUG-1865173 */
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-21,PR955623 begin
        try {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//            animatorShowToolbar(false, activity.getToolbar(), animateDuration);// MODIFIED by jian.pan1, 2016-03-28,BUG-1865173
        } catch(Exception e) {
            e.printStackTrace();
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-03-21,PR955623 end
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-22,PR904487 end

    private static ObjectAnimator objectAnimator = null;
    public static void animatorShowToolbar(boolean show, final Toolbar toolbar, long animateDuration) {// MODIFIED by jian.pan1, 2016-03-28,BUG-1865173
        final float wantAlpha = show ? 1 : 0;
        float alpha = toolbar.getAlpha();
        if ((alpha < 1 && alpha > 0) || alpha == wantAlpha) {
            return;
        }

        calcelAnim(toolbar, !show);
        objectAnimator = ObjectAnimator.ofFloat(toolbar, "alpha", wantAlpha);
        objectAnimator.setDuration(animateDuration);// MODIFIED by jian.pan1, 2016-03-28,BUG-1865173
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (wantAlpha == 1) {
                    if (toolbar.getTranslationY() != 0) toolbar.setTranslationY(0);
                    if (toolbar.getVisibility() != View.VISIBLE) toolbar.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if (wantAlpha == 0) {
                    toolbar.setVisibility(View.GONE);
                }
            }
        });
        objectAnimator.start();
    }
    //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-11-05, PR512437 end

    private static void calcelAnim(Toolbar toolbar, boolean show) {
        if(objectAnimator != null) {
            objectAnimator.cancel();
            objectAnimator = null;
            toolbar.setVisibility(View.VISIBLE);
            toolbar.setAlpha(show ? 1.0f : 0f);
        }
    }

    public static int getWidth(Activity context ) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;;
        return screenWidth;
    }

    public static int getHeight(Activity context ) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeigh = dm.heightPixels;
        return screenHeigh;
    }

    public static int getRealWidth(Activity context ) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        int screenRealWidth = dm.widthPixels;
        int screenRealHeight = dm.heightPixels;
        if(screenRealWidth>screenRealHeight){
            return screenRealHeight;
        }
        return screenRealWidth;
    }
    public final static int tempScreenInPortSplit=0;
    public final static int tempScreenInLandFull = 1;
    public final static int tempScreenInPortFull = 2;
    public final static int tempScreenInLandSplit = 3;

    public static int getScreenInfo(Activity context){
        int width = ScreenUtils.getWidth(context);
        int realWidth = ScreenUtils.getRealWidth(context);
        int height = ScreenUtils.getHeight(context);
        if(width>height)
        {
            if(width== realWidth){
                // split-screen in portrait !!
                Log.d(TAG, "go to split-screen  in portrait");
                return tempScreenInPortSplit ;
            }else{
                //fullScreen in Landscape
                Log.d(TAG,"go to fullScreen in Landscape");
                return tempScreenInLandFull;
            }

        }else {
            if (realWidth == width) {
                //fullScreen in portrait
                Log.d(TAG, "go to fullScreen in portrait");
                return tempScreenInPortFull;
            } else {
                // split-screen in Landscape
                Log.d(TAG, "go to split-screen in Landscape");
                return tempScreenInLandSplit;
            }
        }}

    public static boolean isInSplitScreen(Activity context){
        return getScreenInfo(context) == tempScreenInLandSplit ||
                getScreenInfo(context) == tempScreenInPortSplit;
    }

    /**
     * make sure screen is at bottom or not?
     * @param context
     * @param view
     * @return true :at bottom
     */
    public  static boolean splitScreenIsAtBottom(Activity context,View view){
        //AbstractGalleryActivity activity = (AbstractGalleryActivity)context;
        if (getScreenInfo(context) == tempScreenInPortSplit) {
            /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-10-19,BUG-3079416*/
            if (null == view) {
                return true;
            }
            /* MODIFIED-END by Yaoyu.Yang,BUG-3079416*/
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            Log.d(TAG, "splitScreenIsAtBottom" + location[1] + "location[0]= " + location[0]);
            //sometimes toolbar will hide at(0,-240)
            if (location[1] < 0 && location[0] == 0) {
                return false;
            }
            //if location is not (0,-x) at splitscreen,screen should be at bottom
            if (Math.abs(location[1] - location[0]) > 0) {
                return true;
            }
        }
        return false;
    }
}
