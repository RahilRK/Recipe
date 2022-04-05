package com.hksofttronix.khansama.Admin.AddNewAdmin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.navMenuDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;


public class adminRightsAdapter extends RecyclerView.Adapter<adminRightsAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<navMenuDetail> arrayList;
    Context context;
    adminRightsOnClick onclick;

    Globalclass globalClass;
    Mydatabase mydatabase;

    public adminRightsAdapter(Context context, ArrayList<navMenuDetail> arrayList, adminRightsOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onclick = onClick;
        globalClass = new Globalclass(context);
        mydatabase = new Mydatabase(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.addadmin_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final navMenuDetail model = arrayList.get(pos);

        holder.navMenuName.setText(model.getNavMenuName());
        holder.navMenuDescription.setText(model.getNavMenuDescription());
        if(model.getAccessStatus()) {
            holder.accessStatus.setChecked(true);
            holder.accessStatus.setTextColor(context.getResources().getColor(R.color.green));
        }
        else {
            holder.accessStatus.setChecked(false);
            holder.accessStatus.setTextColor(context.getResources().getColor(R.color.black));
        }

        holder.accessStatus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                onclick.onCheckStatus(pos,model,holder);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void addData(int pos, navMenuDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, navMenuDetail model) {
        arrayList.set(pos, model);
        notifyItemChanged(pos);
    }

    public void deleteData(int pos) {
        arrayList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount());
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {

        TextView navMenuName, navMenuDescription;
        public SwitchCompat accessStatus;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            navMenuName = itemView.findViewById(R.id.navMenuName);
            navMenuDescription = itemView.findViewById(R.id.navMenuDescription);
            accessStatus = itemView.findViewById(R.id.accessStatus);
        }
    }
}
