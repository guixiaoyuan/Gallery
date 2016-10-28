/*
 * Copyright (C) 2011 The Android Open Source Project
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
/* ---------------------------------------------------------------------------------- */
/* 01/13/2015|    jialiang.ren      |      PR-900568       |[Gallery]Time of picture  */
/*           |                      |                      |cannot display compeletely*/
/* ----------|----------------------|----------------------|------------------------- */
/* 04/22/2015| jian.pan1            | CR979742             |[5.0][Gallery] picture detail should show "Date taken" instead of current "Time"
/* ----------|----------------------|----------------------|----------------- */
/* 20/05/2015 |    jialiang.ren     |      PR-1005882         |[Gallery]check the picture of details, gallery FC*/
/*------------|---------------------|------------------------|--------------------------------------------------*/
/* 07/09/2015|dongliang.feng        |PR1080140             |[UE][Gallery]The time display wrong */
/* ----------|----------------------|----------------------|----------------- */
/* 12/03/2015| jian.pan1            | [ALM]Defect:1020140  |[Android6.0][Gallery_v5.2.4.1.0317.0][Force Close]The gallery will happen FC after check the picture details
/* ----------|----------------------|----------------------|----------------- */

package com.tct.gallery3d.ui;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tct.gallery3d.R;
import com.tct.gallery3d.data.MediaDetails;
import com.tct.gallery3d.ui.DetailsAddressResolver.AddressResolvingListener;
import com.tct.gallery3d.ui.DetailsHelper.CloseListener;
import com.tct.gallery3d.ui.DetailsHelper.DetailsSource;
import com.tct.gallery3d.ui.DetailsHelper.DetailsViewContainer;
import com.tct.gallery3d.ui.DetailsHelper.ResolutionResolvingListener;
import com.tct.gallery3d.util.GalleryUtils;

public class DialogDetailsView implements DetailsViewContainer {
    @SuppressWarnings("unused")
    private static final String TAG = "DialogDetailsView";

    private final Activity mActivity;
    private DetailsAdapter mAdapter;
    private MediaDetails mDetails;
    private final DetailsSource mSource;
    private int mIndex;
    private Dialog mDialog;
    private CloseListener mListener;

    public DialogDetailsView(Activity activity, DetailsSource source) {
        mActivity = activity;
        mSource = source;
    }

