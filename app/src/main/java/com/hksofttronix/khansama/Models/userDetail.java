package com.hksofttronix.khansama.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Objects;

public class userDetail implements Parcelable{

    String id,name,emailid,mobilenumber;
    int addressCount;
    int favouriteCount;
    int cartCount;
    boolean admin;
    ArrayList<navMenuDetail> rightsList;

    public userDetail() {
    }

    protected userDetail(Parcel in) {
        id = in.readString();
        name = in.readString();
        emailid = in.readString();
        mobilenumber = in.readString();
        addressCount = in.readInt();
        favouriteCount = in.readInt();
        cartCount = in.readInt();
        admin = in.readByte() != 0;
        rightsList = in.createTypedArrayList(navMenuDetail.CREATOR);
    }

    public static final Creator<userDetail> CREATOR = new Creator<userDetail>() {
        @Override
        public userDetail createFromParcel(Parcel in) {
            return new userDetail(in);
        }

        @Override
        public userDetail[] newArray(int size) {
            return new userDetail[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailid() {
        return emailid;
    }

    public void setEmailid(String emailid) {
        this.emailid = emailid;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    @Exclude
    public int getAddressCount() {
        return addressCount;
    }

    public void setAddressCount(int addressCount) {
        this.addressCount = addressCount;
    }

    @Exclude
    public int getFavouriteCount() {
        return favouriteCount;
    }

    public void setFavouriteCount(int favouriteCount) {
        this.favouriteCount = favouriteCount;
    }

    @Exclude
    public int getCartCount() {
        return cartCount;
    }

    public void setCartCount(int cartCount) {
        this.cartCount = cartCount;
    }

    public boolean getAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public ArrayList<navMenuDetail> getRightsList() {
        return rightsList;
    }

    public void setRightsList(ArrayList<navMenuDetail> rightsList) {
        this.rightsList = rightsList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        userDetail that = (userDetail) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(emailid, that.emailid) &&
                Objects.equals(mobilenumber, that.mobilenumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, emailid, mobilenumber);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(emailid);
        parcel.writeString(mobilenumber);
        parcel.writeInt(addressCount);
        parcel.writeInt(favouriteCount);
        parcel.writeInt(cartCount);
        parcel.writeByte((byte) (admin ? 1 : 0));
        parcel.writeTypedList(rightsList);
    }
}
