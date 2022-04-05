package com.hksofttronix.khansama.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

import java.util.Objects;

public class inventoryDetail implements Parcelable {
    String localid,inventoryId,name,unitId,unit;
    String adminId,adminName;
    double quantity;
    int minimumQuantity;
    boolean isSelected;
    double costPrice;
    double sellingPrice;

    public inventoryDetail() {
    }

    protected inventoryDetail(Parcel in) {
        localid = in.readString();
        inventoryId = in.readString();
        name = in.readString();
        unitId = in.readString();
        unit = in.readString();
        adminId = in.readString();
        adminName = in.readString();
        quantity = in.readDouble();
        minimumQuantity = in.readInt();
        isSelected = in.readByte() != 0;
        costPrice = in.readDouble();
        sellingPrice = in.readDouble();
    }

    public static final Creator<inventoryDetail> CREATOR = new Creator<inventoryDetail>() {
        @Override
        public inventoryDetail createFromParcel(Parcel in) {
            return new inventoryDetail(in);
        }

        @Override
        public inventoryDetail[] newArray(int size) {
            return new inventoryDetail[size];
        }
    };

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

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

    public int getMinimumQuantity() {
        return minimumQuantity;
    }

    public void setMinimumQuantity(int minimumQuantity) {
        this.minimumQuantity = minimumQuantity;
    }

    public boolean getisSelected() {
        return isSelected;
    }

    public void setisSelected(boolean selected) {
        isSelected = selected;
    }

    @Exclude
    public String getLocalid() {
        return localid;
    }

    public void setLocalid(String localid) {
        this.localid = localid;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    @Exclude
    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(localid);
        parcel.writeString(inventoryId);
        parcel.writeString(name);
        parcel.writeString(unitId);
        parcel.writeString(unit);
        parcel.writeString(adminId);
        parcel.writeString(adminName);
        parcel.writeDouble(quantity);
        parcel.writeInt(minimumQuantity);
        parcel.writeByte((byte) (isSelected ? 1 : 0));
        parcel.writeDouble(costPrice);
        parcel.writeDouble(sellingPrice);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        inventoryDetail that = (inventoryDetail) o;
        return minimumQuantity == that.minimumQuantity &&
                Double.compare(that.costPrice, costPrice) == 0 &&
                Double.compare(that.sellingPrice, sellingPrice) == 0 &&
                Objects.equals(name, that.name) &&
                Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, unit, minimumQuantity, costPrice, sellingPrice);
    }
}
