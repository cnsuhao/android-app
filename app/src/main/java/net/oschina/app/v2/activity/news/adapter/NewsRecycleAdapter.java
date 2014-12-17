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

public class NewsRecycleAdapter extends RecycleBaseAdapter {

    @Override
    protected RecycleBaseAdapter.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater(parent.getContext()).inflate(R.layout.v2_list_cell_news,null);
        return new ViewHolder(viewType,view);
    }

    @Override
    protected void onBindItemViewHolder(RecycleBaseAdapter.ViewHolder vh, int position) {
        super.onBindItemViewHolder(vh, position);
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
//    public NewsRecycleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        return new ViewHolder(getLayoutInflater(parent.getContext()).inflate(
//                R.layout.v2_list_cell_news, null));
//    }
//
//
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
//        NewsRecycleAdapter.ViewHolder holder = ( NewsRecycleAdapter.ViewHolder)vh;
//        News news = (News) _data.get(position);
//        holder.title.setText(news.getTitle());
//        holder.source.setText(news.getAuthor());
//        holder.time.setText(DateUtil.getFormatTime(news.getPubDate()));
//        //StringUtils.friendly_time(news.getPubDate())
//        if(StringUtils.isToday(news.getPubDate())){
//            holder.tip.setVisibility(View.VISIBLE);
//            holder.tip.setImageResource(R.drawable.ic_today);
//        } else {
//            holder.tip.setVisibility(View.GONE);
//        }
//        //vh.commentCount.setText(parent.getResources().getString(R.string.comment_count, news.getCommentCount()));
//    }

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

	public static class ViewHolder extends RecycleBaseAdapter.ViewHolder {
		public TextView title, source, time,commentCount;
		public ImageView tip;
		public ViewHolder(int viewType,View view) {
            super(viewType,view);
			title = (TextView) view.findViewById(R.id.tv_title);
			source = (TextView) view.findViewById(R.id.tv_source);
			time = (TextView) view.findViewById(R.id.tv_time);
			commentCount = (TextView) view.findViewById(R.id.tv_comment_count);
			tip = (ImageView) view.findViewById(R.id.iv_tip);
		}
	}
}
