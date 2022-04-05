package com.hksofttronix.khansama.Admin.AddRecipe;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hksofttronix.khansama.Models.ingredientsDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class ingredientsAdapter extends RecyclerView.Adapter<ingredientsAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<ingredientsDetail> arrayList;
    Context context;
    ingredientsOnClick onClick;
    Globalclass globalClass;
    Mydatabase mydatabase;

    public ingredientsAdapter(Context context, ArrayList<ingredientsDetail> arrayList, ingredientsOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onClick = onClick;
        globalClass = new Globalclass(context);
        mydatabase = new Mydatabase(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_ingredients_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final ingredientsDetail model = arrayList.get(pos);

        holder.name.setText(model.getName());

        if(model.getQuantity() == 0) {
            holder.quantity.setText(context.getResources().getString(R.string.add_quantity));
        }
        else {
            holder.quantity.setText(model.getQuantity() + "" + model.getUnit());
        }

        holder.price.setText(Html.fromHtml(context.getResources().getString(R.string.typeprice, String.valueOf(model.getPrice()))));

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

    public void addData(int pos, ingredientsDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, ingredientsDetail model) {
        arrayList.set(pos, model);
        notifyItemChanged(pos);
    }

    public void deleteData(int pos) {
        arrayList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount());
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {
        TextView name, quantity, price;
        ImageView ivdelete;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            quantity = itemView.findViewById(R.id.quantity);
            price = itemView.findViewById(R.id.price);
            ivdelete = itemView.findViewById(R.id.ivdelete);
        }
    }
}
