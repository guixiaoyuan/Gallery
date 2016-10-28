package com.tct.gallery3d.polaroid.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.JsonReader;
import android.util.Log;

import com.tct.gallery3d.R;
import com.tct.gallery3d.polaroid.Poladroid;
import com.tct.gallery3d.polaroid.manager.FrameResource.SloganMargin;

public class FrameManager {

    // Each frame is composed of one or more resources, matching different
    // preview / dpi / saving resolutions
    // Resource images of different sizes being slightly different from other
    // ones,
    // each of them must specify the exact location of the image and the user
    // comment
    // This can be extended to location and date/time info as well
    // This also allows to have several formats of frame

    // --- The Frame Manager starts here ---
    private static boolean mInitDone = false;
    private static HashMap<String, Frame> mFrames = new HashMap<String, Frame>();
    private static ArrayList<Frame> mSortedFrames = new ArrayList<Frame>();

    public static Frame getFrame(String name) {
        Frame frame = mFrames.get(name);
        if (frame == null) {
            Log.i(Poladroid.TAG, "*** FrameManager.getFrame(" + name
                    + "): cannot find frame with such name");
        }
        return frame;
    }

    public static Frame getFrame(int index) {
        if (index < 0 || index >= mSortedFrames.size()) {
            Log.i(Poladroid.TAG, "*** FrameManager.getFrame(" + index + "): index out of range");
            return null;
        }

        return mSortedFrames.get(index);
    }

    public static int getIndexOfFrame(Frame frame) {
        int index = mSortedFrames.indexOf(frame);
        return index;
    }

    public static int getIndexOfFrame(String name) {
        Frame frame = mFrames.get(name);
        return getIndexOfFrame(frame);
    }

    // Read the frame list from JSON file and double-check resources actually
    // exist
    public static void init(Context context) {
        if (mInitDone)
            return;
        mInitDone = true;

        Log.d(Poladroid.TAG, "FrameManager.init(){");

        JsonReader reader = null;
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.frames);
            reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

