package com.jose.feelagaininecuador.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jose on 11/12/2015.
 */
public class DocData {
    private static String mQueueTime;

    private String mTitle;
    private String mDescription;
    private String mImageUri;
    private String mUrl;
    private List<String> mHashTags;

    public DocData() {
        mHashTags = new ArrayList<>();
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getImageUri() {
        return mImageUri;
    }

    public void setImageUri(String imageUri) {
        mImageUri = imageUri;
    }

    public static String getQueueTime() {
        return mQueueTime;
    }

    public static void setQueueTime(String queueTime) {
        mQueueTime = queueTime;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public List<String> getHashTags() {
        return mHashTags;
    }

    public void setHashTags(List<String> hashTags) {
        mHashTags = hashTags;
    }
}
