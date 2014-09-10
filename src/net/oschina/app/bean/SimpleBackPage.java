package net.oschina.app.bean;

import net.oschina.app.v2.activity.comment.fragment.CommentFrament;
import net.oschina.app.v2.activity.comment.fragment.CommentReplyFragment;
import net.oschina.app.v2.activity.favorite.fragment.FavoriteViewPagerFragment;
import net.oschina.app.v2.activity.friend.fragment.FriendViewPagerFragment;
import net.oschina.app.v2.activity.message.fragment.MessageDetailFragment;
import net.oschina.app.v2.activity.message.fragment.MessagePublicFragment;
import net.oschina.app.v2.activity.question.fragment.QuestionPublicFragment;
import net.oschina.app.v2.activity.question.fragment.QuestionTagFragment;
import net.oschina.app.v2.activity.settings.fragment.AboutFragment;
import net.oschina.app.v2.activity.settings.fragment.SettingsFragment;
import net.oschina.app.v2.activity.software.fragment.SoftwareViewPagerFragment;
import net.oschina.app.v2.activity.tweet.fragment.TweetPublicFragment;
import net.oschina.app.v2.activity.user.fragment.UserCenterFragment;
import net.oschina.app.v2.activity.user.fragment.UserProfileFragment;

import com.tonlin.osc.happy.R;

public enum SimpleBackPage {

	SETTINGS(0, R.string.actionbar_title_settings, SettingsFragment.class), 
	ABOUT(1, R.string.actionbar_title_about, AboutFragment.class),
	PROFILE(2, R.string.actionbar_title_profile, UserProfileFragment.class),
	FRIENDS(3, R.string.actionbar_title_friends, FriendViewPagerFragment.class),
	FAVORITES(4, R.string.actionbar_title_favorites, FavoriteViewPagerFragment.class),
	SOFTEARE(5, R.string.actionbar_title_ossoftware, SoftwareViewPagerFragment.class),
	COMMENT(6, R.string.actionbar_title_comment, CommentFrament.class),
	QUESTION_TAG(7, R.string.actionbar_title_question, QuestionTagFragment.class),
	USER_CENTER(8, R.string.actionbar_title_user_center, UserCenterFragment.class),
	QUESTION_PUBLIC(9, R.string.actionbar_title_question_public, QuestionPublicFragment.class),
	TWEET_PUBLIC(10, R.string.actionbar_title_tweet_public, TweetPublicFragment.class),
	REPLY_COMMENT(11, R.string.actionbar_title_reply_comment, CommentReplyFragment.class),
	MESSAGE_PUBLIC(12, R.string.actionbar_title_message_public, MessagePublicFragment.class),
	MESSAGE_DETAIL(13, R.string.actionbar_title_message_detail, MessageDetailFragment.class);

	private int title;
	private Class<?> clz;
	private int value;

	private SimpleBackPage(int value, int title, Class<?> clz) {
		this.value = value;
		this.title = title;
		this.clz = clz;
	}

	public int getTitle() {
		return title;
	}

	public void setTitle(int title) {
		this.title = title;
	}

	public Class<?> getClz() {
		return clz;
	}

	public void setClz(Class<?> clz) {
		this.clz = clz;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public static SimpleBackPage getPageByValue(int val) {
		for (SimpleBackPage p : values()) {
			if (p.getValue() == val)
				return p;
		}
		return null;
	}
}
