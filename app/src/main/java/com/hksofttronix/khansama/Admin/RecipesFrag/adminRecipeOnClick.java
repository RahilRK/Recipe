package com.hksofttronix.khansama.Admin.RecipesFrag;

import com.hksofttronix.khansama.Models.recipeDetail;

public interface adminRecipeOnClick {
    public void onCheckStatus(int position, recipeDetail model, adminRecipeAdapter.RecyclerViewHolders holder);
    public void viewDetail(int position, recipeDetail model);
}
