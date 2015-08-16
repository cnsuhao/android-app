package net.oschina.app.v2.activity.shake;

import android.content.Context;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.seismic.ShakeDetector;
import com.tonlin.osc.happy.R;

import net.oschina.app.v2.AppContext;
import net.oschina.app.v2.api.remote.ShakeApi;
import net.oschina.app.v2.base.BaseFragment;
import net.oschina.app.v2.model.ShakeObject;
import net.oschina.app.v2.utils.StringUtils;
import net.oschina.app.v2.utils.TDevice;
import net.oschina.app.v2.utils.UIHelper;
import net.oschina.app.v2.utils.XmlUtils;

import org.apache.http.Header;

import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Tonlin on 2015/8/14.
 */
public class ShakeFragment extends BaseFragment implements ShakeDetector.Listener {

    private View mRlTop, mRlBottom, mIvTopLine, mIvBottomLine;
    private float distance;
    private boolean mIsAniming;
    private SoundPool sp;
    private int sound, soundSuccess, soundFailure;
    private float volumnRatio;
    private View mLyLoadingTip;
    private View mRlResult;
    private Animation mAnimResultShow;
    private TextView mTvTitle, mTvDesc, mTvAuthor, mTvTime, mTvCommentCount;
    private ImageView mIvAvatar;
    private boolean mIsRequesting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        distance = TDevice.dpToPixel(100);
        sp = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        sound = sp.load(getActivity(), R.raw.shake_sound_male, 1);
        soundSuccess = sp.load(getActivity(), R.raw.shake_match, 1);
        soundFailure = sp.load(getActivity(), R.raw.shake_nomatch, 1);

        AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumnRatio = audioCurrentVolumn / audioMaxVolumn;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sp.release();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.v2_fragment_shake, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mRlTop = view.findViewById(R.id.rl_top);
        mRlBottom = view.findViewById(R.id.rl_bottom);

        mIvTopLine = view.findViewById(R.id.iv_top_line);
        mIvBottomLine = view.findViewById(R.id.iv_bottom_line);

        mLyLoadingTip = view.findViewById(R.id.ly_loading_tip);
        mRlResult = view.findViewById(R.id.rl_result);

        mIvAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
        mTvTitle = (TextView) view.findViewById(R.id.tv_title);
        mTvDesc = (TextView) view.findViewById(R.id.tv_description);
        mTvAuthor = (TextView) view.findViewById(R.id.tv_author);
        mTvTime = (TextView) view.findViewById(R.id.tv_time);
        mTvCommentCount = (TextView) view.findViewById(R.id.tv_comment_count);


        mAnimResultShow = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_shake_result_enter);

        SensorManager sensorManager = (SensorManager)
                getActivity().getSystemService(Context.SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);
    }

    @Override
    public void hearShake() {
        if (mIsAniming || mIsRequesting) {
            return;
        }
        mIsAniming = true;
        playShakeSound();

        mRlResult.setVisibility(View.INVISIBLE);
        mIvTopLine.setVisibility(View.VISIBLE);
        mIvBottomLine.setVisibility(View.VISIBLE);
        ObjectAnimator animTop = ObjectAnimator.ofFloat(mRlTop, "translationY", 0, -distance);
        ObjectAnimator animBottom = ObjectAnimator.ofFloat(mRlBottom, "translationY", 0, distance);
        AnimatorSet mAnimSet = new AnimatorSet();
        mAnimSet.setDuration(3000);
        mAnimSet.play(animTop).with(animBottom);
        mAnimSet.addListener(new SimpleAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator animTop = ObjectAnimator.ofFloat(mRlTop, "translationY", -distance, 0);
                ObjectAnimator animBottom = ObjectAnimator.ofFloat(mRlBottom, "translationY", distance, 0);
                AnimatorSet mAnimSet = new AnimatorSet();
                mAnimSet.setDuration(3000);
                mAnimSet.setStartDelay(1000);
                mAnimSet.play(animTop).with(animBottom);
                mAnimSet.addListener(new SimpleAnimatorListener() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mIsAniming = false;
                        mIvTopLine.setVisibility(View.GONE);
                        mIvBottomLine.setVisibility(View.GONE);
                        startRequest();
                    }
                });
                mAnimSet.start();
            }
        });
        mAnimSet.start();
    }

    private void startRequest() {
        mLyLoadingTip.setVisibility(View.VISIBLE);
        mRlResult.setVisibility(View.INVISIBLE);
        mIsRequesting = true;
        ShakeApi.shake(new ShakeResponseHandler(this));
    }

    private void showResult() {
        mLyLoadingTip.setVisibility(View.GONE);
        mRlResult.setVisibility(View.VISIBLE);
        mRlResult.startAnimation(mAnimResultShow);
    }

    private void playShakeSound() {
        sp.play(sound, volumnRatio, volumnRatio, 1, 0, 1);
    }


    private void executeShakeSuccess(ShakeObject obj) {
        if (StringUtils.isEmpty(obj.getAuthor())
                && StringUtils.isEmpty(obj
                .getCommentCount())
                && StringUtils.isEmpty(obj.getPubDate())) {
            executeShakeFailure();
        } else {
            sp.play(soundSuccess, volumnRatio, volumnRatio, 1, 0, 1);
            fillResult(obj);
            showResult();
        }
        mIsRequesting = false;
    }

    private void fillResult(final ShakeObject obj) {
        mRlResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHelper.showUrlShake(v.getContext(), obj);
            }
        });
        ImageLoader.getInstance().displayImage(obj.getImage(), mIvAvatar);
        mTvTitle.setText(obj.getTitle());
        mTvDesc.setText(obj.getDetail());
        mTvAuthor.setText(obj.getAuthor());
        mTvCommentCount.setText(obj.getCommentCount() + "评论");
        mTvTime.setText(StringUtils.friendly_time(obj.getPubDate()));
    }

    private void executeShakeFailure() {
        sp.play(soundFailure, volumnRatio, volumnRatio, 1, 0, 1);
        AppContext.showToastShort("毛都没摇到一个！");
        mIsRequesting = false;
    }

    private static class ShakeResponseHandler extends AsyncHttpResponseHandler {
        WeakReference<ShakeFragment> ref;

        ShakeResponseHandler(ShakeFragment fragment) {
            ref = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            if (ref == null || ref.get() == null || ref.get().getActivity() == null) {
                return;
            }
            ShakeObject obj = XmlUtils.toBean(
                    ShakeObject.class, new ByteArrayInputStream(bytes));
            ref.get().executeShakeSuccess(obj);
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            if (ref == null || ref.get() == null || ref.get().getActivity() == null) {
                return;
            }
            ref.get().executeShakeFailure();
        }
    }

    public static class SimpleAnimatorListener implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }
}
