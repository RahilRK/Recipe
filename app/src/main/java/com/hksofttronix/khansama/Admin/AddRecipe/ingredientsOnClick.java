package com.hksofttronix.khansama.Admin.AddRecipe;

import com.hksofttronix.khansama.Models.ingredientsDetail;

public interface ingredientsOnClick {
    public void onAddQuantity(int position, ingredientsDetail model);
    public void onDelete(int position, ingredientsDetail model);
}
