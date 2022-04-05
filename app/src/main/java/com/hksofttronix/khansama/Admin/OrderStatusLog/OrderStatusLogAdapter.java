package com.hksofttronix.khansama.Admin.OrderStatusLog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.orderStatusLogDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;


public class OrderStatusLogAdapter extends RecyclerView.Adapter<OrderStatusLogAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<orderStatusLogDetail> arrayList;
    Context context;
    Globalclass globalClass;
    Mydatabase mydatabase;

    public OrderStatusLogAdapter(Context context, ArrayList<orderStatusLogDetail> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
        globalClass = new Globalclass(context);
        mydatabase = new Mydatabase(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.orderstatuslog_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final orderStatusLogDetail model = arrayList.get(pos);

        holder.orderStatus.setText(globalClass.firstLetterCapital(model.getOrderStatus()));
        if(model.getEntryBy().equalsIgnoreCase("USER")) {
            holder.userName.setText("Customer");
            holder.userName.setTextColor(context.getResources().getColor(R.color.black));
        }
        else {
            holder.userName.setText(globalClass.firstLetterCapital(model.getUserName()));
            holder.userName.setTextColor(context.getResources().getColor(R.color.mred));
        }
        holder.logDateTime.setText(globalClass.dateToString(model.getLogDateTime(),"dd MMM yyyy hh:mm aa"));
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void addData(int pos, orderStatusLogDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, orderStatusLogDetail model) {
        arrayList.set(pos, model);
        notifyItemChanged(pos);
    }

    public void deleteData(int pos) {
        arrayList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount());
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {

        TextView orderStatus,userName,logDateTime;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            orderStatus = itemView.findViewById(R.id.orderStatus);
            userName = itemView.findViewById(R.id.userName);
            logDateTime = itemView.findViewById(R.id.logDateTime);
        }
    }
}
