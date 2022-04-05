package com.hksofttronix.khansama.Admin.VendorFrag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hksofttronix.khansama.Models.vendorDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;


public class VendorAdapter extends RecyclerView.Adapter<VendorAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<vendorDetail> arrayList;
    Context context;
    VendorOnClick onClick;
    Globalclass globalClass;
    Mydatabase mydatabase;

    public VendorAdapter(Context context, ArrayList<vendorDetail> arrayList, VendorOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onClick = onClick;
        globalClass = new Globalclass(context);
        mydatabase = new Mydatabase(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.vendor_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final vendorDetail model = arrayList.get(pos);

        holder.vendorName.setText("Name: "+model.getVendorName().substring(0,1).toUpperCase() +
                model.getVendorName().substring(1));
        holder.vendorMobileNumber.setText("Mobile number: "+model.getVendorMobileNumber());

        if(!globalClass.checknull(model.getVendorShopAddress()).equalsIgnoreCase("")) {
            holder.vendorShopAddress.setText("Address: "+model.getVendorShopAddress());
        }
        else {
            holder.vendorShopAddress.setText("Address: -");
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.onClick(pos,model);
            }
        });

        holder.ivdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.onDelete(pos,model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void addData(int pos, vendorDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, vendorDetail model) {
        arrayList.set(pos, model);
        notifyItemChanged(pos);
    }

    public void deleteData(int pos) {
        arrayList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount());
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView vendorName, vendorMobileNumber,vendorShopAddress;
        ImageView ivdelete;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            vendorName = itemView.findViewById(R.id.vendorName);
            vendorMobileNumber = itemView.findViewById(R.id.vendorMobileNumber);
            vendorShopAddress = itemView.findViewById(R.id.vendorShopAddress);
            ivdelete = itemView.findViewById(R.id.ivdelete);
        }
    }
}
