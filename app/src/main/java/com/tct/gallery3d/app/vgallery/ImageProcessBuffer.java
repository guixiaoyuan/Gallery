package com.tct.gallery3d.app.vgallery;

import java.util.ArrayList;

import com.tct.gallery3d.data.MomentsAlbum.ItemBaseInfo;

public class ImageProcessBuffer {
    private ArrayList<ItemBaseInfo> mBuffer;
    private int mMaxLength = 0;

    public ImageProcessBuffer(int maxLength) {
        mBuffer = new ArrayList<ItemBaseInfo>();
        mMaxLength = maxLength;
    }

    public void add(ItemBaseInfo cell) {
        if(mBuffer.size() < mMaxLength) {
            mBuffer.add(cell);
        }
    }

    public ItemBaseInfo remove(int index) {
        return mBuffer.remove(index);
    }

    public ItemBaseInfo get(int index) {
        return mBuffer.get(index);
    }

    public boolean isFull() {
        return (mBuffer.size() == mMaxLength);
    }

    public boolean isEmpty() {
        return mBuffer.isEmpty();
    }

    public ArrayList<ItemBaseInfo> shed(int length) {
        if (length > mBuffer.size()) {
            length = mBuffer.size();
        }

        ArrayList<ItemBaseInfo> result = new ArrayList<ItemBaseInfo>();

        for (int i = 0; i < length; i++) {
            result.add(mBuffer.remove(0));
        }

        return result;
    }

    public ArrayList<ItemBaseInfo> getBuffer() {
        ArrayList<ItemBaseInfo> buffer = new ArrayList<ItemBaseInfo>(mBuffer.size());
        buffer.addAll(mBuffer);
        return buffer;
    }
}
