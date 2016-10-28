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
/* ----------|----------------------|----------------------|----------------- */
/* 13/01/2015|ye.chen               |PR901234              |[Clone][4.7][Downloads]The FL gif file has no dynamic when open in Gallery and FileManager
/* ----------|----------------------|----------------------|----------------- */
package com.tct.gallery3d.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import android.content.ContentResolver;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.drm.DrmManager;

class UriSource extends MediaSource {
    @SuppressWarnings("unused")
    private static final String TAG = "UriSource";
    private static final String IMAGE_TYPE_PREFIX = "image/";
    private static final String IMAGE_TYPE_ANY = "image/*";
    private static final String CHARSET_UTF_8 = "utf-8";

    private GalleryApp mApplication;

    public UriSource(GalleryApp context) {
        super("uri");
        mApplication = context;
    }

    @Override
    public MediaObject createMediaObject(Path path) {
        String segment[] = path.split();
        if (segment.length != 3) {
            throw new RuntimeException("bad path: " + path);
        }
        try {
            String uri = URLDecoder.decode(segment[1], CHARSET_UTF_8);
            String type = URLDecoder.decode(segment[2], CHARSET_UTF_8);
         // [FEATURE]-Add-BEGIN by NJ.yang.zhang3 For PR854045-2014.12.15
            return new UriImage(mApplication, path, Uri.parse(uri),getImageTypeForHeadBytes(type,Uri.parse(uri)));
         // [FEATURE]-Add-END by NJ.yang.zhang3 For PR854045-2014.12.15
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    //[FEATURE]-Add-BEGIN by NJ.yang.zhang3 For PR854045-2014.12.15
    //When parameter of type is "gif", according to the file header check real file format.
    private String getImageTypeForHeadBytes(String type, Uri uri) {
        //[BUGFIX]-Add-BEGIN by ye.chen 13/01/2015,PR901234 [Clone][4.7][Downloads]The FL gif file has no dynamic when open in Gallery and FileManager
        //Don't chekc this file when this it DRM file.
        if(DrmManager.getInstance().isDrm(uri.getPath())){
            Log.d(TAG, "this is DRM filr");
            return type;
        }
      //[BUGFIX]-Add-END by ye.chen 13/01/2015,PR901234 [Clone][4.7][Downloads]The FL gif file has no dynamic when open in Gallery and FileManager
        if (type.contains("gif")) {
            try {
                FileInputStream fis = new FileInputStream(uri.getPath());
                byte[] b = new byte[3];
                fis.read(b, 0, b.length);
                StringBuilder stringBuilder = new StringBuilder();
                if (b == null || b.length <= 0) {
                    return "image/*";
                }
                for (int i = 0; i < b.length; i++) {
                    int v = b[i] & 0xFF;
                    String hv = Integer.toHexString(v);
                    if (hv.length() < 2) {
                        stringBuilder.append(0);
                    }
                        stringBuilder.append(hv);
                }
                String head = stringBuilder.toString();
                switch (head) {
                case "ffd8ff":
                    type = "image/jpg";
                    break;
                case "89504e47":
                    type = "image/png";
                    break;
                case "474946":
                    type = "image/gif";
                    break;
                case "424d":
                    type = "image/bmp";
                    break;
                default:
                    type = "image/*";
                    break;
                }
                fis.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Log.d(TAG, "imagetype:" + type);
        return type;
    }
// [FEATURE]-Add-END by NJ.yang.zhang3 For PR854045-2014.12.15

    private String getMimeType(Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            String extension =
                    MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            String type = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(extension.toLowerCase());
            if (type != null) return type;
        }
        // Assume the type is image if the type cannot be resolved
        // This could happen for "http" URI.
        String type = mApplication.getContentResolver().getType(uri);
        if (type == null) type = "image/*";
        return type;
    }

    @Override
    public Path findPathByUri(Uri uri, String type) {
        String mimeType = getMimeType(uri);

        // Try to find a most specific type but it has to be started with "image/"
        if ((type == null) || (IMAGE_TYPE_ANY.equals(type)
                && mimeType.startsWith(IMAGE_TYPE_PREFIX))) {
            type = mimeType;
        }

        if (type.startsWith(IMAGE_TYPE_PREFIX)) {
            try {
                return Path.fromString("/uri/"
                        + URLEncoder.encode(uri.toString(), CHARSET_UTF_8)
                        + "/" +URLEncoder.encode(type, CHARSET_UTF_8));
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }
        // We have no clues that it is an image
        return null;
    }
}
