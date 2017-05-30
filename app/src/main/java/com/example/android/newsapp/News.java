package com.example.android.newsapp;

/**
 * Created by Adel on 5/29/2017.
 */

public class News {
    private String mTitle;
    private String mSectionName;
    private String mWebUrl;
    private String mPublicationDate;
    private String mAuthor;

    public News(String sectionName, String publicationDate, String title, String webUrl, String author) {
        mTitle = title;
        mSectionName = sectionName;
        mWebUrl = webUrl;
        mPublicationDate = publicationDate;
        mAuthor = author;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmSectionName() {
        return mSectionName;
    }

    public String getmWebUrl() {
        return mWebUrl;
    }

    public String getmPublicationDate() {
        return mPublicationDate;
    }

    public String getmAuthor() {
        return mAuthor;
    }
}