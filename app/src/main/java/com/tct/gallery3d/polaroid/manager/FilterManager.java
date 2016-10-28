package com.tct.gallery3d.polaroid.manager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import com.tct.gallery3d.R;
import com.tct.gallery3d.polaroid.Poladroid;

public class FilterManager {
    static final String ORIGINAL = "original";

    private static boolean mInitDone = false;
    private static HashMap<String, Filter> mFilters = new HashMap<String, Filter>();
    private static ArrayList<Filter> mSortedFilters = new ArrayList<Filter>();

    public static Filter getFilter(String name) {
        return mFilters.get(name);
    }

    public static Filter getFilter(int index) {
        if (index >= 0 && index < mSortedFilters.size()) {
            return mSortedFilters.get(index);
        }
        return null;
    }

    // Read the filter list from JSON file and double-check resources actually
    // exist
    public static void init(Context context) {
        if (mInitDone)
            return;
        mInitDone = true;

        Log.d(Poladroid.TAG, "FilterManager.init(){");

        JsonReader reader = null;
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.filters);
            reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

            reader.beginArray();
            while (reader.hasNext()) {
                Filter filter = readFilter(reader, context);
                if (filter != null) {
                    mFilters.put(filter.getName(), filter);
                    mSortedFilters.add(filter);
                }
            }
            reader.endArray();
        } catch (Exception e) {
            e.printStackTrace();
            mFilters.clear();
            mSortedFilters.clear();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Log.d(Poladroid.TAG, "} FilterManager.init() => filter count: " + mFilters.size());
    }

    private static Filter readFilter(JsonReader reader, Context context) throws Exception {
        String name = null;
        String frameName = null;
        // Use empty filterOperations by default, it's better than null (i.e.
        // existing token but invalid content)
        ArrayList<FilterOperation> filterOperations = new ArrayList<FilterOperation>();

        reader.beginObject();
        while (reader.hasNext()) {
            String token = reader.nextName();
            if (token.equals("name")) {
                name = reader.nextString();
            } else if (token.equals("frame")) {
                frameName = reader.nextString();
            } else if (token.equals("operations")) {
                filterOperations = readOperations(reader, context);
            } else {
                Log.i(Poladroid.TAG, "*** FilterManager.readFilter(): unknown token '" + token + "'");
                reader.skipValue();
            }
        }
        reader.endObject();

        if (name == null || filterOperations == null) {
            Log.i(Poladroid.TAG,
                    "*** FilterManager.readFilter(): missing 'name' attribute or filterOperations");
            return null;
        }

        return new Filter(name, frameName, filterOperations);
    }

    private static ArrayList<FilterOperation> readOperations(JsonReader reader, Context context)
            throws Exception {
        ArrayList<FilterOperation> operations = new ArrayList<FilterOperation>();
        boolean valid = true;

        reader.beginArray();
        while (reader.hasNext()) {
            FilterOperation operation = readOperation(reader, context);
            if (operation == null) {
                valid = false;
            } else {
                operations.add(operation);
            }
        }
        reader.endArray();
        if (!valid) {
            Log.i(Poladroid.TAG, "*** FilterManager.readOperations(): invalid operation found");
            return null;
        }
        return operations;
    }

    private static FilterOperation readOperation(JsonReader reader, Context context)
            throws Exception {
        FilterOperation operation = null;
        BlendMode blendMode = BlendMode.NORMAL;
        float opacity = 1.00f;

        reader.beginObject();
        while (reader.hasNext()) {
            String token = reader.nextName();
            if (token.equals("lut-operation")) {
                operation = readLutOperation(reader, context);
            } else if (token.equals("texture-operation")) {
                operation = readTextureOperation(reader, context);
            } else if (token.equals("opacity")) {
                opacity = (float) reader.nextDouble();
                if (opacity < 0.0f || opacity > 1.0f) {
                    Log.i(Poladroid.TAG, "*** FilterManager.readOperation(): invalid opacity value '"
                            + opacity + "' (expected [ 0.00, 1.00 ])");
                    opacity = (float) Math.max(0.0, Math.min(1.0, opacity));
                }
            } else if (token.equals("blend-mode")) {
                String blendModeString = reader.nextString();
                blendMode = BlendMode.parseBlendMode(blendModeString);
                if (blendMode == BlendMode.UNKNOWN) {
                    Log.i(Poladroid.TAG, "*** FilterManager.readOperation(): invalid blend mode '"
                            + blendModeString + "'");
                }
            } else {
                Log.i(Poladroid.TAG, "*** FilterManager.readOperation(): unknown token '" + token + "'");
                reader.skipValue();
            }
        }
        reader.endObject();

        if (operation == null || blendMode == BlendMode.UNKNOWN) {
            Log.i(Poladroid.TAG,
                    "*** FilterManager.readOperation(): missing 'xxx-operation' attribute or invalid blendMode");
            operation = null;
        } else {
            operation.mBlendMode = blendMode;
            operation.mOpacity = opacity;
        }

        return operation;
    }

    private static FilterOperation readLutOperation(JsonReader reader, Context context)
            throws Exception {
        ArrayList<LutResource> lutResources = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String token = reader.nextName();
            if (token.equals("resources")) {
                lutResources = readLutResources(reader, context);
            } else {
                Log.i(Poladroid.TAG, "*** FilterManager.readLutOperation(): unknown token '" + token + "'");
                reader.skipValue();
            }
        }
        reader.endObject();

        if (lutResources == null || lutResources.isEmpty()) {
            Log.i(Poladroid.TAG, "*** FilterManager.readLutOperation(): missing or empty LUT set");
            return null;
        }

        return new LutFilterOperation(lutResources);
    }

    private static ArrayList<LutResource> readLutResources(JsonReader reader, Context context)
            throws Exception {
        ArrayList<LutResource> lutResources = new ArrayList<LutResource>();

        reader.beginArray();
        while (reader.hasNext()) {
            LutResource lutResource = readLutResource(reader, context);
            if (lutResource != null) {
                lutResources.add(lutResource);
            }
        }
        reader.endArray();

        if (lutResources.isEmpty()) {
            Log.i(Poladroid.TAG, "*** FilterManager.readLutResources(): empty LUT Resources");
            return null;
        }

        return lutResources;
    }

    private static LutResource readLutResource(JsonReader reader, Context context) throws Exception {
        int resId = 0;
        int size = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String token = reader.nextName();
            if (token.equals("res-id")) {
                String resIdString = reader.nextString();
                resId = context.getResources().getIdentifier(resIdString, "drawable",
                        context.getPackageName());
                if (resId == 0) {
                    Log.i(Poladroid.TAG, "*** FilterManager.readLutResource(): cannot find R.drawable."
                            + resIdString);
                }
            } else if (token.equals("size")) {
                size = reader.nextInt();
                if (size <= 0) {
                    Log.i(Poladroid.TAG, "*** FilterManager.readLutResource(): invalid LUT size " + size
                            + " (expecting between 5 and 129)");
                    size = 0;
                }
            } else {
                Log.i(Poladroid.TAG, "*** FilterManager.readLutResource(): unknown token '" + token + "'");
                reader.skipValue();
            }
        }
        reader.endObject();

        if (resId == 0 || size == 0) {
            Log.i(Poladroid.TAG,
                    "*** FilterManager.readLutResource(): missing or invalid resId and / or size attributes");
            return null;
        }

        return new LutResource(resId, size);
    }

    private static FilterOperation readTextureOperation(JsonReader reader, Context context)
            throws Exception {
        ArrayList<TextureResource> textureResources = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String token = reader.nextName();
            if (token.equals("resources")) {
                textureResources = readTextureOperationResources(reader, context);
            } else {
                Log.i(Poladroid.TAG, "*** FilterManager.readTextureOperation(): unknown token '" + token
                        + "'");
                reader.skipValue();
            }
        }
        reader.endObject();

        if (textureResources == null || textureResources.isEmpty()) {
            Log.i(Poladroid.TAG, "*** FilterManager.readTextureOperation(): missing or empty texture set");
            return null;
        }

        return new TextureFilterOperation(textureResources);
    }

    private static ArrayList<TextureResource> readTextureOperationResources(JsonReader reader,
            Context context) throws Exception {
        ArrayList<TextureResource> textureResources = new ArrayList<TextureResource>();

        reader.beginArray();
        while (reader.hasNext()) {
            TextureResource textureResource = readTextureOperationResource(reader, context);
            if (textureResource != null) {
                textureResources.add(textureResource);
            }
        }
        reader.endArray();

        if (textureResources.isEmpty()) {
            Log.i(Poladroid.TAG, "*** FilterManager.readTextureOperationResources(): empty texture set");
            return null;
        }

        return textureResources;
    }

    private static TextureResource readTextureOperationResource(JsonReader reader, Context context)
            throws Exception {
        int resId = 0;
        int targetWidth = 0, targetHeight = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String token = reader.nextName();
            if (token.equals("res-id")) {
                String resIdString = reader.nextString();
                resId = context.getResources().getIdentifier(resIdString, "drawable",
                        context.getPackageName());
                if (resId == 0) {
                    Log.i(Poladroid.TAG, "*** FilterManager.readTextureResource(): invalid target width "
                            + targetWidth);
                }
            } else if (token.equals("target-width")) {
                targetWidth = reader.nextInt();
                if (targetWidth < 0) {
                    Log.i(Poladroid.TAG, "*** FilterManager.readTextureResource(): invalid target height "
                            + targetHeight);
                    targetWidth = 0;
                }
            } else if (token.equals("target-height")) {
                targetHeight = reader.nextInt();
                if (targetHeight < 0) {
                    Log.i(Poladroid.TAG,
                            "*** FilterManager.readTextureOperationResource(): invalid target height "
                                    + targetHeight);
                    targetHeight = 0;
                }
            } else {
                Log.i(Poladroid.TAG, "*** FilterManager.readTextureResource(): unknown token '" + token
                        + "'");
                reader.skipValue();
            }
        }
        reader.endObject();

        if (resId == 0 || targetWidth == 0 || targetHeight == 0) {
            Log.i(Poladroid.TAG,
                    "*** FilterManager.readTextureResource(): missing or invalid resId and / or target size attributes");
            return null;
        }

        return new TextureResource(resId, targetWidth, targetHeight);
    }

    // This is NOT an overridden function, but it works just the same...
    public static Iterator<Filter> iterator() {
        return mSortedFilters.iterator();
    }
}

/* EOF */
