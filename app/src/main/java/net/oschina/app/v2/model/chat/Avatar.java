package net.oschina.app.v2.model.chat;

import cn.bmob.v3.BmobObject;

/**
 * Created by Sim on 2015/6/2.
 */
public class Avatar extends BmobObject {
    private  String fileName;
    private String url;
    private String sourceName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
}

