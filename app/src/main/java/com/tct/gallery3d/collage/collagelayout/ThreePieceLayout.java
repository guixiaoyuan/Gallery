package com.tct.gallery3d.collage.collagelayout;

import com.tct.gallery3d.collage.border.Line;

/**
 * Created by liuxiaoyu on 16-10-10.
 */
public class ThreePieceLayout extends NumberPieceLayout {

    public ThreePieceLayout(int theme) {
        super(theme);
    }

    @Override
    public int getThemeCount() {
        return 6;
    }

    @Override
    public void layout() {
        switch (mTheme) {
            case 0:
                cutBorderEqualPart(getOuterBorder(), 3, Line.Direction.HORIZONTAL);
                break;
            case 1:
                cutBorderEqualPart(getOuterBorder(), 3, Line.Direction.VERTICAL);
                break;
            case 2:
                addLine(getOuterBorder(), Line.Direction.HORIZONTAL, 1f / 2);
                addLine(getBorder(0), Line.Direction.VERTICAL, 1f / 2);
                break;
            case 3:
                addLine(getOuterBorder(), Line.Direction.HORIZONTAL, 1f / 2);
                addLine(getBorder(1), Line.Direction.VERTICAL, 1f / 2);
                break;
            case 4:
                addLine(getOuterBorder(), Line.Direction.VERTICAL, 1f / 2);
                addLine(getBorder(0), Line.Direction.HORIZONTAL, 1f / 2);
                break;
            case 5:
                addLine(getOuterBorder(), Line.Direction.VERTICAL, 1f / 2);
                addLine(getBorder(1), Line.Direction.HORIZONTAL, 1f / 2);
                break;
            default:
                cutBorderEqualPart(getOuterBorder(), 3, Line.Direction.HORIZONTAL);
                break;
        }
    }
}
