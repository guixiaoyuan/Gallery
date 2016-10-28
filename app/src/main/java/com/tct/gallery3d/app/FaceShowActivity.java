/*----------------------------------------------------------------------------------------------------------------------------------------*/
/* 14/11/2015|    su.jiang     |  PR-881174    |[Android5.1][Gallery_v5.2.3.1.0311.0]It bact to albums interface after tap faceshow button*/
/*-----------|-----------------|---------------|------------------------------------------------------------------------------------------*/
/* 17/12/2015|    su.jiang     |  PR-1160996   |[Gallery]The number of face show picture is not right*/
/*-----------|-----------------|---------------|-----------------------------------------------------*/
/* 28/12/2015|    su.jiang     |  PR-1239452   |[Gallery][Face show]Only display last one saved face show video.*/
/*-----------|-----------------|---------------|----------------------------------------------------------------*/
/* 31/12/2015|dongliang.feng   |ALM-1190531    |[Gallery]Pop up gallery force closed when change moments to albums for several times */
/* ----------|-----------------|---------------|----------------- */
/* 31/12/2015|dongliang.feng   |ALM-1173782    |[Android 6.0][Gallery_v5.2.5.1.0321.0][Monitor][Force close]It appears FC when click selfies after open a faceshow */
/* ----------|-----------------|---------------|----------------- */
/* 06/01/2015|    su.jiang     |  PR-1274456   |[Camera]The number of photo displays error in gallery screen after Burst shoot*/
/*-----------|-----------------|---------------|------------------------------------------------------------------------------*/
/* 14/01/2015|    su.jiang     |  PR-1238478   |[Android 6.0][Gallery]The faceshow picture will change to big.*/
/*-----------|-----------------|---------------|--------------------------------------------------------------*/
/* 18/01/2015|    su.jiang     |  PR-1401362   |[GAPP][Android 6.0][Gallery]Gallery will fast exit after delete all selfies.*/
/*-----------|-----------------|---------------|----------------------------------------------------------------------------*/

package com.tct.gallery3d.app;

import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.adapter.FaceShowAdapter;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.fragment.PhotoFragment;
import com.tct.gallery3d.app.model.FaceShowItem;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.DecodeUtils;
import com.tct.gallery3d.data.LocalMediaItem;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaSet;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.exif.ExifInterface;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.ui.BitmapLoader;
import com.tct.gallery3d.ui.Log;
import com.tct.gallery3d.util.Future;
import com.tct.gallery3d.util.FutureListener;
import com.tct.gallery3d.util.PLFUtils;
import com.tct.gallery3d.util.ThreadPool;
import com.tct.gallery3d.util.ThreadPool.Job;
import com.tct.gallery3d.util.ThreadPool.JobContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

public class FaceShowActivity extends AbstractGalleryActivity implements OnClickListener, OnItemClickListener {

    private static final String TAG = "FaceShowActivity";

    private static final int MSG_UPDATE_PHOTO = 0x1;
    private static final int MSG_RELOAD_PHOTO = 0x2;
    private static final int MSG_UPDATE_BITMAP = 0x3;

    private static final int THUMBNAIL_DIALOG = 0x4;
    private static final int THUMBNAIL_VIDEO = 0x5;

    public static final String FACESHOW_ACTION = "faceshow_action";
    private static final String KEY_FIRST_LAUNCH_FACESHOW = "first_launch_faceshow";
    private static final String SHARED_PREFERENCES_NAME = "Gallery";
    private static final String DEFAULT_PATH = "/local/image/item";
    private static final String MUVEE_FACESHOW_NAME = "com.muvee.faceshow";
    private static final String MUVEE_FACESHOW_BROADCAST = "com.muvee.faceshow.BROADCAST";
    private static final String MUVEE_FACESHOW_FRAME_PATH = "com.muvee.faceshow.FRAME_PATH";
    private static final String MUVEE_FACESHOW_FIRSTFRAMESERVICE = "com.muvee.faceshow.FirstFrameService";
    private static final String MUVEE_FACESHOW_IMAGE_LIST = "com.muvee.faceshow.IMAGE_LIST";
    private static final String MUVEE_FACESHOW_FACE_SHOW = "com.muvee.faceshow.FACE_SHOW";
    private static final String MUVEE_FACESHOW_THEME = "com.muvee.faceshow.theme";
    private static final String MUVEE_FACESHOW_SELFIEFOLDER = "com.muvee.faceshow.selfiefolder";
    private static final String MUVEE_FACESHOW_IMAGELIST = "com.muvee.faceshow.imagelist";
    private static final String MUVEE_FACESHOW_OUTPUT_RESOLUTION = "com.muvee.faceshow.output.resolution";
    private static final String MUVEE_FACESHOW_OUTPUT_BITRATE = "com.muvee.faceshow.output.bitrate";
    private static final String MUVEE_SHARE_USECUSTOM = "com.muvee.share.usecustom";

