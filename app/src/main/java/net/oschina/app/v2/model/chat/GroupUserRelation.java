package net.oschina.app.v2.model.chat;

import cn.bmob.v3.BmobObject;

/**
 * Created by Tonlin on 2015/6/2.
 */
public class GroupUserRelation extends BmobObject{
    private IMUser member;
    private IMUser owner;//DB查询限制
    private IMGroup group;
    private int sate;

    public IMUser getMember() {
        return member;
    }

    public void setMember(IMUser member) {
        this.member = member;
    }

    public IMGroup getGroup() {
        return group;
    }

    public void setGroup(IMGroup group) {
        this.group = group;
    }

    public int getSate() {
        return sate;
    }

    public void setSate(int sate) {
        this.sate = sate;
    }

    public IMUser getOwner() {
        return owner;
    }

    public void setOwner(IMUser owner) {
        this.owner = owner;
    }
}
