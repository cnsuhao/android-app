package net.oschina.app.v2.activity.chat.loader;

/**
 * Created by Sim on 2015/1/27.
 */
public interface IName {

    public String getName();

    public String getPhoto();

    public void setCacheTime(long time);

    public long getCacheTime();
}
