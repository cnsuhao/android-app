package net.oschina.app.v2.activity.chat.loader;

/**
 * Created by Tonlin on 2015/1/27.
 */
public interface AwareView {

    public void setHolder(Object object);

    public Object getHolder();

    public void setText(String text);

    public DisplayListener getDisplayListener();

    public void setDisplayListener(DisplayListener listener);
}
