package com.hksofttronix.khansama.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class recipeCategoryDetail implements Parcelable {

    String localid,categoryId,categoryName,categoryIconUrl;

    public recipeCategoryDetail() {
    }

    protected recipeCategoryDetail(Parcel in) {
        localid = in.readString();
        categoryId = in.readString();
        categoryName = in.readString();
        categoryIconUrl = in.readString();
    }

    public static final Creator<recipeCategoryDetail> CREATOR = new Creator<recipeCategoryDetail>() {
        @Override
        public recipeCategoryDetail createFromParcel(Parcel in) {
            return new recipeCategoryDetail(in);
        }

        @Override
        public recipeCategoryDetail[] newArray(int size) {
            return new recipeCategoryDetail[size];
        }
    };

    public String getLocalid() {
        return localid;
    }

    public void setLocalid(String localid) {
        this.localid = localid;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryIconUrl() {
        return categoryIconUrl;
    }

    public void setCategoryIconUrl(String categoryIconUrl) {
        this.categoryIconUrl = categoryIconUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(localid);
        parcel.writeString(categoryId);
        parcel.writeString(categoryName);
        parcel.writeString(categoryIconUrl);
    }
}
