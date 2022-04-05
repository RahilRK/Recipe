package com.hksofttronix.khansama.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

import java.util.Objects;

public class navMenuDetail implements Parcelable{

    String localid;
    String adminId;
    String navMenuId;
    String navMenuName;
    String navMenuDescription;
    boolean accessStatus = false;

    public navMenuDetail() {
    }

    protected navMenuDetail(Parcel in) {
        localid = in.readString();
        adminId = in.readString();
        navMenuId = in.readString();
        navMenuName = in.readString();
        navMenuDescription = in.readString();
        accessStatus = in.readByte() != 0;
    }

    public static final Creator<navMenuDetail> CREATOR = new Creator<navMenuDetail>() {
        @Override
        public navMenuDetail createFromParcel(Parcel in) {
            return new navMenuDetail(in);
        }

        @Override
        public navMenuDetail[] newArray(int size) {
            return new navMenuDetail[size];
        }
    };

    @Exclude
    public String getLocalid() {
        return localid;
    }

    public void setLocalid(String localid) {
        this.localid = localid;
    }

    public String getNavMenuId() {
        return navMenuId;
    }

    public void setNavMenuId(String navMenuId) {
        this.navMenuId = navMenuId;
    }

    @Exclude
    public String getNavMenuName() {
        return navMenuName;
    }

    public void setNavMenuName(String navMenuName) {
        this.navMenuName = navMenuName;
    }

    public boolean getAccessStatus() {
        return accessStatus;
    }

    public void setAccessStatus(boolean accessStatus) {
        this.accessStatus = accessStatus;
    }

    @Exclude
    public String getNavMenuDescription() {
        return navMenuDescription;
    }

    public void setNavMenuDescription(String navMenuDescription) {
        this.navMenuDescription = navMenuDescription;
    }

    @Exclude
    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        navMenuDetail that = (navMenuDetail) o;
        return accessStatus == that.accessStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessStatus);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(localid);
        parcel.writeString(adminId);
        parcel.writeString(navMenuId);
        parcel.writeString(navMenuName);
        parcel.writeString(navMenuDescription);
        parcel.writeByte((byte) (accessStatus ? 1 : 0));
    }
}
