package net.oschina.app.v2.model;

import java.io.IOException;
import java.io.InputStream;

import net.oschina.app.v2.AppException;
import net.oschina.app.v2.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * 博客实体类
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class Blog extends Entity {

    public final static int DOC_TYPE_REPASTE = 0;//转帖
    public final static int DOC_TYPE_ORIGINAL = 1;//原创

    public static final String NODE_BLOG = "blog";
    public static final String NODE_ID = "id";
    public static final String NODE_TITLE = "title";
    public static final String NODE_WHERE = "where";
    public static final String NODE_BODY = "body";
    public static final String NODE_AUTHOR = "author";
    public static final String NODE_AUTHOR_ID = "authorid";
    public static final String NODE_DOCUMENT_TYPE = "documentType";
    public static final String NODE_PUB_DATE = "pubDate";
    public static final String NODE_FAVORITE = "favorite";
    public static final String NODE_COMMENT_COUNT = "commentCount";
    public static final String NODE_URL = "url";


    private String title;
    private String where;
    private String body;
    private String author;
    private int authorId;
    private int documentType;
    private String pubDate;
    private int favorite;
    private int commentCount;
    private String url;

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public int getDocumentType() {
        return documentType;
    }

    public void setDocumentType(int documentType) {
        this.documentType = documentType;
    }

    public static Blog parse(InputStream inputStream) throws IOException, AppException {
        Blog blog = null;
        //获得XmlPullParser解析器
        XmlPullParser xmlParser = Xml.newPullParser();
        try {
            xmlParser.setInput(inputStream, UTF8);
            //获得解析到的事件类别，这里有开始文档，结束文档，开始标签，结束标签，文本等等事件。
            int evtType = xmlParser.getEventType();
            //一直循环，直到文档结束
            while (evtType != XmlPullParser.END_DOCUMENT) {
                String tag = xmlParser.getName();
                switch (evtType) {
                    case XmlPullParser.START_TAG:
                        if (tag.equalsIgnoreCase(NODE_BLOG)) {
                            blog = new Blog();
                        } else if (blog != null) {
                            if (tag.equalsIgnoreCase(NODE_ID)) {
                                blog.id = StringUtils.toInt(xmlParser.nextText(), 0);
                            } else if (tag.equalsIgnoreCase(NODE_TITLE)) {
                                blog.setTitle(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_WHERE)) {
                                blog.setWhere(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_BODY)) {
                                blog.setBody(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_AUTHOR)) {
                                blog.setAuthor(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_AUTHOR_ID)) {
                                blog.setAuthorId(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_DOCUMENT_TYPE)) {
                                blog.setDocumentType(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_PUB_DATE)) {
                                blog.setPubDate(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(NODE_FAVORITE)) {
                                blog.setFavorite(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_COMMENT_COUNT)) {
                                blog.setCommentCount(StringUtils.toInt(xmlParser.nextText(), 0));
                            } else if (tag.equalsIgnoreCase(NODE_URL)) {
                                blog.setUrl(xmlParser.nextText());
                            } else if (tag.equalsIgnoreCase(Notice.NODE_NOTICE)) {// 通知信息
                                blog.setNotice(new Notice());
                            } else if (blog.getNotice() != null) {
                                if (tag.equalsIgnoreCase(Notice.NODE_ATME_COUNT)) {
                                    blog.getNotice().setAtmeCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_MESSAGE_COUNT)) {
                                    blog.getNotice().setMsgCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_REVIEW_COUNT)) {
                                    blog.getNotice().setReviewCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                } else if (tag.equalsIgnoreCase(Notice.NODE_NEWFANS_COUNT)) {
                                    blog.getNotice().setNewFansCount(
                                            StringUtils.toInt(xmlParser.nextText(), 0));
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                //如果xml没有结束，则导航到下一个节点
                evtType = xmlParser.next();
            }
        } catch (XmlPullParserException e) {
            throw AppException.xml(e);
        } finally {
            inputStream.close();
        }
        return blog;
    }
}
