package com.hksofttronix.khansama.Admin.InventoryFrag;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;

import java.util.ArrayList;


public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<inventoryDetail> arrayList;
    Context context;
    InventoryOnClick onclick;

    Globalclass globalClass;

    public InventoryAdapter(Context context, ArrayList<inventoryDetail> arrayList, InventoryOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onclick = onClick;
        globalClass = new Globalclass(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inventory_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final inventoryDetail model = arrayList.get(pos);

        holder.name.setText(model.getName());
        holder.quantity.setText(model.getQuantity() + "" + model.getUnit());
        holder.miminumquantity.setText("Minimum: "+model.getMinimumQuantity() + "" + model.getUnit());

        holder.ivedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onclick.edit(pos,model);
            }
        });

        holder.ivdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onclick.delete(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void addData(int pos, inventoryDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, inventoryDetail model) {
        arrayList.set(pos, model);
        notifyItemChanged(pos);
    }

    public void deleteData(int pos) {
        arrayList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount());
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {
        TextView name, quantity, lastChangeTime, miminumquantity;
        ImageView ivedit,ivdelete;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            quantity = itemView.findViewById(R.id.quantity);
            lastChangeTime = itemView.findViewById(R.id.purchaseTime);
            miminumquantity = itemView.findViewById(R.id.miminumquantity);
            ivedit = itemView.findViewById(R.id.ivedit);
            ivdelete = itemView.findViewById(R.id.ivdelete);
        }
    }
}
