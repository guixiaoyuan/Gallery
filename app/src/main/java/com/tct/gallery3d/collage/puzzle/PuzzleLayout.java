package com.tct.gallery3d.collage.puzzle;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import com.tct.gallery3d.collage.border.Border;
import com.tct.gallery3d.collage.border.BorderComparator;
import com.tct.gallery3d.collage.border.BorderUtil;
import com.tct.gallery3d.collage.border.Line;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * contains all lines and borders.
 * we can add line to divider a border to number of borders.
 * <p>
 * <p>
 * to determine the which border to layout puzzle piece.
 *
 * @see Border
 * <p>
 * Created by liuxiaoyu on 16-10-10.
 */
public abstract class PuzzleLayout {
    protected static final String TAG = "PuzzleLayout";

    protected int mTheme;

    private Border mOuterBorder;

    private List<Border> mBorders = new ArrayList<>();
    private List<Line> mLines = new ArrayList<>();
    private List<Line> mOuterLines = new ArrayList<>(4);

    private Comparator<Border> mBorderComparator = new BorderComparator();

    public PuzzleLayout() {

    }

    public PuzzleLayout(RectF baseRect) {
        setOuterBorder(baseRect);
    }

    /**
     * SET outer border Rect
     * @param baseRect
     */
    public void setOuterBorder(RectF baseRect) {
        float width = baseRect.width();
        float height = baseRect.height();

        PointF one = new PointF(0, 0);
        PointF two = new PointF(width, 0);
        PointF three = new PointF(0, height);
        PointF four = new PointF(width, height);

        Line lineLeft = new Line(one, three);
        Line lineTop = new Line(one, two);
        Line lineRight = new Line(two, four);
        Line lineBottom = new Line(three, four);

        mOuterLines.clear();

        mOuterLines.add(lineLeft);
        mOuterLines.add(lineTop);
        mOuterLines.add(lineRight);
        mOuterLines.add(lineBottom);

        mOuterBorder = new Border(baseRect);

        mBorders.clear();
        mBorders.add(mOuterBorder);
    }

    public abstract void layout();

    /**
     * add line in every border
     * @param border
     * @param direction
     * @param ratio
     * @return
     */
    protected List<Border> addLine(Border border, Line.Direction direction, float ratio) {
        mBorders.remove(border);
        Line line = BorderUtil.createLine(border, direction, ratio);
        mLines.add(line);

        List<Border> borders = BorderUtil.cutBorder(border, line);
        mBorders.addAll(borders);

        updateLineLimit();
        Collections.sort(mBorders, mBorderComparator);

        return borders;
    }

    /**
     * get some little borders according to part and direction,we can add lines in every little borders
     * @param border
     * @param part
     * @param direction
     */
    protected void cutBorderEqualPart(Border border, int part, Line.Direction direction) {
        Border temp = border;
        for (int i = part; i > 1; i--) {
            temp = addLine(temp, direction, (float) (i - 1) / i).get(0);
        }
    }

    /**
     *  add two lines corss to each other ,such as "+",use to cut outborder
     * @param border
     * @param radio
     * @return
     */
    protected List<Border> addCross(Border border, float radio) {
        return addCross(border, radio, radio);
    }

    protected List<Border> addCross(Border border, float horizontalRadio, float verticalRadio) {
        mBorders.remove(border);
        Line horizontal = BorderUtil.createLine(border, Line.Direction.HORIZONTAL, horizontalRadio);
        Line vertical = BorderUtil.createLine(border, Line.Direction.VERTICAL, verticalRadio);
        mLines.add(horizontal);
        mLines.add(vertical);

        List<Border> borders = BorderUtil.cutBorderCross(border, horizontal, vertical);
        mBorders.addAll(borders);

        updateLineLimit();

        if (mBorderComparator == null) {
            mBorderComparator = new BorderComparator();
        }
        Collections.sort(mBorders, mBorderComparator);

        return borders;
    }

