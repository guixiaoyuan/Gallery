package com.tct.gallery3d.app.fragment;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.view.ViewPager.PageTransformer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;
import android.widget.Toolbar;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.AbstractGalleryFragment;
import com.tct.gallery3d.app.BurstShotActivity;
import com.tct.gallery3d.app.FaceShowActivity;
import com.tct.gallery3d.app.GalleryActivity;
import com.tct.gallery3d.app.GalleryApp;
import com.tct.gallery3d.app.MovieActivity;
import com.tct.gallery3d.app.PhotoPageBottomControls;
import com.tct.gallery3d.app.adapter.PhotoDataAdapter;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.app.view.PhotoDetailView;
import com.tct.gallery3d.app.view.PhotoDetailView.GestureEventListener;
import com.tct.gallery3d.app.view.SmoothImageView;
import com.tct.gallery3d.app.view.SmoothImageView.TransformListener;
import com.tct.gallery3d.app.view.TrimVideo;
import com.tct.gallery3d.data.DataManager;
import com.tct.gallery3d.data.LocalMediaItem;
import com.tct.gallery3d.data.MediaDetails;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.data.MediaObject;
import com.tct.gallery3d.data.Path;
import com.tct.gallery3d.drm.DrmManager;
import com.tct.gallery3d.filtershow.FilterShowActivity;
import com.tct.gallery3d.image.ImageWorker.OnImageLoadedListener;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;
import com.tct.gallery3d.polaroid.tools.Utils;
import com.tct.gallery3d.ui.DetailsHelper;
import com.tct.gallery3d.ui.DetailsHelper.CloseListener;
import com.tct.gallery3d.ui.DetailsHelper.DetailsSource;
import com.tct.gallery3d.ui.HackyViewPager;
import com.tct.gallery3d.util.GalleryUtils;
import com.tct.gallery3d.util.PLFUtils;
import com.tct.gallery3d.util.ScreenUtils;

import java.io.File;
import java.util.ArrayList;

