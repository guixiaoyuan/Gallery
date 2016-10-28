package com.tct.gallery3d.filtershow.tools;

import com.tct.gallery3d.filtershow.filters.FilterRepresentation;
import com.tct.gallery3d.filtershow.filters.FilterUserPresetRepresentation;
import com.tct.gallery3d.filtershow.filters.ImageFilterContrast;
import com.tct.gallery3d.filtershow.filters.ImageFilterSharpen;
import com.tct.gallery3d.filtershow.filters.ImageFilterVibrance;
import com.tct.gallery3d.filtershow.imageshow.MasterImage;
import com.tct.gallery3d.filtershow.pipeline.ImagePreset;
import com.tct.gallery3d.ui.Log;

public class AutoEditHelper {

    private String TAG = "AutoEditHelper";

    public static boolean AUTO_EDIT_ON = false;

    public void showCurrentRepresentation(boolean isApply) {
        AUTO_EDIT_ON = isApply;
        ImagePreset oldPreset = MasterImage.getImage().getPreset();

        int contrastValue = getCurrentContrastValue(oldPreset,
                ImageFilterContrast.SERIALIZATION_NAME);
        int vibranceValue = getCurrentContrastValue(oldPreset,
                ImageFilterVibrance.SERIALIZATION_NAME);
        int sharpenValue = getCurrentContrastValue(oldPreset, ImageFilterSharpen.SERIALIZATION_NAME);

        StringBuilder builder = new StringBuilder();
        builder.append("{\"CONTRAST\":{\"Name\":\"Contrast\",\"Value\":\"" + contrastValue + "\"}");
        builder.append(",\"VIBRANCE\":{\"Name\":\"Vibrance\",\"Value\":\"" + vibranceValue + "\"}");
        builder.append(",\"SHARPEN\":{\"Name\":\"Sharpness\",\"Value\":\"" + sharpenValue + "\"}}");

        Log.i(TAG, "showCurrentRepresentation json = " + builder.toString());
        oldPreset.readJsonFromString(builder.toString());
        FilterUserPresetRepresentation representation = new FilterUserPresetRepresentation(TAG,
                oldPreset, -1);

        MasterImage.getImage().setPreset(oldPreset, representation, true);
        MasterImage.getImage().setCurrentFilterRepresentation(representation);
    }

    public int getCurrentContrastValue(ImagePreset oldPreset, String name) {
        FilterRepresentation contrastRepresentation = oldPreset
                .getFilterWithSerializationName(name);
        String value = "0";
        if (contrastRepresentation != null) {
            value = contrastRepresentation.getStateRepresentation();
        }
        Log.i(TAG, "showCurrentRepresentation value == " + value + " name = :" + name);
        if (contrastRepresentation != null) {
            oldPreset.removeFilter(contrastRepresentation);
        }
        return Integer.parseInt(value);
    }

    public void resetAutoEditStatus() {
        AUTO_EDIT_ON = false;
    }
}
