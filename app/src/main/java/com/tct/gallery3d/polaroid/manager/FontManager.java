package com.tct.gallery3d.polaroid.manager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.graphics.Typeface;
import android.util.JsonReader;
import android.util.Log;

import com.tct.gallery3d.R;
import com.tct.gallery3d.polaroid.Poladroid;

public class FontManager {

    // --- The Font Manager starts here ---
    private static boolean mInitDone = false;
    private static HashMap<String, Font> mFonts = new HashMap<String, Font>();
    private static ArrayList<Font> mSortedFonts = new ArrayList<Font>();

    public static Font getFont(String name) {
        Font frame = mFonts.get(name);
        if (frame == null) {
            Log.i(Poladroid.TAG, "*** FontManager.getFont(" + name + "): cannot find frame with such name");
        }
        return frame;
    }

    public static Font getFont(int index) {
        if (index < 0 || index >= mSortedFonts.size()) {
            Log.i(Poladroid.TAG, "*** FontManager.getFont(" + index + "): index out of range");
            return null;
        }

        return mSortedFonts.get(index);
    }

    // Read the frame list from JSON file and double-check resources actually
    // exist
    public static void init(Context context) {
        if (mInitDone)
            return;
        mInitDone = true;

        Log.d(Poladroid.TAG, "FontManager.init(){");

        JsonReader reader = null;
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.fonts);
            reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));

            reader.beginArray();
            while (reader.hasNext()) {
                Font frame = readFont(reader, context);
                if (frame != null) {
                    mFonts.put(frame.getName(), frame);
                    mSortedFonts.add(frame);
                }
            }
            reader.endArray();
        } catch (Exception e) {
            e.printStackTrace();
            mFonts.clear();
            mSortedFonts.clear();
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

        Log.d(Poladroid.TAG, "} FontManager.init() => frame count: " + mFonts.size());
    }

    private static Font readFont(JsonReader reader, Context context) throws Exception {
        String name = null;
        Typeface typeface = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String token = reader.nextName();
            if (token.equals("name")) {
                name = reader.nextString();
            } else if (token.equals("asset")) {
                String asset = reader.nextString();
                try {
                    typeface = Typeface.createFromAsset(context.getAssets(), asset);
                } catch (Exception e) {
                    Log.w(Poladroid.TAG, "*** FontManager.readFont(" + asset + "): " + e.toString());
                }
            } else {
                Log.i(Poladroid.TAG, "*** FontManager.readFont(): unknown token '" + token + "'");
                reader.skipValue();
            }
        }
        reader.endObject();

        if (name == null || typeface == null) {
            Log.i(Poladroid.TAG,
                    "*** FontManager.readFont(): missing 'name' or 'asset' attribute or resources");
            return null;
        }

        return new Font(name, typeface);
    }

    // This is NOT an overridden function, but it works just the same...
    public static Iterator<Font> iterator() {
        return mSortedFonts.iterator();
    }
}

/* EOF */

