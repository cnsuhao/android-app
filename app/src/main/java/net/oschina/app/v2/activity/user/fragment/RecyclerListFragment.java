package net.oschina.app.v2.activity.user.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.base.BaseFragment;

/**
 * Created by Tonlin on 2015/8/20.
 */
public class RecyclerListFragment extends BaseFragment {

    private RecyclerView mRecycleView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.v2_fragment_user_center_tweet, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mRecycleView = (RecyclerView) view.findViewById(R.id.scroll);
        mRecycleView.setHasFixedSize(true);
        LinearLayoutManager  mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(mLayoutManager);
        RecyclerView.Adapter adapter = new MyAdapter();
        mRecycleView.setAdapter(adapter);
    }

    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.v2_list_cell_simple_text, null);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.text.setText("Text :" + position);
        }

        @Override
        public int getItemCount() {
            return 30;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TextView text;

            public ViewHolder(View itemView) {
                super(itemView);
                text = (TextView) itemView.findViewById(R.id.tv_content);
            }
        }
    }
}
