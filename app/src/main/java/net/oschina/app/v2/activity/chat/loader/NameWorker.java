package net.oschina.app.v2.activity.chat.loader;

import android.util.Log;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.model.chat.IMUser;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;


/**
 * 异步加载机制加载并缓存用户或群组信息
 */
public abstract class NameWorker {
    private static final String TAG = NameWorker.class.getSimpleName();

    private INameCache mNameCache;
    private INameCache.NameCacheParams mNameCacheParams;
    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();


    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;


    protected NameWorker() {
        addNameCache();
    }

    /**
     * 通过提供的data参数加载数据并设置到AwareView中，
     * 你需要重写{@link NameWorker#processName(Object)}来自定义属于你自己的逻辑代码。
     * 内存和DB缓存通过INameCache来进行设置
     *
     * @param data      The URL of the image to download.
     * @param awareView The ImageView to bind the downloaded image to.
     */
    public void loadName(Object data, CacheType cacheType, AwareView awareView, DisplayListener listener) {
        if (data == null) {
            return;
        }

        IName value = null;

        if (mNameCache != null) {
            value = mNameCache.getBitmapFromMemCache(String.valueOf(data));
        }

        if (value != null) {
            // value found in memory cache
            awareView.setText(value.getName());
            if (listener != null) {
                listener.onLoadSuccess(awareView, value);
            }
        } else if (cancelPotentialWork(data, awareView)) {

            final ValueWorkerTask task = new ValueWorkerTask(data, cacheType, awareView);
            final AsyncValue asyncDrawable = new AsyncValue(task);
            awareView.setHolder(asyncDrawable);
            Log.e("IMA-LOG","加载时的回调:"+listener);
            awareView.setDisplayListener(listener);

            // NOTE: This uses a custom version of AsyncTask that has been pulled from the
            // framework and slightly modified. Refer to the docs at the top of the class
            // for more info on what was changed.
            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR);

        }
    }

    public void loadName(Object data, CacheType cacheType, AwareView awareView) {
        loadName(data, cacheType, awareView, null);
    }

    /**
     * 设置缓存参数
     *
     * @param cacheParams
     */
    public void addNameCache(INameCache.NameCacheParams cacheParams) {
        mNameCacheParams = cacheParams;
        mNameCache = INameCache.getInstance(mNameCacheParams);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    /**
     * 设置缓存参数
     */
    public void addNameCache() {
        mNameCacheParams = new INameCache.NameCacheParams();
        mNameCache = INameCache.getInstance(mNameCacheParams);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    /**
     * 子类必须要重写本发发来处理传来的data，最终返回IName
     * 这个方法将会在一个后台线程中调用。
     *
     * @param data
     * @return The processed bitmap
     */
    protected abstract IName processName(Object data, CacheType type);

    /**
     * @return 当前使用的缓存.
     */
    public INameCache getNameCache() {
        return mNameCache;
    }

    /**
     * 取消一个加载任务.
     *
     * @param awareView
     */
    public static void cancelWork(AwareView awareView) {
        final ValueWorkerTask valueWorkerTask = getValueWorkerTask(awareView);
        if (valueWorkerTask != null) {
            valueWorkerTask.cancel(true);
            if (BuildConfig.DEBUG) {
                final Object bitmapData = valueWorkerTask.mData;
            }
        }
    }

    /**
     * 返回true表示当前任务被取消或没有任务，否则返回false
     */
    public static boolean cancelPotentialWork(Object data, AwareView awareView) {

        final ValueWorkerTask valueWorkerTask = getValueWorkerTask(awareView);

        if (valueWorkerTask != null) {
            final Object valueData = valueWorkerTask.mData;
            if (valueData == null || !valueData.equals(data)) {
                valueWorkerTask.cancel(true);
                if (BuildConfig.DEBUG) {
                }
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;

    }

    /**
     * @param awareView Any textView
     * @return Retrieve the currently active work task (if any) associated with this awareView.
     * null if there is no such task.
     */
    private static ValueWorkerTask getValueWorkerTask(AwareView awareView) {
        if (awareView != null) {
            final Object holder = awareView.getHolder();
            if (holder instanceof AsyncValue) {
                final AsyncValue asyncDrawable = (AsyncValue) holder;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public IName getCache(String data, CacheType type) {
        IName name = mNameCache.getBitmapFromMemCache(data);
        if (name == null) {
            name = mNameCache.getBitmapFromDiskCache(data, type);
        }
        return name;
    }

    /**
     * The actual AsyncTask that will asynchronously process the name.
     */
    private class ValueWorkerTask extends AsyncTask<Void, Void, IName> {
        private Object mData;
        private CacheType mCacheType;
        private final WeakReference<AwareView> awareViewReference;

        public ValueWorkerTask(Object data, CacheType type, AwareView awareView) {
            mData = data;
            mCacheType = type;
            awareViewReference = new WeakReference<>(awareView);
        }

        /**
         * Background processing.
         */
        @Override
        protected IName doInBackground(Void... params) {

            if (BuildConfig.DEBUG) {
            }

            final String dataString = String.valueOf(mData);
            IName name = null;
            //User drawable = null;

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            // 如果缓存可用并且这个任务没有被其他线程取消，AwareView原本就是绑定这个任务
            if (mNameCache != null && !isCancelled() && getAttachedAwareView() != null
                    && !mExitTasksEarly) {
                name = mNameCache.getBitmapFromDiskCache(dataString, mCacheType);
            }

            if (name == null && !isCancelled() && getAttachedAwareView() != null
                    && !mExitTasksEarly) {
                name = processName(mData, mCacheType);
            }

            if (name != null) {
                if (mNameCache != null) {
                    mNameCache.addBitmapToCache(dataString, name);
                }
            }

            if (BuildConfig.DEBUG) {
            }
            return name;
        }

        @Override
        protected void onPostExecute(IName value) {
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                value = null;
            }

            final AwareView awareView = getAttachedAwareView();
            if (value != null && awareView != null) {
                setAwareViewName(awareView, value);
            }
            if (value == null && awareView != null) {
                if(awareView.getDisplayListener() ==null){
                    Log.e("IMA-LOG","remote前无回调");
                } else {
                    Log.e("IMA-LOG","remote前有回调");
                }
                queryValueFromRemoteDB((String) mData, mCacheType, awareView);
            }
        }

        @Override
        protected void onCancelled(IName value) {
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        private AwareView getAttachedAwareView() {
            final AwareView awareView = awareViewReference.get();
            final ValueWorkerTask valueWorkerTask = getValueWorkerTask(awareView);

            if (this == valueWorkerTask) {
                return awareView;
            }

            return null;
        }
    }

    protected abstract void queryValueFromRemoteDB(String data,
                                                   CacheType cacheType, AwareView awareView);

    protected void executeOnQueryRemoteSuccess(IName value, AwareView awareView) {
        setAwareViewName(awareView, value);
    }

    private static class AsyncValue {
        private final WeakReference<ValueWorkerTask> valueWorkerTaskReference;

        public AsyncValue(ValueWorkerTask bitmapWorkerTask) {
            valueWorkerTaskReference =
                    new WeakReference<>(bitmapWorkerTask);
        }

        public ValueWorkerTask getBitmapWorkerTask() {
            return valueWorkerTaskReference.get();
        }
    }

    private void setAwareViewName(AwareView awareView, IName name) {
        Log.e("IMA-LOG", "setAwareViewName ："+name.getPhoto());
        awareView.setText(name.getName());
        DisplayListener listener = awareView.getDisplayListener();
        if (listener != null) {
            Log.e("IMA-LOG", "有回调");
            listener.onLoadSuccess(awareView, name);
        } else {
            Log.e("IMA-LOG", "无回调");
        }
    }

    /**
     * 暂停后台正在执行的任务。
     */
    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer) params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    protected void initDiskCacheInternal() {
        if (mNameCache != null) {
            mNameCache.initDiskCache();
        }
    }

    protected void clearCacheInternal() {
        if (mNameCache != null) {
            mNameCache.clearCache();
        }
    }

    protected void flushCacheInternal() {
        if (mNameCache != null) {
            mNameCache.flush();
        }
    }

    protected void closeCacheInternal() {
        if (mNameCache != null) {
            mNameCache.close();
            mNameCache = null;
        }
    }

    public void clearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }
}
