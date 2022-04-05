package com.hksofttronix.khansama.Admin.AdminHomeFrag;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hksofttronix.khansama.Admin.AddPurchase.AddPurchase;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;


public class HomeLimitedInventoryAdapter extends RecyclerView.Adapter<HomeLimitedInventoryAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<inventoryDetail> arrayList;
    Context context;
    Globalclass globalClass;

    public HomeLimitedInventoryAdapter(Context context, ArrayList<inventoryDetail> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
        globalClass = new Globalclass(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.homelimitedinventory_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final inventoryDetail model = arrayList.get(pos);

        holder.name.setText(model.getName());
        holder.quantity.setText(model.getQuantity() + "" + model.getUnit());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, AddPurchase.class));
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
        MaterialCardView cardView;
        TextView name, quantity;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            name = itemView.findViewById(R.id.name);
            quantity = itemView.findViewById(R.id.quantity);
        }
    }
}
