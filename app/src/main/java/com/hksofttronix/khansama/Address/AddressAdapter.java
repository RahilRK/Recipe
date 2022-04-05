package com.hksofttronix.khansama.Address;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hksofttronix.khansama.Models.addressDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;


public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<addressDetail> arrayList;
    Context context;
    AdddressOnClick onClick;
    Globalclass globalClass;
    Mydatabase mydatabase;

    public AddressAdapter(Context context, ArrayList<addressDetail> arrayList, AdddressOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onClick = onClick;
        globalClass = new Globalclass(context);
        mydatabase = new Mydatabase(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final addressDetail model = arrayList.get(pos);

        holder.name.setText(model.getName());
        holder.mobileNumber.setText("+91"+model.getMobileNumber());
        holder.address.setText(model.getAddress());
        holder.nearBy.setText(model.getNearBy());
        holder.location.setText(globalClass.firstLetterCapital(model.getCity())+", "
                +globalClass.firstLetterCapital(model.getState())+", "+
                model.getPincode()
                );

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.onClick(pos,model);
            }
        });
        holder.ivEditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.onUpdate(pos,model);
            }
        });

        holder.ivRemoveAddress.setOnClickListener(new View.OnClickListener() {
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

    public void addData(int pos, addressDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, addressDetail model) {
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
        TextView name,mobileNumber,address, nearBy,location;
        ImageView ivEditAddress,ivRemoveAddress;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            name = itemView.findViewById(R.id.name);
            mobileNumber = itemView.findViewById(R.id.mobileNumber);
            address = itemView.findViewById(R.id.address);
            nearBy = itemView.findViewById(R.id.nearBy);
            location = itemView.findViewById(R.id.location);
            ivEditAddress = itemView.findViewById(R.id.ivEditAddress);
            ivRemoveAddress = itemView.findViewById(R.id.ivRemoveAddress);
        }
    }
}
