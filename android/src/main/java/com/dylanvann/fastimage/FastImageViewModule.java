package com.dylanvann.fastimage;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.io.File;

class FastImageViewModule extends ReactContextBaseJavaModule {

    private static final String REACT_CLASS = "FastImageView";

    FastImageViewModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    private static Drawable TRANSPARENT_DRAWABLE = new ColorDrawable(Color.TRANSPARENT);

    @ReactMethod
    public void preload(final ReadableArray sources) {
        final Activity activity = getCurrentActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < sources.size(); i++) {
                    final ReadableMap source = sources.getMap(i);
                    final GlideUrl glideUrl = FastImageViewConverter.glideUrl(source);
                    final Priority priority = FastImageViewConverter.priority(source);
                    Glide
                            .with(activity.getApplicationContext())
                            .load(glideUrl)
                            .priority(priority)
                            .placeholder(TRANSPARENT_DRAWABLE)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .preload();
                }
            }
        });
    }

    @ReactMethod
    public void getSizeCached(Promise promise) {
//        File dirs = new File(getReactApplicationContext().getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR);
        File dirs = getReactApplicationContext().getCacheDir();

        try {
            long totalSize = 0;
            for (File dir : dirs.listFiles()) {
                totalSize += calculateSize(dir);
            }

            promise.resolve(String.valueOf(totalSize));
        } catch (RuntimeException ex) {
            promise.resolve(0);
        }
    }


    @ReactMethod
    public void clearCached() {
        deleteCache(getReactApplicationContext());
    }

    private static long calculateSize(File dir) {
        if (dir == null) return 0;
        if (!dir.isDirectory()) return dir.length();
        long result = 0;
        File[] children = dir.listFiles();
        if (children != null)
            for (File child : children)
                result += calculateSize(child);
        return result;
    }

    private void deleteCache(Context context) {
        try {
//            File dirs = new File(getReactApplicationContext().getCacheDir(), DiskCache.Factory.DEFAULT_DISK_CACHE_DIR);
            File dirs = context.getCacheDir();
            deleteDir(dirs);
        } catch (Exception e) {
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
