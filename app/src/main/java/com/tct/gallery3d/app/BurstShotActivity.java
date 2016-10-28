/*----------------------------------------------------------------------------------------------------------------------------------------------*/
/* 12/11/2015|    su.jiang     |  PR-898084    |[Android5.1][Gallery_v5.2.3.1.0311.0][Force close]Gallery force close when save burst shot photo*/
/*-----------|-----------------|---------------|------------------------------------------------------------------------------------------------*/
/* 21/11/2015|    su.jiang     |  PR-909062    |[Android6.0][Gallery_v5.2.3.1.0312.0][Force Close]Gallery force close when tap back key on saving picture*/
/*-----------|-----------------|---------------|---------------------------------------------------------------------------------------------------------*/
/* 10/12/2015|    su.jiang     |  PR-1059325   |[Gallery]The burst shots preview screen default indication wrong with even numbered photos*/
/*-----------|-----------------|---------------|------------------------------------------------------------------------------------------*/
/* 29/01/2016|    su.jiang     |  PR-1537109   |[GAPP][Android6.0][Gallery]Put out the screen,press the boomkey even took pictures.*/
/*-----------|-----------------|---------------|Click on the button not to shoot pictures preview interface.-----------------------*/
/* 03/04/2016| jian.pan1            |[ALM]Defect:1660727   |[onetouch feedback]edit screentshot in moments layout display error.
/* ----------|----------------------|----------------------|----------------- */
/* 03/09/2016|    su.jiang     |  PR-1761738   |[GAPP][Android6.0][Gallery][Force Close]Gallery force close after saving burst shot.*/
/*-----------|-----------------|---------------|------------------------------------------------------------------------------------*/
package com.tct.gallery3d.app;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.adapter.BurstShotPhotoAdapter;
import com.tct.gallery3d.app.adapter.BurstShotThumbnailAdapter;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.model.BurstShotItem;
import com.tct.gallery3d.app.view.BurstShotView;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.Exif;
import com.tct.gallery3d.data.LocalMediaItem;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.exif.ExifInterface;
import com.tct.gallery3d.filtershow.tools.SaveImage;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.ui.BitmapLoader;
import com.tct.gallery3d.util.Future;
import com.tct.gallery3d.util.FutureListener;
import com.tct.gallery3d.util.ScreenUtils;
import com.tct.gallery3d.util.ThreadPool;

