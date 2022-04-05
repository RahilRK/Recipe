package com.hksofttronix.khansama.Admin.AddRecipeInstructions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;

import java.util.ArrayList;


public class AddRecipeInstructionAdapter extends RecyclerView.Adapter<AddRecipeInstructionAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<String> arrayList;
    Context context;
    AddRecipeInstructionOnClick onclick;

    Globalclass globalClass;

    public AddRecipeInstructionAdapter(Context context, ArrayList<String> arrayList, AddRecipeInstructionOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onclick = onClick;
        globalClass = new Globalclass(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.addinstruction_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        String stepNumber = String.valueOf(pos+1);
        holder.step.setText("Step #"+stepNumber);
        holder.instruction.setText(arrayList.get(pos));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onclick.onClick(pos,arrayList.get(pos));
            }
        });

        holder.ivRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onclick.delete(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void addData(int pos, String text) {
        arrayList.add(pos, text);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, String text) {
        arrayList.set(pos, text);
        notifyItemChanged(pos);
    }

    public void deleteData(int pos) {
        arrayList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount());
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {

        MaterialCardView cardView;
        TextView step,instruction;
        ImageView ivRemove;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            step = itemView.findViewById(R.id.step);
            instruction = itemView.findViewById(R.id.instruction);
            ivRemove = itemView.findViewById(R.id.ivRemove);
        }
    }
}