    /**
     * cut the bigger outborder to some little borders by horizontal and vertical lines
     * @param border
     * @param hSize
     * @param vSize
     * @return
     */
    protected List<Border> cutBorderEqualPart(Border border, int hSize, int vSize) {
        if ((hSize + 1) * (vSize + 1) > 9) {
            Log.e(TAG, "cutBorderEqualPart: the size can not be so great");
            return null;
        }
        mBorders.remove(border);
        List<Border> borders = new ArrayList<>();
        switch (hSize) {
            case 1:
                switch (vSize) {
                    case 1:
                        borders.addAll(addCross(border, 1f / 2));
                        break;
                    case 2:
                        Line l1 = BorderUtil.createLine(border, Line.Direction.VERTICAL, 1f / 3);
                        Line l2 = BorderUtil.createLine(border, Line.Direction.VERTICAL, 2f / 3);
                        Line l3 = BorderUtil.createLine(border, Line.Direction.HORIZONTAL, 1f / 2);

                        mLines.add(l1);
                        mLines.add(l2);
                        mLines.add(l3);

                        borders.addAll(BorderUtil.cutBorder(border, l1, l2, l3, Line.Direction.VERTICAL));
                        break;

                    case 3:
                        Line ll1 = BorderUtil.createLine(border, Line.Direction.VERTICAL, 1f / 4);
                        Line ll2 = BorderUtil.createLine(border, Line.Direction.VERTICAL, 2f / 4);
                        Line ll3 = BorderUtil.createLine(border, Line.Direction.VERTICAL, 3f / 4);
                        Line ll4 = BorderUtil.createLine(border, Line.Direction.HORIZONTAL, 1f / 2);

                        mLines.add(ll1);
                        mLines.add(ll2);
                        mLines.add(ll3);
                        mLines.add(ll4);

                        borders.addAll(BorderUtil.cutBorder(border, ll1, ll2, ll3, ll4, Line.Direction.VERTICAL));

                        break;
                }
                break;

            case 2:
                switch (vSize) {
                    case 1:
                        Line l1 = BorderUtil.createLine(border, Line.Direction.HORIZONTAL, 1f / 3);
                        Line l2 = BorderUtil.createLine(border, Line.Direction.HORIZONTAL, 2f / 3);
                        Line l3 = BorderUtil.createLine(border, Line.Direction.VERTICAL, 1f / 2);

                        mLines.add(l1);
                        mLines.add(l2);
                        mLines.add(l3);

                        borders.addAll(BorderUtil.cutBorder(border, l1, l2, l3, Line.Direction.HORIZONTAL));

                        break;
                    case 2:
                        Line ll1 = BorderUtil.createLine(border, Line.Direction.HORIZONTAL, 1f / 3);
                        Line ll2 = BorderUtil.createLine(border, Line.Direction.HORIZONTAL, 2f / 3);
                        Line ll3 = BorderUtil.createLine(border, Line.Direction.VERTICAL, 1f / 3);
                        Line ll4 = BorderUtil.createLine(border, Line.Direction.VERTICAL, 2f / 3);

                        mLines.add(ll1);
                        mLines.add(ll2);
                        mLines.add(ll3);
                        mLines.add(ll4);

                        borders.addAll(BorderUtil.cutBorder(border, ll1, ll2, ll3, ll4));
                        break;
                }
                break;

            case 3:
                switch (vSize) {
                    case 1:
                        Line ll1 = BorderUtil.createLine(border, Line.Direction.HORIZONTAL, 1f / 4);
                        Line ll2 = BorderUtil.createLine(border, Line.Direction.HORIZONTAL, 2f / 4);
                        Line ll3 = BorderUtil.createLine(border, Line.Direction.HORIZONTAL, 3f / 4);
                        Line ll4 = BorderUtil.createLine(border, Line.Direction.VERTICAL, 1f / 2);

                        mLines.add(ll1);
                        mLines.add(ll2);
                        mLines.add(ll3);
                        mLines.add(ll4);

                        borders.addAll(BorderUtil.cutBorder(border, ll1, ll2, ll3, ll4, Line.Direction.HORIZONTAL));
                        break;
                }
        }

        mBorders.addAll(borders);

        updateLineLimit();
        Collections.sort(mBorders, mBorderComparator);

        return borders;
    }

