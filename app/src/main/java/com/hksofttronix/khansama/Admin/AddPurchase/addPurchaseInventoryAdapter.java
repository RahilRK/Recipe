package com.hksofttronix.khansama.Admin.AddPurchase;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hksofttronix.khansama.Models.purchaseDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;


public class addPurchaseInventoryAdapter extends RecyclerView.Adapter<addPurchaseInventoryAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<purchaseDetail> arrayList;
    Context context;
    addPurchaseInventoryOnClick onClick;
    Globalclass globalClass;
    Mydatabase mydatabase;

    public addPurchaseInventoryAdapter(Context context, ArrayList<purchaseDetail> arrayList, addPurchaseInventoryOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onClick = onClick;
        globalClass = new Globalclass(context);
        mydatabase = new Mydatabase(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.addpurchaseinventory_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final purchaseDetail model = arrayList.get(pos);

        holder.name.setText(model.getName());
        holder.purchasePrice.setText(Html.fromHtml(context.getResources().getString(R.string.typeprice, String.valueOf(model.getPurchasePrice()))));

        if(model.getQuantity() == 0) {
            holder.quantity.setText(context.getResources().getString(R.string.add_quantity));
        }
        else {
            holder.quantity.setText(model.getQuantity() + "" + model.getSelectedUnit());
        }

        holder.quantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClick.onAddQuantity(pos,model);
            }
        });

        holder.ivdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClick.onDelete(pos,model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void addData(int pos, purchaseDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, purchaseDetail model) {
        arrayList.set(pos, model);
        notifyItemChanged(pos);
    }

    public void deleteData(int pos) {
        arrayList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount());
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {
        TextView name,quantity,purchasePrice;
        ImageView ivdelete;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            quantity = itemView.findViewById(R.id.quantity);
            purchasePrice = itemView.findViewById(R.id.purchasePrice);
            ivdelete = itemView.findViewById(R.id.ivdelete);
        }
    }
}