            reader.beginArray();
            while (reader.hasNext()) {
                Frame frame = readFrame(reader, context);
                if (frame != null) {
                    mFrames.put(frame.getName(), frame);
                    mSortedFrames.add(frame);
                }
            }
            reader.endArray();
        } catch (Exception e) {
            e.printStackTrace();
            mFrames.clear();
            mSortedFrames.clear();
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

        Log.d(Poladroid.TAG, "} FrameManager.init() => frame count: " + mFrames.size());
    }

    private static Frame readFrame(JsonReader reader, Context context) throws Exception {
        String name = null;
        String filterName = null;
        Font font = null;
        int sloganFontColor = Color.TRANSPARENT;
        int sloganBorderColor = Color.TRANSPARENT;
        ArrayList<FrameResource> frameResources = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String token = reader.nextName();
            if (token.equals("name")) {
                name = reader.nextString();
            } else if (token.equals("filter")) {
                filterName = reader.nextString();
            } else if (token.equals("font")) {
                String fontName = reader.nextString();
                font = FontManager.getFont(fontName);
            } else if (token.equals("slogan-font-color")) {
                sloganFontColor = readColor(reader, context);
            } else if (token.equals("slogan-border-color")) {
                sloganBorderColor = readColor(reader, context);
            } else if (token.equals("resources")) {
                frameResources = readResources(reader, context);
            } else {
                Log.i(Poladroid.TAG, "*** FrameManager.readFrame(): unknown token '" + token + "'");
                reader.skipValue();
            }
        }
        reader.endObject();

        if (name == null || frameResources == null || frameResources.isEmpty()) {
            Log.i(Poladroid.TAG, "*** FrameManager.readFrame(): missing 'name' attribute or resources");
            return null;
        }

        return new Frame(name, filterName, font, sloganFontColor, sloganBorderColor, frameResources);
    }

    private static int readColor(JsonReader reader, Context context) throws IOException {
        String colorString = reader.nextString();
        if (colorString.length() != 6) {
            Log.i(Poladroid.TAG, "*** FrameManager.readColor(): invalid color lengh '" + colorString + "'");
            return Color.TRANSPARENT;
        }
        return 0xFF000000 | Integer.parseInt(colorString, 16);
    }

    private static Rect readLocation(JsonReader reader, Context context) throws IOException {
        final int INVALID_VALUE = -1000000;
        int left = INVALID_VALUE, top = INVALID_VALUE, right = INVALID_VALUE, bottom = INVALID_VALUE;

        reader.beginObject();
        while (reader.hasNext()) {
            String token = reader.nextName();
            if (token.equals("left")) {
                left = reader.nextInt();
            } else if (token.equals("top")) {
                top = reader.nextInt();
            } else if (token.equals("right")) {
                right = reader.nextInt();
            } else if (token.equals("bottom")) {
                bottom = reader.nextInt();
            } else {
                Log.i(Poladroid.TAG, "*** FrameManager.readLocation(): unknown token '" + token + "'");
                reader.skipValue();
            }
        }
        reader.endObject();

        if (left != INVALID_VALUE && top != INVALID_VALUE && right != INVALID_VALUE
                && bottom != INVALID_VALUE) {
            return new Rect(left, top, right, bottom);
        } else {
            Log.i(Poladroid.TAG, "*** FrameManager.readLocation(): missing one or more fields");
            return null;
        }
    }

    private static Point readResolution(JsonReader reader, Context context) throws IOException {
        final int INVALID_VALUE = -1000000;
        int width = INVALID_VALUE, height = INVALID_VALUE;

        reader.beginObject();
        while (reader.hasNext()) {
            String token = reader.nextName();
            if (token.equals("width")) {
                width = reader.nextInt();
            } else if (token.equals("height")) {
                height = reader.nextInt();
            } else {
                Log.i(Poladroid.TAG, "*** FrameManager.readResolution(): unknown token '" + token + "'");
                reader.skipValue();
            }
        }
        reader.endObject();

        if (width != INVALID_VALUE && height != INVALID_VALUE) {
            return new Point(width, height);
        } else {
            Log.i(Poladroid.TAG, "*** FrameManager.readResolution(): missing one or more fields");
            return null;
        }
    }

    private static SloganMargin readSloganMargin(JsonReader reader, Context context) throws IOException {
        SloganMargin margin = new SloganMargin();

        reader.beginObject();
        while (reader.hasNext()) {
            String token = reader.nextName();
            if (token.equals("left")) {
                margin.left = reader.nextInt();
            } else if (token.equals("right")) {
                margin.right = reader.nextInt();
            }else if (token.equals("top")) {
                margin.top = reader.nextInt();
            }else if (token.equals("bottom")) {
                margin.bottom = reader.nextInt();
            } else {
                Log.e(Poladroid.TAG, "*** FrameManager.readSloganMargin(): unknown token '" + token + "'");
                reader.skipValue();
            }
        }
        reader.endObject();
        return margin;
    }

    private static ArrayList<FrameResource> readResources(JsonReader reader, Context context)
            throws Exception {
        ArrayList<FrameResource> resources = new ArrayList<FrameResource>();

        reader.beginArray();
        while (reader.hasNext()) {
            FrameResource resource = readResource(reader, context);
            if (resource != null) {
                resources.add(resource);
            }
        }
        reader.endArray();

        if (resources.isEmpty()) {
            Log.i(Poladroid.TAG, "*** FrameManager.readResources(): empty resources");
            return null;
        }

        return resources;
    }

    private static FrameResource readResource(JsonReader reader, Context context) throws Exception {
        int fgResId = 0, bgResId = 0;
        Point targetResolution = null;
        Rect pictureLocation = null;
        SloganMargin margin = new SloganMargin();
        float sloganFontSize = 0.0f;
        float sloganBorderSize = 0.0f;
        float tagFontSize = 0.0f;

        reader.beginObject();
        while (reader.hasNext()) {
            String token = reader.nextName();
            if (token.equals("res-id") || token.equals("fg-res-id")) {
                String resIdString = reader.nextString();
                fgResId = context.getResources().getIdentifier(resIdString, "drawable",
                        context.getPackageName());
                if (fgResId == 0) {
                    Log.i(Poladroid.TAG,
                            "*** FrameManager.readResource(): cannot find FG resource R.drawable."
                                    + resIdString);
                }
            } else if (token.equals("bg-res-id")) {
                String resIdString = reader.nextString();
                bgResId = context.getResources().getIdentifier(resIdString, "drawable",
                        context.getPackageName());
                if (bgResId == 0) {
                    Log.i(Poladroid.TAG,
                            "*** FrameManager.readResource(): cannot find BG resource R.drawable."
                                    + resIdString);
                }
            } else if (token.equals("target-resolution")) {
                targetResolution = readResolution(reader, context);
            } else if (token.equals("picture-location")) {
                pictureLocation = readLocation(reader, context);
            } else if (token.equals("slogan-margin")) {
                margin = readSloganMargin(reader, context);
            } else if (token.equals("slogan-font-size")) {
                sloganFontSize = (float) reader.nextDouble();
                if (sloganFontSize < 0.0f) {
                    Log.i(Poladroid.TAG, "*** FrameManager.readResource(): invalid slogan size '"
                            + sloganFontSize + "'");
                    sloganFontSize = 0.0f;
                }
            } else if (token.equals("slogan-border-size")) {
                sloganBorderSize = (float) reader.nextDouble();
                if (sloganBorderSize < 0.0f) {
                    Log.i(Poladroid.TAG, "*** FrameManager.readResource(): invalid slogan boredrsize '"
                            + sloganBorderSize + "'");
                    sloganBorderSize = 0.0f;
                }
            } else if (token.equals("tag-font-size")) {
                tagFontSize = (float) reader.nextDouble();
                if (tagFontSize < 0.0f) {
                    Log.i(Poladroid.TAG, "*** FrameManager.readResource(): invalid tag size '"
                            + tagFontSize + "'");
                    tagFontSize = 0.0f;
                }
            } else {
                Log.i(Poladroid.TAG, "*** FrameManager.readResource(): unknown token '" + token + "'");
                reader.skipValue();
            }
        }
        reader.endObject();

        if (fgResId == 0 || targetResolution == null || pictureLocation == null
                || sloganFontSize == 0.0f) {
            Log.i(Poladroid.TAG, "*** FrameManager.readResource(): one or more missing fields");
            return null;
        }

        return new FrameResource(bgResId, fgResId, targetResolution, pictureLocation, margin,
                sloganFontSize, sloganBorderSize, tagFontSize);
    }

    // This is NOT an overridden function, but it works just the same...
    public static Iterator<Frame> iterator() {
        return mSortedFrames.iterator();
    }
}

/* EOF */

