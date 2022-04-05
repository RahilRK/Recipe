package com.hksofttronix.khansama.OrderDetail;

import com.hksofttronix.khansama.Models.placeOrderDetail;

public interface OrderItemOnClick {
    public void viewDetail(int position, placeOrderDetail model);
    public void viewRecipeInstruction(int position, placeOrderDetail model);
}
