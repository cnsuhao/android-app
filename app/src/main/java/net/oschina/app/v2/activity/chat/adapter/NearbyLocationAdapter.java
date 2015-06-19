package net.oschina.app.v2.activity.chat.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.ListBaseAdapter;

/**
 * Created by Tonlin on 2015/6/19.
 */
public class NearbyLocationAdapter extends ListBaseAdapter {

    private int mSelectedIdx = -1;

    @Override
    protected View getRealView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null || convertView.getTag() == null) {
            convertView = getLayoutInflater(parent.getContext())
                    .inflate(R.layout.v2_list_cell_chat_nearby_location, null);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }


        //vh.name.setText(p.name);
        //vh.desc.setText(p.address);

        vh.selected.setVisibility(mSelectedIdx == position ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }

    public void setSelected(int selected) {
        this.mSelectedIdx = selected;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView name, desc;
        ImageView selected;

        ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.tv_name);
            desc = (TextView) view.findViewById(R.id.tv_desc);
            selected = (ImageView) view.findViewById(R.id.iv_selected);
        }
    }
}
