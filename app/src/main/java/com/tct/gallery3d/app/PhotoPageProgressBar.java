package com.tct.gallery3d.app;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tct.gallery3d.R;

public class PhotoPageProgressBar {

    private RelativeLayout mLayout;
    private ProgressBar mProgressBar;

    public PhotoPageProgressBar(Context context, RelativeLayout parentLayout) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayout = (RelativeLayout) inflater.inflate(R.layout.photopage_progress_bar, parentLayout, false);
        mProgressBar = (ProgressBar) mLayout.findViewById(R.id.progressBar);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mProgressBar.getLayoutParams();

        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56,
                context.getResources().getDisplayMetrics());
        lp.width = size;
        lp.height = size;

        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        parentLayout.addView(mLayout);
    }

    public void showProgressBar() {
        mLayout.setVisibility(View.VISIBLE);
    }

    public void setProgress(int progressPercent) {
        LayoutParams layoutParams = mProgressBar.getLayoutParams();
        layoutParams.width = mProgressBar.getWidth() * progressPercent / 100;
        mProgressBar.setLayoutParams(layoutParams);
    }

    public void hideProgressBar() {
        mLayout.setVisibility(View.INVISIBLE);
    }
}
