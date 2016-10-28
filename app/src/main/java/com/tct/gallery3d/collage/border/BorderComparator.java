package com.tct.gallery3d.collage.border;

import java.util.Comparator;

/**
 * Created by liuxiaoyu on 16-10-10.
 *
 * sort borders
 */
public class BorderComparator implements Comparator<Border> {
    private static final String TAG = "BorderComparator";

    @Override
    public int compare(Border lhs, Border rhs) {
        if (lhs.getRect().top < rhs.getRect().top) {
            return -1;
        } else if (lhs.getRect().top == rhs.getRect().top) {
            if (lhs.getRect().left < rhs.left()) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }
}
