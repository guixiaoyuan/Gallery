package com.tct.gallery3d.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import com.tct.gallery3d.R;

import android.content.Context;
import android.util.JsonReader;

public class StyleLayoutManager {
    private static final String TAG = "StyleLayoutManager";

    public static final int InvalidSize = -1;

    private Context mContext = null;
    private ArrayList<ArrayList<Style>> mStyleArrayList = new ArrayList<ArrayList<Style>>();
    private int mLastRandomValue = -1;
    private int mLastStyleIndex = -1;

    public StyleLayoutManager(Context context) {
        this.mContext = context;
        readJsonConfig();
    }

    public ArrayList<Style> getRandomStyle(int optimalSize) {
        if(optimalSize > 0) {
            for(ArrayList<Style> list : mStyleArrayList) {
                if(list.size() == optimalSize) {
                    mLastRandomValue = InvalidSize;
                    return list;
                }
            }
        }

        Random random = new Random();
        int n = mStyleArrayList.size();
        int randomValue = random.nextInt(n);
        if(randomValue == mLastRandomValue) {
            randomValue = random.nextInt(n);
        }
        ArrayList<Style> list = mStyleArrayList.get(randomValue);
        mLastRandomValue = randomValue;
        return list;
    }

    public void resetNextStyle() {
        mLastStyleIndex = -1;
    }

    public ArrayList<Style> getNextStyle(int overplusCount) {
        int currentStyleIndex = (mLastStyleIndex + 1) % mStyleArrayList.size();
        ArrayList<Style> list = mStyleArrayList.get(currentStyleIndex);
        mLastStyleIndex = currentStyleIndex;
        return list;
    }

    private void readJsonConfig() {
        JsonReader reader = null;

        try {
            InputStream inputStream = mContext.getResources().openRawResource(R.raw.style);
            reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            reader.beginArray();
            while (reader.hasNext()) {
                readStyle(reader);
            }
            reader.endArray();
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "readJsonConfig", e);
        } catch (IOException e) {
            Log.e(TAG, "readJsonConfig", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                Log.e(TAG, "close reader", e);
            }
        }
    }

    private void readStyle(JsonReader reader) throws IOException {

        reader.beginObject();
        while(reader.hasNext()) {
            String token = reader.nextName();
            if("index".equals(token)) {
                int index = reader.nextInt();
            } else if("resources".equals(token)) {
                ArrayList<Style> list = readResources(reader);
                mStyleArrayList.add(list);
            }
        }
        reader.endObject();
    }

    private ArrayList<Style> readResources(JsonReader reader) throws IOException {
        ArrayList<Style> list = new ArrayList<Style>();

        reader.beginArray();
        while(reader.hasNext()) {
            Style style= readResource(reader);
            list.add(style);
        }
        reader.endArray();

        return list;
    }

    private Style readResource(JsonReader reader) throws IOException {
        StylePoint point = null;
        StylePadding padding = null;
        reader.beginObject();
        while(reader.hasNext()) {
            String token = reader.nextName();
            if("point".equals(token)) {
                point = readPoint(reader);
            } else if("padding".equals(token)) {
                padding = readPadding(reader);
            }
        }
        reader.endObject();

        return new Style(point, padding);
    }

    private StylePoint readPoint(JsonReader reader) throws IOException {
        float x = 0, y = 0, w = 0, h = 0;
        reader.beginObject();
        while(reader.hasNext()) {
            String token = reader.nextName();
            if("x".equals(token)) {
                x = (float)reader.nextDouble();
            } else if("y".equals(token)) {
                y = (float)reader.nextDouble();
            } else if("w".equals(token)) {
                w = (float)reader.nextDouble();
            } else if("h".equals(token)) {
                h = (float)reader.nextDouble();
            }
        }
        reader.endObject();
        return new StylePoint(x, y, w, h);
    }

    private StylePadding readPadding(JsonReader reader) throws IOException {
        int t = 0, b = 0, l = 0, r = 0;
        reader.beginObject();
        while(reader.hasNext()) {
            String token = reader.nextName();
            if("t".equals(token)) {
                t = reader.nextInt();
            } else if("b".equals(token)) {
                b = reader.nextInt();
            } else if("l".equals(token)) {
                l = reader.nextInt();
            } else if("r".equals(token)) {
                r = reader.nextInt();
            }
        }
        reader.endObject();
        return new StylePadding(t, b, l, r);
    }

    public static class Style {
        StylePoint point;
        StylePadding padding;

        public Style(StylePoint point, StylePadding padding) {
            this.point = point;
            this.padding = padding;
        }
    }

    public static class StylePoint {
        
        float x;
        float y;
        float w;
        float h;

        public StylePoint(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    public static class StylePadding {
        public int t;//top
        public int b;//bottom
        public int l;//left
        public int r;//right

        public StylePadding(int t, int b, int l, int r) {
            this.t = t;
            this.b = b;
            this.l = l;
            this.r = r;
        }
    }
}