    private static final String FACESHOW_SELFIEFOLDER = "/storage/emulated/0/DCIM/Selfie";
    private static final int MUVEE_FACESHOW_THEME_ID = 3;
    private static final String DEFAULT_FACESHOW_OUTPUT_RESOLUTION = "720x720";
    private static final int DEFAULT_FACESHOW_OUTPUT_BITRATE = 8000000;
    private static final String DEF_NAME = "def_gallery_custom_share_enable";



    private FaceShowItem[] mItems = null;
    private ThreadPool mThreadPool = null;
    private ActionBar mActionBar;
    private String[] mAllPaths = null;

    private GridView mGridView;
    private ImageView mFaceShow;
    private ImageView mPlayView;
    private FaceShowAdapter mFaceShowAdapter;
    private FaceShowDialog mDialog;
    private ThumbnailLoader mDialogThumbnailLoader = null;
    private ThumbnailLoader mThumbnailLoader = null;
    private Toolbar mToolbar;

    private MediaSet mMediaSet;
    private AbstractGalleryActivity mContext;
    private View mContent;

    private static final Uri mWatchUri = Images.Media.EXTERNAL_CONTENT_URI;
    private HashMap<Uri, NotifyBroker> mNotifierMap = new HashMap<Uri, NotifyBroker>();
    private UriChangeNotifiter mChangeNotifiter;

