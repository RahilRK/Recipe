package com.hksofttronix.khansama.OrderDetail;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.placeOrderDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;


public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<placeOrderDetail> arrayList;
    Context context;
    OrderItemOnClick onClick;
    Globalclass globalClass;
    Mydatabase mydatabase;
    int orderStep = 0;

    public OrderItemAdapter(Context context, ArrayList<placeOrderDetail> arrayList, int orderStep, OrderItemOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onClick = onClick;
        this.orderStep = orderStep;
        globalClass = new Globalclass(context);
        mydatabase = new Mydatabase(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.orderitem_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final placeOrderDetail model = arrayList.get(pos);

        holder.recipeName.setText(globalClass.firstLetterCapital(model.getRecipeName()));
        holder.recipePrice.setText(Html.fromHtml(context.getResources().getString(R.string.typeprice, String.valueOf(model.getPrice()))));
        holder.quantity.setText("Quantity: "+model.getQuantity());
        if(orderStep == 4) {
            holder.viewInstruction.setVisibility(View.VISIBLE);
        }
        else {
            holder.viewInstruction.setVisibility(View.GONE);
        }

        if(!globalClass.checknull(model.getRecipeImageUrl()).equalsIgnoreCase("")) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions
                    .transforms(new CenterCrop(), new RoundedCorners(16))
                    .placeholder(R.drawable.ic_appicon)
                    .error(R.drawable.ic_appicon);
            Glide
                    .with(context)
                    .load(model.getRecipeImageUrl())
                    .apply(requestOptions)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                            String error = Log.getStackTraceString(e);
                            globalClass.log(tag,error);
                            globalClass.sendErrorLog(tag,"onLoadFailed",error);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                            return false;
                        }
                    })
                    .into(holder.ivRecipeImage);
        }
        else {
            Glide.with(context)
                    .load(context.getResources().getIdentifier("ic_appicon", "drawable", context.getPackageName()))
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_appicon).centerCrop()
                            .fitCenter())
                    .into(holder.ivRecipeImage);

        }

        holder.ivRecipeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.viewDetail(pos,model);
            }
        });

        holder.viewInstruction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.viewRecipeInstruction(pos,model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void addData(int pos, placeOrderDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, placeOrderDetail model) {
        arrayList.set(pos, model);
        notifyItemChanged(pos);
    }

    public void deleteData(int pos) {
        arrayList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount());
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {
        ImageView ivRecipeImage;
        TextView recipeName,recipePrice,quantity,viewInstruction;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            recipeName = itemView.findViewById(R.id.recipeName);
            recipePrice = itemView.findViewById(R.id.recipePrice);
            quantity = itemView.findViewById(R.id.quantity);
            viewInstruction = itemView.findViewById(R.id.viewInstruction);
        }
    }
}
