/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* 30/04/2015 |    jialiang.ren     |      PR-987771       |[Gallery_v5.1.9.1.0204.0][UI]Size display abnormal in "Export" of edit*/
/*------------|---------------------|----------------------|----------------------------------------------------------------------*/
/* 22/05/2015 |    jialiang.ren     |      PR-1008624      |[Android][Gallery_v5.1.13.1.0204.0][Force close][REG]*/
/*                                                             Gallery will force close when exporting a picture    */
/*------------|---------------------|----------------------|-----------------------------------------------------*/
/* 30/12/2016 |    xiangyu.liu      |      PR-1804268      |[Gallery_v5.2.6.1.0347.0][Monkey][Gallery][Crash]CRASH: com.tct.gallery3d when system testing*/
/*------------|---------------------|----------------------|----------------------------------------------------------------------*/

package com.tct.gallery3d.filtershow.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.filtershow.FilterShowActivity;
import com.tct.gallery3d.filtershow.imageshow.MasterImage;
import com.tct.gallery3d.filtershow.pipeline.ImagePreset;
import com.tct.gallery3d.filtershow.pipeline.ProcessingService;
import com.tct.gallery3d.filtershow.tools.SaveImage;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class ExportDialog extends DialogFragment implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {
    SeekBar mSeekBar;
    TextView mSeekVal;
    EditText mWidthText;
    EditText mHeightText;
    TextView mEstimatedSize;
    int mQuality = 95;
    int mExportWidth = 0;
    int mExportHeight = 0;
    Rect mOriginalBounds;
    int mCompressedSize;
    Rect mCompressedBounds;
    float mExportCompressionMargin = 1.1f;
    float mRatio;
    String mSliderLabel;
    boolean mEditing = false;
    Handler mHandler;
    int mUpdateDelay = 1000;
    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-4-12,ALM-1938213 begin
    ImagePreset preset;
    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-4-12,ALM-1938213 end

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-30,PR987771
    private Button mDoneButton = null;

    Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateCompressionFactor();
            updateSize();
        }
    };

    private class Watcher implements TextWatcher {
        private EditText mEditText;
        Watcher(EditText text) {
            mEditText = text;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            textChanged(mEditText);
        }
    }

    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-4-12,ALM-1938213 begin
    public ExportDialog(Rect originalBounds, ImagePreset imagePreset) {
        mOriginalBounds = originalBounds;
        preset = imagePreset;
    }
    //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-4-12,ALM-1938213 end
    /* MODIFIED-BEGIN by caihong.gu-nb, 2016-04-26,BUG-1999486*/
    public ExportDialog() {
        super();
    }
    /* MODIFIED-END by caihong.gu-nb,BUG-1999486*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mHandler = new Handler(getActivity().getMainLooper());

        View view = inflater.inflate(R.layout.filtershow_export_dialog, container);
        mSeekBar = (SeekBar) view.findViewById(R.id.qualitySeekBar);
        mSeekVal = (TextView) view.findViewById(R.id.qualityTextView);
        mSliderLabel = getString(R.string.quality) + ": ";
        mSeekBar.setProgress(mQuality);
        mSeekVal.setText(mSliderLabel + mSeekBar.getProgress());
        mSeekBar.setOnSeekBarChangeListener(this);
        mWidthText = (EditText) view.findViewById(R.id.editableWidth);
        mHeightText = (EditText) view.findViewById(R.id.editableHeight);
        mEstimatedSize = (TextView) view.findViewById(R.id.estimadedSize);

        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-4-12,ALM-1938213 begin
        //mOriginalBounds = MasterImage.getImage().getOriginalBounds();
        //[ALM][BUGFIX]-Add by TCTNJ,jun.xie-nb, 2015-4-12,ALM-1938213 end
        //[BUGFIX]-Add by TCTNJ,xiangyu.liu, 2016-03-15,PR1804268 -start
        if (mOriginalBounds != null) {
            mOriginalBounds = preset.finalGeometryRect(mOriginalBounds.width(), mOriginalBounds.height());
            mRatio = mOriginalBounds.width() / (float) mOriginalBounds.height();
            mWidthText.setText("" + mOriginalBounds.width());
            mHeightText.setText("" + mOriginalBounds.height());
            mExportWidth = mOriginalBounds.width();
            mExportHeight = mOriginalBounds.height();
            mWidthText.addTextChangedListener(new Watcher(mWidthText));
            mHeightText.addTextChangedListener(new Watcher(mHeightText));
        }
        //[BUGFIX]-Add by TCTNJ,xiangyu.liu, 2016-03-15,PR1804268 -end

        view.findViewById(R.id.cancel).setOnClickListener(this);
        view.findViewById(R.id.done).setOnClickListener(this);
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-30,PR987771
        mDoneButton = (Button)view.findViewById(R.id.done);
        getDialog().setTitle(R.string.export_flattened);

        updateCompressionFactor();
        updateSize();
        return view;
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        // Do nothing
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        // Do nothing
    }

    @Override
    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        mSeekVal.setText(mSliderLabel + arg1);
        mQuality = mSeekBar.getProgress();
        scheduleUpdateCompressionFactor();
    }

    private void scheduleUpdateCompressionFactor() {
        mHandler.removeCallbacks(mUpdateRunnable);
//        mHandler.postDelayed(mUpdateRunnable, mUpdateDelay);
        mHandler.post(mUpdateRunnable);//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-22,PR1008624
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.done:
                FilterShowActivity activity = (FilterShowActivity) getActivity();
                Uri sourceUri = MasterImage.getImage().getUri();
                File dest = SaveImage.getNewFile(activity,  activity.getSelectedImageUri());
                float scaleFactor = mExportWidth / (float) mOriginalBounds.width();
                Intent processIntent = ProcessingService.getSaveIntent(activity, MasterImage
                        .getImage().getPreset(), dest, activity.getSelectedImageUri(), sourceUri,
                        true, mSeekBar.getProgress(), scaleFactor, false);
                activity.startService(processIntent);
                dismiss();
                break;
        }
    }

    public void updateCompressionFactor() {
        Bitmap bitmap = MasterImage.getImage().getFilteredImage();
        if (bitmap == null) {
            return;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, mQuality, out);
        mCompressedSize = out.size();
        mCompressedBounds = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    public void updateSize() {
        if (mCompressedBounds == null) {
            return;
        }
        // This is a rough estimate of the final save size. There's some loose correlation
        // between a compressed jpeg and a larger version of it in function of the image
        // area. Not a perfect estimate by far.
        float originalArea = mCompressedBounds.width() * mCompressedBounds.height();
        float newArea = mExportWidth * mExportHeight;
        float factor = originalArea / (float) mCompressedSize;
        float compressedSize = newArea / factor;
        compressedSize *= mExportCompressionMargin;
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-30,PR987771 begin
        String estimatedSize = null;
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-22,PR1008624 begin
        if(getActivity() != null) {
            estimatedSize = Formatter.formatFileSize(
                    getActivity().getApplicationContext(), (long) compressedSize);
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-22,PR1008624 end
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-30,PR987771 end
        mEstimatedSize.setText(estimatedSize);
    }

    private void textChanged(EditText text) {
        if (mEditing) {
            return;
        }
        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-30,PR987771
        mDoneButton.setEnabled(true);
        mEditing = true;
        int width = 1;
        int height = 1;
        if (text.getId() == R.id.editableWidth) {
            if (mWidthText.getText() != null) {
                String value = String.valueOf(mWidthText.getText());
                if (value.length() > 0) {
                    width = Integer.parseInt(value);
                    if (width > mOriginalBounds.width()) {
                        width = mOriginalBounds.width();
                        mWidthText.setText("" + width);
                    }
                    if (width <= 0) {
                        width = (int) Math.ceil(mRatio);
                        mWidthText.setText("" + width);
                    }
                    height = (int) (width / mRatio);
                } else {
                    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-30,PR987771
                    mDoneButton.setEnabled(false);
                }
                mHeightText.setText("" + height);
            }
        } else if (text.getId() == R.id.editableHeight) {
            if (mHeightText.getText() != null) {
                String value = String.valueOf(mHeightText.getText());
                if (value.length() > 0) {
                    height = Integer.parseInt(value);
                    if (height > mOriginalBounds.height()) {
                        height = mOriginalBounds.height();
                        mHeightText.setText("" + height);
                    }
                    if (height <= 0) {
                        height = 1;
                        mHeightText.setText("" + height);
                    }
                    width = (int) (height * mRatio);
                } else {
                    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-04-30,PR987771
                    mDoneButton.setEnabled(false);
                }
                mWidthText.setText("" + width);
            }
        }
        mExportWidth = width;
        mExportHeight = height;
        updateSize();
        mEditing = false;
    }

}
