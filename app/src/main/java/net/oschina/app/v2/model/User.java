package net.oschina.app.v2.model;

import android.text.TextUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 登录用户实体类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
@XStreamAlias("user")
public class User extends Base {

    public final static int RELATION_ACTION_DELETE = 0x00;//取消关注
    public final static int RELATION_ACTION_ADD = 0x01;//加关注

    public final static int RELATION_TYPE_BOTH = 0x01;//双方互为粉丝
    public final static int RELATION_TYPE_FANS_HIM = 0x02;//你单方面关注他
    public final static int RELATION_TYPE_NULL = 0x03;//互不关注
    public final static int RELATION_TYPE_FANS_ME = 0x04;//只有他关注我
    private static final String NODE_RESULT = "result";
    private static final String NODE_ERROR_CODE = "errorCode";
    private static final String NODE_ERROR_MESSAGE = "errorMessage";
    private static final String NODE_UID = "uid";
    private static final String NODE_LOCATION = "location";
    private static final String NODE_NAME = "name";
    private static final String NODE_FOLLOWERS = "followers";
    private static final String NODE_FANS = "fans";
    private static final String NODE_SOCRE = "score";
    private static final String NODE_PORTRAIT = "portrait";

    //    private int uid;
//    private String location;
//    private String name;
//    private int followers;
//    private int fans;
//    private int score;
//    private String face;
    private String account;
    private String pwd;
    private Result validate;
    private boolean isRememberMe;
//    private String jointime;
//    private String gender;
//    private String devplatform;
//    private String expertise;
//    private int relation;
//    private String latestonline;

    @XStreamAlias("uid")
    private int uid;
    @XStreamAlias("location")
    private String location;
    @XStreamAlias("name")
    private String name;
    @XStreamAlias("followers")
    private int followers;
    @XStreamAlias("fans")
    private int fans;
    @XStreamAlias("score")
    private int score;
    @XStreamAlias("portrait")
    private String face;
    @XStreamAlias("jointime")
    private String jointime;
    @XStreamAlias("gender")
    private String gender;
    @XStreamAlias("devplatform")
    private String devplatform;
    @XStreamAlias("expertise")
    private String expertise;
    @XStreamAlias("relation")
    private int relation;
    @XStreamAlias("latestonline")
    private String latestonline;
    @XStreamAlias("from")
    private String from;
    @XStreamAlias("favoritecount")
    private int favoritecount;

    public boolean isRememberMe() {
        return isRememberMe;
    }

    public void setRememberMe(boolean isRememberMe) {
        this.isRememberMe = isRememberMe;
    }

    public String getJointime() {
        return jointime;
    }

    public void setJointime(String jointime) {
        this.jointime = jointime;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDevplatform() {
        return devplatform;
    }

    public void setDevplatform(String devplatform) {
        this.devplatform = devplatform;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }

    public String getLatestonline() {
        return latestonline;
    }

    public void setLatestonline(String latestonline) {
        this.latestonline = latestonline;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFans() {
        return fans;
    }

    public void setFans(int fans) {
        this.fans = fans;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getFace() {
        return face;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public Result getValidate() {
        return validate;
    }

    public void setValidate(Result validate) {
        this.validate = validate;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public int getFavoriteCount() {
        return favoritecount;
    }

    public String getFrom() {
        return from;
    }

    @Override
    public int hashCode() {
        return uid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User))
            return false;
        if (o == null)
            return false;
        User user = (User) o;
        return user.getUid() == uid;
    }


}
