package com.tct.gallery3d.collage.collagelayout;

import com.tct.gallery3d.collage.border.Line;

/**
 * Created by liuxiaoyu on 16-10-10.
 */
public class OnePieceLayout extends NumberPieceLayout {

    public OnePieceLayout(int theme) {
        super(theme);
    }

    public OnePieceLayout(float radio, int theme) {
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
                addLine(getOuterBorder(), Line.Direction.HORIZONTAL, 1f / 2);
                break;
            case 1:
                addLine(getOuterBorder(), Line.Direction.VERTICAL, 1f / 2);
                break;
            case 2:
                addCross(getOuterBorder(), 1f / 2);
                break;
            case 3:
                cutBorderEqualPart(getOuterBorder(), 2, 1);
                break;
            case 4:
                cutBorderEqualPart(getOuterBorder(), 1, 2);
                break;
            case 5:
                cutBorderEqualPart(getOuterBorder(), 2, 2);
                break;
            default:
                addLine(getOuterBorder(), Line.Direction.HORIZONTAL, 1f / 2);
                break;
        }

    }

}