    /**
     * cut the OutBorder to Spiral layout
     * @param border
     * @return
     */
    protected List<Border> cutSpiral(Border border) {
        mBorders.remove(border);
        List<Border> borders = new ArrayList<>();

        float width = border.width();
        float height = border.height();

        PointF one = new PointF(0, height / 3);
        PointF two = new PointF(width / 3 * 2, 0);
        PointF three = new PointF(width, height / 3 * 2);
        PointF four = new PointF(width / 3, height);
        PointF five = new PointF(width / 3, height / 3);
        PointF six = new PointF(width / 3 * 2, height / 3);
        PointF seven = new PointF(width / 3 * 2, height / 3 * 2);
        PointF eight = new PointF(width / 3, height / 3 * 2);

        Line l1 = new Line(one, six);
        Line l2 = new Line(two, seven);
        Line l3 = new Line(eight, three);
        Line l4 = new Line(five, four);

        l1.setAttachLineStart(border.mLineLeft);
        l1.setAttachLineEnd(l2);
        l1.setUpperLine(border.mLineTop);
        l1.setLowerLine(l3);

        l2.setAttachLineStart(border.mLineTop);
        l2.setAttachLineEnd(l3);
        l2.setUpperLine(border.mLineRight);
        l2.setLowerLine(l4);

        l3.setAttachLineStart(l4);
        l3.setAttachLineEnd(border.mLineRight);
        l3.setUpperLine(l1);
        l3.setLowerLine(border.mLineBottom);

        l4.setAttachLineStart(l1);
        l4.setAttachLineEnd(border.mLineBottom);
        l4.setUpperLine(l2);
        l4.setLowerLine(border.mLineLeft);

        mLines.add(l1);
        mLines.add(l2);
        mLines.add(l3);
        mLines.add(l4);

        Border b1 = new Border(border);
        b1.mLineRight = l2;
        b1.mLineBottom = l1;
        borders.add(b1);

        Border b2 = new Border(border);
        b2.mLineLeft = l2;
        b2.mLineBottom = l3;
        borders.add(b2);

        Border b3 = new Border(border);
        b3.mLineRight = l4;
        b3.mLineTop = l1;
        borders.add(b3);

        Border b4 = new Border(border);
        b4.mLineTop = l1;
        b4.mLineRight = l2;
        b4.mLineLeft = l4;
        b4.mLineBottom = l3;
        borders.add(b4);

        Border b5 = new Border(border);
        b5.mLineLeft = l4;
        b5.mLineTop = l3;
        borders.add(b5);

        mBorders.addAll(borders);
        
        updateLineLimit();
        Collections.sort(mBorders, mBorderComparator);

        return borders;
    }

    /**
     * update line limit
     */
    private void updateLineLimit() {
        for (Line line : mLines) {
            updateUpperLine(line);
            updateLowerLine(line);
        }
    }

    private void updateLowerLine(final Line line) {
        for (Line l : mLines) {
            if (l.getPosition() > line.getLowerLine().getPosition()
                    && l.getPosition() < line.getPosition()
                    && l.getDirection() == line.getDirection()) {

                if (l.getDirection() == Line.Direction.HORIZONTAL
                        && (l.end.x <= line.start.x || l.start.x >= line.end.x)) {
                    continue;
                }

                if (l.getDirection() == Line.Direction.VERTICAL
                        && (l.end.y <= line.start.y || l.start.y >= line.end.y)) {
                    continue;
                }

                line.setLowerLine(l);
            }
        }
    }

    private void updateUpperLine(final Line line) {
        for (Line l : mLines) {
            if (l.getPosition() < line.getUpperLine().getPosition()
                    && l.getPosition() > line.getPosition()
                    && l.getDirection() == line.getDirection()) {

                if (l.getDirection() == Line.Direction.HORIZONTAL
                        && (l.end.x <= line.start.x || l.start.x >= line.end.x)) {
                    continue;
                }

                if (l.getDirection() == Line.Direction.VERTICAL
                        && (l.end.y <= line.start.y || l.start.y >= line.end.y)) {
                    continue;
                }

                line.setUpperLine(l);
            }
        }
    }

    public void reset() {
        mLines.clear();
        mBorders.clear();
        mBorders.add(mOuterBorder);
    }

    public void update() {
        for (Line line : mLines) {
            line.update();
        }
    }

    public int getBorderSize() {
        return mBorders.size();
    }

    public Border getBorder(int index) {
        return mBorders.get(index);
    }

    public List<Line> getLines() {
        return mLines;
    }

    public List<Border> getBorders() {
        return mBorders;
    }

    public Border getOuterBorder() {
        return mOuterBorder;
    }

    public List<Line> getOuterLines() {
        return mOuterLines;
    }

    public int getTheme() {
        return mTheme;
    }

}
