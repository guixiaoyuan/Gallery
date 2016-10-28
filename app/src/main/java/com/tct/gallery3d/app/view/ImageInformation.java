package com.tct.gallery3d.app.view;


public class ImageInformation {

    private int width;
    private int height;

    public ImageInformation(int width, int height) {
        setWidth(width);
        setHeight(height);
    }


    public int getWidth() {
        return this.width;
    }


    public void setWidth(int width) {
        this.width = width;
    }


    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