    @Override
    public void show() {
        reloadDetails();
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-03,Defect:1020140 begin
        if (mDialog != null) {
            mDialog.show();
        } else {
            Log.e(TAG, "show() mDialog is null.");
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-03,Defect:1020140 end
    }

    @Override
    public void hide() {
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-03,Defect:1020140 begin
        if (mDialog != null) {
            mDialog.hide();
        } else {
            Log.e(TAG, "hide() mDialog is null.");
        }
        // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-03,Defect:1020140 end
    }

    @Override
    public boolean isShowing() {
        return mDialog.isShowing();
    }
    @Override
    public void reloadDetails() {
        int index = mSource.getIndex();
        if (index == -1) return;
        MediaDetails details = mSource.getDetails();
        if (details != null) {
            if (mIndex == index && mDetails == details) return;
            mIndex = index;
            mDetails = details;
            setDetails(details);
        } else {
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-03,Defect:1020140 begin
            Log.i(TAG, "reloadDetails() details is null.");
            // [ALM][BUGFIX]-Add by TCTNJ,jian.pan1, 2015-12-03,Defect:1020140 end
        }
    }

    private void setDetails(MediaDetails details) {
        mAdapter = new DetailsAdapter(details);
        String title = String.format(
                mActivity.getString(R.string.details),
                mIndex + 1, mSource.size());
        ListView detailsList = (ListView) LayoutInflater.from(mActivity).inflate(
                R.layout.details_list, null, false);
        detailsList.setAdapter(mAdapter);
        mDialog = new AlertDialog.Builder(mActivity)
            .setView(detailsList)
            .setTitle(title)
//            .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int whichButton) {
//                    mDialog.dismiss();
//                }
//            })
            .create();

        mDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mListener != null) {
                    mListener.onClose();
                }
            }
        });
    }


    private class DetailsAdapter extends BaseAdapter
        implements AddressResolvingListener, ResolutionResolvingListener {
        private final ArrayList<String> mItems;
        private int mLocationIndex;
        private final Locale mDefaultLocale = Locale.getDefault();
        private final DecimalFormat mDecimalFormat = new DecimalFormat(".####");
        private int mWidthIndex = -1;
        private int mHeightIndex = -1;

        public DetailsAdapter(MediaDetails details) {
            Context context = mActivity.getApplicationContext();
            mItems = new ArrayList<String>(details.size());
            mLocationIndex = -1;
            setDetails(context, details);
        }

        private void setDetails(Context context, MediaDetails details) {
            boolean resolutionIsValid = true;
            String path = null;
            for (Entry<Integer, Object> detail : details) {
                String value = "";//[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-20,PR1005882
                switch (detail.getKey()) {
                    case MediaDetails.INDEX_LOCATION: {
                        double[] latlng = (double[]) detail.getValue();
                        mLocationIndex = mItems.size();
                        value = DetailsHelper.resolveAddress(mActivity, latlng, this);
                        break;
                    }
                    case MediaDetails.INDEX_SIZE: {
                        value = Formatter.formatFileSize(
                                context, (Long) detail.getValue());
                        break;
                    }
                    case MediaDetails.INDEX_WHITE_BALANCE: {
                        value = "1".equals(detail.getValue())
                                ? context.getString(R.string.manual)
                                : context.getString(R.string.auto);
                        break;
                    }
                    case MediaDetails.INDEX_FLASH: {
                        MediaDetails.FlashState flash =
                                (MediaDetails.FlashState) detail.getValue();
                        // TODO: camera doesn't fill in the complete values, show more information
                        // when it is fixed.
                        if (flash.isFlashFired()) {
                            value = context.getString(R.string.flash_on);
                        } else {
                            value = context.getString(R.string.flash_off);
                        }
                        break;
                    }
                    case MediaDetails.INDEX_EXPOSURE_TIME: {
                        value = (String) detail.getValue();
                        double time = Double.valueOf(value);
                        if (time < 1.0f) {
                            value = String.format(mDefaultLocale, "%d/%d", 1,
                                    (int) (0.5f + 1 / time));
                        } else {
                            int integer = (int) time;
                            time -= integer;
                            value = String.valueOf(integer) + "''";
                            if (time > 0.0001) {
                                value += String.format(mDefaultLocale, " %d/%d", 1,
                                        (int) (0.5f + 1 / time));
                            }
                        }
                        break;
                    }
                    case MediaDetails.INDEX_WIDTH:
                        mWidthIndex = mItems.size();
                        if (detail.getValue().toString().equalsIgnoreCase("0")) {
                            value = context.getString(R.string.unknown);
                            resolutionIsValid = false;
                        } else {
                            value = toLocalInteger(detail.getValue());
                        }
                        break;
                    case MediaDetails.INDEX_HEIGHT: {
                        mHeightIndex = mItems.size();
                        if (detail.getValue().toString().equalsIgnoreCase("0")) {
                            value = context.getString(R.string.unknown);
                            resolutionIsValid = false;
                        } else {
                            value = toLocalInteger(detail.getValue());
                        }
                        break;
                    }
                    case MediaDetails.INDEX_PATH:
                        // Prepend the new-line as a) paths are usually long, so
                        // the formatting is better and b) an RTL UI will see it
                        // as a separate section and interpret it for what it
                        // is, rather than trying to make it RTL (which messes
                        // up the path).
                        value = "\n" + detail.getValue().toString();
                        path = detail.getValue().toString();
                        break;
                    case MediaDetails.INDEX_ISO:
                        value = toLocalNumber(Integer.parseInt((String) detail.getValue()));
                        break;
                    case MediaDetails.INDEX_FOCAL_LENGTH:
                        double focalLength = Double.parseDouble(detail.getValue().toString());
                        value = toLocalNumber(focalLength);
                        break;
                    case MediaDetails.INDEX_ORIENTATION:
                        value = toLocalInteger(detail.getValue());
                        break;
                        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-22,CR979742 begin
                    case MediaDetails.INDEX_DATETIME:
                        value = detail.getValue().toString();
                        Log.i(TAG, "INDEX_DATETIME:" + value);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss");
                        try {
                            Date date = sdf.parse(value);
                          //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-09-07, PR1080140 begin
                            if (GalleryUtils.getSystemDateFormat() != null) {
                                String dateString = GalleryUtils.getSystemDateFormat().format(date);
                                String timeString = null;
                                if (GalleryUtils.getSystemIs24HourFormat()) {
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                                    timeString = timeFormat.format(date);
                                } else {
                                    SimpleDateFormat formater = new SimpleDateFormat("hh:mm:ss a");
                                    timeString = formater.format(date);
                                }
                                value = dateString + " " + timeString;
                            } else {
                                DateFormat formater = DateFormat.getDateTimeInstance();
                                value = formater.format(date);
                            }
                          //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-09-07, PR1080140 end
                            Log.i(TAG, "INDEX_DATETIME format result:" + value);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                        //[BUGFIX]-Add by TCTNJ,jian.pan1, 2015-04-22,CR979742 end
                    default: {
                        Object valueObj = detail.getValue();
                        // This shouldn't happen, log its key to help us diagnose the problem.
                        /*if (valueObj == null) {
                            Utils.fail("%s's value is Null",
                                    DetailsHelper.getDetailsName(context, detail.getKey()));
                        }*/
                        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-20,PR1005882 begin
                        if(valueObj == null) {
                            Log.e(TAG, "The value is null which key is " + detail.getKey());
                        } else {
                            value = valueObj.toString();
                        }
                        //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-05-20,PR1005882 end
                    }
                }
                int key = detail.getKey();
                if (details.hasUnit(key)) {
                //[BUGFIX]-MOD by TSNJ,yuanxi.jiang, 2016-01-14,Defect:1426570  begin
                    value = String.format("%s:%s %s", DetailsHelper.getDetailsName(
                            context, key), value, context.getString(details.getUnit(key)));
                } else {
                    value = String.format("%s:%s", DetailsHelper.getDetailsName(
                            context, key), value);
                }
                mItems.add(value);
                //[BUGFIX]-MOD by TSNJ,yuanxi.jiang, 2016-01-14,Defect:1426570  end
            }
            if (!resolutionIsValid) {
                DetailsHelper.resolveResolution(path, this);
            }
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mDetails.getDetail(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView txTitle = null;
            TextView txContent = null;
            convertView = LayoutInflater.from(mActivity.getApplicationContext()).inflate(
                    R.layout.details, null);
            if (convertView != null) {
                txTitle = (TextView)convertView.findViewById(R.id.tx_title);
                txContent = (TextView)convertView.findViewById(R.id.tx_content);
            } else {
                txTitle = new TextView(mActivity);
                txContent = new TextView(mActivity);
            }
            //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-13,PR900568 begin
            String string = mItems.get(position);
            int index = string.indexOf(":");
            String title = string.substring(0, index);
            String content = string.substring(index + 1, string.length());
            txTitle.setText(title);
            txContent.setText(content);
            //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-01-13,PR900568 end

            return convertView;
        }

        @Override
        public void onAddressAvailable(String address) {
            mItems.set(mLocationIndex, address);
            notifyDataSetChanged();
        }

        @Override
        public void onResolutionAvailable(int width, int height) {
            if (width == 0 || height == 0) return;
            // Update the resolution with the new width and height
            Context context = mActivity.getApplicationContext();
            String widthString = String.format(mDefaultLocale, "%s: %d",
                    DetailsHelper.getDetailsName(
                            context, MediaDetails.INDEX_WIDTH), width);
            String heightString = String.format(mDefaultLocale, "%s: %d",
                    DetailsHelper.getDetailsName(
                            context, MediaDetails.INDEX_HEIGHT), height);
            mItems.set(mWidthIndex, String.valueOf(widthString));
            mItems.set(mHeightIndex, String.valueOf(heightString));
            notifyDataSetChanged();
        }

        /**
         * Converts the given integer (given as String or Integer object) to a
         * localized String version.
         */
        private String toLocalInteger(Object valueObj) {
            if (valueObj instanceof Integer) {
                return toLocalNumber((Integer) valueObj);
            } else {
                String value = valueObj.toString();
                try {
                    value = toLocalNumber(Integer.parseInt(value));
                } catch (NumberFormatException ex) {
                    // Just keep the current "value" if we cannot
                    // parse it as a fallback.
                }
                return value;
            }
        }

        /** Converts the given integer to a localized String version. */
        private String toLocalNumber(int n) {
            return String.format(mDefaultLocale, "%d", n);
        }

        /** Converts the given double to a localized String version. */
        private String toLocalNumber(double n) {
            return mDecimalFormat.format(n);
        }
    }

    @Override
    public void setCloseListener(CloseListener listener) {
        mListener = listener;
    }
}