@SuppressLint("NewApi")
public class PhotoFragment extends GalleryFragment
        implements PhotoPageBottomControls.Delegate, TransformListener, OnPageChangeListener, GestureEventListener, OnClickListener {

    public final static String TAG = PhotoFragment.class.getSimpleName();
    private AbstractGalleryActivity mContext;

    private ViewGroup mPhotoRootLayout;
    private View mToolbarBackground;
    private ViewGroup mBottomBarBackground;
    private SmoothImageView mSmoothImage;
    private HackyViewPager mViewPager;
    private Menu mMenu;
    private Toolbar mToolbar;
    private View mDecorView;

    private PhotoDataAdapter mAdapter;
    private PhotoPageBottomControls mBottomControls;

    public int mInnerIndex;
    private int mSlotIndex;
    private int mFromPageType = GalleryConstant.FROM_NONE_PAGE;
    public static final int REQUEST_EDIT = 4;
    public static final int REQUEST_TRIM_MUTE = 6;

    public static final String ACTION_POLAROID_EDIT = "action_polaroid_edit";
    public static final String ACTION_NEXTGEN_EDIT = "action_nextgen_edit";
    public static final String ACTION_GALLERY_EDIT = "action_gallery_edit";
    public static final String LOCATION_X = "locationX";
    public static final String LOCATION_Y = "locationY";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    private int mLocationX;
    private int mLocationY;
    private int mWidth;
    private int mHeight;
    private String mItemPath;

    private Handler mHandler = new Handler();

    private MenuItem mSlideShowItem;
    private MenuItem mPrintItem;
    private MenuItem mSetAsItem;
    private DetailsHelper mDetailsHelper;

    private boolean mIsSingleItem = false;
    private boolean isFromBurstShot = false;

    private Drawable mDrawable;

    public void setDrawable(Drawable drawable){
        mDrawable = drawable;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (AbstractGalleryActivity) getGalleryContext();

        initData();
        showLoadingImage(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPhotoRootLayout = (ViewGroup) inflater.inflate(R.layout.photo_page, null);
        mToolbar = (Toolbar) mPhotoRootLayout.findViewById(R.id.toolbar);
        mViewPager = (HackyViewPager) mPhotoRootLayout.findViewById(R.id.id_viewpager);
        mSmoothImage = (SmoothImageView) mPhotoRootLayout.findViewById(R.id.preview_photo);
        mDecorView = mContext.getWindow().getDecorView();

        mToolbarBackground = mPhotoRootLayout.findViewById(R.id.toolbar_background);
        mBottomBarBackground = (ViewGroup) mPhotoRootLayout.findViewById(R.id.bottom_bar_background);
        return mPhotoRootLayout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mDecorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int vis) {
                if ((vis & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0) {
                    showSystemUi(false);
                } else {
                    showSystemUi(true);
                }
            }
        });
        // init toolbar
        mContext.setMargin(mToolbar, true);
        mContext.setActionBar(mToolbar);
        initActionBar();
        toolbarAnimator(false, 0);
        // In MultiWindow Mode ,mToolbar will overlay,so we set paddingTop 30
       /* if (Build.VERSION.SDK_INT >= 24) {
            if (mContext.isInMultiWindowMode()) {
                mToolbar.setPadding(0, 30, 0, 0);
            } else {
                mToolbar.setPadding(0, 0, 0, 0);
            }
        } else {
            mToolbar.setPadding(0, 0, 0, 0);
        }*/

        // init viewpager
        mViewPager.setPageTransformer(true, new PageTransformer() {
            private static final float MIN_SCALE = 0.75f;

            @Override
            public void transformPage(View view, float position) {
                int pageWidth = view.getWidth();
                if (position < -1) {
                    view.setAlpha(0);
                } else if (position <= 0) {
                    view.setAlpha(1);
                    view.setTranslationX(0);
                    view.setScaleX(1);
                    view.setScaleY(1);
                } else if (position <= 1) {
                    view.setAlpha(1 - position);
                    view.setTranslationX(pageWidth * -position);
                    float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                    view.setScaleX(scaleFactor);
                    view.setScaleY(scaleFactor);
                } else {
                    view.setAlpha(0);
                }
            }
        });
        mViewPager.addOnPageChangeListener(this);

        // init smooth image
        mSmoothImage.setOnTransformListener(this);

        mAdapter = new PhotoDataAdapter(this, mViewPager);
        // init bottom controls
        mBottomControls = new PhotoPageBottomControls(this, mContext, mBottomBarBackground);
        mViewPager.setAdapter(mAdapter);

        // init transform
        if (mFromPageType != GalleryConstant.FROM_NONE_PAGE) {
            initSmoothVisible(true);
            transformIn();
        } else {
            initSmoothVisible(false);
            showSystemUi(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mContext.initSystemBar(mToolbar.getVisibility() != View.VISIBLE);
        mAdapter.resume();
        mViewPager.setCurrentItem(mInnerIndex);
        MediaItem item = mAdapter.getCurrentMediaItem();
        if (isFromBurstShot) {
//            updateUi();
            isFromBurstShot = false;
        }
        // update the favourite button
        updateUI(item);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        mAdapter.pause();
        mInnerIndex = mViewPager.getCurrentItem();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        showLoadingImage(true);
        super.onDestroy();
        mAdapter.destroy();
        mAdapter = null;
        setCurrentContent();
        mFromPageType = GalleryConstant.FROM_NONE_PAGE;
    }

    private void initActionBar() {
        ActionBar actionbar = mContext.getActionBar();
        actionbar.setTitle("");
//        actionbar.setBackgroundDrawable(mContext.getDrawable(R.drawable.photopage_actionbar_background));
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }

    private void initData() {
        Bundle data = getArguments();
        if (null != data) {
            mSlotIndex = data.getInt(GalleryConstant.KEY_INDEX_SLOT);
            mInnerIndex = data.getInt(GalleryConstant.KEY_INDEX_HINT, GalleryConstant.INVALID_INDEX);
            mItemPath = data.getString(GalleryConstant.KEY_MEDIA_ITEM_PATH);
            mIsSingleItem = data.getBoolean(GalleryActivity.KEY_SINGLE_ITEM_ONLY);
            mFromPageType = data.getInt(GalleryConstant.KEY_FROM_PAGE, GalleryConstant.FROM_NONE_PAGE);
            if (mFromPageType != GalleryConstant.FROM_NONE_PAGE) {
                mLocationX = data.getInt(LOCATION_X, 0);
                mLocationY = data.getInt(LOCATION_Y, 0);
                mWidth = data.getInt(WIDTH, 0);
                mHeight = data.getInt(HEIGHT, 0);
            }
        }
    }

    private boolean isInSplitScreen() {
        return (ScreenUtils.getScreenInfo(mContext) == ScreenUtils.tempScreenInLandSplit ||
                ScreenUtils.getScreenInfo(mContext) == ScreenUtils.tempScreenInPortSplit);
    }
    private void showSystemUi(boolean show) {
        mContext.initSystemBar(!show);
        if (isInSplitScreen()) {
            return;
        }
        if (show) {
            toolbarAnimator(true, SmoothImageView.ANIMATION_DURATION);
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (mBottomControls != null) {
                mBottomControls.refresh();
                mBottomControls.toggleAnim(true);
            }
        } else {
            toolbarAnimator(false, 0);
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            if (mBottomControls != null) {
                mBottomControls.refresh();
                mBottomControls.toggleAnim(false);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContext.resetToolbar();
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        if (mSmoothImage != null) {
            mSmoothImage.setImageDrawable(null);
        }
        if (mViewPager != null) {
            mViewPager.removeOnPageChangeListener(this);
            mViewPager.removeAllViews();
        }
        if (mPhotoRootLayout != null) {
            mPhotoRootLayout.removeAllViews();
        }
        if (mBottomControls != null) {
            mBottomControls.cleanup();
        }
    }

    @Override
    public boolean canDisplayBottomControls() {
        return true;
    }

    @Override
    public boolean canDisplayBottomControl(int control) {
        if (mAdapter.getCount() <= 0) return false;
        MediaItem item = mAdapter.getCurrentMediaItem();
        boolean isDrm = false;
        if (item == null) {
            return false;
        }
        if (DrmManager.isDrmEnable) {
            if (item.isDrm() == 1) {
                isDrm = true;
            }
        }
        switch (control) {
            case R.id.photopage_bottom_control_share:
                return !isDrm && (item.getSupportedOperations() & MediaItem.SUPPORT_SHARE) != 0;
            case R.id.photopage_bottom_control_edit:
            return !isDrm && (item.getSupportedOperations() & MediaItem.SUPPORT_EDIT) != 0
                    && item.getMediaType() == MediaObject.MEDIA_TYPE_IMAGE;
//                return false;
            case R.id.photopage_bottom_control_video_edit:
                return !isDrm && (item.getSupportedOperations() & MediaItem.SUPPORT_EDIT) != 0
                        && item.getMediaType() == MediaObject.MEDIA_TYPE_VIDEO && GalleryUtils.hasVideoEditApk(mContext)
                        && GalleryUtils.isVideoEditorAvailable(item.getDetails().getDetail(MediaDetails.INDEX_DURATION));
            case R.id.photopage_bottom_control_trim:
                return !isDrm && (item.getSupportedOperations() & MediaItem.SUPPORT_TRIM) != 0 && !isSlowMotion(item.getPath().getSuffix(), item)
                        && GalleryUtils.isVideoTrimAvailable(item.getDetails().getDetail(MediaDetails.INDEX_DURATION));
            case R.id.photopage_bottom_control_favourite:
                return (item.getSupportedOperations() & MediaItem.SUPPORT_FAVOURITE) != 0;
            case R.id.photopage_bottom_control_tap_to_edit:
                return !isDrm && isSlowMotion(item.getPath().getSuffix(), item);
            case R.id.photopage_bottom_control_delete:
                return !mIsSingleItem;
            default:
                return false;
        }
    }

    @Override
    public boolean shouldDisplayBottomControl(int control) {
        if (mAdapter.getCount() <= 0) return false;
        MediaItem item = mAdapter.getCurrentMediaItem();
        boolean isDrm = false;
        if (item == null) {
            return false;
        }
        if (DrmManager.isDrmEnable) {
            if (item.isDrm() == 1) {
                isDrm = true;
            }
        }
        switch (control) {
            case R.id.photopage_bottom_control_share:
                return !isDrm && (item.getSupportedOperations() & MediaItem.SUPPORT_SHARE) != 0;
            case R.id.photopage_bottom_control_edit:
                return !isDrm && (item.getSupportedOperations() & MediaItem.SUPPORT_EDIT) != 0
                        && item.getMediaType() == MediaObject.MEDIA_TYPE_IMAGE;
//                return false;
            case R.id.photopage_bottom_control_video_edit:
                return !isDrm && (item.getSupportedOperations() & MediaItem.SUPPORT_EDIT) != 0
                        && item.getMediaType() == MediaObject.MEDIA_TYPE_VIDEO && GalleryUtils.hasVideoEditApk(mContext)
                        && GalleryUtils.isVideoEditorAvailable(item.getDetails().getDetail(MediaDetails.INDEX_DURATION));
            case R.id.photopage_bottom_control_trim:
                return !isDrm && (item.getSupportedOperations() & MediaItem.SUPPORT_TRIM) != 0
                        && GalleryUtils.isVideoTrimAvailable(item.getDetails().getDetail(MediaDetails.INDEX_DURATION))
                        && !isSlowMotion(item.getPath().getSuffix(), item);
            case R.id.photopage_bottom_control_favourite:
                return (item.getSupportedOperations() & MediaItem.SUPPORT_FAVOURITE) != 0;
            case R.id.photopage_bottom_control_tap_to_edit:
                return !isDrm && isSlowMotion(item.getPath().getSuffix(), item);
            case R.id.photopage_bottom_control_delete:
                return !mIsSingleItem;
            default:
                return false;
        }
    }

    @Override
    public void onBottomControlClicked(int control) {
        MediaItem currentPhoto = mAdapter.getCurrentMediaItem();
        if (currentPhoto == null) {
            return;
        }
        switch (control) {
            case R.id.photopage_bottom_control_edit:
            case R.id.photopage_bottom_control_video_edit:
                launchEditor(currentPhoto);
                break;
            case R.id.photopage_bottom_control_share:
                launchPhotoShare(currentPhoto);
                break;
            case R.id.photopage_bottom_control_favourite:
                if (currentPhoto instanceof LocalMediaItem) {
                    LocalMediaItem item = (LocalMediaItem) currentPhoto;
                    item.toogleFavorite();
                    mBottomControls.invalidateFavourite(item.mIsFavorite);
                }
                break;
            case R.id.photopage_bottom_control_trim:
                launchVideoTrim(currentPhoto);
                break;
            case R.id.photopage_bottom_control_tap_to_edit:
                launchTapToEdit(currentPhoto);
                break;
            case R.id.photopage_bottom_control_delete:
                Path path = currentPhoto.getPath();
                mAdapter.delete(path);
                break;
        }
    }

    private boolean isSlowMotion(String id, MediaItem item) {
        int type = ExifInfoFilter.getInstance(mContext).queryType(id);
        if (item != null && type == ExifInfoFilter.NONE
                && item.getMediaType() == MediaObject.MEDIA_TYPE_VIDEO) {
            type = ExifInfoFilter.getInstance(mContext).filter(id,
                    item.getFilePath(), MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO, -1, true, false, null);
        }
        return type == ExifInfoFilter.SLOWMOTION;
    }

    @Override
    public void refreshBottomControlsWhenReady() {
    }

    private void launchEditor(MediaItem current) {

        if (current == null || (current.getSupportedOperations() & MediaObject.SUPPORT_EDIT) == 0) {
            return;
        }

        Intent intent = new Intent();
        String filePath = current.getFilePath();
        if (TextUtils.isEmpty(filePath))
            return;

        String realType = GalleryUtils.getRealType((GalleryApp) mContext.getApplication(),
                current.getContentUri().toString());
        if (MediaItem.MIME_TYPE_GIF.equals(realType)) {
            Toast.makeText(mContext, R.string.illegal_image_format, Toast.LENGTH_SHORT).show();
            return;
        }

        String mimeType = current.getMimeType();
        if (!TextUtils.isEmpty(mimeType) && mimeType.startsWith("video/")) {
            Path path = current.getPath();
            int userCommentType = ExifInfoFilter.NONE;
            if (path != null) {
                String id = path.getSuffix();
                userCommentType = ExifInfoFilter.getInstance(mContext).queryType(id);
            } else {
                Log.e(TAG, "launchEditor() path is NULL.");
            }
            Log.i(TAG, "launchEditor() userCommentType = " + userCommentType);
            if (userCommentType == ExifInfoFilter.MICROVIDEO) {
                intent.setAction(GalleryConstant.ACTION_EDIT);
            } else {
                intent.setAction(GalleryConstant.ACTION_TRIM);
            }

            if (com.tct.gallery3d.image.Utils.hasN()) {
                intent.setData(current.getContentUri());
            } else {
                Uri uri = Uri.fromFile(new File(filePath));
                intent.setDataAndType(uri, mimeType);
            }
            if (PLFUtils.getBoolean(mContext, "def_gallery_custom_share_enable")) {
                intent.putExtra(GalleryConstant.EXTRA_USE_CUSTOM, true);
            } else {
                intent.putExtra(GalleryConstant.EXTRA_USE_CUSTOM, false);
            }
            if (mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                    .size() > 0) {
                startActivity(intent);
            } else {
                Log.e(TAG, "Can't find muvee App.");
            }
            return;
        }

        if (filePath.startsWith(Utils.POLAROID_PATH_SDCARD0) || filePath.startsWith(Utils.POLAROID_PATH_SDCARD1)) {
            intent.setAction(ACTION_POLAROID_EDIT);
        } else {
            intent.setAction(ACTION_NEXTGEN_EDIT);
        }

        intent.setDataAndType(current.getContentUri(), mimeType).setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() == 0) {
            intent.setAction(ACTION_GALLERY_EDIT);
        }
        if (mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() == 0) {
            intent.setAction(Intent.ACTION_EDIT);
        }
        intent.putExtra(FilterShowActivity.LAUNCH_FULLSCREEN, mContext.isFullscreen());
        startActivityForResult(intent, REQUEST_EDIT);
        overrideTransitionToEditor();
    }

    private void overrideTransitionToEditor() {
        mContext.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * lunch to photo share
     */
    private void launchPhotoShare(MediaItem item) {
        if (item != null) {
            String mimeType = item.getMimeType();
            Uri imageUri = item.getContentUri();
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.setType(mimeType);
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.share)));
        }
    }

    private void launchVideoTrim(MediaItem current) {
        Path path = current.getPath();
        DataManager manager = mContext.getDataManager();
        Intent intent = new Intent(mContext, TrimVideo.class);
//        intent.setData(manager.getContentUri(path));
        intent.setData(current.getContentUri());
        intent.putExtra(GalleryConstant.KEY_MEDIA_ITEM_PATH, current.getFilePath());
        mContext.startActivityForResult(intent, REQUEST_TRIM_MUTE);
    }

    private void launchTapToEdit(MediaItem item) {
        int index = mViewPager.getCurrentItem();
        if (!GalleryUtils.hasSlowMotionApk(mContext)) {
            Toast.makeText(mContext, R.string.no_slowmoedit, Toast.LENGTH_SHORT).show();
            return;
        }
        if (item != null && mAdapter.isSlowMotion(index)) {
            Intent intent = new Intent();
            intent.setAction(GalleryConstant.ACTION_SLOW_MO);
            intent.setDataAndType(Uri.fromFile(new File(item.getFilePath())), "video/mp4");
            try {
                intent.putExtra("isCameraReview", ((GalleryActivity) mContext).isCameraReview);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mContext.startActivity(intent);
        }
    }

    private boolean mTransformStarted = false;

    @Override
    public boolean onBackPressed() {
        if (mTransformStarted) {
            return true;
        }
        if (mFromPageType != GalleryConstant.FROM_NONE_PAGE) {
            if (mAdapter.getCount() > 0) {
                transformOut();
            } else {
                removePhotoFragment(false);
            }
        } else {
            mContext.finish();
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean show = (mDecorView.getSystemUiVisibility() == View.SYSTEM_UI_FLAG_VISIBLE);
        showSystemUi(show);
        mBottomControls.resetBottomButtons();
        mContext.setMargin(mToolbar, true);

        if (ScreenUtils.getScreenInfo(mContext) == ScreenUtils.tempScreenInPortFull) {
            for (int i = 0; i < mViewPager.getChildCount(); i++) {
                ImageButton photoViewButton = (ImageButton) mViewPager.getChildAt(i).findViewById(R.id.id_photoview_button);
                photoViewButton.setPadding(0, 0, 0, 150);
            }
        } else {
            for (int i = 0; i < mViewPager.getChildCount(); i++) {
                ImageButton photoViewButton = (ImageButton) mViewPager.getChildAt(i).findViewById(R.id.id_photoview_button);
                photoViewButton.setPadding(0, 0, 0, 0);
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.photo_page_menu, menu);
        /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-07-11,BUG-2208330 */
        mSlideShowItem = menu.findItem(R.id.action_slideshow_inner);
        mSetAsItem = menu.findItem(R.id.action_setas);
        mPrintItem = menu.findItem(R.id.print);
        /* MODIFIED-END by Yaoyu.Yang,BUG-2208330 */
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        if (null != mMenu && mIsSingleItem) {
            hiddenMenu();
        }
        super.onPrepareOptionsMenu(menu);
    }

    private void hiddenMenu() {
        for (int i = 0; i < mMenu.size(); i++) {
            int id = mMenu.getItem(i).getItemId();
            MenuItem item = mMenu.getItem(i);
            if (id == R.id.action_slideshow_inner || id == R.id.action_delete) {
                item.setVisible(false);
                item.setEnabled(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MediaItem current = mAdapter.getCurrentMediaItem();
        if (current == null) {
            return false;
        }
        Path path = current.getPath();
        int id = item.getItemId();
        if (mIsSingleItem) {
            if (id == R.id.action_slideshow_inner || id == R.id.action_delete) {
                item.setVisible(false);
                item.setEnabled(false);
            }
        }
        switch (id) {
            case R.id.action_slideshow_inner:
                String albumSetPath = mAdapter.getMediaSetPath();
                Bundle data = new Bundle();
                data.putInt(GalleryConstant.KEY_INDEX_SLOT, mViewPager.getCurrentItem());
                data.putInt(GalleryConstant.KEY_FROM_PAGE, GalleryConstant.FROM_PHOTODETAIL_PAGE);
                data.putString(GalleryConstant.KEY_MEDIA_SET_PATH, albumSetPath);
                mContext.startSlideShow(data);
                break;
            case R.id.action_setas:
                launchPhotoSetAs();
                break;
            case R.id.action_details:
                showDetails();
                break;
            case R.id.print:
                mContext.printSelectedImage(mContext.getDataManager().getContentUri(path));
                break;
            case android.R.id.home:
                mContext.onBackPressed();
                break;
            default:
                break;
        }
        return true;
    }

    private void hideDetails() {
        if (mDetailsHelper != null && mDetailsHelper.isShowing()) {
            mDetailsHelper.hide();
        }
    }

    private void showDetails() {
        GalleryUtils.getSystemHourFormat(mContext);
        GalleryUtils.getSystemDateFormat(mContext);
        if (mDetailsHelper == null) {
            mDetailsHelper = new DetailsHelper(mContext, new MyDetailsSource());
            mDetailsHelper.setCloseListener(new CloseListener() {
                @Override
                public void onClose() {
                    hideDetails();
                }
            });
        }
        mDetailsHelper.show();
    }

    private class MyDetailsSource implements DetailsSource {
        @Override
        public MediaDetails getDetails() {
            MediaItem item = mAdapter.getCurrentMediaItem();
            MediaDetails details = null;
            if (item != null) {
                details = item.getDetails();
            }
            return details;
        }

        @Override
        public int size() {
            return mAdapter.getCount();
        }

        @Override
        public int getIndex() {
            return 0;
        }
    }

    private void transformIn() {
        mSmoothImage.setVisibility(View.INVISIBLE);
        if (mDrawable != null) {
            mSmoothImage.setImageDrawable(mDrawable);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    startTransformIn();
                }
            });
        } else {
            Object data = null;
            switch (mFromPageType) {
                case GalleryConstant.FROM_ALBUM_PAGE:
                case GalleryConstant.FROM_ALBUMSET_PAGE:
                case GalleryConstant.FROM_FACESHOW_PAGE:
                case GalleryConstant.FROM_MOMENTS_PAGE:
                    data = DataManager.from(mContext).getMediaObject(Path.fromString(mItemPath));
                    break;
                default:
                    break;
            }
            loadThumbnail(data, mSmoothImage, new OnImageLoadedListener() {
                @Override
                public void onImageLoaded(boolean success) {
                    startTransformIn();
                }
            });
        }
    }

    private void startTransformIn() {
        initSmoothVisible(true);
        mSmoothImage.setOriginalInfo(mWidth, mHeight, mLocationX, mLocationY);
//        mSmoothImage.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
//        mSmoothImage.setScaleType(ScaleType.FIT_CENTER);
        mSmoothImage.transformIn();
        toolbarAnimator(true, SmoothImageView.ANIMATION_DURATION);
        mBottomControls.refresh();
        mBottomControls.toggleAnim(true);
    }

    private void transformOut() {
        long delay = 0;
        final boolean init = initLocation();
        if (!init) {
            delay = 50;
        }

        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                MediaItem item = mAdapter.getCurrentMediaItem();
                loadThumbnail(item, mSmoothImage, new OnImageLoadedListener() {
                    @Override
                    public void onImageLoaded(boolean success) {
                        if (!init) {
                            initLocation();
                        }
                        startTransformOut();
                    }
                });
            }
        }, delay);
    }

    private boolean initLocation() {
        boolean result = false;
        mInnerIndex = mViewPager.getCurrentItem();
        switch (mFromPageType) {
            case GalleryConstant.FROM_ALBUM_PAGE:
                result = ((AlbumFragment) getFragment(AlbumFragment.TAG)).initLocation(mSlotIndex, mInnerIndex);
                break;
            case GalleryConstant.FROM_MOMENTS_PAGE:
                result = ((MomentsFragment) ((GalleryActivity) mContext).getCurrentContent(GalleryActivity.PAGE_MOMENTS))
                        .initLocation(mSlotIndex, mInnerIndex);
                break;
            case GalleryConstant.FROM_FACESHOW_PAGE:
                result = ((FaceShowActivity) mContext).initLocation(mSlotIndex, mInnerIndex);
                break;
            default:
                break;
        }
        return result;
    }

    private void startTransformOut() {
        mContext.overridePendingTransition(0, 0);
        initSmoothVisible(true);
        mSmoothImage.transformOut();
    }

    public void setOriginalInfo(int width, int height, int locationX, int locationY) {
        if (mSmoothImage != null) {
            mSmoothImage.setOriginalInfo(width, height, locationX, locationY);
        }
    }

    @Override
    public void onPlayClicked(int position) {
        MediaItem item = mAdapter.getMediaItem(position);
        int type = item.getMediaType();
        if (type == MediaItem.MEDIA_TYPE_VIDEO) {
            /* MODIFIED-BEGIN by Yaoyu.Yang, 2016-08-04,BUG-2208330*/
            if (ExifInfoFilter.getInstance(mContext).queryType(item.getPath().getSuffix()) == ExifInfoFilter.SLOWMOTION
                    && GalleryUtils.hasSlowMotionApk(mContext)) {
                playSlowMotion(item);
            } else {
                playVideo(item.getPlayUri(), item.getName());
            }
        }
    }

    private void playSlowMotion(MediaItem item) {
        Intent intent = new Intent();
        intent.setAction(GalleryConstant.ACTION_SLOW_MO_PLAY);
        intent.setDataAndType(Uri.fromFile(new File(item.getFilePath())), "video/mp4");
        try {
            mContext.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void playVideo(Uri uri, String title) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW).setClass(mContext, MovieActivity.class)
                    .setDataAndType(uri, "video/*").putExtra(Intent.EXTRA_TITLE, title)
                    .putExtra(MovieActivity.KEY_TREAT_UP_AS_BACK, true);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, R.string.video_err, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActionResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GalleryConstant.REQUEST_BURSTSHOT && data != null) {
            isFromBurstShot = data.getBooleanExtra(BurstShotActivity.TAG, false);
        }
    }

    public void removePhotoFragment(boolean animator) {
        FragmentManager fm = mContext.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (animator) {
            ft.setCustomAnimations(R.anim.fast_fade_in, R.anim.fast_fade_out);
        } else {
            ft.setCustomAnimations(FragmentTransaction.TRANSIT_NONE, FragmentTransaction.TRANSIT_NONE);
        }
        ft.remove(this);
        if (!mContext.isDestroyed()) {
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public void onTransformStart(int mode) {
        mTransformStarted = true;
        if (mode == SmoothImageView.STATE_TRANSFORM_IN) {
            initContentView(false);
        }
    }

    @Override
    public void onTransformComplete(int mode) {
        mTransformStarted = false;
        initContentView(true);
        switch (mode) {
            case SmoothImageView.STATE_TRANSFORM_OUT:
                removePhotoFragment(false);
                break;
            case SmoothImageView.STATE_TRANSFORM_IN:
                initSmoothVisible(false);
                mBottomControls.refresh();
                break;
            default:
                break;
        }
    }

    private void initContentView(boolean visible) {
        switch (mFromPageType) {
            case GalleryConstant.FROM_ALBUM_PAGE:
                ((AlbumFragment) getFragment(AlbumFragment.TAG)).setContentVisible(visible);
                break;
            case GalleryConstant.FROM_MOMENTS_PAGE:
                ((MomentsFragment) ((GalleryActivity) mContext).getCurrentContent(GalleryActivity.PAGE_MOMENTS))
                        .setContentVisible(visible);
                break;
            case GalleryConstant.FROM_FACESHOW_PAGE:
                ((FaceShowActivity) mContext).setContentVisible(visible);
                break;
            default:
                break;
        }
    }

    private void setCurrentContent() {
        AbstractGalleryFragment fragment = null;
        switch (mFromPageType) {
            case GalleryConstant.FROM_ALBUMSET_PAGE:
                fragment = (AlbumSetFragment) ((GalleryActivity) mContext)
                        .getCurrentContent(GalleryActivity.PAGE_ALBUMS);
                break;
            case GalleryConstant.FROM_ALBUM_PAGE:
                fragment = (AlbumFragment) getFragment(AlbumFragment.TAG);
                break;
            case GalleryConstant.FROM_MOMENTS_PAGE:
                fragment = (MomentsFragment) ((GalleryActivity) mContext).getCurrentContent(GalleryActivity.PAGE_MOMENTS);
                break;
            case GalleryConstant.FROM_FACESHOW_PAGE:
                // TODO
                break;
            default:
                break;
        }
        mContext.setContent(fragment);
    }

    private void initSmoothVisible(boolean visible) {
        if (visible) {
            mSmoothImage.setVisibility(View.VISIBLE);
            mViewPager.setVisibility(View.INVISIBLE);
            mPhotoRootLayout.setBackgroundColor(Color.TRANSPARENT);
        } else {
            mSmoothImage.setVisibility(View.INVISIBLE);
            mViewPager.setVisibility(View.VISIBLE);
            mPhotoRootLayout.setBackgroundColor(Color.BLACK);
        }
    }

    /**
     * lunch to photo set as
     */
    private void launchPhotoSetAs() {
        MediaItem current = mAdapter.getCurrentMediaItem();
        if (current == null) {
            return;
        }
        Intent intent;
        try {
            if (current.isDrmEnable() && current.isDrm() != 0) {
                intent = new Intent(GalleryConstant.ACTION_DRM_ATTACH_DATA);
            } else {
                intent = new Intent(Intent.ACTION_ATTACH_DATA);
            }
            intent.setData(current.getContentUri());
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, current.getMimeType());
            Log.w(TAG, "GalleryChange launchPhotoSetAs intent = " + intent);
            mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.set_as)));
        } catch (Exception e) {
            Log.w(TAG, "GalleryChange launchPhotoSetAs Exception e = ", e);
        }
        overrideTransitionToEditor();
    }

    @Override
    public void onPageScrollStateChanged(int index) {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int index) {
        mInnerIndex = index;
        mBottomControls.refresh();
        if (mAdapter != null) {
            MediaItem item = mAdapter.getCurrentMediaItem();
            if (item == null) {
                return;
            }
            updateUI(item);
            resetPage(index);
        }
    }

    private void resetPage(int index) {
        int count = mViewPager.getChildCount();
        if (count > 1) {
            index -= mViewPager.getCurrentItem();
            resetChild(index);
            resetChild(index - 1);
            resetChild(index + 1);
        }
    }

    private void resetChild(int index) {
        View view = mViewPager.getChildAt(index);
        if (view != null) {
            PhotoDetailView detailView = (PhotoDetailView) view.findViewById(R.id.imageView);
            detailView.resetScaleAndCenter();
        }
    }

    private void toolbarAnimator(boolean show, int duration) {
        final float wantAlpha = show ? 1 : 0;
        if ((wantAlpha == 1 && mToolbar.getVisibility() == View.VISIBLE)
                || (wantAlpha == 0 && mToolbar.getVisibility() == View.INVISIBLE)) return;
        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation;
        if (wantAlpha == 1) {
            alphaAnimation = new AlphaAnimation(0, 1);
        } else {
            alphaAnimation = new AlphaAnimation(1, 0);
        }
        alphaAnimation.setDuration(duration);
        animationSet.addAnimation(alphaAnimation);
        mToolbar.startAnimation(animationSet);
        alphaAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mToolbarBackground.setVisibility(View.VISIBLE);
                mBottomBarBackground.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (wantAlpha == 1) {
                    mToolbar.setVisibility(View.VISIBLE);
                } else {
                    mToolbar.setVisibility(View.INVISIBLE);
                    mToolbarBackground.setVisibility(View.INVISIBLE);
                    mBottomBarBackground.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.imageView:
                final int vis = mDecorView.getSystemUiVisibility();
                if ((vis & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0) {
                    mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                } else {
                    mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                }
                //if in spilt-screen,setSystemUiVisibility does not work!
                if(ScreenUtils.getScreenInfo(mContext) == ScreenUtils.tempScreenInLandSplit ||
                        ScreenUtils.getScreenInfo(mContext) == ScreenUtils.tempScreenInPortSplit){
                    if ((vis & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0) {
                         showSystemUi(true);
                         mToolbar.setVisibility(View.VISIBLE);
                         mBottomControls.getContainer().setVisibility(View.VISIBLE);
                    } else {
                        showSystemUi(false);
                        mToolbar.setVisibility(View.GONE);
                        mBottomControls.getContainer().setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.id_photoview_button:
                int index = mViewPager.getCurrentItem();
                String burstShotId = mAdapter.getBurstShotId(index);
                if (!TextUtils.isEmpty(burstShotId)) {
                    ArrayList<String> arrayList = ExifInfoFilter.getInstance(mContext).queryBurstShots(burstShotId);
                    if (arrayList != null) {
                        Intent intent = new Intent();
                        Bundle data = new Bundle();
                        data.putStringArrayList(BurstShotActivity.BURSTSHOTLIST, arrayList);
                        data.putString(BurstShotActivity.BURSTSHOTID, burstShotId);
                        intent.putExtras(data);
                        intent.setClass(mContext, BurstShotActivity.class);
                        mContext.startActivityForResult(intent, GalleryConstant.REQUEST_BURSTSHOT);
                    }
                }
                break;
            default:
                break;
        }
    }

    public void updateUI(MediaItem item) {
        updateActionBarTitle(item);
        updateMenuOperations(item);
        updateBottomControls(item);
    }

    private void updateBottomControls(MediaItem item){
        if(item == null){
            return;
        }
        mBottomControls.refresh();
        if(item instanceof LocalMediaItem){
            mBottomControls.invalidateFavourite(((LocalMediaItem) item).isFavorite());
        }
    }

    private void updateMenuOperations(MediaItem item) {
        if (item == null || mSlideShowItem == null || mSetAsItem == null || mPrintItem == null || mIsSingleItem) {
            return;
        }
        boolean isVisible = true;
        int type = item.getMediaType();
        if (type == MediaItem.MEDIA_TYPE_VIDEO) {
            isVisible = false;
        }
        mSlideShowItem.setVisible(isVisible);
        mSetAsItem.setVisible(isVisible);
        mPrintItem.setVisible(isVisible);
    }

    private void updateActionBarTitle(MediaItem item) {
        mToolbar.setTitle("");
        if (null != item) {
            String id = item.getPath().getSuffix();
            int type = ExifInfoFilter.getInstance(mContext).queryType(id);
            switch (type) {
                case ExifInfoFilter.BURSTSHOTS:
                    String burstShotId = ExifInfoFilter.getInstance(mContext).queryBurstShotId(id);
                    if (!TextUtils.isEmpty(burstShotId)) {
                        ArrayList<String> arrayList = ExifInfoFilter.getInstance(mContext).queryBurstShots(burstShotId);
                        if (arrayList != null) {
                            int count = arrayList.size();
                            String title = mContext.getResources().getQuantityString(R.plurals.number_of_photos, count, count);
                            mToolbar.setTitle(title);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
