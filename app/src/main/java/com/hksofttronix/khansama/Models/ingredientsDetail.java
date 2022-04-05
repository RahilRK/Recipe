package com.hksofttronix.khansama.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

public class ingredientsDetail implements Parcelable {
    String localid,ingredientId,inventoryId,recipeId,name,unitId,unit;
    double quantity;
    String recipeName;
    double price;

    public ingredientsDetail() {
    }

    protected ingredientsDetail(Parcel in) {
        localid = in.readString();
        ingredientId = in.readString();
        inventoryId = in.readString();
        recipeId = in.readString();
        name = in.readString();
        unitId = in.readString();
        unit = in.readString();
        quantity = in.readDouble();
        recipeName = in.readString();
        price = in.readDouble();
    }

    public static final Creator<ingredientsDetail> CREATOR = new Creator<ingredientsDetail>() {
        @Override
        public ingredientsDetail createFromParcel(Parcel in) {
            return new ingredientsDetail(in);
        }

        @Override
        public ingredientsDetail[] newArray(int size) {
            return new ingredientsDetail[size];
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

    @Exclude
    public String getLocalid() {
        return localid;
    }

    public void setLocalid(String localid) {
        this.localid = localid;
    }

    @Exclude
    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(String ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(localid);
        parcel.writeString(ingredientId);
        parcel.writeString(inventoryId);
        parcel.writeString(recipeId);
        parcel.writeString(name);
        parcel.writeString(unitId);
        parcel.writeString(unit);
        parcel.writeDouble(quantity);
        parcel.writeString(recipeName);
        parcel.writeDouble(price);
    }
}
