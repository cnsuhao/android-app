package net.oschina.app.v2.activity.user;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.user.fragment.RecyclerListFragment;

/**
 * Created by Tonlin on 2015/8/20.
 */
public class UserCenterActivityDesign extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MyPagerAdapter mPagerAdapter;
    private RecyclerView mRecycleView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v2_activity_user_center_2);


        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

//        mRecycleView = (RecyclerView) findViewById(R.id.recycleView);
//        if(mRecycleView != null) {
//            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
//            mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//            mRecycleView.setLayoutManager(mLayoutManager);
//            RecyclerView.Adapter adapter = new MyAdapter();
//            mRecycleView.setAdapter(adapter);
//        }
    }


    static class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new RecyclerListFragment();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Title :" + position;
        }
    }

    static public class MyListFragment extends ListFragment {

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.v2_list_cell_simple_text, R.id.tv_content);
            for (int i = 0; i < 30; i++) {
                adapter.add("Simple Text :" + i);
            }
            setListAdapter(adapter);
        }
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
