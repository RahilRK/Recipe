package com.hksofttronix.khansama.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

import java.util.Objects;

public class vendorDetail implements Parcelable {
    String localid,vendorId,vendorName,vendorMobileNumber,vendorShopAddress;

    public vendorDetail() {
    }

    protected vendorDetail(Parcel in) {
        localid = in.readString();
        vendorId = in.readString();
        vendorName = in.readString();
        vendorMobileNumber = in.readString();
        vendorShopAddress = in.readString();
    }

    public static final Creator<vendorDetail> CREATOR = new Creator<vendorDetail>() {
        @Override
        public vendorDetail createFromParcel(Parcel in) {
            return new vendorDetail(in);
        }

        @Override
        public vendorDetail[] newArray(int size) {
            return new vendorDetail[size];
        }
    };

    @Exclude
    public String getLocalid() {
        return localid;
    }

    public void setLocalid(String localid) {
        this.localid = localid;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorMobileNumber() {
        return vendorMobileNumber;
    }

    public void setVendorMobileNumber(String vendorMobileNumber) {
        this.vendorMobileNumber = vendorMobileNumber;
    }

    public String getVendorShopAddress() {
        return vendorShopAddress;
    }

    public void setVendorShopAddress(String vendorShopAddress) {
        this.vendorShopAddress = vendorShopAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(localid);
        parcel.writeString(vendorId);
        parcel.writeString(vendorName);
        parcel.writeString(vendorMobileNumber);
        parcel.writeString(vendorShopAddress);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        vendorDetail that = (vendorDetail) o;
        return Objects.equals(vendorName, that.vendorName) &&
                Objects.equals(vendorMobileNumber, that.vendorMobileNumber) &&
                Objects.equals(vendorShopAddress, that.vendorShopAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendorName, vendorMobileNumber, vendorShopAddress);
    }
}
