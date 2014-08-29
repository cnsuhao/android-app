
package net.oschina.app.v2.activity.news;

import net.oschina.app.R;
import net.oschina.app.bean.NewsList;
import net.oschina.app.v2.activity.news.fragment.NewsFragment;


public enum NewsTab {
    LASTEST(0,NewsList.CATALOG_ALL, R.string.frame_title_news_lastest, NewsFragment.class),
    BLOG(1, NewsList.CATALOG_INTEGRATION,R.string.frame_title_news_blog, NewsFragment.class),
    RECOMMEND(2,NewsList.CATALOG_SOFTWARE, R.string.frame_title_news_recommend, NewsFragment.class);
    
    private Class<?> clz;
    private int idx;
    private int title;
    private int catalog;

    private NewsTab(int idx,int catalog, int title, Class<?> clz) {
        this.idx = idx;
        this.clz = clz;
        this.setCatalog(catalog);
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

	public int getCatalog() {
		return catalog;
	}

	public void setCatalog(int catalog) {
		this.catalog = catalog;
	}
}