    private int itemWidth = 0;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPDATE_PHOTO:
                    mFaceShowAdapter.notifyDataSetChanged();
                    break;
                case MSG_RELOAD_PHOTO:
                    initData();
                    initDialogView();
                    initAdapter();
                    loadAllBitmaps();
                    break;
                case MSG_UPDATE_BITMAP:
                    ((ThumbnailLoader) msg.obj).updateEntry();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.faceshow_activity);

        mContext = (AbstractGalleryActivity) getAndroidContext();
        Bundle data = getIntent().getExtras();
        String path = data.getString(GalleryConstant.KEY_MEDIA_PATH);
        mMediaSet = mContext.getDataManager().getMediaSet(path);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setActionBar(mToolbar);

        Window win = this.getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mGridView = (GridView) this.findViewById(R.id.faceshow_gridview);
        mFaceShow = (ImageView) this.findViewById(R.id.faceshow_photo);
        mPlayView = (ImageView) this.findViewById(R.id.faceshow_play);
        mThreadPool = ((GalleryApp) getApplication()).getThreadPool();

        initData();
        startService();
        initDialogView();
        initActionBar();
        initSystemBar(false);
        initAdapter();
        initListener();
        loadAllBitmaps();
    }

    @Override
    protected void onStart() {
        IntentFilter intentFilter = new IntentFilter(MUVEE_FACESHOW_BROADCAST);
        registerReceiver(firstFrameReceiver, intentFilter);
        super.onStart();
    }

    @Override
    protected void onResume() {
        ArrayList<String> faceShow = ExifInfoFilter.getInstance(this).queryFaceshow(this, 0);
        super.onResume();
        if (mItems.length != faceShow.size()) {
            mHandler.obtainMessage(MSG_RELOAD_PHOTO).sendToTarget();
        }
    }

    @Override
    protected void onStop() {
        unregisterReceiver(firstFrameReceiver);
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDialog != null && mDialog.getDialog().isShowing() == true) {
            mDialog.pauseVideo();
        }
    }

    private BroadcastReceiver firstFrameReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String path = intent.getStringExtra(MUVEE_FACESHOW_FRAME_PATH);
            Log.e(TAG, "path = " + path);
            if (path == null)
                return;
            mThumbnailLoader = new ThumbnailLoader(path, THUMBNAIL_VIDEO);
            mThumbnailLoader.startLoad();
        }
    };

    private void loadAllBitmaps() {
        if (mItems != null) {
            int lenght = mItems.length;
            for (int i = 0; i < lenght; i++) {
                FaceShowItem item = mItems[i];
                item.loader = new FaceShowImageLoader(item.item, i);
                item.loader.startLoad();
            }
        }
    }

    private void initListener() {
        mChangeNotifiter = new UriChangeNotifiter(mWatchUri);
        mPlayView.setOnClickListener(this);
        mGridView.setOnItemClickListener(this);
    }

    private void registerChangeNotifier(Uri uri, UriChangeNotifiter notifier) {
        NotifyBroker broker = null;
        synchronized (mNotifierMap) {
            broker = mNotifierMap.get(uri);
            if (broker == null) {
                broker = new NotifyBroker(mHandler);
                this.getContentResolver().registerContentObserver(uri, true, broker);
                mNotifierMap.put(uri, broker);
            }
        }
        broker.registerNotifier(notifier);
    }

    private class UriChangeNotifiter {

        public UriChangeNotifiter(Uri uri) {
            registerChangeNotifier(uri, this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            mHandler.obtainMessage(MSG_RELOAD_PHOTO).sendToTarget();
        }
    }

    private static class NotifyBroker extends ContentObserver {
        private CopyOnWriteArraySet<UriChangeNotifiter> mChangeNotifiters = new CopyOnWriteArraySet<UriChangeNotifiter>();

        public NotifyBroker(Handler handler) {
            super(handler);
        }

        public synchronized void registerNotifier(UriChangeNotifiter notifier) {
            mChangeNotifiters.add(notifier);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Iterator<UriChangeNotifiter> iterator = mChangeNotifiters.iterator();
            while (iterator.hasNext()) {
                ((UriChangeNotifiter) iterator.next()).onChange(selfChange, uri);
            }
        }
    }

    private void initData() {
        ArrayList<String> faceshow = ExifInfoFilter.getInstance(this).queryFaceshow(this, 0);
        if (faceshow != null) {
            Path path = Path.fromString(DEFAULT_PATH);
            mItems = new FaceShowItem[faceshow.size()];
            mAllPaths = new String[faceshow.size()];
            DataManager data = DataManager.from(this);
            int i = 0;
            for (String id : faceshow) {
                Path childPath = path.getChild(id);
                LocalMediaItem item = (LocalMediaItem) data.getMediaObject(childPath);
                mItems[i] = new FaceShowItem(i, item);
                mAllPaths[i] = item.getFilePath();
                i++;
            }

        }
    }

    private void initDialogView() {
        SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        boolean firstLaunch = sp.getBoolean(KEY_FIRST_LAUNCH_FACESHOW, true);
        mDialog = new FaceShowDialog(this);
        if (firstLaunch) {
            Editor editor = sp.edit();
            editor.putBoolean(KEY_FIRST_LAUNCH_FACESHOW, false);
            editor.commit();
            mDialog.show();
        } else {
            Log.i(TAG, "do not need show faceshow demo");
        }
        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-07-28,BUG-2208330*/
        String path = null;
        if(mMediaSet.getMediaItemCount() >= 1){
            path = mMediaSet.getMediaItem(0, 1).get(0).getFilePath();
        }
        /* MODIFIED-END by Yaoyu.Yang,BUG-2208330*/

        mDialogThumbnailLoader = new ThumbnailLoader(path, THUMBNAIL_DIALOG);
        mDialogThumbnailLoader.startLoad();

    }

    private void initAdapter() {
        if (mItems != null) {
            mFaceShowAdapter = new FaceShowAdapter(this, mItems, getPortraitItemWidth());
            mGridView.setAdapter(mFaceShowAdapter);
        }
    }

    private int getPortraitItemWidth() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            DisplayMetrics mActualMetrics = new DisplayMetrics();
            this.getWindowManager().getDefaultDisplay().getMetrics(mActualMetrics);
            int mItemWidth = (int) (mActualMetrics.widthPixels * 0.25f);
            if (itemWidth == 0) {
                itemWidth = mItemWidth;
            }
            return mItemWidth;
        } else {
            return itemWidth;
        }
    }

    private void startService() {
        Intent service = new Intent();
        service.setClassName(MUVEE_FACESHOW_NAME, MUVEE_FACESHOW_FIRSTFRAMESERVICE);
        service.putExtra(MUVEE_FACESHOW_IMAGE_LIST, mAllPaths);
        startService(service);
    }

    private void initActionBar() {
        mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        String title = String.format(this.getString(R.string.selfies));
        mActionBar.setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return false;
    }

    public class FaceShowImageLoader extends BitmapLoader {

        private MediaItem item;
        private int mIndex;

        public FaceShowImageLoader(MediaItem item, int index) {
            this.item = item;
            this.mIndex = index;
        }

        @Override
        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
            return mThreadPool.submit(item.requestImage(MediaItem.TYPE_THUMBNAIL), this);
        }

        @Override
        protected void onLoadComplete(Bitmap bitmap) {
            if (mIndex >= mItems.length) {
                return;
            }

            FaceShowItem faceShowItem = mItems[mIndex];
            if (faceShowItem == null)
                return;
            faceShowItem.bitmap = bitmap;
            mHandler.obtainMessage(MSG_UPDATE_PHOTO, null).sendToTarget();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.faceshow_play:
                if (mItems.length < 5) {
                    mDialog.setTitle();
                } else {
                    launchFaceShow();
                }
                break;
            default:
                break;
        }
    }

    private void launchFaceShow() {
        Intent intent = new Intent();
        intent.setAction(MUVEE_FACESHOW_FACE_SHOW);
        intent.putExtra(MUVEE_FACESHOW_THEME, MUVEE_FACESHOW_THEME_ID);
        intent.putExtra(MUVEE_FACESHOW_SELFIEFOLDER, FACESHOW_SELFIEFOLDER);
        intent.putExtra(MUVEE_FACESHOW_IMAGELIST, mAllPaths);
        intent.putExtra(MUVEE_FACESHOW_OUTPUT_RESOLUTION, DEFAULT_FACESHOW_OUTPUT_RESOLUTION);
        intent.putExtra(MUVEE_FACESHOW_OUTPUT_BITRATE, DEFAULT_FACESHOW_OUTPUT_BITRATE);
        if (PLFUtils.getBoolean(this, DEF_NAME)) {
            intent.putExtra(MUVEE_SHARE_USECUSTOM, true);
        } else {
            intent.putExtra(MUVEE_SHARE_USECUSTOM, false);
        }
        // we should catch exception and remind the user if Muvee is not installed
        try{
            startActivity(intent);
        }catch (ActivityNotFoundException exception){
            Toast.makeText(this,R.string.no_muvee_app_toast,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (!AbstractGalleryFragment.checkClickable(mContext)) {
            return;
        }

        int slotIndex = position;
        int innerIndex = position;
        String albumSetPath = mMediaSet.getPath().toString();
        MediaItem mediaItem = mMediaSet.getMediaItem(position, 1).get(0);
        // Check the drm play right.
        if (!DrmManager.checkDrmPlayRight(mContext, mediaItem)) {
            return;
        }
        String path = null;
        if (mediaItem != null) {
            path = mediaItem.getPath().toString();
        }
        mContent = mGridView.getChildAt(position);

        Bundle data = new Bundle();
        data.putInt(GalleryConstant.KEY_FROM_PAGE, GalleryConstant.FROM_FACESHOW_PAGE);
        data.putInt(GalleryConstant.KEY_INDEX_SLOT, slotIndex);
        data.putInt(GalleryConstant.KEY_INDEX_HINT, innerIndex);
        data.putString(GalleryConstant.KEY_MEDIA_SET_PATH, albumSetPath);
        data.putString(GalleryConstant.KEY_MEDIA_ITEM_PATH, path);

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        data.putInt(PhotoFragment.LOCATION_X, location[0]);
        data.putInt(PhotoFragment.LOCATION_Y, location[1]);
        data.putInt(PhotoFragment.WIDTH, view.getWidth());
        data.putInt(PhotoFragment.HEIGHT, view.getHeight());
        mContext.startPhotoPage(data);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mItems != null) {
            for (int i = 0; i < mItems.length; i++) {
                mItems[i].loader.recycle();
            }
        }
        if (mDialogThumbnailLoader != null)
            mDialogThumbnailLoader.recycle();
        if (mThumbnailLoader != null)
            mThumbnailLoader.recycle();
        unregisterChangeNotifiter(mWatchUri);
    }

    private class ThumbnailLoader extends BitmapLoader {
        private final String mPath;
        private final int mType;

        public ThumbnailLoader(String path, int type) {
            mPath = path;
            mType = type;
        }

        @Override
        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
            return mThreadPool.submit(new ThumbnailRequest(mPath), this);
        }

        @Override
        protected void onLoadComplete(Bitmap bitmap) {
            mHandler.obtainMessage(MSG_UPDATE_BITMAP, this).sendToTarget();
        }

        public void updateEntry() {
            Bitmap bitmap = getBitmap();
            Log.e(TAG, "## bitmap = " + bitmap);
            if (bitmap == null)
                return;

            if (mItems.length < 5) {
                Uri mUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.faceshow);
                MediaMetadataRetriever media = new MediaMetadataRetriever();
                media.setDataSource(FaceShowActivity.this, mUri);
                Bitmap defectBitmap = media.getFrameAtTime();
                mFaceShow.setImageBitmap(defectBitmap);
            } else {
                Log.e(TAG, "## mFaceShow.setImageBitmap");
                mFaceShow.setImageBitmap(bitmap);
            }
        }
    }

    private class ThumbnailRequest implements Job<Bitmap> {
        private final String mPath;

        public ThumbnailRequest(String path) {
            mPath = path;
        }

        @Override
        public Bitmap run(JobContext jc) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            int type = MediaItem.TYPE_THUMBNAIL;
            int targetSize = MediaItem.getTargetSize(type);
            ExifInterface exif = new ExifInterface();
            byte[] thumbData = null;
            try {
                exif.readExif(mPath);
                thumbData = exif.getThumbnail();
            } catch (FileNotFoundException e) {
                Log.w(TAG, "failed to find file to read thumbnail: " + mPath);
            } catch (IOException e) {
                Log.w(TAG, "IOException, failed to get thumbnail from: " + mPath);
            } catch (Exception e) {
                Log.w(TAG, "failed to get thumbnail from: " + mPath);
            }
            if (thumbData != null) {
                Bitmap bitmap = DecodeUtils.decodeIfBigEnough(jc, thumbData, options, targetSize);
                if (bitmap != null)
                    return bitmap;
            }
            return DecodeUtils.decodeThumbnail(jc, mPath, options, targetSize, type);
        }
    }

    private void unregisterChangeNotifiter(Uri uri) {
        NotifyBroker broker = null;
        synchronized (mNotifierMap) {
            broker = mNotifierMap.get(uri);
            if (broker != null) {
                this.getContentResolver().unregisterContentObserver(broker);
                mNotifierMap.remove(broker);
            }
        }
    }

    public boolean initLocation(int slotIndex, int innerIndex) {
        int position = innerIndex;
        int first = mGridView.getFirstVisiblePosition();
        position -= first;
        return initPosition(position);
    }

    private boolean initPosition(int index) {
        mContent = mGridView.getChildAt(index);
        if (mContent == null) {
            int start = mGridView.getFirstVisiblePosition();
            int end = mGridView.getLastVisiblePosition();
            int position = -1;
            if (index < start) {
                position = start;
            }
            if (index > end) {
                position = end;
            }
            View child = mGridView.getChildAt(position);
            int offset = 0;
            if (child != null) {
                offset = child.getTop() - mGridView.getPaddingTop();
            }
            mGridView.smoothScrollToPositionFromTop(index, offset, 0);
            return false;
        }
        setOriginalInfo(mContent);
        return true;
    }

    public void setOriginalInfo(View content) {
        if (content == null) {
            return;
        }
        int[] location = new int[2];
        mContent.getLocationOnScreen(location);
        mContent.setVisibility(View.INVISIBLE);
        PhotoFragment fragment = (PhotoFragment) mContext.getSupportFragmentManager()
                .findFragmentByTag(PhotoFragment.TAG);
        int width = mContent.getWidth();
        int height = mContent.getHeight();
        int x = location[0];
        int y = location[1];
        fragment.setOriginalInfo(width, height, x, y);
    }

    public void setContentVisible(boolean visible) {
        if (mContent != null) {
            mContent.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
