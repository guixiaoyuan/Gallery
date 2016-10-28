package com.tct.gallery3d.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tct.gallery3d.R;

public class CollapseAlbumItem extends SquareLayout {


    private ImageView mCollapseAlbumType;
    private TextView mCollapseAlbumName;
    private TextView mCollapseItemsCount;
    private AlbumView mCollapseAlbumFirstCover;
    private AlbumView mCollapseAlbumSecondCover;
    private AlbumView mCollapseAlbumThirdCover;
    private AlbumView mCollapseAlbumForthCover;

    public static final int FIRST_COVER = 0;
    public static final int SECOND_COVER = 1;
    public static final int THIRD_COVER = 2;
    public static final int FORTH_COVER = 3;


    private int mSlotIndex;

    public int getSlotIndex() {
        return mSlotIndex;
    }

    public void setSlotIndex(int mSlotIndex) {
        this.mSlotIndex = mSlotIndex;
    }

    public CollapseAlbumItem(Context context) {
        this(context, null);
    }

    public CollapseAlbumItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_collapse_album_layout, this, true);
        mCollapseAlbumType = (ImageView) view.findViewById(R.id.collapse_album_type);
        mCollapseAlbumName = (TextView) view.findViewById(R.id.collapse_album_name);
        mCollapseItemsCount = (TextView) view.findViewById(R.id.collapse_album_item_count);
        mCollapseAlbumFirstCover = (AlbumView) view.findViewById(R.id.collapse_album_cover_first);
        mCollapseAlbumSecondCover = (AlbumView) view.findViewById(R.id.collapse_album_cover_second);
        mCollapseAlbumThirdCover = (AlbumView) view.findViewById(R.id.collapse_album_cover_third);
        mCollapseAlbumForthCover = (AlbumView) view.findViewById(R.id.collapse_album_cover_forth);
    }

    private void setAlbumType() {
        mCollapseAlbumType.setImageResource(R.drawable.ic_folder);
    }

    private void setAlbumName() {
        mCollapseAlbumName.setText(R.string.collapse_albums);
    }

    public void setAlbumItemCount(int size) {
        mCollapseItemsCount.setText(String.valueOf(size));
    }

    public void initAlbumSet() {
        setAlbumName();
        setAlbumType();
    }

    public AlbumView getCover(int position) {
        switch (position) {
            case FIRST_COVER:
                return mCollapseAlbumFirstCover;
            case SECOND_COVER:
                return mCollapseAlbumSecondCover;
            case THIRD_COVER:
                return mCollapseAlbumThirdCover;
            case FORTH_COVER:
                return mCollapseAlbumForthCover;
            default:
                return null;
        }
    }

    public void resetCover(int position) {
        switch (position) {
            case FIRST_COVER:
                mCollapseAlbumFirstCover.setImageDrawable(null);
                break;
            case SECOND_COVER:
                mCollapseAlbumSecondCover.setImageDrawable(null);
                break;
            case THIRD_COVER:
                mCollapseAlbumThirdCover.setImageDrawable(null);
                break;
            case FORTH_COVER:
                mCollapseAlbumForthCover.setImageDrawable(null);
                break;
            default:
                break;
        }
    }
}
