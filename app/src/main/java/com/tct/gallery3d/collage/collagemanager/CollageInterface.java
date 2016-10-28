package com.tct.gallery3d.collage.collagemanager;

import com.tct.gallery3d.collage.puzzle.PuzzleLayout;

import java.util.List;

/**
 * Created by liuxiaoyu on 16-10-10.
 */
public interface CollageInterface {
    /**
     * @param count
     */
    List<PuzzleLayout> getTemplatesByCount(int count);

    /**
     * increase OutBorder Width
     */
    void reduceOutSideBorderWidth();

    /**
     *add OutBorder Width
     */
    void addOutSideBorderWidth();

    /**
     *increase InBorder Width
     */
    void reduceInSideBorderWidth();

    /**
     *add inBorder Width
     */
    void addInsideBorderWidth();

    /**
     *replace image
     */
    void swapImage(String path);

    /**
     *
     */
    void zoomImage();

    /**
     * load select photos by paths from Moments Page
     */
    void loadSelectPhotos();

    /**
     *save the puzzleVIew to a File
     */
    void saveFile();

    /**
     * set the count of pieces and the paths of seletcing photos
     */
    void setPieceAndPath(int size, List<String> mPhotoPaths);

    /**
     * change the PuzzleLayout of PuzzleVIew
     * @param themeId
     */
    void changePuzzleLayout(int themeId);

    /**
     *set default puzzle information
     */
    void setPuzzleLayout();


}
