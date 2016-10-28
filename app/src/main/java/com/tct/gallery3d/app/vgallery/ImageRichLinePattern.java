package com.tct.gallery3d.app.vgallery;

import java.util.ArrayList;

import com.tct.gallery3d.data.MomentsAlbum.ItemBaseInfo;

public abstract class ImageRichLinePattern {
    /* Return the number of items matching the pattern */
    public abstract int match(ArrayList<ItemBaseInfo> images);

    /* Just return how many items the pattern applies */
    public abstract int imageCount();

    /* Return the height of line */
    public abstract void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]);

    private void fillSubDet(float in[][], float out[][], int removeX,
            int removeY, int lev) {

        for (int i = 0; i < (lev - 1); i++) {
            for (int j = 0; j < (lev - 1); j++) {
                if (i >= removeX && j >= removeY) {
                    out[i][j] = in[i + 1][j + 1];
                } else if (i >= removeX) {
                    out[i][j] = in[i + 1][j];
                } else if (j >= removeY) {
                    out[i][j] = in[i][j + 1];
                } else {
                    out[i][j] = in[i][j];
                }
            }
        }
    }

    private void fillValueDet(float in[][], float out[][], int replaceY, int lev) {
        for (int i = 0; i < lev; i++) {
            for (int j = 0; j < lev; j++) {
                if (j == replaceY) {
                    out[i][j] = in[i][lev];
                } else {
                    out[i][j] = in[i][j];
                }
            }
        }
    }

    private float calcDet(float mat[][], int lev) {
        if (lev == 2) {
            return ((mat[0][0] * mat[1][1]) - (mat[0][1] * mat[1][0]));
        } else {
            float result = 0.0f;
            float tempDet[][] = new float[lev - 1][lev - 1];

            for (int i = 0; i < lev; i++) {
                fillSubDet(mat, tempDet, 0, i, lev);

                float subDetValue = calcDet(tempDet, lev - 1);
                if (i % 2 == 0) {
                    result += mat[0][i] * subDetValue;
                } else {
                    result -= mat[0][i] * subDetValue;
                }
            }
            return result;
        }
    }

    protected void calcMatrix(float mat[][], float result[], int lev) {
        float tempValueDet[][] = new float[lev][lev];
        float baseDetValue = calcDet(mat, lev);

        for (int i = 0; i < lev; i++) {
            fillValueDet(mat, tempValueDet, i, lev);

            result[i] = calcDet(tempValueDet, lev) / baseDetValue;
        }
    }
}
