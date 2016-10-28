package com.tct.gallery3d.collage.puzzle;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.tct.gallery3d.R;
import com.tct.gallery3d.app.NewAlbumSelectActivity;
import com.tct.gallery3d.collage.CollageProcessActivity;
import com.tct.gallery3d.collage.border.Border;
import com.tct.gallery3d.collage.border.BorderUtil;
import com.tct.gallery3d.collage.border.Line;
import com.tct.gallery3d.util.ScreenUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * the puzzle view , the number of puzzle piece due to PuzzleLayout
 *
 * @see PuzzleLayout
 * Created by liuxiaoyu on 16-10-10.
 */
public class PuzzleView extends View {
    private static final String TAG = "PuzzleView";

    private enum Mode {
        NONE,
        DRAG,
        ZOOM,
        MOVE,
        SWAP
    }

    private Mode mCurrentMode = Mode.NONE;

    private Paint mBitmapPaint;
    private Paint mOutBorderPaint;
    private Paint mInBorderPaint;
    private Paint mSelectedBorderPaint;

    private RectF mBorderRect;
    private RectF mSelectedRect;

    private PuzzleLayout mPuzzleLayout;

    private float mOutBorderWidth = 0;
    private float mInBorderWidth = 0;
    private float mExtraSize = 100;

    private float mDownX;
    private float mDownY;

    private float mOldDistance;
    private PointF mMidPoint;

    private List<PuzzlePiece> mPuzzlePieces = new ArrayList<>();

    private Line mHandlingLine;
    private PuzzlePiece mHandlingPiece;
    private PuzzlePiece mPreviewHandlingPiece;
    private PuzzlePiece mReplacePiece;

    private List<PuzzlePiece> mChangedPieces = new ArrayList<>();

    private boolean mNeedDrawBorder = false;
    private boolean mMoveLineEnable = true;
    private boolean mNeedDrawOuterBorder = false;

    private Handler mHandler;
    private Context context;
    private Drawable replaceIcon;
    private Bitmap replaceIconBitmap;

    private RectF replaceRect = new RectF();

    private Activity a;

    public PuzzleView(Context context) {
        this(context, null, 0);
    }

    public PuzzlePiece getHandingPiece() {
        return mHandlingPiece;
    }

    public PuzzleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PuzzleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;
        a = (CollageProcessActivity) context;
        replaceIcon = ContextCompat.getDrawable(context, R.drawable.ic_album_add);
        replaceIconBitmap = ((BitmapDrawable) replaceIcon).getBitmap().copy(Bitmap.Config.ARGB_8888, false);
        mBorderRect = new RectF();
        mSelectedRect = new RectF();

        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setFilterBitmap(true);

        mOutBorderPaint = new Paint();

        mOutBorderPaint.setAntiAlias(true);
        mOutBorderPaint.setColor(Color.WHITE);
        mOutBorderPaint.setStrokeWidth(mOutBorderWidth);

        mInBorderPaint = new Paint();
        mInBorderPaint.setAntiAlias(true);
        mInBorderPaint.setColor(Color.WHITE);
        mInBorderPaint.setStrokeWidth(mInBorderWidth);

        mSelectedBorderPaint = new Paint();

        mSelectedBorderPaint.setAntiAlias(true);
        mSelectedBorderPaint.setStyle(Paint.Style.STROKE);
        mSelectedBorderPaint.setColor(Color.parseColor("#00BCD4"));
        mSelectedBorderPaint.setStrokeWidth(8);

