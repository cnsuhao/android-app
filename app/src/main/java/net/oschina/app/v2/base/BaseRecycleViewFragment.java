package net.oschina.app.v2.base;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.MySwipeRefreshLayout;
//import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

//import com.handmark.pulltorefresh.library.PullToRefreshBase;
//import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
//import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.cache.CacheManager;
import net.oschina.app.v2.model.Base;
import net.oschina.app.v2.model.ListEntity;
import net.oschina.app.v2.model.NewsList;
import net.oschina.app.v2.model.Notice;
import net.oschina.app.v2.ui.decorator.DividerItemDecoration;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.ui.widget.FixedRecyclerView;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.UIHelper;

import org.apache.http.Header;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.List;

public abstract class BaseRecycleViewFragment extends BaseTabFragment implements
        RecycleBaseAdapter.OnItemClickListener {

	public static final String BUNDLE_KEY_CATALOG = "BUNDLE_KEY_CATALOG";

    protected MySwipeRefreshLayout mSwipeRefresh;
	protected FixedRecyclerView mListView;
    protected LinearLayoutManager mLayoutManager;
	protected RecycleBaseAdapter mAdapter;
	protected EmptyLayout mErrorLayout;
	protected int mStoreEmptyState = -1;

	protected int mCurrentPage = 0;
	protected int mCatalog = NewsList.CATALOG_ALL;

	private AsyncTask<String, Void, ListEntity> mCacheTask;
	private ParserTask mParserTask;

	protected int getLayoutRes() {
		return R.layout.v2_fragment_swipe_refresh_recyclerview;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			mCatalog = args.getInt(BUNDLE_KEY_CATALOG);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(getLayoutRes(), container, false);
		initViews(view);
		return view;
	}

	protected void initViews(View view) {
		mErrorLayout = (EmptyLayout) view.findViewById(R.id.error_layout);
		mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mCurrentPage = 0;
				mState = STATE_REFRESH;
				mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
				requestData(true);
			}
		});

        mSwipeRefresh = (MySwipeRefreshLayout)view.findViewById(R.id.srl_refresh);
        mSwipeRefresh.setColorSchemeColors(R.color.main_green);
        //mSwipeRefresh.canChildScrollUp();
        mSwipeRefresh.setOnRefreshListener(new MySwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               refresh();
            }
        });
        //int y = mListView.getScrollY();

		mListView = (FixedRecyclerView) view.findViewById(R.id.recycleView);
        mListView.setOnScrollListener(mScrollListener);
        // use a linear layout manager
        mListView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListView.setLayoutManager(mLayoutManager);
        mListView.setHasFixedSize(true);

        if (mAdapter != null) {
			mListView.setAdapter(mAdapter);
			mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
		} else {
			mAdapter = getListAdapter();
            mAdapter.setOnItemClickListener(this);
			// mListView.setRefreshing();
			mListView.setAdapter(mAdapter);

			if (requestDataIfViewCreated()) {
				mCurrentPage = 0;
				mState = STATE_REFRESH;
				mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
				requestData(requestDataFromNetWork());
			} else {
				mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
			}
		}
		if (mStoreEmptyState != -1) {
			mErrorLayout.setErrorType(mStoreEmptyState);
		}
	}
	
	@Override
	public void onDestroyView() {
		mStoreEmptyState = mErrorLayout.getErrorState();
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		cancelReadCacheTask();
		cancelParserTask();
		super.onDestroy();
	}

    @Override
    public void onItemClick(View v, int position, Object item) {

    }

    protected abstract RecycleBaseAdapter getListAdapter();
	
	protected boolean requestDataFromNetWork() {
		return false;
	}
	
	protected boolean requestDataIfViewCreated() {
		return true;
	}

	protected String getCacheKeyPrefix() {
		return null;
	}

	protected ListEntity parseList(InputStream is) throws Exception {
		return null;
	}

	protected ListEntity readList(Serializable seri) {
		return null;
	}

    public void refresh() {
		mCurrentPage = 0;
		mState = STATE_REFRESH;
		requestData(true);
	}

	public void loadMore() {
		if (mState == STATE_NONE) {
			if (mAdapter.getState() == ListBaseAdapter.STATE_LOAD_MORE) {
				mCurrentPage++;
				mState = STATE_LOADMORE;
				requestData(false);
			}
		}
	}

	protected String getCacheKey() {
		return new StringBuffer(getCacheKeyPrefix()).append(mCatalog)
				.append("_").append(mCurrentPage).append("_")
				.append(TDevice.getPageSize()).toString();
	}

	protected void requestData(boolean refresh) {
		String key = getCacheKey();
		if (TDevice.hasInternet()
				&& (!CacheManager.isReadDataCache(getActivity(), key) || refresh)) {
			sendRequestData();
		} else {
			readCacheData(key);
		}
	}

	protected void sendRequestData() {
	}

	private void readCacheData(String cacheKey) {
		cancelReadCacheTask();
		mCacheTask = new CacheTask(getActivity()).execute(cacheKey);
	}

	private void cancelReadCacheTask() {
		if (mCacheTask != null) {
			mCacheTask.cancel(true);
			mCacheTask = null;
		}
	}

	private class CacheTask extends AsyncTask<String, Void, ListEntity> {
		private WeakReference<Context> mContext;

		private CacheTask(Context context) {
			mContext = new WeakReference<Context>(context);
		}

		@Override
		protected ListEntity doInBackground(String... params) {
			Serializable seri = CacheManager.readObject(mContext.get(),
					params[0]);
			if (seri == null) {
				return null;
			} else {
				return readList(seri);
			}
		}

		@Override
		protected void onPostExecute(ListEntity list) {
			super.onPostExecute(list);
			if (list != null) {
				executeOnLoadDataSuccess(list.getList());
			} else {
				executeOnLoadDataError(null);
			}
			executeOnLoadFinish();
		}
	}

	private class SaveCacheTask extends AsyncTask<Void, Void, Void> {
		private WeakReference<Context> mContext;
		private Serializable seri;
		private String key;

		private SaveCacheTask(Context context, Serializable seri, String key) {
			mContext = new WeakReference<Context>(context);
			this.seri = seri;
			this.key = key;
		}

		@Override
		protected Void doInBackground(Void... params) {
			CacheManager.saveObject(mContext.get(), seri, key);
			return null;
		}
	}

	protected AsyncHttpResponseHandler mHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onSuccess(int statusCode, Header[] headers,
				byte[] responseBytes) {
			if (isAdded()) {
				if (mState == STATE_REFRESH) {
					onRefreshNetworkSuccess();
					AppContext.setRefreshTime(getCacheKey(),
							System.currentTimeMillis());
				}
				executeParserTask(responseBytes);
			}
		}

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			if (isAdded()) {
				readCacheData(getCacheKey());
			}
		}
	};

	protected void executeOnLoadDataSuccess(List<?> data) {
		if (mState == STATE_REFRESH)
			mAdapter.clear();
		mAdapter.addData(data);
		mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
		if (data.size() == 0 && mState == STATE_REFRESH) {
			mErrorLayout.setErrorType(EmptyLayout.NODATA);
		} else if (data.size() < TDevice.getPageSize()) {
			if (mState == STATE_REFRESH)
				mAdapter.setState(ListBaseAdapter.STATE_NO_MORE);
			else
				mAdapter.setState(ListBaseAdapter.STATE_NO_MORE);
		} else {
			mAdapter.setState(ListBaseAdapter.STATE_LOAD_MORE);
		}
	}

	protected void onRefreshNetworkSuccess() {

	}

	protected void executeOnLoadDataError(String error) {
		if (mCurrentPage == 0) {
			mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
		} else {
			mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
			mAdapter.setState(ListBaseAdapter.STATE_NETWORK_ERROR);
			mAdapter.notifyDataSetChanged();
		}
	}

	protected void executeOnLoadFinish() {
		//mListView.onRefreshComplete();
        mSwipeRefresh.setRefreshing(false);
		mState = STATE_NONE;
	}

	private void executeParserTask(byte[] data) {
		cancelParserTask();
		mParserTask = new ParserTask(data);
		mParserTask.execute();
	}

	private void cancelParserTask() {
		if (mParserTask != null) {
			mParserTask.cancel(true);
			mParserTask = null;
		}
	}

	class ParserTask extends AsyncTask<Void, Void, String> {

		private byte[] reponseData;
		private boolean parserError;
		private List<?> list;

		public ParserTask(byte[] data) {
			this.reponseData = data;
		}

		@Override
		protected String doInBackground(Void... params) {
			try {
				ListEntity data = parseList(new ByteArrayInputStream(
						reponseData));
				if (data instanceof Base) {
					Notice notice = ((Base) data).getNotice();
					if (notice != null) {
						UIHelper.sendBroadCast(getActivity(), notice);
					}
				}
				new SaveCacheTask(getActivity(), data, getCacheKey()).execute();
				list = data.getList();
			} catch (Exception e) {
				e.printStackTrace();
				parserError = true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (parserError) {
				readCacheData(getCacheKey());
			} else {
				executeOnLoadDataSuccess(list);
				executeOnLoadFinish();
			}
		}
	}

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int lastVisibleItem = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            int totalItemCount = mLayoutManager.getItemCount();
            if (lastVisibleItem >= totalItemCount - 4 && dy > 0) {
                if (mState== STATE_NONE && mAdapter != null
                        && mAdapter.getDataSize() > 0) {
                    loadMore();
                }
            }
        }
    };

    private static final int SCROLL_DISTANCE = 80; // dp

    class MyLayoutManager extends RecyclerView.LayoutManager {

        private static final String TAG = "MyLayoutManager";

        private int mFirstPosition;

        private final int mScrollDistance;

        public MyLayoutManager(Context c) {
            final DisplayMetrics dm = c.getResources().getDisplayMetrics();
            mScrollDistance = (int) (SCROLL_DISTANCE * dm.density + 0.5f);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            final int parentBottom = getHeight() - getPaddingBottom();
            final View oldTopView = getChildCount() > 0 ? getChildAt(0) : null;
            int oldTop = getPaddingTop();
            if (oldTopView != null) {
                oldTop = oldTopView.getTop();
            }

            detachAndScrapAttachedViews(recycler);

            int top = oldTop;
            int bottom;
            final int left = getPaddingLeft();
            final int right = getWidth() - getPaddingRight();

            final int count = state.getItemCount();
            for (int i = 0; mFirstPosition + i < count && top < parentBottom; i++, top = bottom) {
                View v = recycler.getViewForPosition(mFirstPosition + i);
                addView(v, i);
                measureChildWithMargins(v, 0, 0);
                bottom = top + getDecoratedMeasuredHeight(v);
                layoutDecorated(v, left, top, right, bottom);
            }
        }

        @Override
        public RecyclerView.LayoutParams generateDefaultLayoutParams() {
            return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        @Override
        public boolean canScrollVertically() {
            return true;
        }

        @Override
        public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler,
                                      RecyclerView.State state) {
            if (getChildCount() == 0) {
                return 0;
            }

            int scrolled = 0;
            final int left = getPaddingLeft();
            final int right = getWidth() - getPaddingRight();
            if (dy < 0) {
                while (scrolled > dy) {
                    final View topView = getChildAt(0);
                    final int hangingTop = Math.max(-getDecoratedTop(topView), 0);
                    final int scrollBy = Math.min(scrolled - dy, hangingTop);
                    scrolled -= scrollBy;
                    offsetChildrenVertical(scrollBy);
                    if (mFirstPosition > 0 && scrolled > dy) {
                        mFirstPosition--;
                        View v = recycler.getViewForPosition(mFirstPosition);
                        addView(v, 0);
                        measureChildWithMargins(v, 0, 0);
                        final int bottom = getDecoratedTop(topView);
                        final int top = bottom - getDecoratedMeasuredHeight(v);
                        layoutDecorated(v, left, top, right, bottom);
                    } else {
                        break;
                    }
                }
            } else if (dy > 0) {
                final int parentHeight = getHeight();
                while (scrolled < dy) {
                    final View bottomView = getChildAt(getChildCount() - 1);
                    final int hangingBottom =
                            Math.max(getDecoratedBottom(bottomView) - parentHeight, 0);
                    final int scrollBy = -Math.min(dy - scrolled, hangingBottom);
                    scrolled -= scrollBy;
                    offsetChildrenVertical(scrollBy);
                    if (scrolled < dy && state.getItemCount() > mFirstPosition + getChildCount()) {
                        View v = recycler.getViewForPosition(mFirstPosition + getChildCount());
                        final int top = getDecoratedBottom(getChildAt(getChildCount() - 1));
                        addView(v);
                        measureChildWithMargins(v, 0, 0);
                        final int bottom = top + getDecoratedMeasuredHeight(v);
                        layoutDecorated(v, left, top, right, bottom);
                    } else {
                        break;
                    }
                }
            }
            recycleViewsOutOfBounds(recycler);
           // mListView.setDy(scrolled);
            return scrolled;
        }

        @Override
        public View onFocusSearchFailed(View focused, int direction,
                                        RecyclerView.Recycler recycler, RecyclerView.State state) {
            final int oldCount = getChildCount();

            if (oldCount == 0) {
                return null;
            }

            final int left = getPaddingLeft();
            final int right = getWidth() - getPaddingRight();

            View toFocus = null;
            int newViewsHeight = 0;
            if (direction == View.FOCUS_UP || direction == View.FOCUS_BACKWARD) {
                while (mFirstPosition > 0 && newViewsHeight < mScrollDistance) {
                    mFirstPosition--;
                    View v = recycler.getViewForPosition(mFirstPosition);
                    final int bottom = getDecoratedTop(getChildAt(0));
                    addView(v, 0);
                    measureChildWithMargins(v, 0, 0);
                    final int top = bottom - getDecoratedMeasuredHeight(v);
                    layoutDecorated(v, left, top, right, bottom);
                    if (v.isFocusable()) {
                        toFocus = v;
                        break;
                    }
                }
            }
            if (direction == View.FOCUS_DOWN || direction == View.FOCUS_FORWARD) {
                while (mFirstPosition + getChildCount() < state.getItemCount() &&
                        newViewsHeight < mScrollDistance) {
                    View v = recycler.getViewForPosition(mFirstPosition + getChildCount());
                    final int top = getDecoratedBottom(getChildAt(getChildCount() - 1));
                    addView(v);
                    measureChildWithMargins(v, 0, 0);
                    final int bottom = top + getDecoratedMeasuredHeight(v);
                    layoutDecorated(v, left, top, right, bottom);
                    if (v.isFocusable()) {
                        toFocus = v;
                        break;
                    }
                }
            }

            return toFocus;
        }

        public void recycleViewsOutOfBounds(RecyclerView.Recycler recycler) {
            final int childCount = getChildCount();
            final int parentWidth = getWidth();
            final int parentHeight = getHeight();
            boolean foundFirst = false;
            int first = 0;
            int last = 0;
            for (int i = 0; i < childCount; i++) {
                final View v = getChildAt(i);
                if (v.hasFocus() || (getDecoratedRight(v) >= 0 &&
                        getDecoratedLeft(v) <= parentWidth &&
                        getDecoratedBottom(v) >= 0 &&
                        getDecoratedTop(v) <= parentHeight)) {
                    if (!foundFirst) {
                        first = i;
                        foundFirst = true;
                    }
                    last = i;
                }
            }
            for (int i = childCount - 1; i > last; i--) {
                removeAndRecycleViewAt(i, recycler);
            }
            for (int i = first - 1; i >= 0; i--) {
                removeAndRecycleViewAt(i, recycler);
            }
            if (getChildCount() == 0) {
                mFirstPosition = 0;
            } else {
                mFirstPosition += first;
            }
        }
    }
}
