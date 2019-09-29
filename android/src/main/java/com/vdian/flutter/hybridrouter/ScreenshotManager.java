package com.vdian.flutter.hybridrouter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
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
    /**
     * 是否启用文件缓存，暂时关闭
     * TODO 是否有必要启用文件缓存
     */
    private final boolean enableFileCache = false;

    // 允许的 flutter 保存截图最大个数，默认是 2
    private LruCache<String, Bitmap> screenshotCache = new LruCache<String, Bitmap>(2) {
        @Override
        protected void entryRemoved(boolean evicted, @NonNull String key,
                                    @NonNull Bitmap oldValue, Bitmap newValue) {
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
    public void setMaxSize(int maxSize) {
        maxSize = maxSize < 0 ? 0 : maxSize;
        if (maxSize != screenshotCache.maxSize()) {
            screenshotCache.resize(maxSize);
        }
    }

    /**
     * 添加截图管理
     * @param nativePageId
     * @param bitmap
     * @return
     */
    public void addBitmap(String nativePageId, Bitmap bitmap) {
        screenshotCache.put(nativePageId, bitmap);
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
        deleteBitmap(nativePageId);
    }

    @Nullable
    private Bitmap readBitmap(String nativePageId) {
        if (!enableFileCache) return null;
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
        if (!enableFileCache) return ;
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

    private void deleteBitmap(String nativePageId) {
        if (!enableFileCache) return ;
        File dir = appContext.getExternalCacheDir();
        if (dir != null && dir.exists()) {
            File imageFile = new File(dir, getSaveFile(nativePageId));
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
    }

    private String getSaveFile(String nativePageId) {
        return new File("flutter_screen_shot", nativePageId).getPath();
    }
}
