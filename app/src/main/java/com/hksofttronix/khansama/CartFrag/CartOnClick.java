package com.hksofttronix.khansama.CartFrag;

import com.hksofttronix.khansama.Models.cartDetail;

public interface CartOnClick {
    public void onRemove(int position, cartDetail model);
    public void viewDetail(int position, cartDetail model);
    public void minus(int position, cartDetail model, CartAdapter.RecyclerViewHolders holder);
    public void add(int position, cartDetail model, CartAdapter.RecyclerViewHolders holder);
}
