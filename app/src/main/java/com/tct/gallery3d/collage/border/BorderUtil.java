package com.tct.gallery3d.collage.border;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 *Created by liuxiaoyu on 16-10-10.
 */
public class BorderUtil {
    private static final String TAG = "BorderUtil";

    /**
     * create Line by Border
     * @param border
     * @param direction
     * @param ratio
     * @return
     */
    public static Line createLine(final Border border, final Line.Direction direction, final float ratio) {
        PointF one = new PointF();
        PointF two = new PointF();
        if (direction == Line.Direction.HORIZONTAL) {
            one.x = border.left();
            one.y = border.height() * ratio + border.top();
            two.x = border.right();
            two.y = border.height() * ratio + border.top();
        } else if (direction == Line.Direction.VERTICAL) {
            one.x = border.width() * ratio + border.left();
            one.y = border.top();
            two.x = border.width() * ratio + border.left();
            two.y = border.bottom();
        }

        Line line = new Line(one, two);

        if (direction == Line.Direction.HORIZONTAL) {
            line.setAttachLineStart(border.mLineLeft);
            line.setAttachLineEnd(border.mLineRight);

            line.setUpperLine(border.mLineBottom);
            line.setLowerLine(border.mLineTop);

        } else if (direction == Line.Direction.VERTICAL) {
            line.setAttachLineStart(border.mLineTop);
            line.setAttachLineEnd(border.mLineBottom);

            line.setUpperLine(border.mLineRight);
            line.setLowerLine(border.mLineLeft);
        }

        return line;
    }

    /**
     * cut a single border to a double Border according to a line
     * @param border
     * @param line
     * @return
     */
    public static List<Border> cutBorder(final Border border, final Line line) {
        List<Border> list = new ArrayList<>();
        if (line.getDirection() == Line.Direction.HORIZONTAL) {
            Border one = new Border(border);
            one.mLineBottom = line;
            list.add(one);

            Border two = new Border(border);
            two.mLineTop = line;
            list.add(two);
        } else if (line.getDirection() == Line.Direction.VERTICAL) {
            Border one = new Border(border);
            one.mLineRight = line;
            list.add(one);

            Border two = new Border(border);
            two.mLineLeft = line;
            list.add(two);
        }

        return list;
    }

    /**
     * use 3 line to cut a border,so we will get six little borders
     * @param border
     * @param l1
     * @param l2
     * @param l3
     * @param direction
     * @return
     */
    public  static List<Border> cutBorder(final Border border, final Line l1, final Line l2, final Line l3, Line.Direction direction) {
        List<Border> list = new ArrayList<>();
        if (direction == Line.Direction.HORIZONTAL) {
            Border one = new Border(border);
            one.mLineRight = l3;
            one.mLineBottom = l1;
            list.add(one);

            Border two = new Border(border);
            two.mLineLeft = l3;
            two.mLineBottom = l1;
            list.add(two);

            Border three = new Border(border);
            three.mLineRight = l3;
            three.mLineTop = l1;
            three.mLineBottom = l2;
            list.add(three);

            Border four = new Border(border);
            four.mLineLeft = l3;
            four.mLineTop = l1;
            four.mLineBottom = l2;
            list.add(four);

            Border five = new Border(border);
            five.mLineRight = l3;
            five.mLineTop = l2;
            list.add(five);

            Border six = new Border(border);
            six.mLineLeft = l3;
            six.mLineTop = l2;
            list.add(six);
        } else if (direction == Line.Direction.VERTICAL) {

            Border one = new Border(border);
            one.mLineRight = l1;
            one.mLineBottom = l3;
            list.add(one);

            Border two = new Border(border);
            two.mLineLeft = l1;
            two.mLineBottom = l3;
            two.mLineRight = l2;
            list.add(two);

            Border three = new Border(border);
            three.mLineLeft = l2;
            three.mLineBottom = l3;
            list.add(three);

            Border four = new Border(border);
            four.mLineRight = l1;
            four.mLineTop = l3;
            list.add(four);

            Border five = new Border(border);
            five.mLineLeft = l1;
            five.mLineRight = l2;
            five.mLineTop = l3;
            list.add(five);

            Border six = new Border(border);
            six.mLineLeft = l2;
            six.mLineTop = l3;
            list.add(six);
        }

        return list;
    }

