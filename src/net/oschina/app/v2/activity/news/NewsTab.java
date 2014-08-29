
package net.oschina.app.v2.activity.news;

import net.oschina.app.R;
import net.oschina.app.v2.activity.news.fragment.NewsFragment;


public enum NewsTab {
    LASTEST(0, R.string.frame_title_news_lastest, NewsFragment.class),
    BLOG(1, R.string.frame_title_news_blog, NewsFragment.class),
    RECOMMEND(2, R.string.frame_title_news_recommend, NewsFragment.class);
    
    private Class<?> clz;
    private int idx;
    private int title;

    private NewsTab(int idx, int title, Class<?> clz) {
        this.idx = idx;
        this.clz = clz;
        this.setTitle(title);
    }

    public static NewsTab getTabByIdx(int idx) {
        for (NewsTab t : values()) {
            if (t.getIdx() == idx)
                return t;
        }
        return LASTEST;
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
}
