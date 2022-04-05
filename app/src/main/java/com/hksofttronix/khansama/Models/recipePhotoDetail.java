package com.hksofttronix.khansama.Models;

import com.google.firebase.firestore.Exclude;

public class recipePhotoDetail {
    String localid,recipeImageId,recipeId,recipeName,imageName,filepath,url;
    boolean newImages = false;
    boolean firstImage;

    public recipePhotoDetail() {
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

    @Exclude
    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Exclude
    public boolean getNewImages() {
        return newImages;
    }

    public void setNewImages(boolean newImages) {
        this.newImages = newImages;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeImageId() {
        return recipeImageId;
    }

    public void setRecipeImageId(String recipeImageId) {
        this.recipeImageId = recipeImageId;
    }

    public boolean getFirstImage() {
        return firstImage;
    }

    public void setFirstImage(boolean firstImage) {
        this.firstImage = firstImage;
    }
}
