package com.hksofttronix.khansama.Admin.SelectInventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;


public class SelectInventoryAdapter extends RecyclerView.Adapter<SelectInventoryAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<inventoryDetail> arrayList;
    Context context;
    SelectInventoryOnClick onClick;
    Globalclass globalClass;
    Mydatabase mydatabase;
    String action;

    public SelectInventoryAdapter(Context context, String action, ArrayList<inventoryDetail> arrayList, SelectInventoryOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.action = action;
        this.onClick = onClick;
        globalClass = new Globalclass(context);
        mydatabase = new Mydatabase(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectinventory_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final inventoryDetail model = arrayList.get(pos);

        holder.name.setText(model.getName());
        holder.quantity.setText(model.getQuantity() + "" + model.getUnit());

        if(action.equalsIgnoreCase("AddRecipe")) {
            holder.checkbox.setVisibility(View.VISIBLE);
        }
        else if(action.equalsIgnoreCase("AddPurchase")) {
            holder.checkbox.setVisibility(View.VISIBLE);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.onCardViewClick(pos,model);
            }
        });

        holder.checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClick.onCheckBoxClick(pos,model);
            }
        });

        applyIconAnimation(model,holder);
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
        CheckBox checkbox;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            name = itemView.findViewById(R.id.name);
            quantity = itemView.findViewById(R.id.quantity);
            checkbox = itemView.findViewById(R.id.checkbox);
        }
    }

    void applyIconAnimation(inventoryDetail model, RecyclerViewHolders holder)
    {
        if (model.getisSelected())
        {
            holder.checkbox.setChecked(true);
        }
        else
        {
            holder.checkbox.setChecked(false);
        }
    }

    public void SelectAll()
    {
        for(int i=0;i<arrayList.size();i++)
        {
            inventoryDetail model = arrayList.get(i);
            model.setisSelected(true);
            updateData(i,model);
            mydatabase.addInventory(model);
        }
    }

    public void UnSelectAll()
    {
        for(int i=0;i<arrayList.size();i++)
        {
            inventoryDetail model = arrayList.get(i);
            model.setisSelected(false);
            updateData(i,model);
            mydatabase.addInventory(model);
        }
    }

    public int selectedArraySize()
    {
        int size = 0;
        for(int i=0;i<arrayList.size();i++)
        {
            if(arrayList.get(i).getisSelected())
            {
                size++;
            }
        }

        return size;
    }
}
