package com.tct.gallery3d.fastjumper;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class SpanLookupHelper {
    public static int getSpanSize(RecyclerView.LayoutManager lm, int position) {
        if (lm instanceof GridLayoutManager) {
            return ((GridLayoutManager) lm).getSpanSizeLookup().getSpanSize(position);
        } else if (lm instanceof LinearLayoutManager) {
            return 1;
        } else {
            throw new IllegalStateException("Not support " + lm.getClass().getSimpleName());
        }
    }

    public static int getSpanCount(RecyclerView.LayoutManager lm) {
        if (lm instanceof GridLayoutManager) {
            return ((GridLayoutManager) lm).getSpanCount();
        } else if (lm instanceof LinearLayoutManager) {
            return 1;
        } else {
            throw new IllegalStateException("Not support " + lm.getClass().getSimpleName());
        }
    }

    public static int getSpanIndex(RecyclerView.LayoutManager lm, int position) {
        if (lm instanceof GridLayoutManager) {
            return ((GridLayoutManager) lm).getSpanSizeLookup().getSpanIndex(position, getSpanCount(lm));
        } else if (lm instanceof LinearLayoutManager) {
            return 0;
        } else {
            throw new IllegalStateException("Not support " + lm.getClass().getSimpleName());
        }
    }
}
