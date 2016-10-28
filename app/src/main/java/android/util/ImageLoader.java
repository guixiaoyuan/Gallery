/* Copyright (C) 2016 Tcl Corporation Limited */
package android.util;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import com.tct.gallery3d.image.RecyclingBitmapDrawable;
import com.tct.gallery3d.image.Utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ImageLoader {
    private ExecutorService mThreadPool;
    private int mThreadCount = 1;
    private Type mType = Type.LIFO;
    private LinkedList<Runnable> mTasks;
    private Thread mPoolThread;
    private Handler mPoolThreadHander;
    private Handler mHandler;
    private volatile Semaphore mSemaphore = new Semaphore(1);
    private volatile Semaphore mPoolSemaphore;

    private static ImageLoader mInstance;

    public enum Type {
        FIFO, LIFO
    }

    public static ImageLoader getInstance() {

        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(1, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    private ImageLoader(int threadCount, Type type) {
        init(threadCount, type);
    }

    private void init(int threadCount, Type type) {
        // loop thread
        mPoolThread = new Thread() {
            @Override
            public void run() {
                try {
                    mSemaphore.acquire();
                } catch (InterruptedException e) {
                }
                Looper.prepare();

                mPoolThreadHander = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        mThreadPool.execute(getTask());
                        try {
                            mPoolSemaphore.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                };
                mSemaphore.release();
                Looper.loop();
            }
        };
        mPoolThread.start();

        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mPoolSemaphore = new Semaphore(threadCount);
        mTasks = new LinkedList<Runnable>();
        mType = type == null ? Type.LIFO : type;

    }

    public void loadImage(final String path, final ImageView imageView) {
        // set tag
        imageView.setTag(path);
        if (mHandler == null) {
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ImgBeanHolder holder = (ImgBeanHolder) msg.obj;
                    ImageView imageView = holder.imageView;
                    Bitmap bm = holder.bitmap;
                    String path = holder.path;
                    if (bm == null) {
                        return;
                    }
                    if (imageView.getTag().toString().equals(path)) {
                        BitmapDrawable drawable = null;
                        Resources resources = imageView.getContext().getResources();
                        if (Utils.hasHoneycomb()) {
                            // Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
                            drawable = new BitmapDrawable(resources, bm);
                        } else {
                            // Running on Gingerbread or older, so wrap in a RecyclingBitmapDrawable
                            // which will recycle automagically
                            drawable = new RecyclingBitmapDrawable(resources, bm);
                        }
//                        imageView.setImageBitmap(bm);
                        imageView.setImageDrawable(drawable);
                    }
                }
            };
        }

        addTask(new Runnable() {
            @Override
            public void run() {
                ImageSize imageSize = getImageViewWidth(imageView);
                int reqWidth = imageSize.width;
                int reqHeight = imageSize.height;

                Bitmap bm = decodeSampledBitmapFromResource(path, reqWidth, reqHeight);
                ImgBeanHolder holder = new ImgBeanHolder();
                holder.bitmap = bm;
                holder.imageView = imageView;
                holder.path = path;
                Message message = Message.obtain();
                message.obj = holder;

                while (imageView.getVisibility() != View.VISIBLE) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mHandler.sendMessage(message);
                mPoolSemaphore.release();
            }
        });
    }

    private synchronized void addTask(Runnable runnable) {
        try {
            if (mPoolThreadHander == null)
                mSemaphore.acquire();
        } catch (InterruptedException e) {
        }
        mTasks.add(runnable);
        mPoolThreadHander.sendEmptyMessage(0x110);
    }

    private synchronized Runnable getTask() {
        if (mType == Type.FIFO) {
            return mTasks.removeFirst();
        } else if (mType == Type.LIFO) {
            return mTasks.removeLast();
        }
        return null;
    }

    public static ImageLoader getInstance(int threadCount, Type type) {

        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(threadCount, type);
                }
            }
        }
        return mInstance;
    }

    private ImageSize getImageViewWidth(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        final DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
        final LayoutParams params = imageView.getLayoutParams();

        int width = params.width == LayoutParams.WRAP_CONTENT ? 0 : imageView.getWidth(); // Get
                                                                                          // actual
                                                                                          // image
                                                                                          // width
        if (width <= 0)
            width = params.width; // Get layout width parameter
        if (width <= 0)
            width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check
                                                                    // maxWidth
                                                                    // parameter
        if (width <= 0)
            width = displayMetrics.widthPixels;
        int height = params.height == LayoutParams.WRAP_CONTENT ? 0 : imageView.getHeight(); // Get
                                                                                             // actual
                                                                                             // image
                                                                                             // height
        if (height <= 0)
            height = params.height; // Get layout height parameter
        if (height <= 0)
            height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check
                                                                      // maxHeight
                                                                      // parameter
        if (height <= 0)
            height = displayMetrics.heightPixels;
        imageSize.width = width;
        imageSize.height = height;
        return imageSize;

    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (width > reqWidth && height > reqHeight) {
            int widthRatio = Math.round((float) width / (float) reqWidth);
            int heightRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.max(widthRatio, heightRatio);
        }
        return inSampleSize;
    }

    private Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);

        return bitmap;
    }

    private class ImgBeanHolder {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }

    private class ImageSize {
        int width;
        int height;
    }

    private static int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;

                Log.e("TAG", value + "");
            }
        } catch (Exception e) {
        }
        return value;
    }

}
