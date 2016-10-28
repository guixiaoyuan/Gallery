package com.tct.gallery3d.collage.collageadapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.collage.puzzle.PuzzleLayout;
import com.tct.gallery3d.collage.puzzle.PuzzleUtil;
import com.tct.gallery3d.collage.puzzle.PuzzleView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Ricky on 2016/8/25.
 */
public class CollageAdapter extends BaseAdapter {

    private Context context;

    private List<PuzzleLayout> mLayoutData = new ArrayList<>();

    public CollageAdapter(Context context) {
        this.context = context;
    }

    private OnItemClickListener mOnItemClickListener;

    private boolean mNeedDrawBorder = false;

    private boolean mNeedDrawOuterBorder = false;

    private int mPieceSize;

    public int getmPieceSize() {
        return mPieceSize;
    }

    public void setmPieceSize(int mPieceSize) {
        this.mPieceSize = mPieceSize;
    }

    @Override
    public int getCount() {
        return mLayoutData.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void refreshData(List<PuzzleLayout> layoutData) {
        mLayoutData = layoutData;
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.puzzle_item, null);
            holder.mPuzzleImage = (ImageView) convertView.findViewById(R.id.tv_title_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        int resID = getResIdByCount(position);
        holder.mPuzzleImage.setBackgroundResource(resID);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });
        return convertView;
    }

    /**
     * get res id
     * @param position
     * @return
     */
    private int getResIdByCount(int position){
        Log.d("getResIdByCount","getResIdByCount" + position + "size= " + mPieceSize);
       switch (mPieceSize){
           case 2:
              return PuzzleUtil.resTwoPiecesIds[position];
           case 3:
              return PuzzleUtil.resThreePiecesIds[position];
           case 4:
               return PuzzleUtil.resFourPiecesIds[position];
           case 5:
               return PuzzleUtil.resFivePiecesIds[position];
           case 6:
               return PuzzleUtil.resSixPiecesIds[position];
           case 7:
               return PuzzleUtil.resSevenPiecesIds[position];
           case 8:
               return PuzzleUtil.resEightPiecesIds[position];
           case 9:
               return PuzzleUtil.resNinePiecesIds[position];

       }
       return -1;
    }
    private static class ViewHolder {
        private ImageView mPuzzleImage;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public boolean isNeedDrawBorder() {
        return mNeedDrawBorder;
    }

    public void setNeedDrawBorder(boolean needDrawBorder) {
        mNeedDrawBorder = needDrawBorder;
    }

    public boolean isNeedDrawOuterBorder() {
        return mNeedDrawOuterBorder;
    }

    public void setNeedDrawOuterBorder(boolean needDrawOuterBorder) {
        mNeedDrawOuterBorder = needDrawOuterBorder;
    }

    public interface OnItemClickListener {
        void onItemClick(int themeId);
    }
}
