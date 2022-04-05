package com.hksofttronix.khansama.Models;

import java.util.ArrayList;
import java.util.Objects;

public class recipeDetail {

    String localid,recipeId, recipeName,recipeType;
    String adminId,adminName;
    String categoryId,categoryName;
    ArrayList<String> recipeInstructions;
    boolean status = false;
    int additionalCharges,price;
    String actions;

    public recipeDetail() {
    }

    public String getLocalid() {
        return localid;
    }

    public void setLocalid(String localid) {
        this.localid = localid;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public int getAdditionalCharges() {
        return additionalCharges;
    }

    public void setAdditionalCharges(int additionalCharges) {
        this.additionalCharges = additionalCharges;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
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

    public ArrayList<String> getRecipeInstructions() {
        return recipeInstructions;
    }

    public void setRecipeInstructions(ArrayList<String> recipeInstructions) {
        this.recipeInstructions = recipeInstructions;
    }

    public String getActions() {
        return actions;
    }

    public void setActions(String actions) {
        this.actions = actions;
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

    public String getRecipeType() {
        return recipeType;
    }

    public void setRecipeType(String recipeType) {
        this.recipeType = recipeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        recipeDetail that = (recipeDetail) o;
        return additionalCharges == that.additionalCharges &&
                Objects.equals(recipeName, that.recipeName) &&
                Objects.equals(recipeType, that.recipeType) &&
                Objects.equals(categoryId, that.categoryId) &&
                Objects.equals(categoryName, that.categoryName) &&
                Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipeName, recipeType, categoryId, categoryName, additionalCharges);
    }
}
