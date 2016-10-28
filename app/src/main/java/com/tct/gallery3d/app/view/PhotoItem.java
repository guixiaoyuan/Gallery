package com.tct.gallery3d.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.GalleryAppImpl;
import com.tct.gallery3d.app.constant.GalleryConstant;
import com.tct.gallery3d.data.MediaDetails;
import com.tct.gallery3d.data.MediaItem;
import com.tct.gallery3d.picturegrouping.ExifInfoFilter;

public class PhotoItem extends SquareLayout {

    private RecyclingImageView mContent;
    private Context mContext;

    public TextView mVideoTime;
    private ImageView mItemType;
    private ImageView mItemSelected;
    public RelativeLayout mShadowLayout;
    private TextView mPosition;

    private ImageView mItemPrivate;

    private int mSlotIndex = INVALID_INDEX;
    private int mInnerIndex = INVALID_INDEX;
    private MediaItem mMediaItem;
    private int mIndex;

    private static final int INVALID_INDEX = -1;
    private static final int DEFAULT_RES_ID = 0;

    public PhotoItem(Context context) {
        this(context, null);
    }

    public PhotoItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.photo_item_layout, this, true);
        mContent = (RecyclingImageView) view.findViewById(R.id.content);
        mItemType = (ImageView) view.findViewById(R.id.item_type);
        mVideoTime = (TextView) view.findViewById(R.id.video_time);
        mShadowLayout = (RelativeLayout) view.findViewById(R.id.shadow_layout);

        mItemPrivate = (ImageView) view.findViewById(R.id.item_private);

        mItemSelected = (ImageView) view.findViewById(R.id.item_selected);
        mItemSelected.setEnabled(false);
        mPosition = (TextView) findViewById(R.id.position);
        if (GalleryConstant.DEBUG) {
            mPosition.setVisibility(VISIBLE);
        }
    }

    public void showSelected(boolean visible) {
        mItemSelected.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setSelected(boolean selected) {
        mItemSelected.setEnabled(selected);
    }

    public int getSlotIndex() {
        return mSlotIndex;
    }

    public void setSlotIndex(int slotIndex) {
        mSlotIndex = slotIndex;
    }

    public int getInnerIndex() {
        return mInnerIndex;
    }

    public void setInnerIndex(int innerIndex) {
        mInnerIndex = innerIndex;
    }

    public MediaItem getMediaItem() {
        return mMediaItem;
    }

    public void setMediaItem(MediaItem item) {
        this.mMediaItem = item;
        setItemType(item);
        setItemPrivate(item);
    }

    public RecyclingImageView getContent() {
        return mContent;
    }

    private void setItemPrivate(MediaItem item) {
        if (item.isPrivate() == GalleryConstant.PRIVATE_ITEM && GalleryAppImpl.getTctPrivacyModeHelperInstance(mContext).isInPrivacyMode()) {
            mItemPrivate.setVisibility(View.VISIBLE);
        } else {
            mItemPrivate.setVisibility(View.GONE);
        }
    }

    private void setItemType(MediaItem item) {
        String id = item.getPath().getSuffix();
        int type;
        type = ExifInfoFilter.getInstance(getContext()).queryType(id);
        if (type == ExifInfoFilter.NONE && item.getMediaType() == MediaItem.MEDIA_TYPE_VIDEO) {
            type = MediaItem.MEDIA_TYPE_VIDEO;
        }
        int resId = DEFAULT_RES_ID;
        String time = null;
        switch (type) {
            case ExifInfoFilter.PARALLAX:
                resId = R.drawable.picto_parallax;
                break;
            case ExifInfoFilter.PANORAMA:
                resId = R.drawable.ic_thumbnail_panorama;
                break;
            case ExifInfoFilter.NORMALVIDEO:
            case ExifInfoFilter.MICROVIDEO:
                resId = R.drawable.ic_thumbnail_video;
                time = (String) item.getDetails().getDetail(MediaDetails.INDEX_DURATION);
                break;
            case ExifInfoFilter.SLOWMOTION:
                resId = R.drawable.ic_thumbnail_slo_mo;
                time = (String) item.getDetails().getDetail(MediaDetails.INDEX_DURATION);
                break;
            case ExifInfoFilter.FACESHOW:
                resId = R.drawable.ic_face;
                break;
            case ExifInfoFilter.BURSTSHOTS:
                resId = R.drawable.ic_thumbnail_burst;
                break;
            case ExifInfoFilter.BURSTSHOTSHIDDEN: // MODIFIED by Yaoyu.Yang, 2016-07-18,BUG-2208330
            case ExifInfoFilter.NONE:
            case ExifInfoFilter.NORMAL:
            default:
                break;
        }
        if (resId == DEFAULT_RES_ID) {
            mItemType.setVisibility(View.GONE);
            mShadowLayout.setVisibility(View.GONE);
        } else {
            mShadowLayout.setVisibility(View.VISIBLE);
            mItemType.setVisibility(View.VISIBLE);
            mItemType.setImageResource(resId);
        }
        if (time == null) {
            mVideoTime.setVisibility(View.GONE);
        } else {
            mVideoTime.setVisibility(View.VISIBLE);
            mVideoTime.setText(time);
        }
    }

    public void reset() {
        mContent.setImageDrawable(null);
        mVideoTime.setVisibility(View.GONE);
        mItemType.setVisibility(View.GONE);
        mItemSelected.setVisibility(View.GONE);
        mItemPrivate.setVisibility(View.GONE);
        mMediaItem = null;
        mSlotIndex = INVALID_INDEX;
        mInnerIndex = INVALID_INDEX;
        mIndex = INVALID_INDEX;
    }

    public void setPosition(String position) {
        mPosition.setText(position);
    }

    public int getPosition() {
        return mIndex;
    }

    public void setPosition(int position) {
        mIndex = position;
        setPosition("" + position);
    }
}
