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
/* ----------|----------------------|----------------------|----------------- */
/* 26/01/2015|dongliang.feng        |PR910842              |[Android 5.0][Gallery_v5.1.4.1.0107.0] */
/*           |                      |                      |[Translation][UI]The words of draw  */
/*           |                      |                      |isn't translated */
/* ----------|----------------------|----------------------|----------------- */
/* 05/02/2015|    jialiang.ren      |      PR-910210       |[Android5.0][Gallery_v5.1.4.1.0107.0][UI]*/
/*                                                          Some icons are missing in photo editor   */
/* ----------|----------------------|----------------------|-----------------------------------------*/

package com.tct.gallery3d.filtershow.filters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.tct.gallery3d.R;
import com.tct.gallery3d.filtershow.pipeline.ImagePreset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public abstract class BaseFiltersManager implements FiltersManagerInterface {
    protected HashMap<Class, ImageFilter> mFilters = null;
    protected HashMap<String, FilterRepresentation> mRepresentationLookup = null;
    private static final String LOGTAG = "BaseFiltersManager";

    protected ArrayList<FilterRepresentation> mLooks = new ArrayList<FilterRepresentation>();
    protected ArrayList<FilterRepresentation> mBorders = new ArrayList<FilterRepresentation>();
    protected ArrayList<FilterRepresentation> mTools = new ArrayList<FilterRepresentation>();
//    protected ArrayList<FilterRepresentation> mFaceBeautys = new ArrayList<FilterRepresentation>();
    protected ArrayList<FilterRepresentation> mEffects = new ArrayList<FilterRepresentation>();
    private static int mImageBorderSize = 4; // in percent

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 begin
    private ArrayList<Integer> filtersNormalIconId = new ArrayList<Integer>();
    private ArrayList<Integer> filtersSelectedIconId = new ArrayList<Integer>();
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 end

    protected void init() {
        mFilters = new HashMap<Class, ImageFilter>();
        mRepresentationLookup = new HashMap<String, FilterRepresentation>();
        Vector<Class> filters = new Vector<Class>();
        addFilterClasses(filters);
        for (Class filterClass : filters) {
            try {
                Object filterInstance = filterClass.newInstance();
                if (filterInstance instanceof ImageFilter) {
                    mFilters.put(filterClass, (ImageFilter) filterInstance);

                    FilterRepresentation rep =
                        ((ImageFilter) filterInstance).getDefaultRepresentation();
                    if (rep != null) {
                        addRepresentation(rep);
                    }
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void addRepresentation(FilterRepresentation rep) {
        mRepresentationLookup.put(rep.getSerializationName(), rep);
    }

    public FilterRepresentation createFilterFromName(String name) {
        try {
            return mRepresentationLookup.get(name).copy();
        } catch (Exception e) {
            Log.v(LOGTAG, "unable to generate a filter representation for \"" + name + "\"");
            e.printStackTrace();
        }
        return null;
    }

    public ImageFilter getFilter(Class c) {
        return mFilters.get(c);
    }

    @Override
    public ImageFilter getFilterForRepresentation(FilterRepresentation representation) {
        return mFilters.get(representation.getFilterClass());
    }

    public FilterRepresentation getRepresentation(Class c) {
        ImageFilter filter = mFilters.get(c);
        if (filter != null) {
            return filter.getDefaultRepresentation();
        }
        return null;
    }

    public void freeFilterResources(ImagePreset preset) {
        if (preset == null) {
            return;
        }
        Vector<ImageFilter> usedFilters = preset.getUsedFilters(this);
        for (Class c : mFilters.keySet()) {
            ImageFilter filter = mFilters.get(c);
            if (!usedFilters.contains(filter)) {
                filter.freeResources();
            }
        }
    }

    public void freeRSFilterScripts() {
        for (Class c : mFilters.keySet()) {
            ImageFilter filter = mFilters.get(c);
            if (filter != null && filter instanceof ImageFilterRS) {
                ((ImageFilterRS) filter).resetScripts();
            }
        }
    }

    protected void addFilterClasses(Vector<Class> filters) {
        filters.add(ImageFilterTinyPlanet.class);
        filters.add(ImageFilterRedEye.class);
        filters.add(ImageFilterWBalance.class);
        filters.add(ImageFilterExposure.class);
        filters.add(ImageFilterVignette.class);
        filters.add(ImageFilterGrad.class);
        filters.add(ImageFilterContrast.class);
        filters.add(ImageFilterShadows.class);
        filters.add(ImageFilterHighlights.class);
        filters.add(ImageFilterVibrance.class);
        filters.add(ImageFilterSharpen.class);
        filters.add(ImageFilterCurves.class);
        filters.add(ImageFilterDraw.class);
        filters.add(ImageFilterHue.class);
        filters.add(ImageFilterChanSat.class);
        filters.add(ImageFilterSaturated.class);
        filters.add(ImageFilterBwFilter.class);
        filters.add(ImageFilterNegative.class);
        filters.add(ImageFilterEdge.class);
        filters.add(ImageFilterKMeans.class);
        filters.add(ImageFilterFx.class);
        filters.add(ImageFilterBorder.class);
        filters.add(ImageFilterColorBorder.class);
    }

    public ArrayList<FilterRepresentation> getLooks() {
        return mLooks;
    }

    public ArrayList<FilterRepresentation> getBorders() {
        return mBorders;
    }

    public ArrayList<FilterRepresentation> getTools() {
        return mTools;
    }

//    public ArrayList<FilterRepresentation> getFaceBeauty() {
//        return mFaceBeautys;
//    }
//
    public ArrayList<FilterRepresentation> getEffects() {
        return mEffects;
    }

//    public void addFaceBeauty(Context context) {
//        int[] drawid = {
//                R.drawable.filtershow_fx_0005_punch,
//                R.drawable.filtershow_fx_0000_vintage,
//                R.drawable.filtershow_fx_0004_bw_contrast,
//                R.drawable.filtershow_fx_0002_bleach,
//                R.drawable.filtershow_fx_0001_instant,
//                R.drawable.filtershow_fx_0007_washout,
//                R.drawable.filtershow_fx_0003_blue_crush,
//                R.drawable.filtershow_fx_0008_washout_color,
//                R.drawable.filtershow_fx_0006_x_process
//        };
//
//        int[] fxNameid = {
//                R.string.ffx_punch,
//                R.string.ffx_vintage,
//                R.string.ffx_bw_contrast,
//                R.string.ffx_bleach,
//                R.string.ffx_instant,
//                R.string.ffx_washout,
//                R.string.ffx_blue_crush,
//                R.string.ffx_washout_color,
//                R.string.ffx_x_process
//        };
//
//        // Do not localize.
//        String[] serializationNames = {
//                "LUT3D_PUNCH",
//                "LUT3D_VINTAGE",
//                "LUT3D_BW",
//                "LUT3D_BLEACH",
//                "LUT3D_INSTANT",
//                "LUT3D_WASHOUT",
//                "LUT3D_BLUECRUSH",
//                "LUT3D_WASHOUT_COLOR",
//                "LUT3D_XPROCESS"
//        };
//
//        FilterFxRepresentation nullFx =
//                new FilterFxRepresentation(context.getString(R.string.none),
//                        0, R.string.none);
//        mFaceBeautys.add(nullFx);
//
//        for (int i = 0; i < drawid.length; i++) {
//            FilterFxRepresentation fx = new FilterFxRepresentation(
//                    context.getString(fxNameid[i]), drawid[i], fxNameid[i]);
//            fx.setSerializationName(serializationNames[i]);
//            ImagePreset preset = new ImagePreset();
//            preset.addFilter(fx);
//            FilterUserPresetRepresentation rep = new FilterUserPresetRepresentation(
//                    context.getString(fxNameid[i]), preset, -1);
//            mFaceBeautys.add(rep);
//            addRepresentation(fx);
//        }
//    }

    public void addBorders(Context context) {

        // Do not localize
        String[] serializationNames = {
                "FRAME_4X5",
                "FRAME_BRUSH",
                "FRAME_GRUNGE",
                "FRAME_SUMI_E",
                "FRAME_TAPE",
                "FRAME_BLACK",
                "FRAME_BLACK_ROUNDED",
                "FRAME_WHITE",
                "FRAME_WHITE_ROUNDED",
                "FRAME_CREAM",
                "FRAME_CREAM_ROUNDED"
        };

        // The "no border" implementation
        int i = 0;
        FilterRepresentation rep = new FilterImageBorderRepresentation(0);
        mBorders.add(rep);

        // Regular borders
        ArrayList <FilterRepresentation> borderList = new ArrayList<FilterRepresentation>();


        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_4x5);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_brush);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_grunge);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_sumi_e);
        borderList.add(rep);

        rep = new FilterImageBorderRepresentation(R.drawable.filtershow_border_tape);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(Color.BLACK, mImageBorderSize, 0);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(Color.BLACK, mImageBorderSize,
                mImageBorderSize);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(Color.WHITE, mImageBorderSize, 0);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(Color.WHITE, mImageBorderSize,
                mImageBorderSize);
        borderList.add(rep);

        int creamColor = Color.argb(255, 237, 237, 227);
        rep = new FilterColorBorderRepresentation(creamColor, mImageBorderSize, 0);
        borderList.add(rep);

        rep = new FilterColorBorderRepresentation(creamColor, mImageBorderSize,
                mImageBorderSize);
        borderList.add(rep);

        for (FilterRepresentation filter : borderList) {
            filter.setSerializationName(serializationNames[i++]);
            addRepresentation(filter);
            mBorders.add(filter);
        }

    }

    public void addLooks(Context context) {
        int[] drawid = {
                R.drawable.filtershow_fx_0005_punch,
                R.drawable.filtershow_fx_0000_vintage,
                R.drawable.filtershow_fx_0004_bw_contrast,
                R.drawable.filtershow_fx_0002_bleach,
                R.drawable.filtershow_fx_0001_instant,
                R.drawable.filtershow_fx_0007_washout,
                R.drawable.filtershow_fx_0003_blue_crush,
                R.drawable.filtershow_fx_0008_washout_color,
                R.drawable.filtershow_fx_0006_x_process
        };

        int[] fxNameid = {
                R.string.ffx_punch,
                R.string.ffx_vintage,
                R.string.ffx_bw_contrast,
                R.string.ffx_bleach,
                R.string.ffx_instant,
                R.string.ffx_washout,
                R.string.ffx_blue_crush,
                R.string.ffx_washout_color,
                R.string.ffx_x_process
        };

        // Do not localize.
        String[] serializationNames = {
                "LUT3D_PUNCH",
                "LUT3D_VINTAGE",
                "LUT3D_BW",
                "LUT3D_BLEACH",
                "LUT3D_INSTANT",
                "LUT3D_WASHOUT",
                "LUT3D_BLUECRUSH",
                "LUT3D_WASHOUT_COLOR",
                "LUT3D_XPROCESS"
        };

        FilterFxRepresentation nullFx =
                new FilterFxRepresentation(context.getString(R.string.none),
                        0, R.string.none);
        mLooks.add(nullFx);

        for (int i = 0; i < drawid.length; i++) {
            FilterFxRepresentation fx = new FilterFxRepresentation(
                    context.getString(fxNameid[i]), drawid[i], fxNameid[i]);
            fx.setSerializationName(serializationNames[i]);
            ImagePreset preset = new ImagePreset();
            preset.addFilter(fx);
            FilterUserPresetRepresentation rep = new FilterUserPresetRepresentation(
                    context.getString(fxNameid[i]), preset, -1);
            mLooks.add(rep);
            addRepresentation(fx);
        }
    }

    public void addEffects() {
        mEffects.add(getRepresentation(ImageFilterTinyPlanet.class));
        mEffects.add(getRepresentation(ImageFilterWBalance.class));
        mEffects.add(getRepresentation(ImageFilterExposure.class));
        mEffects.add(getRepresentation(ImageFilterVignette.class));
        mEffects.add(getRepresentation(ImageFilterGrad.class));
        mEffects.add(getRepresentation(ImageFilterContrast.class));
        mEffects.add(getRepresentation(ImageFilterShadows.class));
        mEffects.add(getRepresentation(ImageFilterHighlights.class));
        mEffects.add(getRepresentation(ImageFilterVibrance.class));
        mEffects.add(getRepresentation(ImageFilterSharpen.class));
        mEffects.add(getRepresentation(ImageFilterCurves.class));
        mEffects.add(getRepresentation(ImageFilterHue.class));
        mEffects.add(getRepresentation(ImageFilterChanSat.class));
        mEffects.add(getRepresentation(ImageFilterBwFilter.class));
        mEffects.add(getRepresentation(ImageFilterNegative.class));
        mEffects.add(getRepresentation(ImageFilterEdge.class));
        mEffects.add(getRepresentation(ImageFilterKMeans.class));
    }

    public void addTools(Context context) {

        int[] textId = {
                R.string.crop,
                R.string.straighten,
                R.string.rotate,
                R.string.mirror
        };

        int[] overlayId = {
                R.drawable.ic_edit_crop,
                R.drawable.ic_edit_straighten,
                R.drawable.ic_edit_rotate,
                R.drawable.ic_edit_mirror
        };

        FilterRepresentation[] geometryFilters = {
                new FilterCropRepresentation(),
                new FilterStraightenRepresentation(),
                new FilterRotateRepresentation(),
                new FilterMirrorRepresentation()
        };

        for (int i = 0; i < textId.length; i++) {
            FilterRepresentation geometry = geometryFilters[i];
            geometry.setTextId(textId[i]);
            geometry.setOverlayId(overlayId[i]);
            geometry.setOverlayOnly(true);
            if (geometry.getTextId() != 0) {
                geometry.setName(context.getString(geometry.getTextId()));
            }
            mTools.add(geometry);
        }

        //mTools.add(getRepresentation(ImageFilterRedEye.class));
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-26, PR910842 begin
        FilterRepresentation filterRepresentation = getRepresentation(ImageFilterDraw.class);
        filterRepresentation.setName(context.getString(R.string.imageDraw));
        mTools.add(filterRepresentation);
        //[BUGFIX]-Modify by TCTNJ, dongliang.feng, 2015-01-26, PR910842 end
    }

    public void removeRepresentation(ArrayList<FilterRepresentation> list,
                                          FilterRepresentation representation) {
        for (int i = 0; i < list.size(); i++) {
            FilterRepresentation r = list.get(i);
            if (r.getFilterClass() == representation.getFilterClass()) {
                list.remove(i);
                break;
            }
        }
    }

    public void setFilterResources(Resources resources) {
        ImageFilterBorder filterBorder = (ImageFilterBorder) getFilter(ImageFilterBorder.class);
        filterBorder.setResources(resources);
        ImageFilterFx filterFx = (ImageFilterFx) getFilter(ImageFilterFx.class);
        filterFx.setResources(resources);
    }

    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 begin
    public ArrayList<Integer> getFiltersNormalIconId() {
        filtersNormalIconId.add(R.drawable.ic_adjust_autocolor_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_exposure_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_vignette_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_graduated_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_contrast_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_shadow_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_highlights_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_vibrance_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_sharpness_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_curves_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_hue_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_saturation_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_bwfilter_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_negative_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_edges_deselected);
        filtersNormalIconId.add(R.drawable.ic_adjust_posterize_deselected);

        filtersNormalIconId.add(R.drawable.ic_adjust_autocolor_deselected);

        return filtersNormalIconId;
    }

    public ArrayList<Integer> getFiltersSelectedIconId() {
        filtersSelectedIconId.add(R.drawable.ic_adjust_autocolor);
        filtersSelectedIconId.add(R.drawable.ic_adjust_exposure);
        filtersSelectedIconId.add(R.drawable.ic_adjust_vignette);
        filtersSelectedIconId.add(R.drawable.ic_adjust_graduated);
        filtersSelectedIconId.add(R.drawable.ic_adjust_contrast);
        filtersSelectedIconId.add(R.drawable.ic_adjust_shadow);
        filtersSelectedIconId.add(R.drawable.ic_adjust_highlights);
        filtersSelectedIconId.add(R.drawable.ic_adjust_vibrance);
        filtersSelectedIconId.add(R.drawable.ic_adjust_sharpness);
        filtersSelectedIconId.add(R.drawable.ic_adjust_curves);
        filtersSelectedIconId.add(R.drawable.ic_adjust_hue);
        filtersSelectedIconId.add(R.drawable.ic_adjust_saturation);
        filtersSelectedIconId.add(R.drawable.ic_adjust_bwfilter);
        filtersSelectedIconId.add(R.drawable.ic_adjust_negative);
        filtersSelectedIconId.add(R.drawable.ic_adjust_edges);
        filtersSelectedIconId.add(R.drawable.ic_adjust_posterize);

        filtersSelectedIconId.add(R.drawable.ic_adjust_autocolor);

        return filtersSelectedIconId;
    }
    //[BUGFIX]-Add by TCTNJ,jialiang.ren, 2015-02-05,PR910210 end
}
