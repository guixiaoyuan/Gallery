package com.tct.gallery3d.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.data.DataSourceType;
import com.tct.gallery3d.data.MediaSet;

public class AlbumItem extends SquareLayout {

    private ImageView mAlbumType;
    private TextView mAlbumName;
    private TextView mItemsCount;
    private AlbumView mAlbumCover;

    private int mSlotIndex;

    public int getSlotIndex() {
        return mSlotIndex;
    }

    public void setSlotIndex(int mSlotIndex) {
        this.mSlotIndex = mSlotIndex;
    }

    public AlbumItem(Context context) {
        this(context, null);
    }

    public AlbumItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.album_item_layout, this, true);
        mAlbumType = (ImageView) view.findViewById(R.id.album_type);
        mAlbumName = (TextView) view.findViewById(R.id.album_name);
        mItemsCount = (TextView) view.findViewById(R.id.album_item_count);
        mAlbumCover = (AlbumView) view.findViewById(R.id.album_cover);
    }

    private void setAlbumType(int type) {
        int res;
        switch (type) {
            case DataSourceType.ALBUM_CAMERA:
                res = R.drawable.ic_photo_camera;
                break;
            case DataSourceType.ALBUM_FACESHOW:
                res = R.drawable.ic_selfie;
                break;
            case DataSourceType.ALBUM_FAVORITE:
                res = R.drawable.ic_favorite;
                break;
            case DataSourceType.ALBUM_VIDEO:
                res = R.drawable.ic_video;
                break;
            case DataSourceType.ALBUH_SCREENSHOT:
                res = R.drawable.ic_screenshot;
                break;
            case DataSourceType.ALBUM_PRIVATE:
                res = R.drawable.ic_thumbnail_private;
                break;
            case DataSourceType.ALBUM_NORMAL:
            default:
                res = R.drawable.ic_folder;
        }
        mAlbumType.setImageResource(res);
    }

    private void setAlbumName(String name) {
        mAlbumName.setText(name);
    }

    public void setAlbumItemCount(int size) {
        mItemsCount.setText(String.valueOf(size));
    }

    public void initAlbumSet(MediaSet album) {
        if (album != null) {
            String name = album.getName();
            setAlbumName(name);
            int type = album.getAlbumType();
            setAlbumType(type);
        }
    }

    public AlbumView getCover() {
        return mAlbumCover;
    }
}
