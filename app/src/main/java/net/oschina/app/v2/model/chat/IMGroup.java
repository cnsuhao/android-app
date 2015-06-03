package net.oschina.app.v2.model.chat;

import net.oschina.app.v2.activity.chat.loader.IName;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

/**
 * Created by Tonlin on 2015/5/29.
 */
public class IMGroup extends BmobObject implements IName {

    private String name;
    private String imId;
    private String photo;
    private IMUser owner;
    private String desc;
    private Boolean isPrivate;
    private BmobRelation members;

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

    public String getImId() {
        return imId;
    }

    public void setImId(String imId) {
        this.imId = imId;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public IMUser getOwner() {
        return owner;
    }

    public void setOwner(IMUser owner) {
        this.owner = owner;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public BmobRelation getMembers() {
        return members;
    }

    public void setMembers(BmobRelation members) {
        this.members = members;
    }
}
