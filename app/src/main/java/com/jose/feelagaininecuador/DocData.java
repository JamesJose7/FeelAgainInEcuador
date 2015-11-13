package com.jose.feelagaininecuador;

/**
 * Created by Jose on 11/12/2015.
 */
public class DocData {
    private static String mQueueTime;

    private String mTitle;
    private String mDescription;
    private String mImageUri;

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
}
