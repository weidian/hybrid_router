package com.vdian.flutter.hybridrouter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * ┏┛ ┻━━━━━┛ ┻┓
 * ┃　　　　　　 ┃
 * ┃　　　━　　　┃
 * ┃　┳┛　  ┗┳　┃
 * ┃　　　　　　 ┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　 ┃
 * ┗━┓　　　┏━━━┛
 * * ┃　　　┃   神兽保佑
 * * ┃　　　┃   代码无BUG！
 * * ┃　　　┗━━━━━━━━━┓
 * * ┃　　　　　　　    ┣┓
 * * ┃　　　　         ┏┛
 * * ┗━┓ ┓ ┏━━━┳ ┓ ┏━┛
 * * * ┃ ┫ ┫   ┃ ┫ ┫
 * * * ┗━┻━┛   ┗━┻━┛
 *
 * 截图管理类，为了优化页面切换体验，提供截图显示占位图功能
 *
 * @author qigengxin
 * @since 2019-07-03 13:57
 */
public class ScreenshotManager {
    // 上下文
    private final Context appContext;
    // 允许的 flutter 保存截图最大个数，默认是 2
    private LruCache<String, Bitmap> screenshotCache = new LruCache<String, Bitmap>(2) {
        @Override
        protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
            if (evicted) {
                // need save bitmap to local file
                saveBitmap(key, newValue);
            }
        }
    };

    public ScreenshotManager(Context appContext) {
        this.appContext = appContext.getApplicationContext();
    }

    /**
     * 设置 bitmap 缓存个数
     * @param maxSize bitmap 缓存个数
     */
    public ScreenshotManager setMaxSize(int maxSize) {
        maxSize = maxSize < 0 ? 0 : maxSize;
        if (maxSize != screenshotCache.maxSize()) {
            screenshotCache.resize(maxSize);
        }
        return this;
    }

    /**
     * 添加截图管理
     * @param nativePageId
     * @param bitmap
     * @return
     */
    public ScreenshotManager addBitmap(String nativePageId, Bitmap bitmap) {
        screenshotCache.put(nativePageId, bitmap);
        return this;
    }

    public Bitmap getBitmap(String nativePageId) {
        Bitmap bitmap = screenshotCache.get(nativePageId);
        if (bitmap == null) {
            bitmap = readBitmap(nativePageId);
            if (bitmap != null) {
                addBitmap(nativePageId, bitmap);
            }
        }
        return bitmap;
    }

    public void removeCache(String nativePageId) {
        screenshotCache.remove(nativePageId);
        File dir = appContext.getExternalCacheDir();
        if (dir != null && dir.exists()) {
            File imageFile = new File(dir, getSaveFile(nativePageId));
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
    }

    @Nullable
    private Bitmap readBitmap(String nativePageId) {
        File dir = appContext.getExternalCacheDir();
        if (dir != null && dir.exists()) {
            File imageFile = new File(dir, getSaveFile(nativePageId));
            if (imageFile.exists()) {
                return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            }
        }
        return null;
    }

    private void saveBitmap(String nativePageId, Bitmap bitmap) {
        File dir = appContext.getExternalCacheDir();
        if (dir != null && dir.exists()) {
            File imageFile = new File(dir, getSaveFile(nativePageId));
            if (imageFile.getParentFile().mkdirs()) {
                try {
                    FileOutputStream outputStream = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                } catch (FileNotFoundException ignore) {
                }
            }
        }
    }

    private String getSaveFile(String nativePageId) {
        return new File("flutter_screen_shot", nativePageId).getPath();
    }
}
