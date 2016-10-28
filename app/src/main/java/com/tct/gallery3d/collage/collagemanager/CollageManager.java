package com.tct.gallery3d.collage.collagemanager;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.tct.gallery3d.app.AbstractGalleryActivity;
import com.tct.gallery3d.app.Log;
import com.tct.gallery3d.collage.FileUtil;
import com.tct.gallery3d.collage.puzzle.PuzzleLayout;
import com.tct.gallery3d.collage.puzzle.PuzzleLayoutHelper;
import com.tct.gallery3d.collage.puzzle.PuzzleUtil;
import com.tct.gallery3d.collage.puzzle.PuzzleView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by liuxiaoyu on 16-10-10.
 */
public class CollageManager implements CollageInterface {

    private AbstractGalleryActivity mContext;
    private int pieceSize = 0;
    private PuzzleLayout mPuzzleLayout;
    private List<String> mPhotoPaths;
    private PuzzleView mPuzzleView;
    private List<Bitmap> pieces = new ArrayList<>();


    public CollageManager(AbstractGalleryActivity context,PuzzleView puzzleView) {
        this.mContext = context;
        this.mPuzzleView = puzzleView;
    }

    @Override
    public List<PuzzleLayout> getTemplatesByCount(int count) {
        List<PuzzleLayout> puzzleLayouts = new ArrayList<>();
        puzzleLayouts.addAll(PuzzleLayoutHelper.getAllThemeLayout(count));
        return puzzleLayouts;

    }

    @Override
    public void loadSelectPhotos() {
        final int count = mPhotoPaths.size() > mPuzzleLayout.getBorderSize() ?
                mPuzzleLayout.getBorderSize() : mPhotoPaths.size();
        for (int i = 0; i < count; i++) {
          final String temp_path = mPhotoPaths.get(i);
            Glide.with(mContext.getApplicationContext()).load(mPhotoPaths.get(i))
                    .asBitmap()
                    .override(540,540)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                    pieces.add(bitmap);
                    if (pieces.size() == count) {
                        if (mPhotoPaths.size() < mPuzzleLayout.getBorderSize()) {
                            for (int i = 0; i < mPuzzleLayout.getBorderSize(); i++) {
                                mPuzzleView.addPiece(pieces.get(i % count));
                            }
                        } else {
                            mPuzzleView.addPieces(pieces);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void addOutSideBorderWidth() {
        if (mPuzzleView.getOutBorderWidth() < 200) {
            mPuzzleView.setOutBorderWidth(mPuzzleView.getOutBorderWidth()+10);

        }
    }

    @Override
    public void reduceOutSideBorderWidth() {
        if (mPuzzleView.getOutBorderWidth() > 20) {
            mPuzzleView.setOutBorderWidth(mPuzzleView.getOutBorderWidth()-10);

        }
    }

    @Override
    public void reduceInSideBorderWidth() {
        if (mPuzzleView.getInBorderWidth() > 10) {
            mPuzzleView.setInBorderWidth(mPuzzleView.getInBorderWidth()-10);

        }
    }

    @Override
    public void addInsideBorderWidth() {
        if (mPuzzleView.getInBorderWidth() < 100) {
            mPuzzleView.setInBorderWidth(mPuzzleView.getInBorderWidth()+10);
        }
    }

    @Override
    public void setPieceAndPath(int pieceSize,List<String> mPhotoPaths) {
        this.pieceSize = pieceSize;
        this.mPhotoPaths = mPhotoPaths;
    }

    @Override
    public void saveFile() {
        File file = FileUtil.getNewFile(mContext, "Puzzle");
        mPuzzleView.save(file, new PuzzleView.Callback() {
            @Override
            public void onSuccess() {
                Toast.makeText(mContext, "保存成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                Toast.makeText(mContext, "保存失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void swapImage(String path) {
        Glide.with(mContext.getApplicationContext()).load(path)
                .asBitmap()
                .override(540,540)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                         mPuzzleView.replace(bitmap);
                    }
                });
    }

    @Override
    public void zoomImage() {

    }

    @Override
    public void changePuzzleLayout(int themeId) {
        mPuzzleLayout = PuzzleUtil.getPuzzleLayout(pieceSize, themeId);
        mPuzzleView.setPuzzleLayout(mPuzzleLayout);

        if (pieces.size() > 0) {
            for (int i = 0; i < pieces.size(); i++) {
                mPuzzleView.addPiece(pieces.get(i));
            }
        }
    }

    @Override
    public void setPuzzleLayout() {
        mPuzzleLayout = PuzzleUtil.getPuzzleLayout(pieceSize, 0);
        mPuzzleView.setPuzzleLayout(mPuzzleLayout);
        mPuzzleView.setMoveLineEnable(true);
        mPuzzleView.setNeedDrawBorder(true);
        mPuzzleView.setNeedDrawOuterBorder(true);
        mPuzzleView.setExtraSize(100);
        mPuzzleView.setOutBorderWidth(20);
        mPuzzleView.setInBorderWidth(10);
        mPuzzleView.setBorderColor(Color.WHITE);
        mPuzzleView.setSelectedBorderColor(Color.parseColor("#99BBFB"));
    }
}
