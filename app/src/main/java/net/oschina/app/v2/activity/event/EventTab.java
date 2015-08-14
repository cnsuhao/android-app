package net.oschina.app.v2.activity.event;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.event.fragment.EventFragment;
import net.oschina.app.v2.model.EventList;
import net.oschina.app.v2.model.FriendList;

public enum EventTab {
	RECENT(0, EventList.EVENT_LIST_TYPE_NEW_EVENT,
			R.string.frame_title_event_recent, EventFragment.class),

	MINE(1, EventList.EVENT_LIST_TYPE_MY_EVENT, R.string.frame_title_event_mine,
			EventFragment.class);

	private Class<?> clz;
	private int idx;
	private int title;
	private int catalog;

	private EventTab(int idx, int catalog, int title, Class<?> clz) {
		this.idx = idx;
		this.clz = clz;
		this.setCatalog(catalog);
		this.setTitle(title);
	}

	public static EventTab getTabByIdx(int idx) {
		for (EventTab t : values()) {
			if (t.getIdx() == idx)
				return t;
		}
		return RECENT;
	}

	public Class<?> getClz() {
		return clz;
	}

	public void setClz(Class<?> clz) {
		this.clz = clz;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}

	public int getTitle() {
		return title;
	}

	public void setTitle(int title) {
		this.title = title;
	}

	public int getCatalog() {
		return catalog;
	}

	public void setCatalog(int catalog) {
		this.catalog = catalog;
	}
}
