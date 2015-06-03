package net.oschina.app.v2.model.chat;

import android.text.TextUtils;
import android.view.textservice.TextInfo;

import net.oschina.app.v2.activity.chat.loader.IName;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import cn.bmob.v3.BmobUser;

/**
 * Created by Tonlin on 2015/5/28.
 */
public class IMUser extends BmobUser implements IName {

    private String name;
    private String photo;
    private String imUserName;
    private String imPassword;
    private String sortKey;
    private Integer uid;

    private boolean selected;

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

    public String getSortKey() {
        if (sortKey == null) {
            String sortKey = "";
            if(TextUtils.isEmpty(getName()))
                return "";
            if (isChinese(getName().charAt(0))) {
                String str = spell(getName().substring(0, 1));
                sortKey = str.substring(0, 1).toUpperCase();
            } else {
                sortKey = getName().substring(0, 1).toUpperCase();
            }
            // TLog.log("sortKey:" + sortKey);
            setSortKey(sortKey);
        }
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public static boolean isChinese(char a) {
        int v = (int) a;
        return (v >= 19968 && v <= 171941);
    }

    public static String spell(String chinese) {
        if (chinese == null) {
            return null;
        }
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);// 小写
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);// 不标声调
        format.setVCharType(HanyuPinyinVCharType.WITH_V);// u:的声母替换为v
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < chinese.length(); i++) {
                String[] array = PinyinHelper.toHanyuPinyinStringArray(
                        chinese.charAt(i), format);
                if (array == null || array.length == 0) {
                    continue;
                }
                String s = array[0];// 不管多音字,只取第一个
                char c = s.charAt(0);// 大写第一个字母
                String pinyin = String.valueOf(c).toUpperCase()
                        .concat(s.substring(1));
                sb.append(pinyin);
            }
            return sb.toString();
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
