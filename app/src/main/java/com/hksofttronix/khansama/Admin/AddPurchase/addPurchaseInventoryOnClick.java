package com.hksofttronix.khansama.Admin.AddPurchase;

import com.hksofttronix.khansama.Models.purchaseDetail;

public interface addPurchaseInventoryOnClick {
    public void onAddQuantity(int position, purchaseDetail model);
    public void onDelete(int position, purchaseDetail model);
}
