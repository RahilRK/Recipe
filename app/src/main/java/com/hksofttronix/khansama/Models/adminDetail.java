package com.hksofttronix.khansama.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class adminDetail  implements Parcelable{
    String adminId, adminName, adminMobileNumber, adminEmailId;
    int addressCount;
    int favouriteCount;
    int cartCount;
    boolean isAdmin;

    public adminDetail() {
    }


    protected adminDetail(Parcel in) {
        adminId = in.readString();
        adminName = in.readString();
        adminMobileNumber = in.readString();
        adminEmailId = in.readString();
        addressCount = in.readInt();
        favouriteCount = in.readInt();
        cartCount = in.readInt();
        isAdmin = in.readByte() != 0;
    }

    public static final Creator<adminDetail> CREATOR = new Creator<adminDetail>() {
        @Override
        public adminDetail createFromParcel(Parcel in) {
            return new adminDetail(in);
        }

        @Override
        public adminDetail[] newArray(int size) {
            return new adminDetail[size];
        }
    };

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminMobileNumber() {
        return adminMobileNumber;
    }

    public void setAdminMobileNumber(String adminMobileNumber) {
        this.adminMobileNumber = adminMobileNumber;
    }

    public String getAdminEmailId() {
        return adminEmailId;
    }

    public void setAdminEmailId(String adminEmailId) {
        this.adminEmailId = adminEmailId;
    }

    public int getAddressCount() {
        return addressCount;
    }

    public void setAddressCount(int addressCount) {
        this.addressCount = addressCount;
    }

    public int getFavouriteCount() {
        return favouriteCount;
    }

    public void setFavouriteCount(int favouriteCount) {
        this.favouriteCount = favouriteCount;
    }

    public int getCartCount() {
        return cartCount;
    }

    public void setCartCount(int cartCount) {
        this.cartCount = cartCount;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(adminId);
        parcel.writeString(adminName);
        parcel.writeString(adminMobileNumber);
        parcel.writeString(adminEmailId);
        parcel.writeInt(addressCount);
        parcel.writeInt(favouriteCount);
        parcel.writeInt(cartCount);
        parcel.writeByte((byte) (isAdmin ? 1 : 0));
    }
}
