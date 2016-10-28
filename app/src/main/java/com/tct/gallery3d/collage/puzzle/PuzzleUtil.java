package com.tct.gallery3d.collage.puzzle;

import com.tct.gallery3d.R;
import com.tct.gallery3d.collage.collagelayout.EightPieceLayout;
import com.tct.gallery3d.collage.collagelayout.FivePieceLayout;
import com.tct.gallery3d.collage.collagelayout.FourPieceLayout;
import com.tct.gallery3d.collage.collagelayout.NinePieceLayout;
import com.tct.gallery3d.collage.collagelayout.OnePieceLayout;
import com.tct.gallery3d.collage.collagelayout.SevenPieceLayout;
import com.tct.gallery3d.collage.collagelayout.SixPieceLayout;
import com.tct.gallery3d.collage.collagelayout.ThreePieceLayout;
import com.tct.gallery3d.collage.collagelayout.TwoPieceLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuxiaoyu on 16-10-10.
 */
public class PuzzleUtil {
    private static final String TAG = "PuzzleUtil";

    private PuzzleUtil() {

    }

    public static final int[] resTwoPiecesIds = new int[]{
            R.drawable.ic_collage_2_1,
            R.drawable.ic_collage_2_2,
            R.drawable.ic_collage_1_8,
            R.drawable.ic_collage_2_7,

    };

    public static final int[] resThreePiecesIds = new int[]{
            R.drawable.ic_collage_3_6,
            R.drawable.ic_collage_3_5,
            R.drawable.ic_collage_3_8,
            R.drawable.ic_collage_3_7,
            R.drawable.ic_collage_3_3,
            R.drawable.ic_collage_3_2,

    };

    public static final int[] resFourPiecesIds = new int[]{
            R.drawable.ic_collage_4_6,
            R.drawable.ic_collage_4_5,
            R.drawable.ic_collage_4_1,
            R.drawable.ic_collage_4_4,

    };

    public static final int[] resFivePiecesIds = new int[]{
            R.drawable.ic_collage_5_1,
            R.drawable.ic_collage_5_2,
            R.drawable.ic_collage_5_3,
            R.drawable.ic_collage_5_4,
            R.drawable.ic_collage_5_5,
            R.drawable.ic_collage_5_6,
            R.drawable.ic_collage_5_7,

    };

    public static final int[] resSixPiecesIds = new int[]{
            R.drawable.ic_collage_6_6,
            R.drawable.ic_collage_6_2,
            R.drawable.ic_collage_6_3,
            R.drawable.ic_collage_6_1,

    };

    public static final int[] resSevenPiecesIds = new int[]{
            R.drawable.ic_collage_7_1,
            R.drawable.ic_collage_7_6,
            R.drawable.ic_collage_7_7,


    };

    public static final int[] resEightPiecesIds = new int[]{
            R.drawable.ic_collage_8_6,
            R.drawable.ic_collage_8_3,
            R.drawable.ic_collage_8_7,

    };

    public static final int[] resNinePiecesIds = new int[]{
            R.drawable.ic_collage_9_1,
            R.drawable.ic_collage_9_3,
            R.drawable.ic_collage_9_6,

    };

    public static PuzzleLayout getPuzzleLayout(int borderSize, int themeId) {
        switch (borderSize) {
            case 1:
                return new OnePieceLayout(themeId);
            case 2:
                return new TwoPieceLayout(themeId);
            case 3:
                return new ThreePieceLayout(themeId);
            case 4:
                return new FourPieceLayout(themeId);
            case 5:
                return new FivePieceLayout(themeId);
            case 6:
                return new SixPieceLayout(themeId);
            case 7:
                return new SevenPieceLayout(themeId);
            case 8:
                return new EightPieceLayout(themeId);
            case 9:
                return new NinePieceLayout(themeId);
            default:
                return new OnePieceLayout(themeId);
        }
    }

    public static List<PuzzleLayout> getAllPuzzleLayoutByItemCount(int count) {
        List<PuzzleLayout> puzzleLayouts = new ArrayList<>();
        puzzleLayouts.addAll(PuzzleLayoutHelper.getAllThemeLayout(count));
        return puzzleLayouts;
    }
}
