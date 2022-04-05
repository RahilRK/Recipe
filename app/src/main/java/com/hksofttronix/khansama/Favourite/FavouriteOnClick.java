package com.hksofttronix.khansama.Favourite;

import com.hksofttronix.khansama.Models.favouriteDetail;

public interface FavouriteOnClick {
    public void onRemove(int position, favouriteDetail model);
    public void viewDetail(int position, favouriteDetail model);
}
