package net.oschina.app.v2.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.oschina.app.v2.AppException;
import net.oschina.app.v2.utils.StringUtils;
import net.oschina.app.v2.utils.TLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 动弹列表实体类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class TweetList extends Entity implements ListEntity {

    public final static int CATALOG_LASTEST = 0;
    public final static int CATALOG_HOT = -1;
    private static final String NODE_TWEET_COUNT = "tweetCount";
    private static final java.lang.String TAG = "TweetList";

    private int pageSize;
    private int tweetCount;
    private List<Tweet> tweetlist = new ArrayList<Tweet>();

    public int getPageSize() {
        return pageSize;
    }

    public int getTweetCount() {
        return tweetCount;
    }

    public List<Tweet> getTweetlist() {
        return tweetlist;
    }

    public static String inputStream2String(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = -1;
        while ((i = is.read()) != -1) {
            baos.write(i);
        }
        return baos.toString();
    }

    public static TweetList parse(InputStream inputStream) throws IOException,
            AppException {
        //TLog.log(TAG,"tweet list:"+inputStream2String(inputStream));
        TweetList tweetlist = new TweetList();
        Tweet tweet = null;
        List<User> likeList = null;
        User user = null;
        // 获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        try {
            xmlParser.setInput(inputStream, UTF8);
            // 获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType = xmlParser.getEventType();
            // 一直循环，直到文档结束
            while (evtType != XmlPullParser.END_DOCUMENT) {
                String tag = xmlParser.getName();
                switch (evtType) {
                    case XmlPullParser.START_TAG:
                        //TLog.log(TAG, "start tag:" + tag);
                        if (tag.equalsIgnoreCase(NODE_TWEET_COUNT)) {
                            tweetlist.tweetCount = StringUtils.toInt(
                                    xmlParser.nextText(), 0);
                        } else if (tag.equalsIgnoreCase(NODE_PAGE_SIZE)) {
                            tweetlist.pageSize = StringUtils.toInt(
                                    xmlParser.nextText(), 0);
                        } else if (tag.equalsIgnoreCase(Tweet.NODE_START)) {
                            tweet = new Tweet();
                        } else if (tweet != null) {
                            if (tag.equalsIgnoreCase("likeList")) {
                                likeList = new ArrayList<>();
                            } else if (likeList != null) {
                                if (tag.equalsIgnoreCase("user")) {
                                    user = new User();
                                } else if (user != null) {
                                    if (tag.equalsIgnoreCase("name")) {
                                        user.setName(xmlParser.nextText());
                                    } else if (tag.equalsIgnoreCase("portrait")) {
                                        String face = xmlParser.nextText();
                                        //TLog.log(TAG, "parser like user face：" + face);
                                        user.setFace(face);
                                    }
                                }
                            } else if (tag.equalsIgnoreCase(Tweet.NODE_ID)) {
                                tweet.id = StringUtils.toInt(xmlParser.nextText(),
                                        0);
                            } else if (tag.equalsIgnoreCase(Tweet.NODE_FACE)) {
                                String face = xmlParser.nextText();
                               // TLog.log(TAG, "parser face：" + face);
                                tweet.setFace(face);
                            } else if (tag.equalsIgnoreCase(Tweet.NODE_BODY)) {
                                tweet.setBody(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Tweet.NODE_AUTHOR)) {
                                String author = xmlParser.nextText();
                                //TLog.log(TAG, "parser author：" + author);
                                tweet.setAuthor(author);
                            } else if (tag.equalsIgnoreCase(Tweet.NODE_AUTHORID)) {
                                tweet.setAuthorId(StringUtils.toInt(
                                        xmlParser.nextText(), 0));
                            } else if (tag
                                    .equalsIgnoreCase(Tweet.NODE_COMMENTCOUNT)) {
                                tweet.setCommentCount(StringUtils.toInt(
                                        xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Tweet.NODE_PUBDATE)) {
                                tweet.setPubDate(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Tweet.NODE_IMGSMALL)) {
                                tweet.setImgSmall(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Tweet.NODE_IMGBIG)) {
                                tweet.setImgBig(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Tweet.NODE_APPCLIENT)) {
                                tweet.setAppClient(StringUtils.toInt(
                                        xmlParser.nextText(), 0));
                            }
                        } else if (tag.equalsIgnoreCase(Notice.NODE_NOTICE)) {
                            tweetlist.setNotice(new Notice());
                        } else if (tweetlist.getNotice() != null) {
                            if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                                tweetlist.getNotice().setAtmeCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                                tweetlist.getNotice().setMsgCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                                tweetlist.getNotice().setReviewCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                                tweetlist.getNotice().setNewFansCount(
                                        StringUtils.toInt(xmlParser.nextText(), 0));
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        // 如果遇到标签结束，则把对象添加进集合中
                        if (tag.equalsIgnoreCase(Tweet.NODE_START) && tweet != null) {
                            tweetlist.getTweetlist().add(tweet);
                            tweet = null;
                        } else if (tag.equalsIgnoreCase("likeList") && likeList != null) {
                            likeList = null;
                        } else if (tag.equalsIgnoreCase("user") && user != null) {
                            user = null;
                        }
                        break;
                }
                // 如果xml没有结束，则导航到下一个节点
                evtType = xmlParser.next();
            }
        } catch (XmlPullParserException e) {
            throw AppException.xml(e);
        } finally {
            inputStream.close();
        }
        return tweetlist;
    }

    @Override
    public List<?> getList() {
        return tweetlist;
    }
}
