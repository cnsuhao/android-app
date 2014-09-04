package net.oschina.app.v2.emoji;

import java.util.ArrayList;
import java.util.List;

import net.oschina.app.R;
import net.oschina.app.v2.adapter.RecyclingPagerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class EmojiViewPagerAdapter extends RecyclingPagerAdapter {
	private LayoutInflater infalter;
	private int mEmojiHeight;
	private List<List<Emoji>> mPagers = new ArrayList<List<Emoji>>();

	public EmojiViewPagerAdapter(Context context, List<List<Emoji>> pager,
			int emojiHeight) {
		infalter = LayoutInflater.from(context);
		mPagers = pager;
		mEmojiHeight = emojiHeight;
	}

	public void setEmojiHeight(int emojiHeight) {
		mEmojiHeight = emojiHeight;
		notifyDataSetChanged();
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup container) {
		ViewHolder vh = null;
		if (convertView == null) {
			convertView = infalter.inflate(R.layout.v2_pager_emoji, null);
			vh = new ViewHolder(convertView);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		EmojiAdatper adapter = new EmojiAdatper(mPagers.get(position),
				mEmojiHeight);
		vh.gv.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		return convertView;
	}

	@Override
	public int getCount() {
		return mPagers.size();
	}

	static class ViewHolder {
		GridView gv;

		public ViewHolder(View v) {
			gv = (GridView) v.findViewById(R.id.gv_emoji);
		}
	}
}
