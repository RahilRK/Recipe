package com.hksofttronix.khansama.Admin.RecipeCategoryFrag;

import com.hksofttronix.khansama.Models.recipeCategoryDetail;

public interface adminRecipeOnClick {
    public void onClick(int position, recipeCategoryDetail model);
    public void onDelete(int position, recipeCategoryDetail model);
}
