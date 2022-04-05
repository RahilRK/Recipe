package com.hksofttronix.khansama.Admin.PurchaseFrag;

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

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.ArrayList;


public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<purchaseDetail> arrayList;
    Context context;
    PurchaseOnClick onclick;

    Globalclass globalClass;

    public PurchaseAdapter(Context context, ArrayList<purchaseDetail> arrayList, PurchaseOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onclick = onClick;
        globalClass = new Globalclass(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.purchase_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final purchaseDetail model = arrayList.get(pos);

        holder.name.setText(model.getName());
        holder.purchasePrice.setText(Html.fromHtml(context.getResources().getString(R.string.typeprice, String.valueOf(model.getPurchasePrice()))));
        holder.vendorName.setText(
                Html.fromHtml(context.getResources().getString(R.string.typevendorname,
                        globalClass.firstLetterCapital(model.getVendorName()))));
        holder.enterBy.setText(
                Html.fromHtml(context.getResources().getString(R.string.typeenterby, globalClass.firstLetterCapital(model.getAdminName()))));

        if(!globalClass.checknull(model.getBillno()).equalsIgnoreCase("")) {
            holder.billNo.setText(
                    Html.fromHtml(context.getResources().getString(R.string.typebillno, model.getBillno())));
        }
        else {
            holder.billNo.setText(
                    Html.fromHtml(context.getResources().getString(R.string.typebillno, "-")));
        }

        if(model.getPurchaseTime() != null) {
            holder.purchaseTime.setText(globalClass.milliSecondToDateFormat(model.getPurchaseTime().getTime(),
                    "dd MMM yyyy"));
        }
        else {
            holder.purchaseTime.setText("-");
        }

        holder.viewDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.expandable_layout.isExpanded())
                {
                    holder.expandable_layout.collapse();
                    holder.viewDetails.setText(context.getResources().getString(R.string.view_details));

                }
                else
                {
                    holder.expandable_layout.expand();
                    holder.viewDetails.setText(context.getResources().getString(R.string.hide_details));
                }
            }
        });

        holder.ivdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onclick.delete(model);
            }
        });

        convertQuantity(holder,model);
    }

    void convertQuantity(RecyclerViewHolders holder, purchaseDetail model) {

        if(model.getUnit().equalsIgnoreCase("ml") ||
                model.getUnit().equalsIgnoreCase("grams")) {

            if(model.getQuantity() >= 1000) {
                double convertToResult = model.getQuantity()/1000;
                if(model.getUnit().equalsIgnoreCase("ml")) {
                    holder.quantity.setText(String.valueOf(convertToResult)+ "liter");
                }
                else if(model.getUnit().equalsIgnoreCase("grams")) {
                    holder.quantity.setText(String.valueOf(convertToResult)+ "kg");
                }
            }
            else {
                holder.quantity.setText(String.valueOf(model.getQuantity())+ "" +model.getUnit());
            }
        }
        else {
            holder.quantity.setText(String.valueOf(model.getQuantity())+ "" +model.getUnit());
        }
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
        TextView name, quantity,purchasePrice, vendorName, billNo,enterBy,purchaseTime,viewDetails;
        ImageView ivdelete;
        ExpandableLayout expandable_layout;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            quantity = itemView.findViewById(R.id.quantity);
            purchasePrice = itemView.findViewById(R.id.purchasePrice);
            vendorName = itemView.findViewById(R.id.vendorName);
            billNo = itemView.findViewById(R.id.billNo);
            enterBy = itemView.findViewById(R.id.enterBy);
            purchaseTime = itemView.findViewById(R.id.purchaseTime);
            viewDetails = itemView.findViewById(R.id.viewDetails);
            ivdelete = itemView.findViewById(R.id.ivdelete);
            expandable_layout = itemView.findViewById(R.id.expandable_layout);
        }
    }
}
