package com.hksofttronix.khansama.Address;

import com.hksofttronix.khansama.Models.addressDetail;

public interface AdddressOnClick {
    public void onClick(int position, addressDetail model);
    public void onDelete(int position, addressDetail model);
    public void onUpdate(int position, addressDetail model);
}
