package net.oschina.app.v2.ui;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.utils.AvatarUtils;
import net.oschina.app.v2.utils.StringUtils;
import net.oschina.app.v2.utils.UIHelper;

public class AvatarView extends SimpleDraweeView {
    private static final String PGIF = "portrait.gif";
    private int id;
    private String name;
    private boolean mGroup =false;

    public AvatarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AvatarView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(getResources());
        builder.setPlaceholderImage(getResources().getDrawable(R.drawable.ic_default_avatar));
        //builder.setFailureImage(getResources().getDrawable(R.drawable.ic_default_avatar));
        builder.setRoundingParams(new RoundingParams().setRoundAsCircle(true));
        GenericDraweeHierarchy hierarchy = builder.build();
        setHierarchy(hierarchy);

        //setBackgroundResource(R.drawable.ic_default_avatar);
        setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(name)) {
                    UIHelper.showUserCenter(getContext(), id, name);
                }
            }
        });
    }

    public void setUserInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setGroup(boolean group) {
        if(mGroup == group)
            return;
        mGroup = group;
        if (group) {
            getHierarchy().setPlaceholderImage(getResources().getDrawable(R.drawable.ic_default_avatar_group));
        } else {
            getHierarchy().setPlaceholderImage(getResources().getDrawable(R.drawable.ic_default_avatar));
        }
    }

    public void setAvatarUrl(String url) {
        setImageURI(Uri.parse(url));
//        if (url.endsWith(PGIF) || StringUtils.isEmpty(url))
//            setImageBitmap(null);
//        else {
//            //ImageLoader.getInstance().displayImage(
//            //		AvatarUtils.getMiddleAvatar(url), this);
//        }
    }
}
