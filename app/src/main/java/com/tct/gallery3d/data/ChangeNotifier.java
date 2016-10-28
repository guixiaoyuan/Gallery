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

package com.tct.gallery3d.data;

import android.net.Uri;
import android.os.Handler;

import com.tct.gallery3d.app.GalleryApp;

import java.util.concurrent.atomic.AtomicBoolean;

// This handles change notification for media sets.
public class ChangeNotifier {

    private MediaSet mMediaSet;
    private AtomicBoolean mContentDirty = new AtomicBoolean(true);
    private int mCount = 0;
    private Handler mHandler;

    public ChangeNotifier(MediaSet set, Uri uri, GalleryApp application) {
        mMediaSet = set;
        mHandler = new Handler(application.getMainLooper());
        application.getDataManager().registerChangeNotifier(uri, this);
    }

    public ChangeNotifier(MediaSet set, Uri[] uris, GalleryApp application) {
        mMediaSet = set;
        mHandler = new Handler(application.getMainLooper());
        for (int i = 0; i < uris.length; i++) {
            application.getDataManager().registerChangeNotifier(uris[i], this);
        }
    }

    // Returns the dirty flag and clear it.
    public boolean isDirty() {
        return mContentDirty.compareAndSet(true, false);
    }

    public void fakeChange() {
        onChange(false);
    }

    protected void onChange(boolean selfChange) {
        mCount++;
        mHandler.removeCallbacksAndMessages(null);
        if (mCount > 100) {
            notifyData();
            mCount = 0;
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifyData();
                }
            }, 200);
        }
    }

    private void notifyData() {
        if (mContentDirty.compareAndSet(false, true)) {
            mMediaSet.notifyContentChanged();
        }
    }
}