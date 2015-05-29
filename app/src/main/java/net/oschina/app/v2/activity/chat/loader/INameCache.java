package net.oschina.app.v2.activity.chat.loader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;


import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.activity.chat.db.ContactDataHelper;
import net.oschina.app.v2.model.chat.IMGroup;
import net.oschina.app.v2.model.chat.IMUser;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class INameCache {
    private static final String TAG = "ImageCache";

    // Constants to easily toggle various caches
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
    private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = false;

    //private DiskLruCache mDiskLruCache;
    private Map<String, IName> mMemoryCache;
    private NameCacheParams mCacheParams;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private ContactDataHelper mContactHelper;

    private Set<SoftReference<IName>> mReusableBitmaps;

    /**
     * Create a new ImageCache object using the specified parameters. This should not be
     * called directly by other classes, instead use
     * {@link INameCache#getInstance(android.support.v4.app.FragmentManager, NameCacheParams)} to fetch an ImageCache
     * instance.
     *
     * @param cacheParams The cache parameters to use to initialize the cache
     */
    private INameCache(NameCacheParams cacheParams) {
        init(cacheParams);
    }

    /**
     * Return an {@link INameCache} instance. A {@link RetainFragment} is used to retain the
     * ImageCache object across configuration changes such as a change in device orientation.
     *
     * @param fragmentManager The fragment manager to use when dealing with the retained fragment.
     * @param cacheParams     The cache parameters to use if the ImageCache needs instantiation.
     * @return An existing retained ImageCache object or a new one if one did not exist
     */
    public static INameCache getInstance(NameCacheParams cacheParams) {

        // Search for, or create an instance of the non-UI RetainFragment
        // final RetainFragment mRetainFragment = findOrCreateRetainFragment(fragmentManager);

        // See if we already have an ImageCache stored in RetainFragment
        INameCache imageCache = null;//(INameCache) mRetainFragment.getObject();

        // No existing ImageCache, create one and store it in RetainFragment
        if (imageCache == null) {
            imageCache = new INameCache(cacheParams);
            //mRetainFragment.setObject(imageCache);
        }

        return imageCache;
    }

    /**
     * Initialize the cache, providing all parameters.
     *
     * @param cacheParams The cache parameters to initialize the cache
     */
    private void init(NameCacheParams cacheParams) {
        mCacheParams = cacheParams;
        // Set up memory cache
        if (mCacheParams.memoryCacheEnabled) {
            // If we're running on Honeycomb or newer, create a set of reusable bitmaps that can be
            // populated into the inBitmap field of BitmapFactory.Options. Note that the set is
            // of SoftReferences which will actually not be very effective due to the garbage
            // collector being aggressive clearing Soft/WeakReferences. A better approach
            // would be to use a strongly references bitmaps, however this would require some
            // balancing of memory usage between this set and the bitmap LruCache. It would also
            // require knowledge of the expected size of the bitmaps. From Honeycomb to JellyBean
            // the size would need to be precise, from KitKat onward the size would just need to
            // be the upper bound (due to changes in how inBitmap can re-use bitmaps).
            if (Utils.hasHoneycomb()) {
                mReusableBitmaps = Collections.synchronizedSet(new HashSet<SoftReference<IName>>());
            }

            mMemoryCache = new HashMap<>();
        }


        // By default the disk cache is not initialized here as it should be initialized
        // on a separate thread due to disk access.
        if (cacheParams.initDiskCacheOnCreate) {
            // Set up disk cache
            initDiskCache();
        }
    }

    /**
     * Initializes the disk cache.  Note that this includes disk access so this should not be
     * executed on the main/UI thread. By default an ImageCache does not initialize the disk
     * cache when it is created, instead you should call initDiskCache() to initialize it on a
     * background thread.
     */
    public void initDiskCache() {
        // Set up disk cache
        synchronized (mDiskCacheLock) {
            mContactHelper = new ContactDataHelper(AppContext.context());
            mContactHelper.initialize();

            mDiskCacheStarting = false;
            mDiskCacheLock.notifyAll();
        }
    }

    /**
     * Adds a bitmap to both memory and disk cache.
     *
     * @param data  Unique identifier for the bitmap to store
     * @param value The bitmap drawable to store
     */
    public void addBitmapToCache(String data, IName value) {
        if (data == null || value == null) {
            return;
        }

        // Add to memory cache
        if (mMemoryCache != null) {
            mMemoryCache.put(data, value);
        }

        synchronized (mDiskCacheLock) {
            // Add to disk cache
            if (mContactHelper != null) {
                mContactHelper.beginTransaction();
                if (value instanceof IMUser) {
                    Log.e(TAG, "添加User 到数据库：" + value);
                    mContactHelper.addOrUpdateUser((IMUser) value);
                } else if (value instanceof IMGroup) {
                    Log.e(TAG, "Group 到数据库：" + value);
                    mContactHelper.addOrUpdateGroup((IMGroup) value);
                }
                mContactHelper.setTransactionSuccessful();
                mContactHelper.endTransaction();
            }
        }
    }

    /**
     * Get from memory cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap drawable if found in cache, null otherwise
     */
    public IName getBitmapFromMemCache(String data) {

        IName memValue = null;

        if (mMemoryCache != null) {
            memValue = mMemoryCache.get(data);
        }

        if (BuildConfig.DEBUG && memValue != null) {
            Log.d(TAG, "Memory cache hit");
        }

        return memValue;

    }

    /**
     * Get from disk cache.
     *
     * @param data Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public IName getBitmapFromDiskCache(String data, CacheType type) {
        IName name = null;

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mContactHelper != null) {
                mContactHelper.beginTransaction();
                if (CacheType.USER.equals(type)) {
                    name = mContactHelper.queryUserByIMUsername(data);
                    Log.e(TAG, "从数据库获取用户信息:" + data + " ->" + name);
                } else if (CacheType.GROUP.equals(type)) {
                    name = mContactHelper.queryGroupByImId(data);
                    Log.e(TAG, "从数据库获取群组信息:" + data + " ->" + name);
                }
                mContactHelper.setTransactionSuccessful();
                mContactHelper.endTransaction();
            }
            return name;
        }
    }

    /**
     * Clears both the memory and disk cache associated with this ImageCache object. Note that
     * this includes disk access so this should not be executed on the main/UI thread.
     */
    public void clearCache() {
        if (mMemoryCache != null) {
            mMemoryCache.clear();
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Memory cache cleared");
            }
        }

        synchronized (mDiskCacheLock) {
            mDiskCacheStarting = true;
//            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
//                try {
//                    mDiskLruCache.delete();
//                    if (BuildConfig.DEBUG) {
//                        Log.d(TAG, "Disk cache cleared");
//                    }
//                } catch (IOException e) {
//                    Log.e(TAG, "clearCache - " + e);
//                }
//                mDiskLruCache = null;
//                initDiskCache();
//            }
        }
    }

    /**
     * Flushes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void flush() {
        synchronized (mDiskCacheLock) {
//            if (mDiskLruCache != null) {
//                try {
//                    mDiskLruCache.flush();
//                    if (BuildConfig.DEBUG) {
//                        Log.d(TAG, "Disk cache flushed");
//                    }
//                } catch (IOException e) {
//                    Log.e(TAG, "flush - " + e);
//                }
//            }
        }
    }

    /**
     * Closes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void close() {
        synchronized (mDiskCacheLock) {
//            if (mDiskLruCache != null) {
//                try {
//                    if (!mDiskLruCache.isClosed()) {
//                        mDiskLruCache.close();
//                        mDiskLruCache = null;
//                        if (BuildConfig.DEBUG) {
//                            Log.d(TAG, "Disk cache closed");
//                        }
//                    }
//                } catch (IOException e) {
//                    Log.e(TAG, "close - " + e);
//                }
//            }
        }
    }

    public void clear(String key, CacheType type) {
        if (mMemoryCache != null) {
            mMemoryCache.remove(key);
        }
        if (mContactHelper != null) {
            mContactHelper.beginTransaction();
            if (CacheType.USER.equals(type)) {
                mContactHelper.deleteUserByIMUsername(key);
            } else if (CacheType.GROUP.equals(type)) {
                mContactHelper.deleteGroupByImId(key);
            }
            mContactHelper.setTransactionSuccessful();
            mContactHelper.endTransaction();
        }
    }

    /**
     * A holder class that contains cache parameters.
     */
    public static class NameCacheParams {
        //public int memCacheSize = 100;
        public boolean memoryCacheEnabled = true;
        public boolean initDiskCacheOnCreate = true;

        public NameCacheParams() {
        }
    }

    /**
     * Locate an existing instance of this Fragment or if not found, create and
     * add it using FragmentManager.
     *
     * @param fm The FragmentManager manager to use.
     * @return The existing instance of the Fragment or the new instance if just
     * created.
     */
    private static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {

        // Check to see if we have retained the worker fragment.
        RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag(TAG);

        // If not retained (or first time running), we need to create and add it.
        if (mRetainFragment == null) {
            mRetainFragment = new RetainFragment();
            fm.beginTransaction().add(mRetainFragment, TAG).commitAllowingStateLoss();
        }

        return mRetainFragment;

    }

    /**
     * A simple non-UI Fragment that stores a single Object and is retained over configuration
     * changes. It will be used to retain the ImageCache object.
     */
    public static class RetainFragment extends Fragment {
        private Object mObject;

        /**
         * Empty constructor as per the Fragment documentation
         */
        public RetainFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure this Fragment is retained over a configuration change
            setRetainInstance(true);
        }

        /**
         * Store a single object in this Fragment.
         *
         * @param object The object to store
         */
        public void setObject(Object object) {
            mObject = object;
        }

        /**
         * Get the stored object.
         *
         * @return The stored object
         */
        public Object getObject() {
            return mObject;
        }
    }

}
