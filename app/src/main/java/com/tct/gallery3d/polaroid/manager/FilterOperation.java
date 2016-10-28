package com.tct.gallery3d.polaroid.manager;

import android.content.Context;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Script.KernelID;

public abstract class FilterOperation {
    protected BlendMode mBlendMode = BlendMode.NORMAL;
    protected float mOpacity = 1.0f;

    public enum Quality {
        // Keep in this order, from lowest quality to highest quality
        // (increasing ordinal())
        LOWEST, MEDIUM, HIGHEST
    }

    void setBlendMode(BlendMode blendMode) {
        mBlendMode = blendMode;
    }

    BlendMode getBlendMode() {
        return mBlendMode;
    }

    void setOpacity(float opacity) {
        mOpacity = Math.max(0.0f, Math.max(1.0f, opacity));
    }

    float getOpacity() {
        return mOpacity;
    }

    public abstract void prepare(RenderScript rs, Context context, Quality targetQuality,
            int targetWidth, int targetHeight);

    public abstract void destroy(RenderScript rs, Context context);

    public abstract KernelID getKernelID();
}

/* EOF */

