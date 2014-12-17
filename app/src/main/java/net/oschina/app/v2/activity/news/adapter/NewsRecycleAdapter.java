package net.oschina.app.v2.activity.news.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.RecycleBaseAdapter;
import net.oschina.app.v2.model.News;
import net.oschina.app.v2.utils.DateUtil;
import net.oschina.app.v2.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class NewsRecycleAdapter extends RecycleBaseAdapter<NewsRecycleAdapter.ViewHolder> {

    private ArrayList _data = new ArrayList();

    private LayoutInflater mInflater;
    public static final int STATE_EMPTY_ITEM = 0;
    public static final int STATE_LOAD_MORE = 1;
    public static final int STATE_NO_MORE = 2;
    public static final int STATE_NO_DATA = 3;
    public static final int STATE_LESS_ONE_PAGE = 4;
    public static final int STATE_NETWORK_ERROR = 5;

    protected int state = STATE_LESS_ONE_PAGE;

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }

    protected LayoutInflater getLayoutInflater(Context context) {
        if (mInflater == null) {
            mInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        return mInflater;
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

    @Override
    public NewsRecycleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(getLayoutInflater(parent.getContext()).inflate(
                R.layout.v2_list_cell_news, null));
    }

    @Override
    public int getItemCount() {
        return _data.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
        NewsRecycleAdapter.ViewHolder holder = ( NewsRecycleAdapter.ViewHolder)vh;
        News news = (News) _data.get(position);
        holder.title.setText(news.getTitle());
        holder.source.setText(news.getAuthor());
        holder.time.setText(DateUtil.getFormatTime(news.getPubDate()));
        //StringUtils.friendly_time(news.getPubDate())
        if(StringUtils.isToday(news.getPubDate())){
            holder.tip.setVisibility(View.VISIBLE);
            holder.tip.setImageResource(R.drawable.ic_today);
        } else {
            holder.tip.setVisibility(View.GONE);
        }
        //vh.commentCount.setText(parent.getResources().getString(R.string.comment_count, news.getCommentCount()));
    }

    //    @Override
//    public void onBindViewHolder(NewsRecycleAdapter.ViewHolder holder, int position) {

//    }

//    @SuppressLint("InflateParams")
//	protected View getRealView(int position, View convertView, ViewGroup parent) {
//		ViewHolder vh = null;
//		if (convertView == null || convertView.getTag() == null) {
//			convertView = getLayoutInflater(parent.getContext()).inflate(
//					R.layout.v2_list_cell_news, null);
//			vh = new ViewHolder(convertView);
//			convertView.setTag(vh);
//		} else {
//			vh = (ViewHolder) convertView.getTag();
//		}
//
//		News news = (News) _data.get(position);
//
//		vh.title.setText(news.getTitle());
//		vh.source.setText(news.getAuthor());
//		vh.time.setText(DateUtil.getFormatTime(news.getPubDate()));
//		//StringUtils.friendly_time(news.getPubDate())
//		if(StringUtils.isToday(news.getPubDate())){
//			vh.tip.setVisibility(View.VISIBLE);
//			vh.tip.setImageResource(R.drawable.ic_today);
//		} else {
//			vh.tip.setVisibility(View.GONE);
//		}
//		vh.commentCount.setText(parent.getResources().getString(R.string.comment_count, news.getCommentCount()));
//		//Drawable d = parent.getContext().getResources().getDrawable(R.drawable.ic_comment_count);
//		//vh.commentCount.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
//		return convertView;
//	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public TextView title, source, time,commentCount;
		public ImageView tip;
		public ViewHolder(View view) {
            super(view);
			title = (TextView) view.findViewById(R.id.tv_title);
			source = (TextView) view.findViewById(R.id.tv_source);
			time = (TextView) view.findViewById(R.id.tv_time);
			commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
			tip = (ImageView) view.findViewById(R.id.iv_tip);
		}
	}
}
