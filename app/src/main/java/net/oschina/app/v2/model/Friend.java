package net.oschina.app.v2.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.oschina.app.v2.utils.GetPinYinUtil;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 好友实体类
 *
 * @author FireAnt（http://my.oschina.net/LittleDY）
 * @created 2014年11月6日 上午11:37:31
 */
@SuppressWarnings("serial")
@XStreamAlias("friend")
public class Friend extends Entity implements Parcelable {

    @XStreamAlias("userid")
    private int userid;

    @XStreamAlias("name")
    private String name;

    @XStreamAlias("from")
    private String from;

    @XStreamAlias("portrait")
    private String portrait;

    @XStreamAlias("expertise")
    private String expertise;

    @XStreamAlias("gender")
    private int gender;

    private String sortKey;
    private boolean selected;

    private String pinYinHeader;
    private String pinYin;

    public Friend(){}

    public Friend(Parcel source) {
        name = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getSortKey() {
        if (sortKey == null) {
            String sortKey = "";
            if (TextUtils.isEmpty(getName()))
                return "";
            String str = GetPinYinUtil.getPinYinHeadChar(getName());
            if (!TextUtils.isEmpty(str))
                sortKey = str.substring(0, 1).toUpperCase();
            setSortKey(sortKey);
        }
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public String getPinYinHeader() {
        if (pinYinHeader == null) {
            pinYinHeader = GetPinYinUtil.getPinYinHeadChar(getName());
            if(pinYinHeader!=null)
                pinYinHeader = pinYinHeader.toUpperCase();
        }
        return pinYinHeader == null ? "" : pinYinHeader;
    }

    public String getPinYin() {
        if (pinYin == null) {
            pinYin = GetPinYinUtil.getPingYin(getName());
            if(pinYin!=null)
                pinYin = pinYin.toUpperCase();
        }
        return pinYin == null ? "" : pinYin;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null)
            return false;
        if(o == this)
            return true;
        if(!(o instanceof Friend))
            return false;
        Friend f = (Friend) o;
        return f.getUserid() == userid;
    }

    @Override
    public int hashCode() {
        return userid;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Creator<Friend> CREATOR = new Creator<Friend>() {
        @Override
        public Friend createFromParcel(Parcel source) {
            return new Friend(source);
        }

        @Override
        public Friend[] newArray(int size) {
            return new Friend[size];
        }
    };
}
