package net.oschina.app.v2.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.utils.TDevice;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class RecycleBaseAdapter extends RecyclerView.Adapter<RecycleBaseAdapter.ViewHolder> {

	public static final int STATE_EMPTY_ITEM = 0;
	public static final int STATE_LOAD_MORE = 1;
	public static final int STATE_NO_MORE = 2;
	public static final int STATE_NO_DATA = 3;
	public static final int STATE_LESS_ONE_PAGE = 4;
	public static final int STATE_NETWORK_ERROR = 5;

    public static final int TYPE_FOOTER = 0x101;
    public static final int TYPE_HEADER = 0x102;

	protected int state = STATE_LESS_ONE_PAGE;

	protected int _loadmoreText;
	protected int _loadFinishText;
	protected int mScreenWidth;

	private LayoutInflater mInflater;

    @SuppressWarnings("rawtypes")
    protected ArrayList _data = new ArrayList();

    private WeakReference<OnItemClickListener> mListener;

    public interface OnItemClickListener {
       public void onItemClick(View view,int position,Object item);
    }

    protected LayoutInflater getLayoutInflater(Context context) {
		if (mInflater == null) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		return mInflater;
	}

	public void setScreenWidth(int width) {
		mScreenWidth = width;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getState() {
		return this.state;
	}

	public RecycleBaseAdapter() {
		_loadmoreText = R.string.loading;
		_loadFinishText = R.string.loading_no_more;
	}

    @Override
	public int getItemCount() {
		switch (getState()) {
		case STATE_EMPTY_ITEM:
			return getDataSize() + 1;
		case STATE_NETWORK_ERROR:
		case STATE_LOAD_MORE:
			return getDataSize() + 1;
		case STATE_NO_DATA:
			return 0;
		case STATE_NO_MORE:
			return getDataSize() + 1;
		case STATE_LESS_ONE_PAGE:
			return getDataSize();
		default:
			break;
		}
		return getDataSize();
	}

	public int getDataSize() {
		return _data.size();
	}

    public Object getItem(int arg0) {
		if (arg0 < 0)
			return null;
		if (_data.size() > arg0) {
			return _data.get(arg0);
		}
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@SuppressWarnings("rawtypes")
	public void setData(ArrayList data) {
		_data = data;
		notifyDataSetChanged();
	}

	@SuppressWarnings("rawtypes")
	public ArrayList getData() {
		return _data == null ? (_data = new ArrayList()) : _data;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addData(List data) {
		if (_data == null) {
			_data = new ArrayList();
		}
		_data.addAll(data);
		notifyDataSetChanged();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addItem(Object obj) {
		if (_data == null) {
			_data = new ArrayList();
		}
		_data.add(obj);
		notifyDataSetChanged();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addItem(int pos, Object obj) {
		if (_data == null) {
			_data = new ArrayList();
		}
		_data.add(pos, obj);
		notifyDataSetChanged();
	}

	public void removeItem(Object obj) {
		_data.remove(obj);
		notifyDataSetChanged();
	}

	public void clear() {
		_data.clear();
		notifyDataSetChanged();
	}

	public void setLoadmoreText(int loadmoreText) {
		_loadmoreText = loadmoreText;
	}

	public void setLoadFinishText(int loadFinishText) {
		_loadFinishText = loadFinishText;
	}

	protected boolean loadMoreHasBg() {
		return true;
	}

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = new WeakReference<OnItemClickListener>(listener);
    }


    private boolean hasHeader(){
        return false;
    }

    private boolean hasFooter(int position){
        switch (getState()) {
            case STATE_EMPTY_ITEM:
            case STATE_LOAD_MORE:
            case STATE_NO_MORE:
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 && hasHeader()) {
            return TYPE_HEADER;
        } else if(position == getItemCount()-1 && hasFooter(position)) {
            return TYPE_FOOTER;
        }
        return super.getItemViewType(position);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh;
        if(viewType == TYPE_FOOTER) {
            View v = getLayoutInflater(parent.getContext())
                    .inflate(R.layout.v2_list_cell_footer, null);
            vh = new FooterViewHolder(viewType,v);
        } else if(viewType == TYPE_HEADER) {
            vh = onCreateHeaderViewHolder(parent, viewType);
        } else {
            vh = onCreateItemViewHolder(parent,viewType);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(holder.viewType == TYPE_HEADER) {
            onBindHeaderViewHolder(holder,position);
        } else if(holder.viewType == TYPE_FOOTER) {
            onBindFooterViewHolder(holder,position);
        } else {
            onBindItemViewHolder(holder,position);
        }
    }

    private void onBindFooterViewHolder(ViewHolder holder,int position){
        FooterViewHolder vh = (FooterViewHolder)holder;
        if (!loadMoreHasBg()) {
			vh.loadmore.setBackgroundDrawable(null);
        }
        switch (getState()) {
            case STATE_LOAD_MORE:
                vh.loadmore.setVisibility(View.VISIBLE);
                vh.progress.setVisibility(View.VISIBLE);
                vh.text.setVisibility(View.VISIBLE);
                vh.text.setText(_loadmoreText);
                break;
            case STATE_NO_MORE:
                vh.loadmore.setVisibility(View.VISIBLE);
                vh.progress.setVisibility(View.GONE);
                vh.text.setVisibility(View.VISIBLE);
                vh.text.setText(_loadFinishText);
                break;
            case STATE_EMPTY_ITEM:
                vh.progress.setVisibility(View.GONE);
                vh.loadmore.setVisibility(View.GONE);
                vh.text.setVisibility(View.GONE);
                break;
            case STATE_NETWORK_ERROR:
                vh.loadmore.setVisibility(View.VISIBLE);
                vh.progress.setVisibility(View.GONE);
                vh.text.setVisibility(View.VISIBLE);
                if (TDevice.hasInternet()) {
                    vh.text.setText("对不起,出错了");
                } else {
                    vh.text.setText("没有可用的网络");
                }
                break;
            default:
                vh.loadmore.setVisibility(View.GONE);
                vh.progress.setVisibility(View.GONE);
                vh.text.setVisibility(View.GONE);
                break;
        }
    }

    protected ViewHolder onCreateHeaderViewHolder(ViewGroup parent,int viewType){
        if(hasHeader()) {
            throw new RuntimeException("hasHeader return true, you must implement onCreateHeaderViewHolder");
        }
        //TODO do nothing...
        return null;
    }

    protected ViewHolder onCreateItemViewHolder(ViewGroup parent,int viewType){
        //TODO do nothing...
        throw new RuntimeException(" you must implement onCreateItemViewHolder");
    }

    protected void onBindHeaderViewHolder(ViewHolder holder,int position){
        //TODO do nothing...
    }

    protected void onBindItemViewHolder(ViewHolder holder,int position){
        //TODO do nothing...
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public int viewType;

        public ViewHolder(int viewType,View v) {
            super(v);
            this.viewType = viewType;
        }
    }

    public static class FooterViewHolder extends ViewHolder {
        public ProgressBar progress;
        public TextView text;
        public View loadmore;
        public FooterViewHolder(int viewType, View v) {
            super(viewType, v);
            loadmore = v;
            progress = (ProgressBar) v.findViewById(R.id.progressbar);
            text = (TextView)v.findViewById(R.id.text);
        }
    }
}
