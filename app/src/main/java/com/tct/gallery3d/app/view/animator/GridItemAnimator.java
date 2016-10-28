package com.tct.gallery3d.app.view.animator;

import android.support.v13.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

public class GridItemAnimator extends BaseItemAnimator {

//    private static final int DELAY_TIME = 50;
//    private static final int MAX_DELAY_TIME = 300;

    @Override
    protected void preAnimateRemoveImpl(RecyclerView.ViewHolder holder) {
        super.preAnimateRemoveImpl(holder);
    }

    @Override
    protected void animateRemoveImpl(final RecyclerView.ViewHolder holder) {
        ViewCompat.animate(holder.itemView)
                .translationY(holder.itemView.getHeight())
                .alpha(0)
                .setListener(new DefaultRemoveVpaListener(holder))
                .setDuration(getRemoveDuration())
//                .setStartDelay(DELAY_TIME)
                .start();
    }

    @Override
    protected void preAnimateAddImpl(RecyclerView.ViewHolder holder) {
        int[] outLocation = new int[2];
        holder.itemView.getLocationInWindow(outLocation);
        ViewCompat.setTranslationY(holder.itemView, outLocation[1]);
        ViewCompat.setAlpha(holder.itemView, 0);
    }

    @Override
    protected void animateAddImpl(final RecyclerView.ViewHolder holder) {
//        long delay = getAddDuration();
//        if (mLayoutManager != null) {
//            int first = mLayoutManager.findFirstVisibleItemPosition();
//            int diff = holder.getAdapterPosition() - first;
//            delay = Math.abs((diff) * DELAY_TIME / 8);
//        }
//        if (delay > MAX_DELAY_TIME) {
//            delay = MAX_DELAY_TIME;
//        }
        ViewCompat.animate(holder.itemView)
                .translationY(0)
                .alpha(1)
                .setDuration(getAddDuration())
                .setListener(new DefaultAddVpaListener(holder))
//                .setStartDelay(delay)
                .start();
    }
}
