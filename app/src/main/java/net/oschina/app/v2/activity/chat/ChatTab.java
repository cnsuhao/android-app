
package net.oschina.app.v2.activity.chat;

import com.tonlin.osc.happy.R;

import net.oschina.app.v2.activity.chat.fragment.ContactFragment;
import net.oschina.app.v2.activity.chat.fragment.ConversationFragment;
import net.oschina.app.v2.model.BlogList;
import net.oschina.app.v2.model.NewsList;


public enum ChatTab {
    CONVERSATION(0,NewsList.CATALOG_ALL, R.string.frame_title_chat_conversation, ConversationFragment.class),
    CONTACT(1, BlogList.CATALOG_LATEST,R.string.frame_title_chat_contact, ContactFragment.class);

    private Class<?> clz;
    private int idx;
    private int title;
    private int catalog;

    private ChatTab(int idx, int catalog, int title, Class<?> clz) {
        this.idx = idx;
        this.clz = clz;
        this.setCatalog(catalog);
        this.setTitle(title);
    }

    public static ChatTab getTabByIdx(int idx) {
        for (ChatTab t : values()) {
            if (t.getIdx() == idx)
                return t;
        }
        return CONVERSATION;
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
