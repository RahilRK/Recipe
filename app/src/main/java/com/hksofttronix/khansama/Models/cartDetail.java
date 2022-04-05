package com.hksofttronix.khansama.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;

public class cartDetail implements Parcelable{

    String localid,cartId,recipeId,recipeName,recipeImageId,recipeImageUrl;
    String userId;
    int price;
    int quantity;
    int sum;
    boolean recipeStatus = true;

    public cartDetail() {
    }

    protected cartDetail(Parcel in) {
        localid = in.readString();
        cartId = in.readString();
        recipeId = in.readString();
        recipeName = in.readString();
        recipeImageId = in.readString();
        recipeImageUrl = in.readString();
        userId = in.readString();
        price = in.readInt();
        quantity = in.readInt();
        sum = in.readInt();
        recipeStatus = in.readByte() != 0;
    }

    public static final Creator<cartDetail> CREATOR = new Creator<cartDetail>() {
        @Override
        public cartDetail createFromParcel(Parcel in) {
            return new cartDetail(in);
        }

        @Override
        public cartDetail[] newArray(int size) {
            return new cartDetail[size];
        }
    };

    @Exclude
    public String getLocalid() {
        return localid;
    }

    public void setLocalid(String localid) {
        this.localid = localid;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeImageUrl() {
        return recipeImageUrl;
    }

    public void setRecipeImageUrl(String recipeImageUrl) {
        this.recipeImageUrl = recipeImageUrl;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Exclude
    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public String getRecipeImageId() {
        return recipeImageId;
    }

    public void setRecipeImageId(String recipeImageId) {
        this.recipeImageId = recipeImageId;
    }

    public boolean getRecipeStatus() {
        return recipeStatus;
    }

    public void setRecipeStatus(boolean recipeStatus) {
        this.recipeStatus = recipeStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(localid);
        parcel.writeString(cartId);
        parcel.writeString(recipeId);
        parcel.writeString(recipeName);
        parcel.writeString(recipeImageId);
        parcel.writeString(recipeImageUrl);
        parcel.writeString(userId);
        parcel.writeInt(price);
        parcel.writeInt(quantity);
        parcel.writeInt(sum);
        parcel.writeByte((byte) (recipeStatus ? 1 : 0));
    }
}
