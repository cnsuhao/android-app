package net.oschina.app.v2.model.chat;

import net.oschina.app.v2.activity.chat.loader.IName;

import cn.bmob.v3.BmobUser;

/**
 * Created by Tonlin on 2015/5/28.
 */
public class IMUser extends BmobUser implements IName {

    private String name;
    private String photo;
    private String imUserName;
    private String imPassword;

    public String getImUserName() {
        return imUserName;
    }

    public void setImUserName(String imUserName) {
        this.imUserName = imUserName;
    }

    public String getImPassword() {
        return imPassword;
    }

    public void setImPassword(String imPassword) {
        this.imPassword = imPassword;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPhoto() {
        return photo;
    }

    @Override
    public void setCacheTime(long time) {

    }

    @Override
    public long getCacheTime() {
        return 0;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
