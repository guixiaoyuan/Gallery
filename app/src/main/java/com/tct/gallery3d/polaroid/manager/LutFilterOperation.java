package com.tct.gallery3d.polaroid.manager;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Script.KernelID;
import android.support.v8.renderscript.ScriptIntrinsic3DLUT;
import android.support.v8.renderscript.Type;
import android.util.Log;

import com.tct.gallery3d.polaroid.Poladroid;

public class LutFilterOperation extends FilterOperation {
    private ScriptIntrinsic3DLUT mScript;
    private ArrayList<LutResource> mLutResources = new ArrayList<LutResource>();
    private Allocation mLutAllocation;

    LutFilterOperation(ArrayList<LutResource> lutResources) {
        if (lutResources == null) {
            Log.i(Poladroid.TAG, "*** new LutFilterOperation(): lutSet is null");
        } else {
            Log.d(Poladroid.TAG, "new LutFilterOperation(lutSet size: " + lutResources.size() + ")");
            mLutResources = lutResources;
        }
    }

    public KernelID getKernelID() {
        return mScript.getKernelID();
    }

    private LutResource pickBestLutResource(Quality targetQuality) {
        LutResource bestLutResource = null;
        float bestRatio = 0.f; // Will be initialized by the first texture
        int targetSize = 1; // NOT ZERO, to avoid division by zero...

        switch (targetQuality) {
        case LOWEST:
            targetSize = 17;
            break; // Empirical
        case MEDIUM:
            targetSize = 33;
            break; // Empirical
        case HIGHEST:
            targetSize = 65;
            break; // Empirical
        }

        Log.d(Poladroid.TAG, "LutFilterOperation.pickBestLut(target: " + targetQuality + ", targetSize: "
                + targetSize + "){");

        for (LutResource lutResource : mLutResources) {
            Log.d(Poladroid.TAG, "Checking " + lutResource.mSize);

            float sizeRatio = 1f * lutResource.mSize / targetSize;
            Log.d(Poladroid.TAG, "  Ratio: " + sizeRatio);
            if (sizeRatio < 1) {
                sizeRatio = (float) (1f / sizeRatio / Math.sqrt(sizeRatio));
            }
            // if (sizeRatio < 1){
            // // Enlarging is worse than shrinking => make it look bad
            // sizeRatio = 1f / sizeRatio / sizeRatio;
            // }
            float ratio = sizeRatio;
            Log.d(Poladroid.TAG, "  Larger than 1 ratio: " + ratio);

            if (bestLutResource == null || ratio < bestRatio) {
                bestLutResource = lutResource;
                bestRatio = ratio;
                Log.d(Poladroid.TAG, "  => Looks better...");
            }
        }

        Log.d(Poladroid.TAG, "} LutFilterOperation.pickBestLutResource() => chosen LUT Resource: "
                + bestLutResource.mSize);

        return bestLutResource;
    }

    public void prepare(RenderScript rs, Context context, Quality targetQuality, int targetWidth,
            int targetHeight) {
        Log.d(Poladroid.TAG, "LutFilterOperation.prepare(target: " + targetWidth + "x" + targetHeight
                + ", quality: " + targetQuality + "){");

        LutResource lutResource = pickBestLutResource(targetQuality);

        mScript = ScriptIntrinsic3DLUT.create(rs, Element.U8_4(rs));
        initLut(rs, context, lutResource.mResId);
        Log.d(Poladroid.TAG, "} LutFilterOperation.prepare() => lut " + mLutAllocation.getType().getX()
                + "x" + mLutAllocation.getType().getY() + "x" + mLutAllocation.getType().getZ());
    }

    public void destroy(RenderScript rs, Context context) {
        if (mLutAllocation != null) {
            Log.d(Poladroid.TAG, "LutFilterOperation.destroy(){");
            mLutAllocation.destroy();
            mLutAllocation = null;
            Log.d(Poladroid.TAG, "} LutFilterOperation.destroy()");
        }
    }

    private int[] load3DLut(Context context, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId, options);

        int[] lut = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(lut, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        bitmap.recycle();

        return lut;
    }

    private void initLut(RenderScript rs, Context context, int resId) {
        Log.d(Poladroid.TAG, "LutFilterOperation.initLut(){");

        int[] lut = load3DLut(context, resId);
        int lutSize = (int) Math.round(Math.cbrt(lut.length));
        Log.d(Poladroid.TAG, "Lut size: " + lutSize);

        Type.Builder tb = new Type.Builder(rs, Element.U8_4(rs));
        tb.setX(lutSize);
        tb.setY(lutSize);
        tb.setZ(lutSize);
        Type t = tb.create();
        mLutAllocation = Allocation.createTyped(rs, t);
        mLutAllocation.copyFromUnchecked(lut);
        mScript.setLUT(mLutAllocation);

        Log.d(Poladroid.TAG, "} LutFilterOperation.initLut()");
    }
}

/* EOF */