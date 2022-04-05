package com.hksofttronix.khansama.Admin.InventoryFrag;

import com.hksofttronix.khansama.Models.inventoryDetail;

public interface InventoryOnClick {
    public void edit(int position, inventoryDetail todolistmodel);
    public void delete(inventoryDetail todolistmodel);
}
