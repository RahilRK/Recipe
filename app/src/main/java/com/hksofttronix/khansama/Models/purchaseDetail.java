package com.hksofttronix.khansama.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class purchaseDetail implements Parcelable{

    String localId,vendorId,vendorName,purchaseId,inventoryId,name,unit,unitId,billno;
    String adminId,adminName;
    String selectedUnit;
    double quantity,purchasePrice,perPrice;
    @ServerTimestamp
    Date purchaseTime;
    boolean showDialogueToChangePrice = true;

    public purchaseDetail() {
    }

    protected purchaseDetail(Parcel in) {
        localId = in.readString();
        vendorId = in.readString();
        vendorName = in.readString();
        purchaseId = in.readString();
        inventoryId = in.readString();
        name = in.readString();
        unit = in.readString();
        unitId = in.readString();
        billno = in.readString();
        adminId = in.readString();
        adminName = in.readString();
        selectedUnit = in.readString();
        quantity = in.readDouble();
        purchasePrice = in.readDouble();
        perPrice = in.readDouble();
        showDialogueToChangePrice = in.readByte() != 0;
    }

    public static final Creator<purchaseDetail> CREATOR = new Creator<purchaseDetail>() {
        @Override
        public purchaseDetail createFromParcel(Parcel in) {
            return new purchaseDetail(in);
        }

        @Override
        public purchaseDetail[] newArray(int size) {
            return new purchaseDetail[size];
        }
    };

    public String getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }

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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getBillno() {
        return billno;
    }

    public void setBillno(String billno) {
        this.billno = billno;
    }

    @Exclude
    public String getSelectedUnit() {
        return selectedUnit;
    }

    public void setSelectedUnit(String selectedUnit) {
        this.selectedUnit = selectedUnit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Date getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(Date purchaseTime) {
        this.purchaseTime = purchaseTime;
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

    @Exclude
    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
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

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    @Exclude
    public boolean getShowDialogueToChangePrice() {
        return showDialogueToChangePrice;
    }

    public void setShowDialogueToChangePrice(boolean showDialogueToChangePrice) {
        this.showDialogueToChangePrice = showDialogueToChangePrice;
    }

    public double getPerPrice() {
        return perPrice;
    }

    public void setPerPrice(double perPrice) {
        this.perPrice = perPrice;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(localId);
        parcel.writeString(vendorId);
        parcel.writeString(vendorName);
        parcel.writeString(purchaseId);
        parcel.writeString(inventoryId);
        parcel.writeString(name);
        parcel.writeString(unit);
        parcel.writeString(unitId);
        parcel.writeString(billno);
        parcel.writeString(adminId);
        parcel.writeString(adminName);
        parcel.writeString(selectedUnit);
        parcel.writeDouble(quantity);
        parcel.writeDouble(purchasePrice);
        parcel.writeDouble(perPrice);
        parcel.writeByte((byte) (showDialogueToChangePrice ? 1 : 0));
    }
}
