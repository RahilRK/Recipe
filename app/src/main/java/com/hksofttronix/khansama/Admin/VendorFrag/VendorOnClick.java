package com.hksofttronix.khansama.Admin.VendorFrag;

import com.hksofttronix.khansama.Models.vendorDetail;

public interface VendorOnClick {
    public void onClick(int position, vendorDetail model);
    public void onDelete(int position, vendorDetail model);
}
