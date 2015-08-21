package net.oschina.app.v2.activity.user.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.model.User;
import net.oschina.app.v2.ui.empty.EmptyLayout;
import net.oschina.app.v2.utils.StringUtils;

/**
 * Created by Tonlin on 2015/8/21.
 */
public class UserBaseInfoAdapter extends RecyclerView.Adapter<UserBaseInfoAdapter.ViewHolder> {
    public static final int STATE_INFO = 0;
    public static final int STATE_ERROR = 1;
    public static final int STATE_LOADING = 2;
    private static final String TAG = "UserBaseInfoAdapter";
    private int state;
    private User mUser;

    private OnSingleViewClickListener mListener;

    public interface OnSingleViewClickListener{
        void onSingleViewClick(View v);
    }

    public UserBaseInfoAdapter(OnSingleViewClickListener lis){
        mListener = lis;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public void setState(int state) {
        this.state = state;
        Log.e(TAG,"set state :"+state);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.v2_list_user_center_information, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (state){
            case STATE_INFO:
                holder.errorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
                holder.lyInfo.setVisibility(View.VISIBLE);
                if (mUser != null) {
                    holder.tvJoinTime.setText(mUser.getJointime());
                    holder.location.setText(mUser.getLocation());
                    holder.developmentPlatform.setText(mUser.getDevplatform());
                    holder. academicFocus.setText(mUser.getExpertise());
                    holder.latestLoginTime.setText(StringUtils.friendly_time(mUser.getLatestonline()));
                }
                break;
            case STATE_ERROR:
                holder.errorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
                holder.lyInfo.setVisibility(View.GONE);
                break;
            case STATE_LOADING:
                holder.errorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
                holder.lyInfo.setVisibility(View.GONE);
                break;
        }
        if(mListener!=null){
            holder.errorLayout.setOnLayoutClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onSingleViewClick(v);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private EmptyLayout errorLayout;
        private TextView tvJoinTime, location, developmentPlatform, academicFocus,latestLoginTime;
        private LinearLayout lyInfo;
        public ViewHolder(View itemView) {
            super(itemView);
            lyInfo = (LinearLayout) itemView.findViewById(R.id.ly_info);
            errorLayout = (EmptyLayout) itemView.findViewById(R.id.error_layout);
            tvJoinTime = (TextView) itemView
                    .findViewById(R.id.tv_join_time);
            location = (TextView) itemView
                    .findViewById(R.id.tv_location);
            developmentPlatform = (TextView) itemView
                    .findViewById(R.id.tv_development_platform);
            academicFocus = (TextView) itemView
                    .findViewById(R.id.tv_academic_focus);
            latestLoginTime = (TextView)itemView.findViewById(R.id.tv_latest_login_time);
        }
    }
}
