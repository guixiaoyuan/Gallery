package com.tct.gallery3d.collage.border;

import android.graphics.PointF;
import android.graphics.RectF;

import com.tct.gallery3d.collage.puzzle.PuzzlePiece;

import java.util.Arrays;
import java.util.List;

/**
 * the border to layout puzzle piece
 *
 * @see PuzzlePiece
 * each border consist of four lines : left,top,right,bottom
 * @see Line
 * <p>
 * Created by liuxiaoyu on 16-10-10.
 */
public class Border {
    public Line mLineLeft;
    public Line mLineTop;
    public Line mLineRight;
    public Line mLineBottom;

    public Border(Border src) {
        mLineLeft = src.mLineLeft;
        mLineTop = src.mLineTop;
        mLineRight = src.mLineRight;
        mLineBottom = src.mLineBottom;
    }

    public Border(RectF baseRect) {
        setBaseRect(baseRect);
    }

    private void setBaseRect(RectF baseRect) {
        float width = baseRect.width();
        float height = baseRect.height();

        PointF one = new PointF(0, 0);
        PointF two = new PointF(width, 0);
        PointF three = new PointF(0, height);
        PointF four = new PointF(width, height);

        mLineLeft = new Line(one, three);
        mLineTop = new Line(one, two);
        mLineRight = new Line(two, four);
        mLineBottom = new Line(three, four);
    }

    public float width() {
        return mLineRight.start.x - mLineLeft.start.x;
    }


    public float height() {
        return mLineBottom.start.y - mLineTop.start.y;
    }

    public float left() {
        return mLineLeft.start.x;
    }

    public float top() {
        return mLineTop.start.y;
    }

    public float right() {
        return mLineRight.start.x;
    }

    public float bottom() {
        return mLineBottom.start.y;
    }

    public float centerX() {
        return right() - left();
    }

    public float centerY() {
        return bottom() - top();
    }

    List<Line> getLines() {
        return Arrays.asList(mLineLeft, mLineTop, mLineRight, mLineBottom);
    }

    public RectF getRect() {
        return new RectF(
                left(),
                top(),
                right(),
                bottom());
    }

    public boolean contains(Line line) {
        return mLineLeft == line || mLineTop == line || mLineRight == line || mLineBottom == line;
    }


    @Override
    public String toString() {
        return "left line:\n" +
                mLineLeft.toString() +
                "\ntop line:\n" +
                mLineTop.toString() +
                "\nright line:\n" +
                mLineRight.toString() +
                "\nbottom line:\n" +
                mLineBottom.toString() +
                "\nthe rect is \n" +
                getRect().toString();
    }
}
