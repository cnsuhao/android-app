package net.oschina.app.v2.activity.chat.loader;

/**
 * Created by Tonlin on 2015/1/27.
 */
public interface DisplayListener {

    void onLoadSuccess(AwareView awareView, IName name);

    void onLoadFailure(AwareView awareView, IName name);
}
