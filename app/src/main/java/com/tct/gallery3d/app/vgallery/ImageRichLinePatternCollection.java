package com.tct.gallery3d.app.vgallery;

import java.util.ArrayList;

import com.tct.gallery3d.data.MomentsAlbum.ItemBaseInfo;

public class ImageRichLinePatternCollection {

    private static final int PATTERN_NUM = 29;

    private ImageRichLinePattern[] patterns;
    private ArrayList<Integer> availablePatterns;
    private int[] aPatternMatchSum;

    public boolean isImageListPortrait(ArrayList<ItemBaseInfo> images,
            int aTypes[], int aNum[], int lev) {
        // aTypes.length == aNum.length && lev <= images.size()
        boolean res = true;

        for (int i = 0; i < lev; i++) {
            int type = images.get(i).layoutType;

            for (int j = 0; j < aTypes.length; j++) {
                if (aNum[j] > 0 && (aTypes[j] & type) != 0) {
                    aNum[j]--;
                    break;
                }
            }
        }

        for (int i = 0; i < aNum.length; i++) {
            if (aNum[i] != 0) {
                res = false;
                break;
            }
        }

        return res;
    }

    public void adjustImageList(ArrayList<ItemBaseInfo> images,
            ArrayList<ItemBaseInfo> adjustImages, int baseIndex[]) {
        // images.size() = baseIndex.length
        adjustImages.add(images.get(0));
        baseIndex[0] = 0;

        for (int i = 1; i < images.size(); i++) {
            int j = 0;
            for (; j < adjustImages.size(); j++) {
                if (images.get(i).ratio > adjustImages.get(j).ratio) {
                    for (int k = adjustImages.size() - 1; k >= j; k--) {
                        baseIndex[k + 1] = baseIndex[k];
                    }
                    break;
                }
            }
            adjustImages.add(j, images.get(i));
            baseIndex[j] = i;
        }
    }

    public void restoreImageList(ArrayList<ItemBaseInfo> images,
            ArrayList<ItemBaseInfo> adjustImages, int baseIndex[]) {
        // adjustImages.size() = baseIndex.length
        for (int i = 0; i < adjustImages.size(); i++) {
            images.get(baseIndex[i]).w = adjustImages.get(i).w;
            images.get(baseIndex[i]).h = adjustImages.get(i).h;
            images.get(baseIndex[i]).x = adjustImages.get(i).x;
            images.get(baseIndex[i]).y = adjustImages.get(i).y;
        }
    }

    public int checkPattern(ArrayList<ItemBaseInfo> images, int scope[]) {
        int minIndex = 0;
        ArrayList<Integer> tempPatterns = new ArrayList<Integer>();

        if (scope != null && scope.length >= 2 && scope[0] >= 0 && scope[1] >= scope[0]) {
            for (int i = scope[0]; i <= scope[1]; i++) {
                if (patterns[i].match(images) != -1) {
                    tempPatterns.add(i);
                    if (aPatternMatchSum[i] < aPatternMatchSum[tempPatterns.get(minIndex)]) {
                        minIndex = tempPatterns.size() - 1;
                    }
                }
            }
        }

        availablePatterns.clear();

        for (int i = 0; i < tempPatterns.size(); i++) {
            if (aPatternMatchSum[tempPatterns.get(i)] <= aPatternMatchSum[tempPatterns
                    .get(minIndex)]) {
                availablePatterns.add(tempPatterns.get(i));
            }
        }

        return availablePatterns.size();
    }

    public int getPatternId(int index) {
        return availablePatterns.get(index);
    }

    public int pickNumForPattern(int index) {
        return patterns[availablePatterns.get(index)].imageCount();
    }

    public void applyPattern(ArrayList<ItemBaseInfo> images, int index, int in[], int pad, int out[]) {
        patterns[availablePatterns.get(index)].layout(images, in, pad, out);
    }

    public void changePatternMatchSum(int index, int lev) {
        aPatternMatchSum[index] += lev;
    }

    public ImageRichLinePatternCollection() {
        patterns = new ImageRichLinePattern[PATTERN_NUM];
        availablePatterns = new ArrayList<Integer>();
        aPatternMatchSum = new int[PATTERN_NUM];

        for (int i = 0; i < PATTERN_NUM; i++) {
            aPatternMatchSum[i] = 0;
        }

        patterns[0] = new ImageRichLinePattern() {

            public int imageCount() {
                return 5;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 5) {
                    return -1;
                }

                for (int i = 0; i < 5; i++) {
                    int type = images.get(i).layoutType;

                    if (ItemBaseInfo.IMAGE_LANDSCAPE == type || ItemBaseInfo.IMAGE_PANORAMA == type) {
                        return -1;
                    }
                }
                return 5;
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {
                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[5];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { 0, adjustImages.get(1).ratio, -adjustImages.get(2).ratio, 0, 0, 0 },
                        { 0, 0, 0, adjustImages.get(3).ratio, -adjustImages.get(4).ratio, 0 },
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio, 0,
                                -adjustImages.get(3).ratio, 0, 2 * pad }, { 0, 1, 1, -1, -1, 0 },
                        { 1, 1, 1, 0, 0, totalWidth - 6 * pad }, };

                float widths[] = new float[5];

                calcMatrix(matrix, widths, 5);

                for (int i = 0; i < 5; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(1).x = 0;
                adjustImages.get(1).y = 0;

                adjustImages.get(2).x = adjustImages.get(1).w + 2 * pad;
                adjustImages.get(2).y = 0;

                adjustImages.get(0).x = adjustImages.get(2).x + adjustImages.get(2).w
                        + 2 * pad;
                adjustImages.get(0).y = 0;

                adjustImages.get(3).x = 0;
                adjustImages.get(3).y = adjustImages.get(1).h + 2 * pad;

                adjustImages.get(4).x = adjustImages.get(3).x + adjustImages.get(3).w
                        + 2 * pad;
                adjustImages.get(4).y = adjustImages.get(3).y;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };
        };

