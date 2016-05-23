package com.menupicture.menupicture;

import android.graphics.Bitmap;

/**
 * Created by wenjie on 5/22/16.
 */
public class ImageItem {
    private String imageUrl;
    private String title;

    public ImageItem(String imageUrl, String title) {
        super();
        this.imageUrl = imageUrl;
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
