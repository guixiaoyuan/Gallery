package com.tct.gallery3d.polaroid.config;

import android.content.Context;

import com.tct.gallery3d.R;
import com.tct.gallery3d.polaroid.manager.Filter;
import com.tct.gallery3d.polaroid.manager.FilterManager;
import com.tct.gallery3d.polaroid.manager.Font;
import com.tct.gallery3d.polaroid.manager.FontManager;
import com.tct.gallery3d.polaroid.manager.Frame;
import com.tct.gallery3d.polaroid.manager.FrameManager;

// This simple class contains the configuration of a whole Polaroid effect,
// as 'edited' by the user.
// It can be used in the editor (i.e. the slogan can be saved into this object then applied to another frame)
// It can also be used if the user goes to the 'send' screen but wants to come back and change something in the polaroid editor
// Finally, it should also be used when pausing / resuming the activities
public class PolaroidConfig {
    public String mFilterName;
    public String mFrameName;
    public String mSlogan;
    public String mFontName;
    public String mDateTag;
    public String mLocationTag;
    public int mTagStatus;
    // Tells if the user has ever selected a filter manually
    public boolean mFilterSelected = false;
    // Tells if the user has ever selected a frame manually
    public boolean mFrameSelected = false;
    // If true, it means a filter sets a frame as preferred frame
    // and there is no need for that frame to set its preferred filter back
    public boolean mFrameFromFilter = false;
    // Same, in the other direction Extend if needed (font color, font size,
    // slogan position, date / location info, etc.)
    public boolean mFilterFromFrame = false;

    public PolaroidConfig(Context context) {
        reset(context);
    }

    public void reset(Context context) {
        try {
            mFilterName = null;
            mFrameName = null;
            mFontName = null;
            mSlogan = context.getResources().getString(R.string.polaroid_default_slogan);
            mDateTag = "";
            mLocationTag = "";
            mFilterSelected = false;
            mFrameSelected = false;
            mFrameFromFilter = false;
            mFilterFromFrame = false;

            Filter filter = FilterManager.getFilter(0);
            if (filter != null) {
                mFilterName = filter.getName();
            }

            Font font = FontManager.getFont(0);
            if (font != null) {
                mFontName = font.getName();
            }

            Frame frame = FrameManager.getFrame(0);
            if (frame != null) {
                mFrameName = frame.getName();
                if (frame.mFont != null) {
                    mFontName = frame.mFont.getName();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/* EOF */