        patterns[1] = new ImageRichLinePattern() {
            public int imageCount() {
                return 5;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 5) {
                    return -1;
                }

                for (int i = 0; i < 5; i++) {
                    int type = images.get(i).layoutType;

                    if (ItemBaseInfo.IMAGE_LANDSCAPE == type || ItemBaseInfo.IMAGE_PANORAMA == type) {
                        return -1;
                    }
                }
                return 5;
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {
                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[5];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { 0, adjustImages.get(1).ratio, -adjustImages.get(2).ratio, 0, 0, 0 },
                        { 0, 0, 0, adjustImages.get(3).ratio, -adjustImages.get(4).ratio, 0 },
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio, 0,
                                -adjustImages.get(3).ratio, 0, 2 * pad }, { 0, 1, 1, -1, -1, 0 },
                        { 1, 1, 1, 0, 0, totalWidth - 6 * pad }, };

                float widths[] = new float[5];

                calcMatrix(matrix, widths, 5);

                for (int i = 0; i < 5; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = adjustImages.get(0).w + 2 * pad;
                adjustImages.get(1).y = 0;

                adjustImages.get(2).x = adjustImages.get(1).x + adjustImages.get(1).w
                        + 2 * pad;
                adjustImages.get(2).y = 0;

                adjustImages.get(3).x = adjustImages.get(1).x;
                adjustImages.get(3).y = adjustImages.get(1).h + 2 * pad;

                adjustImages.get(4).x = adjustImages.get(3).x + adjustImages.get(3).w
                        + 2 * pad;
                adjustImages.get(4).y = adjustImages.get(3).y;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };
        };

        patterns[2] = new ImageRichLinePattern() {
            public int imageCount() {
                return 5;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 5) {
                    return -1;
                }

                for (int i = 0; i < 5; i++) {
                    int type = images.get(i).layoutType;

                    if (ItemBaseInfo.IMAGE_LANDSCAPE == type || ItemBaseInfo.IMAGE_PANORAMA == type) {
                        return -1;
                    }
                }
                return 5;
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {
                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[5];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio, 0,
                                -adjustImages.get(3).ratio, 0, 2 * pad },
                        { adjustImages.get(0).ratio, 0, -adjustImages.get(2).ratio, 0,
                                -adjustImages.get(4).ratio, 2 * pad }, { 0, 0, 1, 0, -1, 0 },
                        { 0, 1, 0, -1, 0, 0 }, { 1, 1, 1, 0, 0, totalWidth - 6 * pad }, };

                float widths[] = new float[5];

                calcMatrix(matrix, widths, 5);

                for (int i = 0; i < 5; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(1).x = 0;
                adjustImages.get(1).y = 0;

                adjustImages.get(0).x = adjustImages.get(1).w + 2 * pad;
                adjustImages.get(0).y = 0;

                adjustImages.get(2).x = adjustImages.get(0).x + adjustImages.get(0).w
                        + 2 * pad;
                adjustImages.get(2).y = 0;

                adjustImages.get(3).x = 0;
                adjustImages.get(3).y = adjustImages.get(1).h + 2 * pad;

                adjustImages.get(4).x = adjustImages.get(2).x;
                adjustImages.get(4).y = adjustImages.get(2).h + 2 * pad;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };

        };

        patterns[3] = new ImageRichLinePattern() {
            public int imageCount() {
                return 4;
            }

            public int match(ArrayList<ItemBaseInfo> images) {
                if (images.size() < 4) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE,
                        ItemBaseInfo.IMAGE_SQUARE | ItemBaseInfo.IMAGE_PORTRAIT,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 1, 2, 1 };

                if (true == isImageListPortrait(images, aTypes, aNum, 4)) {
                    return 4;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[4];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { 0, adjustImages.get(1).ratio, -adjustImages.get(2).ratio, 0, 0 },
                        { 0, -1, -1, 1, 2 * pad },
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio, 0,
                                -adjustImages.get(3).ratio, 2 * pad },
                        { 1, 0, 0, 1, totalWidth - 4 * pad }, };

                float widths[] = new float[4];

                calcMatrix(matrix, widths, 4);

                for (int i = 0; i < 4; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = 0;

                adjustImages.get(3).x = adjustImages.get(0).w + 2 * pad;
                adjustImages.get(3).y = 0;

                adjustImages.get(1).x = adjustImages.get(3).x;
                adjustImages.get(1).y = adjustImages.get(3).h + 2 * pad;

                adjustImages.get(2).x = adjustImages.get(1).x + adjustImages.get(1).w
                        + 2 * pad;
                adjustImages.get(2).y = adjustImages.get(1).y;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };
        };

        patterns[4] = new ImageRichLinePattern() {
            public int imageCount() {
                return 4;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 4) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE,
                        ItemBaseInfo.IMAGE_SQUARE | ItemBaseInfo.IMAGE_PORTRAIT,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 1, 2, 1 };

                if (true == isImageListPortrait(images, aTypes, aNum, 4)) {
                    return 4;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[4];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { 0, adjustImages.get(1).ratio, -adjustImages.get(2).ratio, 0, 0 },
                        { 0, -1, -1, 1, 2 * pad },
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio, 0,
                                -adjustImages.get(3).ratio, 2 * pad },
                        { 1, 0, 0, 1, totalWidth - 4 * pad }, };

                float widths[] = new float[4];

                calcMatrix(matrix, widths, 4);

                for (int i = 0; i < 4; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = adjustImages.get(0).w + 2 * pad;
                adjustImages.get(1).y = 0;

                adjustImages.get(2).x = adjustImages.get(1).x + adjustImages.get(1).w
                        + 2 * pad;
                adjustImages.get(2).y = 0;

                adjustImages.get(3).x = adjustImages.get(1).x;
                adjustImages.get(3).y = adjustImages.get(1).h + 2 * pad;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };
        };

        patterns[5] = new ImageRichLinePattern() {
            public int imageCount() {
                return 4;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 4) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE,
                        ItemBaseInfo.IMAGE_SQUARE | ItemBaseInfo.IMAGE_PORTRAIT,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 1, 2, 1 };

                if (true == isImageListPortrait(images, aTypes, aNum, 4)) {
                    return 4;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[4];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { 0, adjustImages.get(1).ratio, -adjustImages.get(2).ratio, 0, 0 },
                        { 0, -1, -1, 1, 2 * pad },
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio, 0,
                                -adjustImages.get(3).ratio, 2 * pad },
                        { 1, 0, 0, 1, totalWidth - 4 * pad }, };

                float widths[] = new float[4];

                calcMatrix(matrix, widths, 4);

                for (int i = 0; i < 4; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(3).x = 0;
                adjustImages.get(3).y = 0;

                adjustImages.get(0).x = adjustImages.get(3).w + 2 * pad;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = 0;
                adjustImages.get(1).y = adjustImages.get(3).h + 2 * pad;

                adjustImages.get(2).x = adjustImages.get(1).w + 2 * pad;
                adjustImages.get(2).y = adjustImages.get(1).y;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };
        };

        patterns[6] = new ImageRichLinePattern() {
            public int imageCount() {
                return 4;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 4) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE,
                        ItemBaseInfo.IMAGE_SQUARE | ItemBaseInfo.IMAGE_PORTRAIT,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 1, 2, 1 };

                if (true == isImageListPortrait(images, aTypes, aNum, 4)) {
                    return 4;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[4];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { 0, adjustImages.get(1).ratio, -adjustImages.get(2).ratio, 0, 0 },
                        { 0, -1, -1, 1, 2 * pad },
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio, 0,
                                -adjustImages.get(3).ratio, 2 * pad },
                        { 1, 0, 0, 1, totalWidth - 4 * pad }, };

                float widths[] = new float[4];

                calcMatrix(matrix, widths, 4);

                for (int i = 0; i < 4; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(1).x = 0;
                adjustImages.get(1).y = 0;

                adjustImages.get(2).x = adjustImages.get(1).w + 2 * pad;
                adjustImages.get(2).y = 0;

                adjustImages.get(0).x = adjustImages.get(2).x + adjustImages.get(2).w
                        + 2 * pad;
                adjustImages.get(0).y = 0;

                adjustImages.get(3).x = 0;
                adjustImages.get(3).y = adjustImages.get(1).h + 2 * pad;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };
        };

        patterns[7] = new ImageRichLinePattern() {
            public int imageCount() {
                return 3;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 3) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 2, 1 };

                if (true == isImageListPortrait(images, aTypes, aNum, 3)) {
                    return 3;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[3];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { 0, 1, -1, 0 },
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio,
                                -adjustImages.get(2).ratio, 2 * pad },
                        { 1, 1, 0, totalWidth - 4 * pad }, };

                float widths[] = new float[3];

                calcMatrix(matrix, widths, 3);

                for (int i = 0; i < 3; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = adjustImages.get(0).w + 2 * pad;
                adjustImages.get(1).y = 0;

                adjustImages.get(2).x = adjustImages.get(1).x;
                adjustImages.get(2).y = adjustImages.get(1).h + 2 * pad;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };
        };

        patterns[8] = new ImageRichLinePattern() {
            public int imageCount() {
                return 3;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 3) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 2, 1 };

                if (true == isImageListPortrait(images, aTypes, aNum, 3)) {
                    return 3;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[3];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { 0, 1, -1, 0 },
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio,
                                -adjustImages.get(2).ratio, 2 * pad },
                        { 1, 1, 0, totalWidth - 4 * pad }, };

                float widths[] = new float[3];

                calcMatrix(matrix, widths, 3);

                for (int i = 0; i < 3; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(1).x = 0;
                adjustImages.get(1).y = 0;

                adjustImages.get(2).x = 0;
                adjustImages.get(2).y = adjustImages.get(1).h + 2 * pad;

                adjustImages.get(0).x = adjustImages.get(1).w + 2 * pad;
                adjustImages.get(0).y = 0;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };
        };

        patterns[9] = new ImageRichLinePattern() {
            public int imageCount() {
                return 5;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 5) {
                    return -1;
                }

                for (int i = 0; i < 5; i++) {
                    int type = images.get(i).layoutType;
                    if (!(ItemBaseInfo.IMAGE_LANDSCAPE == type || ItemBaseInfo.IMAGE_SQUARE == type)) {
                        return -1;
                    }
                }
                return 5;
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[5];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { 1, -1, 0, 0, 0, 0 },
                        { 0, 0, 1, -1, 0, 0 },
                        { 0, 0, 1, 0, -1, 0 },
                        { adjustImages.get(0).ratio, adjustImages.get(1).ratio,
                                -adjustImages.get(2).ratio, -adjustImages.get(3).ratio,
                                -adjustImages.get(4).ratio, 2 * pad },
                        { 1, 0, 1, 0, 0, totalWidth - 4 * pad }, };

                float widths[] = new float[5];

                calcMatrix(matrix, widths, 5);

                for (int i = 0; i < 5; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = 0;
                adjustImages.get(1).y = adjustImages.get(0).h + 2 * pad;

                adjustImages.get(2).x = adjustImages.get(0).w + 2 * pad;
                adjustImages.get(2).y = 0;

                adjustImages.get(3).x = adjustImages.get(2).x;
                adjustImages.get(3).y = adjustImages.get(2).h + 2 * pad;

                adjustImages.get(4).x = adjustImages.get(2).x;
                adjustImages.get(4).y = adjustImages.get(3).y + adjustImages.get(3).h
                        + 2 * pad;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + adjustImages.get(1).h + 4 * pad;
            };
        };

        patterns[10] = new ImageRichLinePattern() {
            public int imageCount() {
                return 5;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 5) {
                    return -1;
                }

                for (int i = 0; i < 5; i++) {
                    int type = images.get(i).layoutType;
                    if (!(ItemBaseInfo.IMAGE_LANDSCAPE == type || ItemBaseInfo.IMAGE_SQUARE == type)) {
                        return -1;
                    }
                }
                return 5;
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[5];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { 1, -1, 0, 0, 0, 0 },
                        { 0, 0, 1, -1, 0, 0 },
                        { 0, 0, 1, 0, -1, 0 },
                        { adjustImages.get(0).ratio, adjustImages.get(1).ratio,
                                -adjustImages.get(2).ratio, -adjustImages.get(3).ratio,
                                -adjustImages.get(4).ratio, 2 * pad },
                        { 1, 0, 1, 0, 0, totalWidth - 4 * pad }, };

                float widths[] = new float[5];

                calcMatrix(matrix, widths, 5);

                for (int i = 0; i < 5; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(2).x = 0;
                adjustImages.get(2).y = 0;

                adjustImages.get(3).x = 0;
                adjustImages.get(3).y = adjustImages.get(2).h + 2 * pad;

                adjustImages.get(4).x = 0;
                adjustImages.get(4).y = adjustImages.get(3).y + adjustImages.get(3).h
                        + 2 * pad;

                adjustImages.get(0).x = adjustImages.get(2).w + 2 * pad;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = adjustImages.get(0).x;
                adjustImages.get(1).y = adjustImages.get(0).h + 2 * pad;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + adjustImages.get(1).h + 4 * pad;
            };
        };

        patterns[11] = new ImageRichLinePattern() {
            public int imageCount() {
                return 4;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 4) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 2, 2 };

                if (true == isImageListPortrait(images, aTypes, aNum, 4)) {
                    return 4;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[4];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio, 0, 0, 0 },
                        { 0, 0, 1, -1, 0 },
                        { adjustImages.get(0).ratio, 0, -adjustImages.get(2).ratio,
                                -adjustImages.get(3).ratio, 2 * pad },
                        { 1, 1, 1, 0, totalWidth - 6 * pad }, };

                float widths[] = new float[4];

                calcMatrix(matrix, widths, 4);

                for (int i = 0; i < 4; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = adjustImages.get(0).w + 2 * pad;
                adjustImages.get(1).y = 0;

                adjustImages.get(2).x = adjustImages.get(1).x + adjustImages.get(1).w
                        + 2 * pad;
                adjustImages.get(2).y = 0;

                adjustImages.get(3).x = adjustImages.get(2).x;
                adjustImages.get(3).y = adjustImages.get(2).h + 2 * pad;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };
        };

        patterns[12] = new ImageRichLinePattern() {
            public int imageCount() {
                return 4;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 4) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 2, 2 };

                if (true == isImageListPortrait(images, aTypes, aNum, 4)) {
                    return 4;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[4];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio, 0, 0, 0 },
                        { 0, 0, 1, -1, 0 },
                        { adjustImages.get(0).ratio, 0, -adjustImages.get(2).ratio,
                                -adjustImages.get(3).ratio, 2 * pad },
                        { 1, 1, 1, 0, totalWidth - 6 * pad }, };

                float widths[] = new float[4];

                calcMatrix(matrix, widths, 4);

                for (int i = 0; i < 4; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = 0;

                adjustImages.get(2).x = adjustImages.get(0).w + 2 * pad;
                adjustImages.get(2).y = 0;

                adjustImages.get(3).x = adjustImages.get(2).x;
                adjustImages.get(3).y = adjustImages.get(2).h + 2 * pad;

                adjustImages.get(1).x = adjustImages.get(2).x + adjustImages.get(2).w
                        + 2 * pad;
                adjustImages.get(1).y = 0;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };
        };

        patterns[13] = new ImageRichLinePattern() {
            public int imageCount() {
                return 4;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 4) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 2, 2 };

                if (true == isImageListPortrait(images, aTypes, aNum, 4)) {
                    return 4;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[4];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio, 0, 0, 0 },
                        { 0, 0, 1, -1, 0 },
                        { adjustImages.get(0).ratio, 0, -adjustImages.get(2).ratio,
                                -adjustImages.get(3).ratio, 2 * pad },
                        { 1, 1, 1, 0, totalWidth - 6 * pad }, };

                float widths[] = new float[4];

                calcMatrix(matrix, widths, 4);

                for (int i = 0; i < 4; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(2).x = 0;
                adjustImages.get(2).y = 0;

                adjustImages.get(3).x = 0;
                adjustImages.get(3).y = adjustImages.get(2).h + 2 * pad;

                adjustImages.get(0).x = adjustImages.get(2).w + 2 * pad;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = adjustImages.get(0).x + adjustImages.get(0).w
                        + 2 * pad;
                adjustImages.get(1).y = 0;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };
        };

        patterns[14] = new ImageRichLinePattern() {
            public int imageCount() {
                return 4;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 4) {
                    return -1;
                }

                for (int i = 0; i < 4; i++) {
                    if (ItemBaseInfo.IMAGE_PANORAMA == images.get(i).layoutType) {
                        return -1;
                    }
                }

                if (images.get(0).ratio / images.get(1).ratio == images.get(2).ratio
                        / images.get(3).ratio) {/* Same as single line layout */
                    return -1;
                }

                float leftYRatio = images.get(0).ratio + images.get(2).ratio;
                float rightYRatio = images.get(1).ratio + images.get(3).ratio;

                float lrRatio = leftYRatio / rightYRatio;
                float totalYRatio = (leftYRatio * rightYRatio) / (leftYRatio + rightYRatio);

                if (lrRatio < 0.4f || lrRatio > 2.5f || totalYRatio < 0.25f || totalYRatio > 0.8f) {
                    return -1;
                }

                return 4;
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                int totalWidth = in[0];

                float matrix[][] = {
                        { 1, 0, -1, 0, 0 },
                        { 0, 1, 0, -1, 0 },
                        { images.get(0).ratio, -images.get(1).ratio, images.get(2).ratio,
                                -images.get(3).ratio, 0 }, { 1, 1, 0, 0, totalWidth - 4 * pad }, };

                float widths[] = new float[4];

                calcMatrix(matrix, widths, 4);

                for (int i = 0; i < 4; i++) {
                    ItemBaseInfo image = images.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                images.get(0).x = 0;
                images.get(0).y = 0;

                images.get(1).x = images.get(0).w + 2 * pad;
                images.get(1).y = 0;

                images.get(2).x = 0;
                images.get(2).y = images.get(0).h + 2 * pad;

                images.get(3).x = images.get(1).x;
                images.get(3).y = images.get(1).h + 2 * pad;

                out[0] = totalWidth;
                out[1] = images.get(0).h + images.get(2).h + 4 * pad;
            };

        };

        patterns[15] = new ImageRichLinePattern() {
            public int imageCount() {
                return 4;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 4) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 3, 1 };

                if (true == isImageListPortrait(images, aTypes, aNum, 4)) {
                    return 4;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {
                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[4];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { 0, 1, -1, 0, 0 },
                        { 0, 0, 1, -1, 0 },
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio,
                                -adjustImages.get(2).ratio, -adjustImages.get(3).ratio, 4 * pad },
                        { 1, 1, 0, 0, totalWidth - 4 * pad }, };

                float widths[] = new float[4];

                calcMatrix(matrix, widths, 4);

                for (int i = 0; i < 4; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = adjustImages.get(0).w + 2 * pad;
                adjustImages.get(1).y = 0;

                adjustImages.get(2).x = adjustImages.get(1).x;
                adjustImages.get(2).y = adjustImages.get(1).y + adjustImages.get(1).h
                        + 2 * pad;

                adjustImages.get(3).x = adjustImages.get(1).x;
                adjustImages.get(3).y = adjustImages.get(2).y + adjustImages.get(2).h
                        + 2 * pad;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };
        };

        patterns[16] = new ImageRichLinePattern() {
            public int imageCount() {
                return 4;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 4) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 3, 1 };

                if (true == isImageListPortrait(images, aTypes, aNum, 4)) {
                    return 4;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {
                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[4];
                int totalWidth = in[0];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { 0, 1, -1, 0, 0 },
                        { 0, 0, 1, -1, 0 },
                        { adjustImages.get(0).ratio, -adjustImages.get(1).ratio,
                                -adjustImages.get(2).ratio, -adjustImages.get(3).ratio, 4 * pad },
                        { 1, 0, 0, 1, totalWidth - 4 * pad }, };

                float widths[] = new float[4];

                calcMatrix(matrix, widths, 4);

                for (int i = 0; i < 4; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.w = (int) widths[i];
                    image.h = (int) (image.w * image.ratio);
                }

                adjustImages.get(1).x = 0;
                adjustImages.get(1).x = 0;

                adjustImages.get(2).x = 0;
                adjustImages.get(2).y = adjustImages.get(1).y + adjustImages.get(1).h
                        + 2 * pad;

                adjustImages.get(3).x = 0;
                adjustImages.get(3).y = adjustImages.get(2).y + adjustImages.get(2).h
                        + 2 * pad;

                adjustImages.get(0).x = adjustImages.get(1).x + adjustImages.get(1).w
                        + 2 * pad;
                adjustImages.get(0).y = 0;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = totalWidth;
                out[1] = adjustImages.get(0).h + 2 * pad;
            };
        };

        // Following patterns for landscape mode
        patterns[17] = new ImageRichLinePattern() {
            public int imageCount() {
                return 3;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 3) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE,
                        ItemBaseInfo.IMAGE_SQUARE | ItemBaseInfo.IMAGE_PORTRAIT };
                int aNum[] = { 1, 2 };

                if (true == isImageListPortrait(images, aTypes, aNum, 3)) {
                    return 3;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[4];
                int totalHeight = in[1];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { -1 / adjustImages.get(0).ratio, -1 / adjustImages.get(1).ratio,
                                1 / adjustImages.get(2).ratio, 2 * pad }, { 1, -1, 0, 0 },
                        { 1, 0, 1, totalHeight - 4 * pad }, };

                float heights[] = new float[3];

                calcMatrix(matrix, heights, 3);

                for (int i = 0; i < 3; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.h = (int) heights[i];
                    image.w = (int) (image.h / image.ratio);
                }

                adjustImages.get(2).x = 0;
                adjustImages.get(2).y = 0;

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = adjustImages.get(2).h + 2 * pad;

                adjustImages.get(1).x = adjustImages.get(0).w + 2 * pad;
                adjustImages.get(1).y = adjustImages.get(0).y;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = adjustImages.get(2).w + 2 * pad;
                out[1] = totalHeight;
            };
        };

        patterns[18] = new ImageRichLinePattern() {
            public int imageCount() {
                return 3;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 3) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE,
                        ItemBaseInfo.IMAGE_SQUARE | ItemBaseInfo.IMAGE_PORTRAIT };
                int aNum[] = { 1, 2 };

                if (true == isImageListPortrait(images, aTypes, aNum, 3)) {
                    return 3;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[4];
                int totalHeight = in[1];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { -1 / adjustImages.get(0).ratio, -1 / adjustImages.get(1).ratio,
                                1 / adjustImages.get(2).ratio, 2 * pad }, { 1, -1, 0, 0 },
                        { 1, 0, 1, totalHeight - 4 * pad }, };

                float heights[] = new float[3];

                calcMatrix(matrix, heights, 3);

                for (int i = 0; i < 3; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.h = (int) heights[i];
                    image.w = (int) (image.h / image.ratio);
                }

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = adjustImages.get(0).w + 2 * pad;
                adjustImages.get(1).y = 0;

                adjustImages.get(2).x = 0;
                adjustImages.get(2).y = adjustImages.get(0).h + 2 * pad;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = adjustImages.get(2).w + 2 * pad;
                out[1] = totalHeight;
            };
        };

        patterns[19] = new ImageRichLinePattern() {
            public int imageCount() {
                return 6;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 6) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 4, 2 };

                if (true == isImageListPortrait(images, aTypes, aNum, 6)) {
                    return 6;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[6];
                int totalHeight = in[1];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { -1 / adjustImages.get(0).ratio, -1 / adjustImages.get(1).ratio,
                                -1 / adjustImages.get(2).ratio, 0, 1 / adjustImages.get(4).ratio,
                                1 / adjustImages.get(5).ratio, 2 * pad },
                        { 0, 0, 1 / adjustImages.get(2).ratio, -1 / adjustImages.get(3).ratio, 0,
                                0, 0 }, { 1, 0, -1, -1, 0, 0, 2 * pad }, { 1, -1, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 1, -1, 0 }, { 1, 0, 0, 0, 1, 0, totalHeight - 4 * pad }, };

                float heights[] = new float[6];

                calcMatrix(matrix, heights, 6);

                for (int i = 0; i < 6; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.h = (int) heights[i];
                    image.w = (int) (image.h / image.ratio);
                }

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = adjustImages.get(0).w + 2 * pad;
                adjustImages.get(1).y = 0;

                adjustImages.get(2).x = adjustImages.get(1).x + adjustImages.get(1).w
                        + 2 * pad;
                adjustImages.get(2).y = 0;

                adjustImages.get(3).x = adjustImages.get(2).x;
                adjustImages.get(3).y = adjustImages.get(2).h + 2 * pad;

                adjustImages.get(4).x = 0;
                adjustImages.get(4).y = adjustImages.get(0).h + 2 * pad;

                adjustImages.get(5).x = adjustImages.get(4).w + 2 * pad;
                adjustImages.get(5).y = adjustImages.get(4).y;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = adjustImages.get(4).w + adjustImages.get(5).w + 4 * pad;
                out[1] = totalHeight;
            };
        };

        patterns[20] = new ImageRichLinePattern() {
            public int imageCount() {
                return 6;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 6) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 4, 2 };

                if (true == isImageListPortrait(images, aTypes, aNum, 6)) {
                    return 6;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[6];
                int totalHeight = in[1];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { -1 / adjustImages.get(0).ratio, -1 / adjustImages.get(1).ratio,
                                -1 / adjustImages.get(2).ratio, 0, 1 / adjustImages.get(4).ratio,
                                1 / adjustImages.get(5).ratio, 2 * pad },
                        { 0, 0, 1 / adjustImages.get(2).ratio, -1 / adjustImages.get(3).ratio, 0,
                                0, 0 }, { 1, 0, -1, -1, 0, 0, 2 * pad }, { 1, -1, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 1, -1, 0 }, { 1, 0, 0, 0, 1, 0, totalHeight - 4 * pad }, };

                float heights[] = new float[6];

                calcMatrix(matrix, heights, 6);

                for (int i = 0; i < 6; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.h = (int) heights[i];
                    image.w = (int) (image.h / image.ratio);
                }

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = 0;

                adjustImages.get(2).x = adjustImages.get(0).w + 2 * pad;
                adjustImages.get(2).y = 0;

                adjustImages.get(3).x = adjustImages.get(2).x;
                adjustImages.get(3).y = adjustImages.get(2).h + 2 * pad;

                adjustImages.get(1).x = adjustImages.get(2).x + adjustImages.get(2).w
                        + 2 * pad;
                adjustImages.get(1).y = 0;

                adjustImages.get(4).x = 0;
                adjustImages.get(4).y = adjustImages.get(0).h + 2 * pad;

                adjustImages.get(5).x = adjustImages.get(4).w + 2 * pad;
                adjustImages.get(5).y = adjustImages.get(4).y;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = adjustImages.get(4).w + adjustImages.get(5).w + 4 * pad;
                out[1] = totalHeight;
            };
        };

        patterns[21] = new ImageRichLinePattern() {
            public int imageCount() {
                return 6;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 6) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 4, 2 };

                if (true == isImageListPortrait(images, aTypes, aNum, 6)) {
                    return 6;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[6];
                int totalHeight = in[1];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { -1 / adjustImages.get(0).ratio, -1 / adjustImages.get(1).ratio,
                                -1 / adjustImages.get(2).ratio, 0, 1 / adjustImages.get(4).ratio,
                                1 / adjustImages.get(5).ratio, 2 * pad },
                        { 0, 0, 1 / adjustImages.get(2).ratio, -1 / adjustImages.get(3).ratio, 0,
                                0, 0 }, { 1, 0, -1, -1, 0, 0, 2 * pad }, { 1, -1, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 1, -1, 0 }, { 1, 0, 0, 0, 1, 0, totalHeight - 4 * pad }, };

                float heights[] = new float[6];

                calcMatrix(matrix, heights, 6);

                for (int i = 0; i < 6; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.h = (int) heights[i];
                    image.w = (int) (image.h / image.ratio);
                }

                adjustImages.get(2).x = 0;
                adjustImages.get(2).y = 0;

                adjustImages.get(3).x = 0;
                adjustImages.get(3).y = adjustImages.get(2).h + 2 * pad;

                adjustImages.get(0).x = adjustImages.get(2).w + 2 * pad;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = adjustImages.get(0).x + adjustImages.get(0).w
                        + 2 * pad;
                adjustImages.get(1).y = 0;

                adjustImages.get(4).x = 0;
                adjustImages.get(4).y = adjustImages.get(0).h + 2 * pad;

                adjustImages.get(5).x = adjustImages.get(4).w + 2 * pad;
                adjustImages.get(5).y = adjustImages.get(4).y;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = adjustImages.get(4).w + adjustImages.get(5).w + 4 * pad;
                out[1] = totalHeight;
            };
        };

        patterns[22] = new ImageRichLinePattern() {
            public int imageCount() {
                return 6;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 6) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 4, 2 };

                if (true == isImageListPortrait(images, aTypes, aNum, 6)) {
                    return 6;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[6];
                int totalHeight = in[1];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { -1 / adjustImages.get(0).ratio, -1 / adjustImages.get(1).ratio,
                                -1 / adjustImages.get(2).ratio, 0, 1 / adjustImages.get(4).ratio,
                                1 / adjustImages.get(5).ratio, 2 * pad },
                        { 0, 0, 1 / adjustImages.get(2).ratio, -1 / adjustImages.get(3).ratio, 0,
                                0, 0 }, { 1, 0, -1, -1, 0, 0, 2 * pad }, { 1, -1, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 1, -1, 0 }, { 1, 0, 0, 0, 1, 0, totalHeight - 4 * pad }, };

                float heights[] = new float[6];

                calcMatrix(matrix, heights, 6);

                for (int i = 0; i < 6; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.h = (int) heights[i];
                    image.w = (int) (image.h / image.ratio);
                }

                adjustImages.get(4).x = 0;
                adjustImages.get(4).y = 0;

                adjustImages.get(5).x = adjustImages.get(4).w + 2 * pad;
                adjustImages.get(5).y = 0;

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = adjustImages.get(4).h + 2 * pad;

                adjustImages.get(1).x = adjustImages.get(0).w + 2 * pad;
                adjustImages.get(1).y = adjustImages.get(0).y;

                adjustImages.get(2).x = adjustImages.get(1).x + adjustImages.get(1).w
                        + 2 * pad;
                adjustImages.get(2).y = adjustImages.get(0).y;

                adjustImages.get(3).x = adjustImages.get(2).x;
                adjustImages.get(3).y = adjustImages.get(2).y + adjustImages.get(2).h
                        + 2 * pad;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = adjustImages.get(4).w + adjustImages.get(5).w + 4 * pad;
                out[1] = totalHeight;
            };
        };

        patterns[23] = new ImageRichLinePattern() {
            public int imageCount() {
                return 6;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 6) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 4, 2 };

                if (true == isImageListPortrait(images, aTypes, aNum, 6)) {
                    return 6;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[6];
                int totalHeight = in[1];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { -1 / adjustImages.get(0).ratio, -1 / adjustImages.get(1).ratio,
                                -1 / adjustImages.get(2).ratio, 0, 1 / adjustImages.get(4).ratio,
                                1 / adjustImages.get(5).ratio, 2 * pad },
                        { 0, 0, 1 / adjustImages.get(2).ratio, -1 / adjustImages.get(3).ratio, 0,
                                0, 0 }, { 1, 0, -1, -1, 0, 0, 2 * pad }, { 1, -1, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 1, -1, 0 }, { 1, 0, 0, 0, 1, 0, totalHeight - 4 * pad }, };

                float heights[] = new float[6];

                calcMatrix(matrix, heights, 6);

                for (int i = 0; i < 6; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.h = (int) heights[i];
                    image.w = (int) (image.h / image.ratio);
                }

                adjustImages.get(4).x = 0;
                adjustImages.get(4).y = 0;

                adjustImages.get(5).x = adjustImages.get(4).w + 2 * pad;
                adjustImages.get(5).y = 0;

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = adjustImages.get(4).h + 2 * pad;

                adjustImages.get(2).x = adjustImages.get(0).x + adjustImages.get(0).w
                        + 2 * pad;
                adjustImages.get(2).y = adjustImages.get(0).y;

                adjustImages.get(3).x = adjustImages.get(2).x;
                adjustImages.get(3).y = adjustImages.get(2).y + adjustImages.get(2).h
                        + 2 * pad;

                adjustImages.get(1).x = adjustImages.get(2).x + adjustImages.get(2).w
                        + 2 * pad;
                adjustImages.get(1).y = adjustImages.get(0).y;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = adjustImages.get(4).w + adjustImages.get(5).w + 4 * pad;
                out[1] = totalHeight;
            };
        };

        patterns[24] = new ImageRichLinePattern() {
            public int imageCount() {
                return 6;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 6) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 4, 2 };

                if (true == isImageListPortrait(images, aTypes, aNum, 6)) {
                    return 6;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[6];
                int totalHeight = in[1];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { -1 / adjustImages.get(0).ratio, -1 / adjustImages.get(1).ratio,
                                -1 / adjustImages.get(2).ratio, 0, 1 / adjustImages.get(4).ratio,
                                1 / adjustImages.get(5).ratio, 2 * pad },
                        { 0, 0, 1 / adjustImages.get(2).ratio, -1 / adjustImages.get(3).ratio, 0,
                                0, 0 }, { 1, 0, -1, -1, 0, 0, 2 * pad }, { 1, -1, 0, 0, 0, 0, 0 },
                        { 0, 0, 0, 0, 1, -1, 0 }, { 1, 0, 0, 0, 1, 0, totalHeight - 4 * pad }, };

                float heights[] = new float[6];

                calcMatrix(matrix, heights, 6);

                for (int i = 0; i < 6; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.h = (int) heights[i];
                    image.w = (int) (image.h / image.ratio);
                }

                adjustImages.get(4).x = 0;
                adjustImages.get(4).y = 0;

                adjustImages.get(5).x = adjustImages.get(4).w + 2 * pad;
                adjustImages.get(5).y = 0;

                adjustImages.get(2).x = 0;
                adjustImages.get(2).y = adjustImages.get(4).h + 2 * pad;

                adjustImages.get(3).x = 0;
                adjustImages.get(3).y = adjustImages.get(2).y + adjustImages.get(2).h
                        + 2 * pad;

                adjustImages.get(0).x = adjustImages.get(2).x + adjustImages.get(2).w
                        + 2 * pad;
                adjustImages.get(0).y = adjustImages.get(2).y;

                adjustImages.get(1).x = adjustImages.get(0).x + adjustImages.get(0).w
                        + 2 * pad;
                adjustImages.get(1).y = adjustImages.get(2).y;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = adjustImages.get(4).w + adjustImages.get(5).w + 4 * pad;
                out[1] = totalHeight;
            };
        };

        patterns[25] = new ImageRichLinePattern() {
            public int imageCount() {
                return 5;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 5) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 2, 3 };

                if (true == isImageListPortrait(images, aTypes, aNum, 5)) {
                    return 5;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[5];
                int totalHeight = in[1];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { -1 / adjustImages.get(0).ratio, -1 / adjustImages.get(1).ratio,
                                -1 / adjustImages.get(2).ratio, 1 / adjustImages.get(3).ratio,
                                1 / adjustImages.get(4).ratio, 2 * pad }, { 1, 0, -1, 0, 0, 0 },
                        { 1, -1, 0, 0, 0, 0 }, { 0, 0, 0, 1, -1, 0 },
                        { 1, 0, 0, 1, 0, totalHeight - 4 * pad }, };

                float heights[] = new float[5];

                calcMatrix(matrix, heights, 5);

                for (int i = 0; i < 5; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.h = (int) heights[i];
                    image.w = (int) (image.h / image.ratio);
                }

                adjustImages.get(3).x = 0;
                adjustImages.get(3).y = 0;

                adjustImages.get(4).x = adjustImages.get(3).w + 2 * pad;
                adjustImages.get(4).y = 0;

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = adjustImages.get(3).h + 2 * pad;

                adjustImages.get(1).x = adjustImages.get(0).x + adjustImages.get(0).w
                        + 2 * pad;
                adjustImages.get(1).y = adjustImages.get(0).y;

                adjustImages.get(2).x = adjustImages.get(1).x + adjustImages.get(1).w
                        + 2 * pad;
                adjustImages.get(2).y = adjustImages.get(0).y;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = adjustImages.get(3).w + adjustImages.get(4).w + 4 * pad;
                out[1] = totalHeight;
            };
        };

        patterns[26] = new ImageRichLinePattern() {
            public int imageCount() {
                return 5;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 5) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 2, 3 };

                if (true == isImageListPortrait(images, aTypes, aNum, 5)) {
                    return 5;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[5];
                int totalHeight = in[1];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { -1 / adjustImages.get(0).ratio, -1 / adjustImages.get(1).ratio,
                                -1 / adjustImages.get(2).ratio, 1 / adjustImages.get(3).ratio,
                                1 / adjustImages.get(4).ratio, 2 * pad }, { 1, 0, -1, 0, 0, 0 },
                        { 1, -1, 0, 0, 0, 0 }, { 0, 0, 0, 1, -1, 0 },
                        { 1, 0, 0, 1, 0, totalHeight - 4 * pad }, };

                float heights[] = new float[5];

                calcMatrix(matrix, heights, 5);

                for (int i = 0; i < 5; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.h = (int) heights[i];
                    image.w = (int) (image.h / image.ratio);
                }

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = adjustImages.get(0).x + adjustImages.get(0).w
                        + 2 * pad;
                adjustImages.get(1).y = adjustImages.get(0).y;

                adjustImages.get(2).x = adjustImages.get(1).x + adjustImages.get(1).w
                        + 2 * pad;
                adjustImages.get(2).y = adjustImages.get(0).y;

                adjustImages.get(3).x = 0;
                adjustImages.get(3).y = adjustImages.get(0).h + 2 * pad;

                adjustImages.get(4).x = adjustImages.get(3).w + 2 * pad;
                adjustImages.get(4).y = adjustImages.get(3).y;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = adjustImages.get(3).w + adjustImages.get(4).w + 4 * pad;
                out[1] = totalHeight;
            };
        };

        patterns[27] = new ImageRichLinePattern() {
            public int imageCount() {
                return 4;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 4) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 1, 3 };

                if (true == isImageListPortrait(images, aTypes, aNum, 4)) {
                    return 4;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[4];
                int totalHeight = in[1];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { -1 / adjustImages.get(0).ratio, -1 / adjustImages.get(1).ratio,
                                -1 / adjustImages.get(2).ratio, 1 / adjustImages.get(3).ratio,
                                4 * pad }, { 1, 0, -1, 0, 0 }, { 1, -1, 0, 0, 0 },
                        { 1, 0, 0, 1, totalHeight - 4 * pad }, };

                float heights[] = new float[4];

                calcMatrix(matrix, heights, 4);

                for (int i = 0; i < 4; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.h = (int) heights[i];
                    image.w = (int) (image.h / image.ratio);
                }

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = 0;

                adjustImages.get(1).x = adjustImages.get(0).x + adjustImages.get(0).w
                        + 2 * pad;
                adjustImages.get(1).y = adjustImages.get(0).y;

                adjustImages.get(2).x = adjustImages.get(1).x + adjustImages.get(1).w
                        + 2 * pad;
                adjustImages.get(2).y = adjustImages.get(0).y;

                adjustImages.get(3).x = 0;
                adjustImages.get(3).y = adjustImages.get(0).h + 2 * pad;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = adjustImages.get(3).w + 2 * pad;
                out[1] = totalHeight;
            };
        };

        patterns[28] = new ImageRichLinePattern() {
            public int imageCount() {
                return 4;
            }

            public int match(ArrayList<ItemBaseInfo> images) {

                if (images.size() < 4) {
                    return -1;
                }

                int aTypes[] = { ItemBaseInfo.IMAGE_LANDSCAPE | ItemBaseInfo.IMAGE_SQUARE,
                        ItemBaseInfo.IMAGE_PORTRAIT | ItemBaseInfo.IMAGE_SLIM };
                int aNum[] = { 1, 3 };

                if (true == isImageListPortrait(images, aTypes, aNum, 4)) {
                    return 4;
                } else {
                    return -1;
                }
            }

            public void layout(ArrayList<ItemBaseInfo> images, int in[], int pad, int out[]) {

                ArrayList<ItemBaseInfo> adjustImages = new ArrayList<ItemBaseInfo>();
                int baseIndex[] = new int[4];
                int totalHeight = in[1];

                adjustImageList(images, adjustImages, baseIndex);

                float matrix[][] = {
                        { -1 / adjustImages.get(0).ratio, -1 / adjustImages.get(1).ratio,
                                -1 / adjustImages.get(2).ratio, 1 / adjustImages.get(3).ratio,
                                4 * pad }, { 1, 0, -1, 0, 0 }, { 1, -1, 0, 0, 0 },
                        { 1, 0, 0, 1, totalHeight - 4 * pad }, };

                float heights[] = new float[4];

                calcMatrix(matrix, heights, 4);

                for (int i = 0; i < 4; i++) {
                    ItemBaseInfo image = adjustImages.get(i);
                    image.h = (int) heights[i];
                    image.w = (int) (image.h / image.ratio);
                }

                adjustImages.get(3).x = 0;
                adjustImages.get(3).y = 0;

                adjustImages.get(0).x = 0;
                adjustImages.get(0).y = adjustImages.get(3).h + 2 * pad;

                adjustImages.get(1).x = adjustImages.get(0).x + adjustImages.get(0).w
                        + 2 * pad;
                adjustImages.get(1).y = adjustImages.get(0).y;

                adjustImages.get(2).x = adjustImages.get(1).x + adjustImages.get(1).w
                        + 2 * pad;
                adjustImages.get(2).y = adjustImages.get(0).y;

                restoreImageList(images, adjustImages, baseIndex);

                out[0] = adjustImages.get(3).w + 2 * pad;
                out[1] = totalHeight;
            };
        };
    }
}
