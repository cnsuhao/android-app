package net.oschina.app.bean;

import net.oschina.app.R;
import net.oschina.app.v2.activity.comment.fragment.CommentFrament;
import net.oschina.app.v2.activity.favorite.fragment.FavoriteViewPagerFragment;
import net.oschina.app.v2.activity.friend.fragment.FriendViewPagerFragment;
import net.oschina.app.v2.activity.question.fragment.QuestionTagFragment;
import net.oschina.app.v2.activity.settings.fragment.AboutFragment;
import net.oschina.app.v2.activity.settings.fragment.SettingsFragment;
import net.oschina.app.v2.activity.software.fragment.SoftwareViewPagerFragment;
import net.oschina.app.v2.activity.user.fragment.UserProfileFragment;
import net.oschina.app.v2.activity.user.fragment.UserCenterFragment;

public enum SimpleBackPage {

	SETTINGS(0, R.string.actionbar_title_settings, SettingsFragment.class), 
	ABOUT(1, R.string.actionbar_title_about, AboutFragment.class),
	PROFILE(2, R.string.actionbar_title_profile, UserProfileFragment.class),
	FRIENDS(3, R.string.actionbar_title_friends, FriendViewPagerFragment.class),
	FAVORITES(4, R.string.actionbar_title_favorites, FavoriteViewPagerFragment.class),
	SOFTEARE(5, R.string.actionbar_title_ossoftware, SoftwareViewPagerFragment.class),
	COMMENT(6, R.string.actionbar_title_comment, CommentFrament.class),
	QUESTION_TAG(7, R.string.actionbar_title_question, QuestionTagFragment.class),
	USER_CENTER(8, R.string.actionbar_title_user_center, UserCenterFragment.class);

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
