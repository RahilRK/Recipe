package com.hksofttronix.khansama.Admin.AdminListFrag;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.userDetail;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;


public class AdminListAdapter extends RecyclerView.Adapter<AdminListAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<userDetail> arrayList;
    Context context;
    AdminListOnClick onclick;

    Globalclass globalClass;

    public AdminListAdapter(Context context, ArrayList<userDetail> arrayList, AdminListOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onclick = onClick;
        globalClass = new Globalclass(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adminlist_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final userDetail model = arrayList.get(pos);

        holder.name.setText(model.getName());
        holder.mobilenumber.setText(
                Html.fromHtml(context.getResources().getString(R.string.typemobilenumber,
                        globalClass.firstLetterCapital(" +91 "+model.getMobilenumber()))));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onclick.viewDetail(model);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void addData(int pos, userDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, userDetail model) {
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
        TextView name, mobilenumber;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            name = itemView.findViewById(R.id.name);
            mobilenumber = itemView.findViewById(R.id.mobilenumber);
        }
    }
}
