package net.oschina.app.v2.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.ArrayList;
import java.util.List;

/**
 * TweetLikeUserList.java
 * 
 * @author 火蚁(http://my.oschina.net/u/253900)
 *
 * @data 2015-3-26 下午4:23:32
 */
@SuppressWarnings("serial")
@XStreamAlias("oschina")
public class TweetLikeUserList implements ListEntity {
    
    @XStreamAlias("likeList")
    private List<User> list = new ArrayList<User>();

    public List<User> getList() {
        return list;
    }

    public void getList(List<User> list) {
        this.list = list;
    }
}

