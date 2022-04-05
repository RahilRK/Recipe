package com.hksofttronix.khansama.Admin.AdminHomeFrag;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Admin.AllOrders.AllOrderOnClick;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.allOrderItemDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;

public class HomeOrderListAdapter extends RecyclerView.Adapter<HomeOrderListAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<allOrderItemDetail> arrayList;
    Context context;
    AllOrderOnClick onClick;
    Globalclass globalClass;
    Mydatabase mydatabase;

    public HomeOrderListAdapter(Context context, ArrayList<allOrderItemDetail> arrayList, AllOrderOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onClick = onClick;
        globalClass = new Globalclass(context);
        mydatabase = new Mydatabase(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.homeorder_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final allOrderItemDetail model = arrayList.get(pos);

        holder.recipeName.setText(globalClass.firstLetterCapital(model.getRecipeName()));
        holder.orderStatus.setText(globalClass.firstLetterCapital(model.getOrderStatus()));
        holder.item.setText(Html.fromHtml(context.getResources().getString(R.string.itemBold, String.valueOf(model.getItem()))));
        holder.total.setText(Html.fromHtml(context.getResources().getString(R.string.totalBold, String.valueOf(model.getTotal()))));

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

                            String parameter = "";
                            Gson gson = new Gson();
                            if(model != null) {
                                parameter = gson.toJson(model);
                            }
                            globalClass.log(tag,"parameter: "+parameter);
                            globalClass.sendResponseErrorLog(tag,"onLoadFailed: ",error,parameter);

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

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.onClick(pos,model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void addData(int pos, allOrderItemDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, allOrderItemDetail model) {
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
        ImageView ivRecipeImage;
        TextView recipeName,item,total;
        MaterialButton orderStatus;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            recipeName = itemView.findViewById(R.id.recipeName);
            item = itemView.findViewById(R.id.item);
            total = itemView.findViewById(R.id.total);
            orderStatus = itemView.findViewById(R.id.orderStatus);
        }
    }
}