    /**
     *
     * @param border
     * @param l1
     * @param l2
     * @param l3
     * @param l4
     * @param direction
     * @return
     */
    public static List<Border> cutBorder(final Border border, final Line l1, final Line l2, final Line l3, final Line l4, Line.Direction direction) {
        List<Border> list = new ArrayList<>();
        if (direction == Line.Direction.HORIZONTAL) {

            Border one = new Border(border);
            one.mLineRight = l4;
            one.mLineBottom = l1;
            list.add(one);

            Border two = new Border(border);
            two.mLineLeft = l4;
            two.mLineBottom = l1;
            list.add(two);

            Border three = new Border(border);
            three.mLineRight = l4;
            three.mLineTop = l1;
            three.mLineBottom = l2;
            list.add(three);

            Border four = new Border(border);
            four.mLineLeft = l4;
            four.mLineTop = l1;
            four.mLineBottom = l2;
            list.add(four);

            Border five = new Border(border);
            five.mLineRight = l4;
            five.mLineTop = l2;
            five.mLineBottom = l3;
            list.add(five);

            Border six = new Border(border);
            six.mLineLeft = l4;
            six.mLineTop = l2;
            six.mLineBottom = l3;
            list.add(six);

            Border seven = new Border(border);
            seven.mLineRight = l4;
            seven.mLineTop = l3;
            list.add(seven);

            Border eight = new Border(border);
            eight.mLineLeft = l4;
            eight.mLineTop = l3;
            list.add(eight);

        } else if (direction == Line.Direction.VERTICAL) {

            Border one = new Border(border);
            one.mLineRight = l1;
            one.mLineBottom = l4;
            list.add(one);

            Border two = new Border(border);
            two.mLineLeft = l1;
            two.mLineBottom = l4;
            two.mLineRight = l2;
            list.add(two);

            Border three = new Border(border);
            three.mLineLeft = l2;
            three.mLineRight = l3;
            three.mLineBottom = l4;
            list.add(three);

            Border four = new Border(border);
            four.mLineLeft = l3;
            four.mLineBottom = l4;
            list.add(four);

            Border five = new Border(border);
            five.mLineRight = l1;
            five.mLineTop = l4;
            list.add(five);

            Border six = new Border(border);
            six.mLineLeft = l1;
            six.mLineRight = l2;
            six.mLineTop = l4;
            list.add(six);

            Border seven = new Border(border);
            seven.mLineLeft = l2;
            seven.mLineRight = l3;
            seven.mLineTop = l4;
            list.add(seven);

            Border eight = new Border(border);
            eight.mLineLeft = l3;
            eight.mLineTop = l4;
            list.add(eight);
        }

        return list;
    }

    /**
     * use 4 lines to cut a big border ,we will get 9 little borders
     * @param border
     * @param l1
     * @param l2
     * @param l3
     * @param l4
     * @return
     */
    public static List<Border> cutBorder(final Border border, final Line l1, final Line l2, final Line l3, final Line l4) {
        List<Border> list = new ArrayList<>();

        Border one = new Border(border);
        one.mLineRight = l3;
        one.mLineBottom = l1;
        list.add(one);

        Border two = new Border(border);
        two.mLineLeft = l3;
        two.mLineRight = l4;
        two.mLineBottom = l1;
        list.add(two);

        Border three = new Border(border);
        three.mLineLeft = l4;
        three.mLineBottom = l1;
        list.add(three);

        Border four = new Border(border);
        four.mLineRight = l3;
        four.mLineTop = l1;
        four.mLineBottom = l2;
        list.add(four);

        Border five = new Border(border);
        five.mLineRight = l4;
        five.mLineLeft = l3;
        five.mLineTop = l1;
        five.mLineBottom = l2;
        list.add(five);

        Border six = new Border(border);
        six.mLineLeft = l4;
        six.mLineTop = l1;
        six.mLineBottom = l2;
        list.add(six);

        Border seven = new Border(border);
        seven.mLineRight = l3;
        seven.mLineTop = l2;
        list.add(seven);

        Border eight = new Border(border);
        eight.mLineRight = l4;
        eight.mLineLeft = l3;
        eight.mLineTop = l2;
        list.add(eight);

        Border nine = new Border(border);
        nine.mLineLeft = l4;
        nine.mLineTop = l2;
        list.add(nine);

        return list;
    }

    /**
     * cut a border to four borders according to two lines across each other
     * @param border
     * @param horizontal
     * @param vertical
     * @return
     */
    public static List<Border> cutBorderCross(final Border border, final Line horizontal, final Line vertical) {
        List<Border> list = new ArrayList<>();

        Border one = new Border(border);
        one.mLineBottom = horizontal;
        one.mLineRight = vertical;
        list.add(one);

        Border two = new Border(border);
        two.mLineBottom = horizontal;
        two.mLineLeft = vertical;
        list.add(two);

        Border three = new Border(border);
        three.mLineTop = horizontal;
        three.mLineRight = vertical;
        list.add(three);

        Border four = new Border(border);
        four.mLineTop = horizontal;
        four.mLineLeft = vertical;
        list.add(four);

        return list;
    }

    /**
     * create a matrix which let bitmap centerCrop in the border rect
     */
    public static Matrix createMatrix(Border border, Bitmap bitmap, float extraSize) {
        return createMatrix(border, bitmap.getWidth(), bitmap.getHeight(), extraSize);
    }

    public  static Matrix createMatrix(Border border, Drawable drawable, float extraSize) {
        return createMatrix(border, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), extraSize);
    }


    public  static Matrix createMatrix(Border border, int width, int height, float extraSize) {
        final RectF rectF = border.getRect();

        Matrix matrix = new Matrix();

        float offsetX = rectF.centerX() - width / 2;
        float offsetY = rectF.centerY() - height / 2;

        matrix.postTranslate(offsetX, offsetY);

        float scale;

        if (width * rectF.height() > rectF.width() * height) {
            scale = (rectF.height() + extraSize) / height;
        } else {
            scale = (rectF.width() + extraSize) / width;
        }

        matrix.postScale(scale, scale, rectF.centerX(), rectF.centerY());

        return matrix;
    }
}
