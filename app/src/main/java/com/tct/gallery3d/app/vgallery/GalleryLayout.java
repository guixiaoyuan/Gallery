package com.tct.gallery3d.app.vgallery;

import java.util.ArrayList;
import java.util.Random;

import com.tct.gallery3d.data.MomentsAlbum.ItemBaseInfo;

import android.content.res.Configuration;

public class GalleryLayout {
    private static final int MAX_BUFFER_LENGTH = 6;
    private int mLastPattern = -2; /*-1 indicates using the single line, -2 indicates starting*/

    private ImageProcessBuffer mImageProcessBuffer = null;
    private ImageRichLinePatternCollection mPatterns = null;
    private Random mRandom = null;

    private int totalWidth = 0;
    private int totalHeight = 0;
    private int orientationType = Configuration.ORIENTATION_PORTRAIT;
    private int thumbnailPad = 0;
    private int scope[] = new int[2];

    private int continueNum = 0;
    private float preRatio = 0;
    
    private ArrayList<ImagePatternGroup> groups; 

    public class ImagePatternLine {
        public int lineTop;
        public int lineBottom;
        public ArrayList<ItemBaseInfo> images = new ArrayList<ItemBaseInfo>();

        public ImagePatternLine(ArrayList<ItemBaseInfo> array) {
            images.clear();
            images.addAll(array);

            lineTop = java.lang.Integer.MAX_VALUE;
            lineBottom = 0;
            for(ItemBaseInfo item : images) {
                if(item.y < lineTop) {
                    lineTop = item.y;
                }
                if((item.y + item.h) > lineBottom) {
                    lineBottom = item.y + item.h;
                }
            }
        }
    }

    public class ImagePatternGroup {
        public int groupTop;
        public int groupBottom;
        public ArrayList<ImagePatternLine> lines = new ArrayList<ImagePatternLine>();

        public ImagePatternGroup() {
            groupTop = 0;
            groupBottom = 0;
            lines.clear();
        }

        public void addLine(ArrayList<ItemBaseInfo> array) {
            ImagePatternLine line = new ImagePatternLine(array);
            addLine(line);
        }

        public void addLine(ImagePatternLine line) {
            if(lines.size() > 0) {
                groupBottom += 2 * thumbnailPad;
            }
            ArrayList<ItemBaseInfo> images = line.images;
            for(ItemBaseInfo item : images) {
                item.y += groupBottom;
            }

            line.lineTop += groupBottom;
            line.lineBottom += groupBottom;
            groupBottom += line.lineBottom - line.lineTop;
            lines.add(line);
        }
    }

    public GalleryLayout(int containerWidth, int containerHeight, int pad, int orientationType) {
        this.totalWidth = containerWidth;
        this.totalHeight = containerHeight;

        this.mImageProcessBuffer = new ImageProcessBuffer(MAX_BUFFER_LENGTH);
        this.mPatterns = new ImageRichLinePatternCollection();
        this.mRandom = new Random();

        this.thumbnailPad = pad;
        this.orientationType = orientationType;
        this.groups = new ArrayList<ImagePatternGroup>();
    }

    public void reset() {
        groups.clear();
    }

    public void setRichPatternScope(int start, int end) {
        scope[0] = start;
        scope[1] = end;
    }

    public void setLayoutWidth(int containerWidth) {
        this.totalWidth = containerWidth;
    }

    public void setLayoutHeight(int containerHeight) {
        this.totalHeight = containerHeight;
    }

    private void processImageBuffer() {
        /* Try to find some pattern */
        int availPattern = mPatterns.checkPattern(mImageProcessBuffer.getBuffer(), scope);

        if (availPattern > 0) {

            int choice;

//            if (mLastPattern == -1) {
//                choice = mRandom.nextInt(availPattern);
//            } else {
//                choice = mRandom.nextInt(availPattern + 1); /*
//                                                            * Introduce the
//                                                            * random to cover
//                                                            * fall-back single
//                                                            * line mode
//                                                            */
//            }
            if (mLastPattern == 0) {
                choice = availPattern;
            } else {
                choice = availPattern - 1;
            }

            if (choice < availPattern && mLastPattern == mPatterns.getPatternId(choice)) {
                /* Pattern collapse with last one, try to find another */
                if (availPattern == 1) {
                    choice = availPattern; /* Using single line */
                } else {
                    choice = (choice + 1) % (availPattern + 1);
                }
            }

            if (choice < availPattern) {
                int consumeCount = mPatterns.pickNumForPattern(choice);

                ArrayList<ItemBaseInfo> images = mImageProcessBuffer.shed(consumeCount);

                int in[] = { totalWidth, totalHeight };
                int out[] = new int[2];

                mPatterns.applyPattern(images, choice, in, thumbnailPad, out);
                mLastPattern = mPatterns.getPatternId(choice);
                mPatterns.changePatternMatchSum(mLastPattern, 1);

                ImagePatternGroup group = groups.get(groups.size() - 1);
                group.addLine(images);

            } else {
                ImageSingleLineGroup line = new ImageSingleLineGroup(totalWidth, totalHeight,
                        thumbnailPad, orientationType);
                while (true == line.needMoreImage() && false == mImageProcessBuffer.isEmpty()
                        && line.optimizePattern(mImageProcessBuffer.get(0))) {
                    line.addImage(mImageProcessBuffer.remove(0));
                }
                line.stretchLayout();
                mLastPattern = -1;
                
                ImagePatternGroup group = groups.get(groups.size() - 1);
                group.addLine(line.getImages());
            }
        } else {
            ImageSingleLineGroup line = new ImageSingleLineGroup(totalWidth, totalHeight,
                    thumbnailPad, orientationType);
            while (true == line.needMoreImage() && false == mImageProcessBuffer.isEmpty()
                    && line.optimizePattern(mImageProcessBuffer.get(0))) {
                line.addImage(mImageProcessBuffer.remove(0));
            }
            line.stretchLayout();
            mLastPattern = -1;
            
            ImagePatternGroup group = groups.get(groups.size() - 1);
            group.addLine(line.getImages());
        }
    }

    public void addNewGroup() {
        ImagePatternGroup group = new ImagePatternGroup();
        groups.add(group);
    }

    public void addNewGroupFinish() {
        addImageFinish();
    }

    public void addImage(ItemBaseInfo imageItem) {

        mImageProcessBuffer.add(imageItem);

        if (mImageProcessBuffer.isFull()) {
            processImageBuffer();
        }
    }

    public void addImageFinish() {
        while (!mImageProcessBuffer.isEmpty()) {
            processImageBuffer();
        }
    }
}