        mHandler = new Handler();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mPuzzleLayout == null || mPuzzleLayout.getBorderSize() == 0) {
            Log.e(TAG, "the puzzle layout or its border can not be null");
            return;
        }

        //draw piece
        for (int i = 0; i < mPuzzleLayout.getBorderSize(); i++) {
            Border border = mPuzzleLayout.getBorder(i);

            if (i >= mPuzzlePieces.size()) {
                break;
            }

            PuzzlePiece piece = mPuzzlePieces.get(i);
            canvas.save();
            canvas.clipRect(border.getRect());
            if (mPuzzlePieces.size() > i) {
                piece.draw(canvas, mBitmapPaint);
            }

            canvas.restore();
        }

        //draw divide line
        if (mNeedDrawBorder) {
            for (Line line : mPuzzleLayout.getLines()) {
                drawLine(canvas, line, true);
            }
        }

        //draw outer line
        if (mNeedDrawOuterBorder) {
            for (Line line : mPuzzleLayout.getOuterLines()) {
                drawLine(canvas, line, false);
            }
        }

        //draw selected border
        if (mHandlingPiece != null && mCurrentMode != Mode.SWAP) {
            drawSelectedBorder(canvas, mHandlingPiece);
        }

        if (mHandlingPiece != null && mCurrentMode != Mode.SWAP) {
            drawReplaceIcon(canvas);
        }

        //TODO
        if (mHandlingPiece != null && mCurrentMode == Mode.SWAP) {
            //mHandlingPiece.draw(canvas, mBitmapPaint, 128);
            mHandlingPiece.draw(canvas, mBitmapPaint, 128);
            if (mReplacePiece != null) {
                drawSelectedBorder(canvas, mReplacePiece);
            }
        }
    }

    /**
     * left is inBorder
     *
     * @return true inBorder
     */
    private boolean leftIsInLine() {
        if (mSelectedRect.left == 0) {
            return false;
        }
        return true;
    }

    /**
     * right is inBorder
     *
     * @return true inBorder
     */
    private boolean rightIsInLine() {
        if (mSelectedRect.right == ScreenUtils.getRealWidth(a)) {
            return false;
        }
        return true;
    }

    /**
     * TOP is inBorder
     *
     * @return true inBorder
     */
    private boolean topIsInLine() {
        if (mSelectedRect.top == 0) {
            return false;
        }
        return true;
    }

    /**
     * BOTTOM is inBorder
     *
     * @return true inBorder
     */
    private boolean bottomIsInLine() {
        if (mSelectedRect.bottom == ScreenUtils.getRealWidth(a)) {
            return false;
        }
        return true;
    }

    /**
     * draw replace Icon
     *
     * @param canvas
     */
    private void drawReplaceIcon(Canvas canvas) {
        canvas.save();
        float x = mSelectedRect.left + mSelectedRect.width() / 2 - replaceIconBitmap.getWidth() / 2;
        float y = mSelectedRect.top + mSelectedRect.height() - replaceIconBitmap.getHeight() / 2 - 120;

        replaceRect.left = x;
        replaceRect.right = replaceRect.left + replaceIconBitmap.getWidth();
        replaceRect.top = y;
        replaceRect.bottom = replaceRect.top + replaceIconBitmap.getHeight();

        canvas.drawRect(replaceRect, mSelectedBorderPaint);
        canvas.translate(x, y);
        canvas.drawBitmap(replaceIconBitmap, 0, 0, null);
        canvas.restore();
    }

    /**
     * draw Selected Border
     *
     * @param canvas
     * @param piece
     */
    private void drawSelectedBorder(Canvas canvas, PuzzlePiece piece) {
        mSelectedRect.set(piece.getBorder().getRect());
        Log.i("puzzle", " rect=" + mSelectedRect);
        if (leftIsInLine()) {
            mSelectedRect.left += mInBorderWidth / 2;
        } else {
            mSelectedRect.left += mOutBorderWidth / 2f;
        }
        if (rightIsInLine()) {
            mSelectedRect.right -= mInBorderWidth / 2;
        } else {
            mSelectedRect.right -= mOutBorderWidth / 2f;
        }
        if (topIsInLine()) {
            mSelectedRect.top += mInBorderWidth / 2;
        } else {
            mSelectedRect.top += mOutBorderWidth / 2f;
        }
        if (bottomIsInLine()) {
            mSelectedRect.bottom -= mInBorderWidth / 2;
        } else {
            mSelectedRect.bottom -= mOutBorderWidth / 2f;
        }

        canvas.drawRect(mSelectedRect, mSelectedBorderPaint);

        mSelectedBorderPaint.setStyle(Paint.Style.FILL);
     /*   for (Line line : piece.getBorder().getLines()) {
            if (mPuzzleLayout.getLines().contains(line)) {
                if (line.getDirection() == Line.Direction.HORIZONTAL) {
                    canvas.drawRoundRect(
                            line.getCenterBound(mSelectedRect.centerX(),
                                    mSelectedRect.width(),
                                    mBorderWidth,
                                    line == piece.getBorder().mLineTop),

                            mBorderWidth * 2,
                            mBorderWidth * 2,
                            mSelectedBorderPaint);
                } else if (line.getDirection() == Line.Direction.VERTICAL) {
                    canvas.drawRoundRect(
                            line.getCenterBound(mSelectedRect.centerY(),
                                    mSelectedRect.height(),
                                    mBorderWidth,
                                    line == piece.getBorder().mLineLeft),

                            mBorderWidth * 2,
                            mBorderWidth * 2,
                            mSelectedBorderPaint);
                }
            }
        }*/
        mSelectedBorderPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mMoveLineEnable) {
            return super.onTouchEvent(event);
        }

        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();

                mHandlingLine = findHandlingLine();

                if (mHandlingLine != null) {
                    mCurrentMode = Mode.MOVE;
                    mChangedPieces.clear();
                    mChangedPieces.addAll(findChangedPiece());

                    for (int i = 0; i < mChangedPieces.size(); i++) {
                        mChangedPieces.get(i).getDownMatrix().set(mChangedPieces.get(i).getMatrix());
                    }

                } else {
                    mHandlingPiece = findHandlingPiece();
                    if (mHandlingPiece != null) {
                        mCurrentMode = Mode.DRAG;
                        mHandlingPiece.getDownMatrix().set(mHandlingPiece.getMatrix());

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mCurrentMode = Mode.SWAP;
                                Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
                                vib.vibrate(80);
                                invalidate();
                                Log.d(TAG, "run: long pressed");
                            }
                        }, 1000);
                    }
                }
                if (replaceRect.contains(mDownX, mDownY)) {
                    Toast.makeText(context, "Click to swap photo", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, NewAlbumSelectActivity.class);
                    String filePath = null;
                    intent.putExtra(NewAlbumSelectActivity.TARGET_PATH, filePath);
                    a.startActivityForResult(intent, 99);
                    return true;
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:

                mOldDistance = calculateDistance(event);
                mMidPoint = calculateMidPoint(event);

                if (mHandlingPiece != null
                        && isInPhotoArea(mHandlingPiece, event.getX(1), event.getY(1))
                        && mCurrentMode != Mode.MOVE) {
                    mCurrentMode = Mode.ZOOM;
                }
                break;


            case MotionEvent.ACTION_MOVE:
                switch (mCurrentMode) {
                    case NONE:
                        break;
                    case DRAG:
                        dragPiece(mHandlingPiece, event);
                        break;
                    case ZOOM:
                        zoomPiece(mHandlingPiece, event);
                        break;
                    case MOVE:
                        moveLine(event);
                        mPuzzleLayout.update();
                        updatePieceInBorder(event);
                        break;

                    case SWAP:
                        mReplacePiece = findReplacePiece(event);
                        dragPiece(mHandlingPiece, event);
                        Log.d(TAG, "onTouchEvent: replace");
                        break;
                }

                if ((Math.abs(event.getX() - mDownX) > 10 || Math.abs(event.getY() - mDownY) > 10)
                        && mCurrentMode != Mode.SWAP) {
                    mHandler.removeCallbacksAndMessages(null);
                }

                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                mHandlingLine = null;
                switch (mCurrentMode) {
                    case DRAG:
                        if (!mHandlingPiece.isFilledBorder()) {
                            moveToFillBorder(mHandlingPiece);
                        }


                        if (mPreviewHandlingPiece == mHandlingPiece
                                && Math.abs(mDownX - event.getX()) < 3
                                && Math.abs(mDownY - event.getY()) < 3) {
                            //mHandlingPiece = null;
                        }

                        mPreviewHandlingPiece = mHandlingPiece;
                        break;
                    case ZOOM:
                        if (!mHandlingPiece.isFilledBorder()) {
                            fillBorder(mHandlingPiece);
                            mHandlingPiece.setScaleFactor(0f);
                        }
                        break;

                    case SWAP:
                        if (mHandlingPiece != null && mReplacePiece != null) {
                            Drawable temp = mHandlingPiece.getDrawable();

                            mHandlingPiece.setDrawable(mReplacePiece.getDrawable());
                            mReplacePiece.setDrawable(temp);
                            fillBorder(mHandlingPiece);
                            fillBorder(mReplacePiece);

                        }
                        fillBorder(mHandlingPiece);
                        mHandlingPiece = null;
                        mReplacePiece = null;
                        break;
                }

                mCurrentMode = Mode.NONE;

                mHandler.removeCallbacksAndMessages(null);
                invalidate();
                break;

            case MotionEvent.ACTION_POINTER_UP:

                break;
        }
        return true;

    }

    /**
     * zoom pircures
     *
     * @param piece
     * @param event
     */
    private void zoomPiece(PuzzlePiece piece, MotionEvent event) {
        if (piece != null && event.getPointerCount() >= 2) {
            float newDistance = calculateDistance(event);

            piece.getMatrix().set(piece.getDownMatrix());
            piece.getMatrix().postScale(
                    newDistance / mOldDistance, newDistance / mOldDistance, mMidPoint.x, mMidPoint.y);

            piece.setScaleFactor(piece.getMappedWidth() / piece.getWidth());
        }
    }

    /**
     * drag Piece
     *
     * @param piece
     * @param event
     */
    private void dragPiece(PuzzlePiece piece, MotionEvent event) {
        if (piece != null) {
            piece.getMatrix().set(piece.getDownMatrix());
            piece.getMatrix().postTranslate(event.getX() - mDownX, event.getY() - mDownY);

            piece.setTranslateX(piece.getMappedCenterPoint().x
                    - piece.getBorder().centerX());

            piece.setTranslateY(piece.getMappedCenterPoint().y
                    - piece.getBorder().centerY());
        }
    }

    /**
     * Fill Border
     *
     * @param piece
     */
    private void moveToFillBorder(PuzzlePiece piece) {
        Border border = piece.getBorder();
        RectF rectF = piece.getMappedBound();
        float offsetX = 0f;
        float offsetY = 0f;

        if (rectF.left > border.left()) {
            offsetX = border.left() - rectF.left;
        }

        if (rectF.top > border.top()) {
            offsetY = border.top() - rectF.top;
        }

        if (rectF.right < border.right()) {
            offsetX = border.right() - rectF.right;
        }

        if (rectF.bottom < border.bottom()) {
            offsetY = border.bottom() - rectF.bottom;
        }

        piece.getMatrix().postTranslate(offsetX, offsetY);

        piece.setTranslateX(border.centerX() - piece.getMappedCenterPoint().x);
        piece.setTranslateY(border.centerY() - piece.getMappedCenterPoint().y);

        if (!piece.isFilledBorder()) {
            fillBorder(piece);
        }
    }


    /**
     * let piece fill with its border
     *
     * @param piece puzzle piece which can not be null
     * @return the scale factor to fill with border
     */
    private void fillBorder(PuzzlePiece piece) {
        piece.getMatrix().reset();

        final RectF rectF = piece.getBorder().getRect();

        float offsetX = rectF.centerX() - piece.getWidth() / 2;
        float offsetY = rectF.centerY() - piece.getHeight() / 2;

        piece.getMatrix().postTranslate(offsetX, offsetY);
        float scale = calculateFillScaleFactor(piece);

        piece.getMatrix().postScale(scale, scale, rectF.centerX(), rectF.centerY());

        if (piece.getRotation() != 0) {
            rotate(piece, piece.getRotation(), false);
        }

        if (piece.isNeedHorizontalFlip()) {
            flipHorizontally(piece, false);
        }

        if (piece.isNeedVerticalFlip()) {
            flipVertically(piece, false);
        }


        piece.setTranslateX(0f);
        piece.setTranslateY(0f);
        piece.setScaleFactor(0f);
    }

    /**
     * calculate the Scale for filling border
     *
     * @param piece
     * @return
     */
    private float calculateFillScaleFactor(PuzzlePiece piece) {
        final RectF rectF = piece.getBorder().getRect();
        float scale;
        if (piece.getRotation() == 90 || piece.getRotation() == 270) {
            if (piece.getHeight() * rectF.height() > rectF.width() * piece.getWidth()) {
                scale = (rectF.height() + mExtraSize) / piece.getWidth();
            } else {
                scale = (rectF.width() + mExtraSize) / piece.getHeight();
            }
        } else {
            if (piece.getWidth() * rectF.height() > rectF.width() * piece.getHeight()) {
                scale = (rectF.height() + mExtraSize) / piece.getHeight();
            } else {
                scale = (rectF.width() + mExtraSize) / piece.getWidth();
            }
        }
        return scale;
    }

    /**
     * calculate Scale for filling borders
     *
     * @param piece
     * @param border
     * @return
     */
    private float calculateFillScaleFactor(PuzzlePiece piece, Border border) {
        final RectF rectF = border.getRect();
        float scale;
        if (piece.getWidth() * rectF.height() > rectF.width() * piece.getHeight()) {
            scale = rectF.height() / piece.getHeight();
        } else {
            scale = rectF.width() / piece.getWidth();
        }
        return scale;
    }


    //TODO
    private void updatePieceInBorder(MotionEvent event) {
        for (PuzzlePiece piece : mChangedPieces) {
            float scale = calculateFillScaleFactor(piece, mPuzzleLayout.getOuterBorder());

            if (piece.getScaleFactor() > scale && piece.isFilledBorder()) {
                piece.getMatrix().set(piece.getDownMatrix());

                if (mHandlingLine.getDirection() == Line.Direction.HORIZONTAL) {
                    piece.getMatrix().postTranslate(0, (event.getY() - mDownY) / 2);
                } else if (mHandlingLine.getDirection() == Line.Direction.VERTICAL) {
                    piece.getMatrix().postTranslate((event.getX() - mDownX) / 2, 0);
                }

            } else if (piece.isFilledBorder() && (piece.getTranslateX() != 0f || piece.getTranslateY() != 0f)) {
                piece.getMatrix().set(piece.getDownMatrix());

                if (mHandlingLine.getDirection() == Line.Direction.HORIZONTAL) {
                    piece.getMatrix().postTranslate(0, (event.getY() - mDownY) / 2);
                } else if (mHandlingLine.getDirection() == Line.Direction.VERTICAL) {
                    piece.getMatrix().postTranslate((event.getX() - mDownX) / 2, 0);
                }

            } else
                fillBorder(piece);
        }
    }

    /**
     * we can move lines to change the size of borders
     *
     * @param event
     */
    private void moveLine(MotionEvent event) {
        if (mHandlingLine == null) {
            return;
        }

        if (mHandlingLine.getDirection() == Line.Direction.HORIZONTAL) {
            mHandlingLine.moveTo(event.getY(), 40);
        } else if (mHandlingLine.getDirection() == Line.Direction.VERTICAL) {
            mHandlingLine.moveTo(event.getX(), 40);
        }


    }

    /**
     * find temp-handle piece
     *
     * @return
     */
    private List<PuzzlePiece> findChangedPiece() {
        if (mHandlingLine == null) return new ArrayList<>();

        List<PuzzlePiece> puzzlePieces = new ArrayList<>();

        for (PuzzlePiece piece : mPuzzlePieces) {
            if (piece.getBorder().contains(mHandlingLine)) {
                puzzlePieces.add(piece);
            }
        }

        return puzzlePieces;
    }

    private Line findHandlingLine() {
        for (Line line : mPuzzleLayout.getLines()) {
            if (line.contains(mDownX, mDownY, 20)) {
                return line;
            }
        }
        return null;
    }

    private PuzzlePiece findHandlingPiece() {
        for (PuzzlePiece piece : mPuzzlePieces) {
            if (piece.contains(mDownX, mDownY)) {
                return piece;
            }
        }
        return null;
    }

    private PuzzlePiece findReplacePiece(MotionEvent event) {
        for (PuzzlePiece piece : mPuzzlePieces) {
            if (piece.contains(event.getX(), event.getY())
                    && piece != mHandlingPiece) {
                return piece;
            }
        }
        return null;
    }

    private boolean isInPhotoArea(PuzzlePiece handlingPhoto, float x, float y) {
        return handlingPhoto.contains(x, y);
    }

    private float calculateDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * calculate between two fingers
     * @param event
     * @return
     */
    private PointF calculateMidPoint(MotionEvent event) {
        float x = (event.getX(0) + event.getX(1)) / 2;
        float y = (event.getY(0) + event.getY(1)) / 2;
        return new PointF(x, y);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBorderRect.left = getPaddingLeft();
        mBorderRect.top = getPaddingTop();
        mBorderRect.right = w - getPaddingRight();
        mBorderRect.bottom = h - getPaddingBottom();

        if (mPuzzleLayout != null) {
            mPuzzleLayout.setOuterBorder(mBorderRect);
            mPuzzleLayout.layout();
        }

        if (mPuzzlePieces.size() != 0) {
            for (int i = 0; i < mPuzzlePieces.size(); i++) {
                PuzzlePiece piece = mPuzzlePieces.get(i);
                piece.setBorder(mPuzzleLayout.getBorder(i));
                piece.getMatrix().set(
                        BorderUtil.createMatrix(mPuzzleLayout.getBorder(i), piece.getWidth(), piece.getHeight(), mExtraSize));
            }
        }

        invalidate();

    }

    /**
     * replace bitmap
     * @param bitmap
     */
    public void replace(Bitmap bitmap) {
        ;
        replace(new BitmapDrawable(getResources(), bitmap));
    }

    public void replace(Drawable bitmapDrawable) {
        if (mHandlingPiece == null) {
            return;
        }
        mHandlingPiece.setDrawable(bitmapDrawable);
        fillBorder(mHandlingPiece);

        invalidate();
    }


    public void flipHorizontally() {
        flipHorizontally(mHandlingPiece, true);
    }

    public void flipVertically() {
        flipVertically(mHandlingPiece, true);
    }

    private void flipHorizontally(PuzzlePiece piece, boolean needChangeStatus) {
        if (piece == null) return;
        if (needChangeStatus) {
            piece.setNeedHorizontalFlip(!piece.isNeedHorizontalFlip());
        }
        piece.getMatrix().postScale(-1, 1, piece.getMappedCenterPoint().x, piece.getMappedCenterPoint().y);

        invalidate();
    }

    private void flipVertically(PuzzlePiece piece, boolean needChangeStatus) {
        if (piece == null) return;
        if (needChangeStatus) {
            piece.setNeedVerticalFlip(!piece.isNeedVerticalFlip());
        }


        piece.getMatrix().postScale(1, -1, piece.getMappedCenterPoint().x, piece.getMappedCenterPoint().y);

        invalidate();
    }

    /**
     * rotate mHandlingPiece
     * @param rotate
     */
    public void rotate(float rotate) {
        rotate(mHandlingPiece, rotate, true);
    }

    private void rotate(PuzzlePiece piece, float rotate, boolean needChangeStatus) {
        if (piece == null) return;
        if (needChangeStatus) {
            piece.setRotation((piece.getRotation() + rotate) % 360f);
        }

        if (needChangeStatus) {
            piece.getMatrix().postRotate(rotate, piece.getMappedCenterPoint().x, piece.getMappedCenterPoint().y);
            fillBorder(piece);
        } else {
            piece.getMatrix().postRotate(piece.getRotation(), piece.getMappedCenterPoint().x, piece.getMappedCenterPoint().y);
        }

        invalidate();
    }

    /**
     * add Piece to puzzlelayout
     * @param bitmap
     */
    public void addPiece(final Bitmap bitmap) {
        Log.d("addPiece", "addPiece");
        addPiece(new BitmapDrawable(getResources(), bitmap));
    }

    public void addPieces(final List<Bitmap> bitmaps) {
        for (Bitmap bitmap : bitmaps) {
            addPiece(bitmap);
        }

        invalidate();
    }


    public void addPiece(final Drawable drawable) {
        int index = mPuzzlePieces.size();

        if (index >= mPuzzleLayout.getBorderSize()) {
            Log.e(TAG, "addPiece: can not add more. the current puzzle layout can contains "
                    + mPuzzleLayout.getBorderSize() + " puzzle piece.");
            return;
        }

        Matrix matrix = BorderUtil.createMatrix(mPuzzleLayout.getBorder(index), drawable, mExtraSize);

        PuzzlePiece layoutPhoto = new PuzzlePiece(drawable, mPuzzleLayout.getBorder(index), matrix);
        mPuzzlePieces.add(layoutPhoto);

        invalidate();
    }


    private void drawLine(Canvas canvas, Line line, boolean isInLine) {
        if (isInLine) {
            canvas.drawLine(line.start.x, line.start.y, line.end.x, line.end.y, mInBorderPaint);
        } else {
            canvas.drawLine(line.start.x, line.start.y, line.end.x, line.end.y, mOutBorderPaint);
        }

    }

    public void reset() {
        mHandlingLine = null;
        mHandlingPiece = null;

        if (mPuzzleLayout != null) {
            mPuzzleLayout.reset();
        }
        mPuzzlePieces.clear();
        mChangedPieces.clear();

        invalidate();
    }

    public PuzzleLayout getPuzzleLayout() {
        return mPuzzleLayout;
    }

    public void setPuzzleLayout(PuzzleLayout puzzleLayout) {
        reset();

        mPuzzleLayout = puzzleLayout;
        mPuzzleLayout.setOuterBorder(mBorderRect);
        mPuzzleLayout.layout();

        invalidate();
    }

    public float getOutBorderWidth() {
        return mOutBorderWidth;
    }

    public float getInBorderWidth() {
        return mInBorderWidth;
    }

    /**
     *set OutBorder Width
     * @param borderWidth
     */
    public void setOutBorderWidth(float borderWidth) {
        mOutBorderWidth = borderWidth;
        mOutBorderPaint.setStrokeWidth(borderWidth);
        invalidate();
    }

    /**
     * set InBorder Width
     * @param borderWidth
     */
    public void setInBorderWidth(float borderWidth) {
        mInBorderWidth = borderWidth;
        mInBorderPaint.setStrokeWidth(mInBorderWidth);
        invalidate();
    }

    public boolean isNeedDrawBorder() {
        return mNeedDrawBorder;
    }

    public void setNeedDrawBorder(boolean needDrawBorder) {
        mNeedDrawBorder = needDrawBorder;
        mHandlingPiece = null;
        mPreviewHandlingPiece = null;
        invalidate();
    }

    public boolean isMoveLineEnable() {
        return mMoveLineEnable;
    }

    public void setMoveLineEnable(boolean moveLineEnable) {
        mMoveLineEnable = moveLineEnable;
    }

    public float getExtraSize() {
        return mExtraSize;
    }

    public void setExtraSize(float extraSize) {
        if (extraSize < 0) {
            Log.e(TAG, "setExtraSize: the extra size must be greater than 0");
            mExtraSize = 0;
        } else {
            mExtraSize = extraSize;
        }
    }

    public boolean isNeedDrawOuterBorder() {
        return mNeedDrawOuterBorder;
    }

    public void setNeedDrawOuterBorder(boolean needDrawOuterBorder) {
        mNeedDrawOuterBorder = needDrawOuterBorder;
    }

    public void setBorderColor(@ColorInt int color) {
        mOutBorderPaint.setColor(color);
        invalidate();
    }

    public void setSelectedBorderColor(@ColorInt int color) {
        mSelectedBorderPaint.setColor(color);
        invalidate();
    }


    public Bitmap createBitmap() {
        mHandlingPiece = null;

        invalidate();

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);

        return bitmap;
    }

    public void save(File file) {
        save(file, 100, null);
    }

    public void save(File file, Callback callback) {
        save(file, 100, callback);
    }

    /**
     * save file and notify
     * @param file
     * @param quality
     * @param callback
     */
    public void save(File file, int quality, Callback callback) {
        Bitmap bitmap = null;
        FileOutputStream outputStream = null;

        try {
            bitmap = createBitmap();
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

            if (!file.exists()) {
                Log.e(TAG, "notifySystemGallery: the file do not exist.");
                return;
            }

            try {
                MediaStore.Images.Media.insertImage(getContext().getContentResolver(),
                        file.getAbsolutePath(), file.getName(), null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

            if (callback != null) {
                callback.onSuccess();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onFailed();
            }
        } finally {
            if (bitmap != null) {
                bitmap.recycle();
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public interface Callback {
        void onSuccess();

        void onFailed();
    }

    public interface OnPieceSelectedListener {
        void onPieceSelected(PuzzlePiece piece);
    }
}
