package com.hksofttronix.khansama.Instructions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.recipeInstructionDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;


public class InstructionAdapter extends RecyclerView.Adapter<InstructionAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<recipeInstructionDetail> arrayList;
    Context context;
    InstructionOnClick onClick;
    Globalclass globalClass;
    Mydatabase mydatabase;

    public InstructionAdapter(Context context, ArrayList<recipeInstructionDetail> arrayList, InstructionOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onClick = onClick;
        globalClass = new Globalclass(context);
        mydatabase = new Mydatabase(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.instruction_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final recipeInstructionDetail model = arrayList.get(pos);

        holder.step.setText("Step #"+model.getStepNumber());
        holder.instruction.setText(model.getInstruction());

        if(model.getChecked()) {
            holder.mainLayout.setBackgroundColor(context.getResources().getColor(R.color.bg_gray));
            holder.checkbox.setChecked(true);
        }
        else {
            holder.mainLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
            holder.checkbox.setChecked(false);
        }

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onClick.onCheckBoxClick(pos,model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void addData(int pos, recipeInstructionDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, recipeInstructionDetail model) {
        arrayList.set(pos, model);
        notifyItemChanged(pos);
    }

    public void deleteData(int pos) {
        arrayList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount());
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {

        LinearLayout mainLayout;
        CheckBox checkbox;
        TextView step,instruction;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            mainLayout = itemView.findViewById(R.id.mainLayout);
            checkbox = itemView.findViewById(R.id.checkbox);
            step = itemView.findViewById(R.id.step);
            instruction = itemView.findViewById(R.id.instruction);
        }
    }
}
