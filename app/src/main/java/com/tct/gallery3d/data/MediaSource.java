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

import com.tct.gallery3d.data.MediaSet.ItemConsumer;

import java.util.ArrayList;

public abstract class MediaSource {
    private static final String TAG = "MediaSource";

    public static final String LOCAL_SET_PATH = "/local/all";
    public static final String LOCAL_SELECT_PATH = "/local/select";
    public static final String LOCAL_ALBUM_PATH = "/local/all/*";
    public static final String LOCAL_IMAGE_SET_PATH = "/local/image";
    public static final String LOCAL_IMAGE_ALBUM_PATH = "/local/image/*";
    public static final String LOCAL_IMAGE_ITEM_PATH = "/local/image/item/*";

    public static final String LOCAL_VIDEO_SET_PATH = "/local/video";
    public static final String LOCAL_VIDEO_ALBUM_PATH = "/local/video/*";
    public static final String LOCAL_VIDEO_ITEM_PATH = "/local/video/item/*";

    public static final String LOCAL_CAMERA_SET_PATH = "/local/camera";
    public static final String LOCAL_ALL_VIDEOS_SET_PATH = "/local/all/video";
    public static final String LOCAL_MOMENTS_SET_PATH = "/local/moments";
    public static final String LOCAL_FACESHOW_SET_PATH = "/local/faceshow";
    public static final String LOCAL_FAVORITE_SET_PATH = "/local/favorite";
    public static final String LOCAL_SLOWMOTION_SET_PATH = "/local/slowmotion";
    public static final String LOCAL_PRIVATE_SET_PATH = "/local/private";


    public static final String LOCAL_COLLAPSED_PATH = "/local/collapsed";
    private String mPrefix;

    protected MediaSource(String prefix) {
        mPrefix = prefix;
    }

    public String getPrefix() {
        return mPrefix;
    }

    public Path findPathByUri(Uri uri, String type) {
        return null;
    }

    public abstract MediaObject createMediaObject(Path path);

    public void pause() {
    }

    public void resume() {
    }

    public Path getDefaultSetOf(Path item) {
        return null;
    }

    public long getTotalUsedCacheSize() {
        return 0;
    }

    public long getTotalTargetCacheSize() {
        return 0;
    }

    public static class PathId {
        public PathId(Path path, int id) {
            this.path = path;
            this.id = id;
        }
        public Path path;
        public int id;
    }

    // Maps a list of Paths (all belong to this MediaSource) to MediaItems,
    // and invoke consumer.consume() for each MediaItem with the given id.
    //
    // This default implementation uses getMediaObject for each Path. Subclasses
    // may override this and provide more efficient implementation (like
    // batching the database query).
    public void mapMediaItems(ArrayList<PathId> list, ItemConsumer consumer) {
        int n = list.size();
        for (int i = 0; i < n; i++) {
            PathId pid = list.get(i);
            MediaObject obj;
            synchronized (DataManager.LOCK) {
                obj = pid.path.getObject();
                if (obj == null) {
                    try {
                        obj = createMediaObject(pid.path);
                    } catch (Throwable th) {
                        Log.w(TAG, "cannot create media object: " + pid.path, th);
                    }
                }
            }
            if (obj != null) {
                consumer.consume(pid.id, (MediaItem) obj);
            }
        }
    }
}
