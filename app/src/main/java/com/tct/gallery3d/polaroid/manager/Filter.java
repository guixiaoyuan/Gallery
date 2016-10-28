package com.tct.gallery3d.polaroid.manager;

import java.util.ArrayList;

import android.content.Context;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptGroup;
import android.support.v8.renderscript.Type;
import android.util.Log;

import com.tct.gallery3d.polaroid.Poladroid;
import com.tct.gallery3d.polaroid.manager.FilterOperation.Quality;

public class Filter {
    private ArrayList<FilterOperation> mFilterOperations = new ArrayList<FilterOperation>();
    private String mName;
    private String mFrameName;
    private ScriptGroup mScriptGroup;

    public Filter(String name, String frameName, ArrayList<FilterOperation> filterOperations) {
        mName = name;
        mFrameName = frameName;

        Log.d(Poladroid.TAG, "new Filter(" + name + ", frame: " + frameName + ", filterOperations: "
                + ((filterOperations == null) ? null : filterOperations.size()) + ")");

        if (filterOperations != null) {
            mFilterOperations = filterOperations;
        }
    }

    public String getName() {
        return mName;
    }

    public Frame getPreferredFrame() {
        if (mFrameName != null) {
            return FrameManager.getFrame(mFrameName);
        }
        return null;
    }

    public String getPreferredFrameName() {
        return mFrameName;
    }

    boolean isEmpty() {
        return mFilterOperations.size() == 0;
    }

    boolean isValid() {
        for (FilterOperation filterOperation : mFilterOperations) {
            if (filterOperation == null) {
                Log.w(Poladroid.TAG, "*** Filter.isValid(" + mName
                        + "): null filter operation => filter is marked invalid");
                return false;
            }
        }
        return true;
    }

    public int getSize() {
        return mFilterOperations.size();
    }

    public void prepare(RenderScript rs, Context context, Quality quality, int targetWidth,
            int targetHeight, Type type) {
        Log.d(Poladroid.TAG, "Filter.prepare(" + mName + ", quality: " + quality + ", target: "
                + targetWidth + "x" + targetHeight + "){");

        for (int i = 0; i < mFilterOperations.size(); i++) {
            FilterOperation filterOperation = mFilterOperations.get(i);
            filterOperation.prepare(rs, context, quality, targetWidth, targetHeight);
        }
        if (mFilterOperations.size() > 0) {
            ScriptGroup.Builder builder = new ScriptGroup.Builder(rs);

            for (int i = 0; i < mFilterOperations.size(); i++) {
                builder.addKernel(mFilterOperations.get(i).getKernelID());
            }

            for (int i = 0; i < mFilterOperations.size() - 1; i++) {
                builder.addConnection(type, mFilterOperations.get(i).getKernelID(),
                        mFilterOperations.get(i + 1).getKernelID());
            }

            mScriptGroup = builder.create();
        }

        Log.d(Poladroid.TAG, "} Filter.prepare()");
    }

    public void destroy(RenderScript rs, Context context) {
        Log.d(Poladroid.TAG, "Filter.destroy(" + mName + "){");

        if (mFilterOperations.size() > 0 && mScriptGroup != null) {
            mScriptGroup.setInput(mFilterOperations.get(0).getKernelID(), null);
            mScriptGroup.setOutput(mFilterOperations.get(mFilterOperations.size() - 1)
                    .getKernelID(), null);
            mScriptGroup = null;
        }
        for (int i = 0; i < mFilterOperations.size(); i++) {
            FilterOperation filterOperation = mFilterOperations.get(i);
            filterOperation.destroy(rs, context);
        }
        Log.d(Poladroid.TAG, "} Filter.destroy()");
    }

    public void render(RenderScript rs, Allocation inAllocation, Allocation outAllocation) {
        Log.d(Poladroid.TAG, "Filter.render(" + mName + "){");

        if (mFilterOperations.size() > 0 && mScriptGroup != null) {
            mScriptGroup.setInput(mFilterOperations.get(0).getKernelID(), inAllocation);
            mScriptGroup.setOutput(mFilterOperations.get(mFilterOperations.size() - 1)
                    .getKernelID(), outAllocation);
            mScriptGroup.execute();
        } else {
            // Nothing to do
            outAllocation.copyFrom(inAllocation);
        }

        Log.d(Poladroid.TAG, "} Filter.render()");
    }

    @Override
    public String toString() {
        return "Filter { " + mName + ", op count: " + mFilterOperations + " }";
    }
}

/* EOF */