public class BurstShotActivity extends Activity implements
        OnItemSelectedListener, Callback {

    public static final String TAG = "BurstShotActivity";

    public static final String BURSTSHOTLIST = "burstshot-arraylist";
    public static final String BURSTSHOTID = "burstshot-id";

    private static final int MSG_UPDATE_PHOTO = 0x1;
    private static final int MSG_UPDATE_THUMBNAIL = 0x2;
    private static final int MSG_SAVE_BITMAP_FINISH = 0x4;

    private static final int DEFAULT_COMPRESS_QUALITY = 90;

    private BurstShotView mGallery;
    private BurstShotView mThunmnailGallery;

    private BurstShotPhotoAdapter mBurstShotPhotoAdapter;
    private BurstShotThumbnailAdapter mBurstShotThumbnailAdapter;

    private String mBurstShotId = null;
    private BurstShotItem[] mItems = null;
    private ThreadPool mThreadPool = null;
    private SaveBitmapTask mSaveBitmapTask = null;

    private ActionMode mActionMode;
    private ActionBar mActionBar;
    private SystemBarTintManager mTintManager = null;
    private Menu mMenu;
    private MenuItem mSaveMenu;
    private RelativeLayout mLoadingView;//[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-09,PR1761738
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-12,PR898084 begin
    private List<Uri> newUriList = new ArrayList<Uri>();

    private boolean isOverWrite = false;
    private boolean isSaveImage = false;
    private boolean isSelect = false;
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-12,PR898084 end
    public static final String KEY_LOCKED_CAMERA = "is-camera-review";
    private boolean isCameraReview = false;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case BurstShotDialog.MSG_OVERWRITE:
                startSaveToOtherBitmap();
                break;
            case BurstShotDialog.MSG_INDIVIDUAL_IMAGE:
                startSaveBitmapTask();
                break;
            case MSG_UPDATE_PHOTO:
                /* MODIFIED-BEGIN by hao.yin, 2016-03-21, BUG-1841612 */
                Log.d(TAG, "=====mHandler ===MSG_UPDATE_PHOTO=====");
                mBurstShotPhotoAdapter.notifyDataSetChanged();
                break;
            case MSG_UPDATE_THUMBNAIL:
                Log.d(TAG, "=====mHandler ===MSG_UPDATE_THUMBNAIL=====");
                /* MODIFIED-END by hao.yin,BUG-1841612 */
                mBurstShotThumbnailAdapter.notifyDataSetChanged();
                break;
            case MSG_SAVE_BITMAP_FINISH:
                finishSaveBitmapTask();
                break;
            default:
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle intent = getIntent().getExtras();
        ArrayList<String> burstShotIdList = intent
                .getStringArrayList(BURSTSHOTLIST);
        mBurstShotId = intent.getString(BURSTSHOTID);
        setContentView(R.layout.burstshot_main);
        mLoadingView = (RelativeLayout) this.findViewById(R.id.loading_layout);//[BUGFIX]-Add by TCTNJ,su.jiang, 2016-03-09,PR1761738
        initActionBar();
        mActionMode = this.startActionMode(this);
        View customView = LayoutInflater.from(this).inflate(
                R.layout.burstshot_actionmode, null);
        mActionMode.setCustomView(customView);
        mThreadPool = ((GalleryApp) getApplication()).getThreadPool();
        initBurstData(burstShotIdList);
        initAdapter();
        initListener();
        loadAllBitmap();
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/10,PR1718974 begin
        isCameraReview =intent.getBoolean(KEY_LOCKED_CAMERA,false);
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenOnOffReceive, screenFilter);
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/10,PR1718974 end
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-29,PR1537109 begin
    @Override
    protected void onResume() {
        super.onResume();
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/03/10,PR1718974 begin
        if(isCameraReview){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
        //[BUGFIX]-Modify by TCTNJ,caihong.gu-nb, 2016/03/10,PR1718974 end
        Log.d(TAG, "=====onResume=====;nowTime="+System.currentTimeMillis());// MODIFIED by hao.yin, 2016-03-21, BUG-1841612
    }
    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-01-29,PR1537109 end

    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/10,PR1718974 begin
    private BroadcastReceiver mScreenOnOffReceive = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null){
                if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
                }else if(Intent.ACTION_SCREEN_OFF.equals(intent.getAction())&&isCameraReview){
                    finish();
                }
            }
        }
    };
    //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/10,PR1718974 end

    public void initBurstData(ArrayList<String> arrayList) {
        Path path = Path.fromString("/local/image/item");
        mItems = new BurstShotItem[arrayList.size()];
        DataManager data = DataManager.from(this);
        int i = 0;
        for (String id : arrayList) {
            Path childPath = path.getChild(id);
            LocalMediaItem item = (LocalMediaItem) data
                    .getMediaObject(childPath);
            mItems[i] = new BurstShotItem(false, i, item);
            i++;
        }
    }

    @SuppressWarnings("deprecation")
    private void initAdapter() {
        int position = mItems.length / 2;//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-12-10,PR1059325
        Log.e(TAG, "selection = " + position);
        float spacing = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                8, getResources().getDisplayMetrics());

        // init gallery
        mGallery = (BurstShotView) this.findViewById(R.id.gallery);
        mBurstShotPhotoAdapter = new BurstShotPhotoAdapter(this,mItems);
        mGallery.setAdapter(mBurstShotPhotoAdapter);
        mGallery.setSpacing((int) spacing);
        mGallery.setSelection(position);

        // init thunmnail gallery
        mThunmnailGallery = (BurstShotView) this
                .findViewById(R.id.thumbnailgallery);
        mThunmnailGallery.setSelection(position);
        mBurstShotThumbnailAdapter = new BurstShotThumbnailAdapter(this, mItems, position);
        mThunmnailGallery.setAdapter(mBurstShotThumbnailAdapter);
        mThunmnailGallery.setSpacing((int) spacing);

    }

    private void initListener() {

        mGallery.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                boolean checked = mItems[position].isSelected;
                if (checked) {
                    mItems[position].isSelected = false;
                } else {
                    mItems[position].isSelected = true;
                }
                Log.e(TAG, "Click : " + position);
                display();
                mBurstShotPhotoAdapter.notifyDataSetChanged();
            }
        });

        mThunmnailGallery.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                mBurstShotThumbnailAdapter.setPosition(position);
            }
        });

        mGallery.setOnItemSelectedListener(this);
        mThunmnailGallery.setOnItemSelectedListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mBurstShotPhotoAdapter.setItemSize();
        mBurstShotPhotoAdapter.notifyDataSetChanged();
        mBurstShotThumbnailAdapter.setItemSize();
        mBurstShotThumbnailAdapter.notifyDataSetChanged();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
            long id) {
        mGallery.setSelection(position);
        mThunmnailGallery.setSelection(position);
        mBurstShotThumbnailAdapter.setPosition(position);
        mBurstShotThumbnailAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void display() {
        for (int i = 0; i < mItems.length; i++) {
            if (mItems[i].isSelected) {
                mSaveMenu.setIcon(R.drawable.ic_check);
                mSaveMenu.setEnabled(true);
                isSelect = true;
            }
        }
        if (!isSelect) {
            mSaveMenu.setIcon(R.drawable.brust_shot_unok);
            mSaveMenu.setEnabled(false);
        }
        isSelect = false;
    }

    public void initActionBar() {
        mActionBar = this.getActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mTintManager = new SystemBarTintManager(this);
        mTintManager.setStatusBarTintEnabled(false);
        mTintManager.setStatusBarTintColor(Color.BLACK);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.burstshot_menu, menu);
        mMenu = menu;
        mSaveMenu = mMenu.findItem(R.id.burstshot_menu_save);
        mSaveMenu.setVisible(true);
        mSaveMenu.setEnabled(false);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
        case R.id.burstshot_menu_save:
            BurstShotDialog dialog = new BurstShotDialog(
                    BurstShotActivity.this, mHandler);
            dialog.show();
            break;
        default:
            break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (mSaveBitmapTask != null) mSaveBitmapTask.cancel(true);//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-21,PR909062
        finishActivity();
    }

    public void finishActivity() {
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for(int i=0;i<mItems.length;i++){
            if(mItems[i].loader != null) mItems[i].loader.cancelLoad();//[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-11-21,PR909062
        }
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/10,PR1718974 begin
        unregisterReceiver(mScreenOnOffReceive);
        //[BUGFIX]-Add by TCTNJ,caihong.gu-nb, 2016/03/10,PR1718974 end
    }

    private void startSaveBitmapTask() {
        if (mItems == null || mItems.length == 0)
            return;

        showProgressBar();
        isSaveImage = true;
        int length = mItems.length;
        ArrayList<Uri> uriArray = new ArrayList<Uri>();
        HashMap<Uri, Bitmap> uriMap = new HashMap<Uri, Bitmap>();
        for (int i = 0; i < length; i++) {
            BurstShotItem item = mItems[i];
            if (item.isSelected) {
                Uri uri = item.item.getContentUri();
                uriArray.add(uri);
                uriMap.put(uri, item.bitmap);
            }
        }
        mSaveBitmapTask = new SaveBitmapTask(uriArray);
        mSaveBitmapTask.execute(uriMap);
    }

    private void startSaveToOtherBitmap() {
        if (mItems == null || mItems.length == 0)
            return;

        showProgressBar();
        isOverWrite = true;
        isSaveImage = true;
        int length = mItems.length;
        ArrayList<Uri> uriArray = new ArrayList<Uri>();
        HashMap<Uri, Bitmap> uriMap = new HashMap<Uri, Bitmap>();
        for (int i = 0; i < length; i++) {
            BurstShotItem item = mItems[i];
            if (item.isSelected) {
                Uri uri = item.item.getContentUri();
                uriArray.add(uri);
                uriMap.put(uri, item.bitmap);
            }
        }
        mSaveBitmapTask = new SaveBitmapTask(uriArray);
        mSaveBitmapTask.execute(uriMap);
    }

    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-12,PR898084 begin
    private void showProgressBar(){
        mLoadingView.setVisibility(View.VISIBLE);//[BUGFIX]-Modify by TCTNJ,su.jiang, 2016-03-09,PR1761738
        mSaveMenu.setEnabled(false);
    }

    private void deleteAllBurstShots(){
        ContentResolver contentResolver = getContentResolver();
        Uri baseUri = Images.Media.EXTERNAL_CONTENT_URI;
        for(int i = 0; i < mItems.length ; i++){
            contentResolver.delete(baseUri, "_id = ?",
                    new String[] { String.valueOf(mItems[i].item.id) });
        }
        ExifInfoFilter.getInstance(this).removeBurstShot(mBurstShotId);
    }
    //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-12,PR898084 end

    private void finishSaveBitmapTask() {
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-12,PR898084 begin
        /*if(isOverWrite){
            setResult(GalleryConstant.REQUEST_BURSTSHOT, new Intent().setData(newUriList.get(0)));
        }*/
        if (isSaveImage) {
            setResult(GalleryConstant.REQUEST_BURSTSHOT, new Intent().putExtra(TAG,true));
        }
        //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-12,PR898084 end
        finishActivity();
    }

    private void loadAllBitmap() {
        int length = mItems.length;
        for (int i = 0; i < length; i++) {
            BurstShotItem item = mItems[i];
            Log.d(TAG, "=====loadAllBitmap===;item="+mItems[i]+";index="+i+";item.item="+item.item);// MODIFIED by hao.yin, 2016-03-21, BUG-1841612
            item.loader = new BurstShotImageLoader(item.item, i);
            item.loader.startLoad();
        }
    }

    private class SaveBitmapTask extends
            AsyncTask<HashMap<Uri, Bitmap>, Void, Boolean> {
        private ArrayList<Uri> mUriArray;

        public SaveBitmapTask(ArrayList<Uri> uriArray) {
            mUriArray = uriArray;
        }

        @Override
        protected Boolean doInBackground(HashMap<Uri, Bitmap>... params) {
            Log.d(TAG, "SaveBitmapTask.doInBackground {");
            HashMap<Uri, Bitmap> bitmapMap = params[0];

            for (Uri sourceUri : mUriArray) {
                //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-11-21,PR909062 begin
                Bitmap bitmap = bitmapMap.get(sourceUri);
                File file = SaveImage.getNewFile(getBaseContext(), sourceUri);
                if (file != null) {
                    //get exif
                    ExifInterface exif = Exif.getExifData(sourceUri,
                            BurstShotActivity.this);
                    if (exif != null && bitmap != null) {
                        if (exif.isContainExifInfo()) {
                            exif.deleteTag(ExifInterface.TAG_USER_COMMENT);
                            if (Exif.saveExifDataToBitmap(file, sourceUri,
                                    bitmap, exif, DEFAULT_COMPRESS_QUALITY)) {

                                //update database
                                // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 begin
                                ContentValues values = SaveImage.getContentValues(getBaseContext(), sourceUri, file, System.currentTimeMillis(), bitmap);
                                // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2016-03-04,Defect:1660727 end
                                Uri savedUri = getBaseContext().getContentResolver().insert(
                                        Images.Media.EXTERNAL_CONTENT_URI, values);
                                newUriList.add(savedUri);//[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-12,PR898084
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    Log.e(TAG, "Thread.sleep", e);
                                }
                            }
                        }
                    }
                    //[BUGFIX]-Modify by TCTNJ,su.jiang, 2015-11-21,PR909062 end
                } else {
                    Log.d(TAG, "source " + sourceUri
                            + " getLocalFileFromUri is failed");
                }
            }
            Log.d(TAG, "} SaveBitmapTask.doInBackground");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-12,PR898084 begin
                if (isOverWrite) {
                    deleteAllBurstShots();
                }
                //[BUGFIX]-Add by TCTNJ,su.jiang, 2015-11-12,PR898084 end
                mHandler.obtainMessage(MSG_SAVE_BITMAP_FINISH).sendToTarget();
            }
        }
    }

    public class BurstShotImageLoader extends BitmapLoader {

        private MediaItem item;
        private int mIndex;

        public BurstShotImageLoader(MediaItem item, int index) {
            this.item = item;
            this.mIndex = index;
        }

        @Override
        protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
            if (item == null)
                return null;
            return mThreadPool.submit(
                    item.requestImage(MediaItem.TYPE_THUMBNAIL), this);
        }

        @Override
        protected void onLoadComplete(Bitmap bitmap) {
            Log.d(TAG, "=====mIndex="+mIndex+";mItems[mIndex].bitmap="+bitmap+";nowTime="+System.currentTimeMillis());// MODIFIED by hao.yin, 2016-03-21, BUG-1841612
            mItems[mIndex].bitmap = bitmap;
            mHandler.obtainMessage(MSG_UPDATE_PHOTO, null).sendToTarget();
            mHandler.obtainMessage(MSG_UPDATE_THUMBNAIL, null).sendToTarget();
        }
    }
}
