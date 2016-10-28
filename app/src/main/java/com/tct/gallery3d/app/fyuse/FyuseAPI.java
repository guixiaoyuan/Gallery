//
// com.fyusion.fyuse
// 
// Created by Vlad Glavtchev on 9/1/15.
// Copyright (c) 2015 Fyusion. All rights reserved.
//
/* ----------|----------------------|----------------------|----------------- */
/* 21/12/2015|chengbin.du-nb        |ALM-1121296           |[Gallery]Fyuse pictures can't display in gallery after capture a Fyuse picture
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.app.fyuse;

public class FyuseAPI
{
    public static final String FYUSE_SDK_APPNAME = "com.fyusion.sdk";
    public static final String FYUSE_PACKAGE_NAME = "com.fyusion.sdk";
    public static final String FYUSE_SDK = "FyuseSDK";

    public static final int VERSION = 2;
    public static final String COMMAND = "command";
    public static final String VERSION_KEY = "version";
    public interface Action {
        String LIST_DIRECTORY = "listDirectory";
        String DELETE_FILE = "deleteFile";
        String RESUME_CAMERA = "com.fyusion.fyuse.Camera.CameraActivity";
        String OPEN_VIEWER = "com.fyusion.fyuse.FullScreenActivity";
    }
}
