package com.tct.gallery3d.util;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;

import com.tct.gallery3d.data.MediaItem;

/**
 * Created by liuxiaoyu on 16-9-19.
 * Use for Drag one View to Another application
 */
public class DragUtil {

    private final static String TAG = "DragUtil";

    /**
     * start drag
     *
     * @param item
     * @param view
     */
    public static void startDrag(MediaItem item, View view) {
        Intent intent = createIntent(item, view);
        createClipDataAndDrag(intent, view, item);
    }

    private static Intent createIntent(MediaItem item, View view) {
        String mimeType = item.getMimeType();
        Uri imageUri = item.getContentUri();
        Log.d(TAG, "imageUri= " + imageUri + " mimeType= " + mimeType);
        Intent intent = new Intent();
        intent.setDataAndType(imageUri, mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        view.setTag(imageUri);
        return intent;
    }

    private static void createClipDataAndDrag(Intent intent, View view, MediaItem mediaItem) {
        // create ClipData Object
        ClipData.Item item = new ClipData.Item(mediaItem.getContentUri());
        ClipData data = new ClipData(view.getTag().toString(),
                new String[]{ClipDescription.MIMETYPE_TEXT_URILIST}, item);
        View.DragShadowBuilder shadow = new View.DragShadowBuilder(view);
        // start drag
        if (Build.VERSION.SDK_INT >= 24) {
            view.startDragAndDrop(data, shadow, null, View.DRAG_FLAG_GLOBAL|View.DRAG_FLAG_GLOBAL_URI_WRITE
            |View.DRAG_FLAG_GLOBAL_URI_READ);
        }
    }
}
