package com.hksofttronix.khansama.Admin.SelectInventory;

import com.hksofttronix.khansama.Models.inventoryDetail;

public interface SelectInventoryOnClick {
    public void onCardViewClick(int position, inventoryDetail model);
    public void onCheckBoxClick(int position, inventoryDetail model);
}
