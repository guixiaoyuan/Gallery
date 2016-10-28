package com.tct.gallery3d.app.vgallery;

import java.util.ArrayList;

import com.tct.gallery3d.data.MomentsAlbum.ItemBaseInfo;


import android.R.integer;
import android.content.res.Configuration;
import android.util.Log;

public class ImageSingleLineGroup {

    private static final int MAX_HEIGHT = 540;
    private static final int MIN_HEIGHT = 180;
    private static final int MAX_WIDTH = 540;
    private static final int SINGLE_MIN_HEIGHT = 100;

    private int totalWidth = 1080;
    private int totalHeight = 1080;
    private int thumbnail_pad = 10;
    private int orientationType = 1;
    private int height;
    private int width;

    private ArrayList<ItemBaseInfo> imageList = new ArrayList<ItemBaseInfo>();

    public ImageSingleLineGroup(int containerWidth, int containerHeight, int pad, int type) {
        totalWidth = containerWidth;
        thumbnail_pad = pad;

        totalHeight = containerHeight;
        orientationType = type;
    }

    private void layout() {

        if (orientationType == Configuration.ORIENTATION_LANDSCAPE) {
            verticalLayout();
        } else {
            horizontalLayout();
        }
    }

    public void addImage(ItemBaseInfo image) {
        imageList.add(image);
    }

    public ArrayList<ItemBaseInfo> getImages() {
        ArrayList<ItemBaseInfo> list = new ArrayList<ItemBaseInfo>();
        list.addAll(imageList);
        return list;
    }

    private void horizontalLayout() {
        int maxContentWidth = totalWidth;
        int contentWidth = 0;

        /* First Round, to resize all picture as high as MAX_HEIGHT */
        for (int i = 0; i < imageList.size(); i++) {
            ItemBaseInfo image = imageList.get(i);
            image.h = MAX_HEIGHT - 2 * thumbnail_pad;
            //[BUGFIX]-Add-BEGIN by chunhua.liu 12/17/2015 for defect 1175398
            if(image.bmpHeight != 0){
                image.w = (image.bmpWidth * image.h) / image.bmpHeight;

                contentWidth += image.w + 2 * thumbnail_pad;
            }
            //[BUGFIX]-Add-END by chunhua.liu 12/17/2015 for defect 1175398
        }

        if (contentWidth > maxContentWidth) {
            for (int i = 0; i < imageList.size(); i++) {
                ItemBaseInfo image = imageList.get(i);

                image.h = ((image.h + 2 * thumbnail_pad) * maxContentWidth)
                        / contentWidth - 2 * thumbnail_pad;
                image.w = ((image.w + 2 * thumbnail_pad) * maxContentWidth)
                        / contentWidth - 2 * thumbnail_pad;
                //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/07,PR1176136 begin
                if(image.h <= 0){
                    image.h = SINGLE_MIN_HEIGHT;
                }
                //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/07,PR1176136 end
            }
        }

        contentWidth = 0;

        for (int i = 0; i < imageList.size(); i++) {
            ItemBaseInfo image = imageList.get(i);

            image.x = contentWidth;
            image.y = 0;

            contentWidth += image.w + 2 * thumbnail_pad;
            height = image.h + 2 * thumbnail_pad;
        }

        width = contentWidth;
    }

    private void verticalLayout() {
        int maxContentHeight = totalHeight;
        int contentHeight = 0;

        /* First Round, to resize all picture as width as MAX_WIDTH */
        for (int i = 0; i < imageList.size(); i++) {
            ItemBaseInfo image = imageList.get(i);
            image.w = MAX_WIDTH - 2 * thumbnail_pad;
            image.h = (image.bmpHeight * image.w) / image.bmpWidth;

            contentHeight += image.h + 2 * thumbnail_pad;
        }

        if (contentHeight > maxContentHeight) {
            for (int i = 0; i < imageList.size(); i++) {
                ItemBaseInfo image = imageList.get(i);

                image.h = ((image.h + 2 * thumbnail_pad) * maxContentHeight)
                        / contentHeight - 2 * thumbnail_pad;
                image.w = ((image.w + 2 * thumbnail_pad) * maxContentHeight)
                        / contentHeight - 2 * thumbnail_pad;
            }
        }

        contentHeight = 0;

        for (int i = 0; i < imageList.size(); i++) {
            ItemBaseInfo image = imageList.get(i);

            image.x = 0;
            image.y = contentHeight;

            contentHeight += image.h + 2 * thumbnail_pad;
            width = image.w + 2 * thumbnail_pad;
        }

        height = contentHeight;
    }

    public boolean properForMoreImage() {
        if (imageList.size() >= 4 || height <= MIN_HEIGHT) {
            return false;
        }
        return true;
    }

    public boolean needMoreImage() {
        if (imageList.isEmpty()) {
            return true;
        } else if (imageList.size() >= 4) {
            layout();
            return false; // Consider as "full" if 4 pictures in one line...
        }

        layout();

        if (orientationType == Configuration.ORIENTATION_LANDSCAPE) {
            if (height < (totalHeight - 30)) {
                return true;
            } else {
                return false;
            }

        } else {
            if (width < (totalWidth - 30)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void stretchLayout() {
        if (orientationType == Configuration.ORIENTATION_LANDSCAPE) {
            if (height > (totalHeight - 150) && height < totalHeight) {
                for (int i = 0; i < imageList.size(); i++) {
                    ItemBaseInfo image = imageList.get(i);

                    image.h = ((image.h + 2 * thumbnail_pad) * totalHeight)
                            / height - 2 * thumbnail_pad;
                    image.w = ((image.w + 2 * thumbnail_pad) * totalHeight) / height
                            - 2 * thumbnail_pad;
                }

                int contentHeight = 0;

                for (int i = 0; i < imageList.size(); i++) {
                    ItemBaseInfo image = imageList.get(i);

                    image.x = 0;
                    image.y = contentHeight;

                    contentHeight += image.h + 2 * thumbnail_pad;
                    width = image.w + 2 * thumbnail_pad;
                }

                height = contentHeight;
            }
        } else {
            if (width > (totalWidth - 150) && width < totalWidth) {
                for (int i = 0; i < imageList.size(); i++) {
                    ItemBaseInfo image = imageList.get(i);

                    image.h = ((image.h + 2 * thumbnail_pad) * totalWidth) / width
                            - 2 * thumbnail_pad;
                    image.w = ((image.w + 2 * thumbnail_pad) * totalWidth) / width
                            - 2 * thumbnail_pad;
                }

                int contentWidth = 0;

                for (int i = 0; i < imageList.size(); i++) {
                    ItemBaseInfo image = imageList.get(i);

                    image.x = contentWidth;
                    image.y = 0;

                    contentWidth += image.w + 2 * thumbnail_pad;
                    height = image.h + 2 * thumbnail_pad;
                }

                width = contentWidth;
            }
        }
    }

    // Maybe need to add more condition
    public boolean optimizePattern(ItemBaseInfo nextImage) {
        boolean ret = true;
        if (imageList.size() == 3) {
            if (imageList.get(0).layoutType == ItemBaseInfo.IMAGE_PORTRAIT
                    && imageList.get(0).layoutType == ItemBaseInfo.IMAGE_PORTRAIT
                    && imageList.get(0).layoutType == ItemBaseInfo.IMAGE_PORTRAIT
                    && nextImage.layoutType == ItemBaseInfo.IMAGE_LANDSCAPE) {
                ret = false;
            }
        }
        return ret;
    }
}
