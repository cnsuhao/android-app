package net.oschina.app.v2.model.chat;

import cn.bmob.v3.BmobObject;

/**
 * Created by Tonlin on 2015/6/1.
 */
public class Invite extends BmobObject {

    private IMUser from;
    private IMUser to;
    private String message;
    private Long date;

    public IMUser getTo() {
        return to;
    }

    public void setTo(IMUser to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public IMUser getFrom() {

        return from;
    }

    public void setFrom(IMUser from) {
        this.from = from;
    }
}
