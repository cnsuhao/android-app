package net.oschina.app.v2.activity.user.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.base.ListBaseAdapter;

/**
 * Created by Tonlin on 2015/8/20.
 */
public class MyListFragment extends BaseFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.v2_fragment_user_center_listview,container,false);
        ListView lv = (ListView) view.findViewById(R.id.scroll);
        lv.setAdapter(new MyAdpater());
        return view;
    }

    class MyAdpater extends ListBaseAdapter {
        @Override
        public int getCount() {
            return 30;
        }

        @Override
        protected View getRealView(int position, View convertView, ViewGroup parent) {

            return LayoutInflater.from(parent.getContext()).inflate(R.layout.v2_list_cell_simple_text,null);
        }
    }
}
