/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tct.gallery3d.app;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import com.tcl.statisticsdk.agent.StatisticsAgent;
import com.tct.gallery3d.R;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.DownloadCache;
import com.tct.gallery3d.db.DataBaseManager;
import com.tct.gallery3d.gadget.WidgetUtils;
import com.tct.gallery3d.image.ImageResizer;
import com.tct.gallery3d.image.ImageWorker;
import com.tct.gallery3d.picasasource.PicasaSource;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.ThreadPool;
import com.tct.gallery3d.util.UsageStatistics;

import java.io.File;

import tct.util.privacymode.TctPrivacyModeHelper;

public class GalleryAppImpl extends Application implements GalleryApp {

    private static final String DOWNLOAD_FOLDER = "download";
    private static final long DOWNLOAD_CAPACITY = 64 * 1024 * 1024; // 64M
    public static boolean sHasNewColumn = false;
    public static boolean sHasPrivateColumn = false;

    private DataManager mDataManager;
    private ThreadPool mThreadPool;
    private DownloadCache mDownloadCache;
    private DataBaseManager mDataBaseManager;
    private ImageWorker mImageWorker;
    private static TctPrivacyModeHelper sTctPrivacyModeHelper;
    private static GalleryAppImpl sInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        if (getResources().getBoolean(R.bool.feature_Gallery2_MIG_SDK_on)) {
            StatisticsAgent.init(getApplicationContext());
            StatisticsAgent.setDebugMode(true);
            StatisticsAgent.setSessionTimeOut(this, GalleryConstant.DEFAULT_SESSION_TIME_OUT);
        }
        initializeAsyncTask();
        GalleryUtils.initialize(this);
        WidgetUtils.initialize(this);
        PicasaSource.initialize(this);
        UsageStatistics.initialize(this);
        ExifInfoFilter.getInstance(this);
        sInstance = this;
    }

    @Override
    public Context getAndroidContext() {
        return this;
    }

    public static GalleryAppImpl getInstance() {
        if (sInstance == null) {
            sInstance = new GalleryAppImpl();
        }
        return sInstance;
    }

    @Override
    public synchronized DataManager getDataManager() {
        if (mDataManager == null) {
            mDataManager = new DataManager(this);
            mDataManager.initializeSourceMap();
        }
        return mDataManager;
    }

    @Override
    public synchronized ThreadPool getThreadPool() {
        if (mThreadPool == null) {
            mThreadPool = new ThreadPool();
        }
        return mThreadPool;
    }

    @Override
    public ImageWorker getImageWorker() {
        if(mImageWorker == null){
            mImageWorker = new ImageResizer(this);
        }
        return mImageWorker;
    }

    @Override
    public synchronized DownloadCache getDownloadCache() {
        if (mDownloadCache == null) {
            File cacheDir = new File(getExternalCacheDir(), DOWNLOAD_FOLDER);

            if (!cacheDir.isDirectory()) cacheDir.mkdirs();

            if (!cacheDir.isDirectory()) {
                throw new RuntimeException(
                        "fail to create: " + cacheDir.getAbsolutePath());
            }
            mDownloadCache = new DownloadCache(this, cacheDir, DOWNLOAD_CAPACITY);
        }
        return mDownloadCache;
    }

    private void initializeAsyncTask() {
        // AsyncTask class needs to be loaded in UI thread.
        // So we load it here to comply the rule.
        try {
            Class.forName(AsyncTask.class.getName());
        } catch (ClassNotFoundException e) {
        }
    }

    @Override
    public synchronized DataBaseManager getDataBaseManager() {
        if(mDataBaseManager == null){
            mDataBaseManager = new DataBaseManager(this.getAndroidContext());
        }
        return mDataBaseManager;
    }

    public static TctPrivacyModeHelper getTctPrivacyModeHelperInstance (Context context) {
        if (sTctPrivacyModeHelper != null) {
            return sTctPrivacyModeHelper;
        }
        sTctPrivacyModeHelper = TctPrivacyModeHelper.createHelper(context);
        return sTctPrivacyModeHelper;
    }
}
