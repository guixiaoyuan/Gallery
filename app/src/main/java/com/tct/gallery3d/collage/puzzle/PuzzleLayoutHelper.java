package com.tct.gallery3d.collage.puzzle;

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
public class PuzzleLayoutHelper {
    private PuzzleLayoutHelper() {

    }

    public static List<PuzzleLayout> getAllThemeLayout(int pieceCount) {
        List<PuzzleLayout> puzzleLayouts = new ArrayList<>();
        switch (pieceCount) {
            case 1:
                for (int i = 0; i < 6; i++) {
                    puzzleLayouts.add(new OnePieceLayout(i));
                }
                break;
            case 2:
                for (int i = 0; i < 4; i++) {
                    puzzleLayouts.add(new TwoPieceLayout(i));
                }
                break;
            case 3:
                for (int i = 0; i < 6; i++) {
                    puzzleLayouts.add(new ThreePieceLayout(i));
                }
                break;
            case 4:
                for (int i = 0; i < 4; i++) {
                    puzzleLayouts.add(new FourPieceLayout(i));
                }
                break;
            case 5:
                for (int i = 0; i < 7; i++) {
                    puzzleLayouts.add(new FivePieceLayout(i));
                }
                break;
            case 6:
                for (int i = 0; i < 4; i++) {
                    puzzleLayouts.add(new SixPieceLayout(i));
                }
                break;
            case 7:
                for (int i = 0; i < 3; i++) {
                    puzzleLayouts.add(new SevenPieceLayout(i));
                }
                break;
            case 8:
                for (int i = 0; i < 3; i++) {
                    puzzleLayouts.add(new EightPieceLayout(i));
                }
                break;
            case 9:
                for (int i = 0; i < 3; i++) {
                    puzzleLayouts.add(new NinePieceLayout(i));
                }
                break;

        }

        return puzzleLayouts;
    }

}
