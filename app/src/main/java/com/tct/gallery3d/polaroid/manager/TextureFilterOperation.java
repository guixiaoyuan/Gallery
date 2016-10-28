package com.tct.gallery3d.polaroid.manager;

import java.util.ArrayList;

import com.tct.gallery3d.polaroid.ScriptC_texture;
import com.tct.gallery3d.polaroid.Poladroid;

import android.content.Context;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Sampler;
import android.support.v8.renderscript.Script.KernelID;
import android.util.Log;

public class TextureFilterOperation extends FilterOperation {
    private ScriptC_texture mScript;
    private ArrayList<TextureResource> mTextureResources = new ArrayList<TextureResource>();
    private Allocation mTextureAllocation;
    boolean mSaved = false; // Just for debug

    public TextureFilterOperation(ArrayList<TextureResource> textureResources) {
        if (textureResources == null) {
            Log.i(Poladroid.TAG, "*** new TextureFilterOperation(): textureSet is null");
        } else {
            Log.d(Poladroid.TAG, "new TextureFilterOperation(textureSet size: " + textureResources.size()
                    + ")");
            mTextureResources = textureResources;
        }
    }

    public KernelID getKernelID() {
        return mScript.getKernelID_root();
    }

    private TextureResource pickBestTextureResource(int targetWidth, int targetHeight) {
        TextureResource bestTextureResource = null;
        float bestRatio = -1f; // Will be initialized by the first texture

        Log.d(Poladroid.TAG, "TextureFilterOperation.pickBestTextureResource(target: " + targetWidth + "x"
                + targetHeight + "){");

        // Avoid division by zero...
        if (targetWidth <= 0)
            targetWidth = 1;
        if (targetHeight <= 0)
            targetHeight = 1;

        for (TextureResource textureResource : mTextureResources) {
            Log.d(Poladroid.TAG, "Checking " + textureResource.mTargetWidth + "x"
                    + textureResource.mTargetHeight);
            float widthRatio = 1f * textureResource.mTargetWidth / targetWidth;
            float heightRatio = 1f * textureResource.mTargetHeight / targetHeight;
            Log.d(Poladroid.TAG, "  Ratios: " + widthRatio + " and " + heightRatio);
            if (widthRatio < 1) {
                // Enlarging is worse than shrinking => make it look bad
                widthRatio = (float) (1f / widthRatio / Math.sqrt(widthRatio));
            }
            if (heightRatio < 1) {
                // Enlarging is worse than shrinking => make it look bad
                heightRatio = (float) (1f / heightRatio / Math.sqrt(heightRatio));
            }
            float ratio = Math.max(widthRatio, heightRatio);
            Log.d(Poladroid.TAG, "  Larger than 1 ratios: " + widthRatio + " and " + heightRatio
                    + ", overall ratio: " + ratio);

            if (bestTextureResource == null || ratio < bestRatio) {
                bestTextureResource = textureResource;
                bestRatio = ratio;
                Log.d(Poladroid.TAG, "  => Looks better...");
            }
        }

        Log.d(Poladroid.TAG, "} TextureFilterOperation.pickBestTextureResource() => " + "chosen texture: "
                + bestTextureResource.mTargetWidth + "x" + bestTextureResource.mTargetHeight);
        return bestTextureResource;
    }

    public void prepare(RenderScript rs, Context context, Quality targetQuality, int targetWidth,
            int targetHeight) {
        Log.d(Poladroid.TAG, "TextureFilterOperation.prepare(target: " + targetWidth + "x" + targetHeight
                + ", quality: " + targetQuality + "){");
        mScript = new ScriptC_texture(rs);

        TextureResource textureResource = pickBestTextureResource(targetWidth, targetHeight);
        mTextureAllocation = Allocation.createFromBitmapResource(rs, context.getResources(),
                textureResource.mResId);

        // Poladroid.saveBitmap(bitmap, "textureBitmap");
        // Poladroid.saveAllocation(mTextureAllocation, "textureAllocation");
        mScript.invoke_setTexture(mTextureAllocation, Sampler.CLAMP_LINEAR(rs));
        // bitmap.recycle();

        if (!mSaved) {
            mSaved = true;
            // Poladroid.saveAllocation(mTextureAllocation, "textureAllocation");
        }

        int scriptBlendMode;
        switch (mBlendMode) {
        case MULTIPLY:
            scriptBlendMode = mScript.get_BLEND_MODE_MULTIPLY();
            break;
        case SCREEN:
            scriptBlendMode = mScript.get_BLEND_MODE_SCREEN();
            break;
        case SOFTLIGHT:
            scriptBlendMode = mScript.get_BLEND_MODE_SOFTLIGHT();
            break;
        case LINEAR_BURN:
            scriptBlendMode = mScript.get_BLEND_MODE_LINEAR_BURN();
            break;
        case LINEAR_DODGE:
            scriptBlendMode = mScript.get_BLEND_MODE_LINEAR_DODGE();
            break;
        case NORMAL:
            scriptBlendMode = mScript.get_BLEND_MODE_NORMAL();
            break;
        default:
            scriptBlendMode = mScript.get_BLEND_MODE_NORMAL();
            break;
        }

        mScript.invoke_setBlendMode(scriptBlendMode);
        mScript.invoke_setOutputResolution(targetWidth, targetHeight);
        mScript.invoke_setOpacity(mOpacity);

        Log.d(Poladroid.TAG, "} TextureFilterOperation.prepare() => texture "
                + mTextureAllocation.getType().getX() + "x" + mTextureAllocation.getType().getY());
    }

    public void destroy(RenderScript rs, Context context) {
        if (mTextureAllocation != null) {
            Log.d(Poladroid.TAG, "TextureFilterOperation.destroy(){");
            mTextureAllocation.destroy();
            mTextureAllocation = null;
            Log.d(Poladroid.TAG, "} TextureFilterOperation.destroy()");
        }
    }
}

/* EOF */
