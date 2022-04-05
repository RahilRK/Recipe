package com.hksofttronix.khansama.HomeFrag;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import com.google.android.material.card.MaterialCardView;
import com.hksofttronix.khansama.Models.recipeCategoryDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;

import java.util.ArrayList;


public class categoryAdapter extends RecyclerView.Adapter<categoryAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<recipeCategoryDetail> arrayList;
    Context context;
    categoryOnClick onclick;

    Globalclass globalClass;

    public int mSelectedItem = 0;

    public categoryAdapter(Context context, ArrayList<recipeCategoryDetail> arrayList, categoryOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onclick = onClick;
        globalClass = new Globalclass(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final recipeCategoryDetail model = arrayList.get(pos);

        if(globalClass.checknull(model.getCategoryIconUrl()) != null) {

            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions
                    .transforms(new CenterCrop(), new RoundedCorners(16))
                    .placeholder(R.drawable.ic_appicon)
                    .error(R.drawable.ic_appicon);
            Glide
                    .with(context)
                    .load(model.getCategoryIconUrl())
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
                    .into(holder.ivCategoryImage);
        }
        else {
            Glide.with(context)
                    .load(context.getResources().getIdentifier("ic_appicon", "drawable", context.getPackageName()))
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_appicon).centerCrop()
                            .fitCenter())
                    .into(holder.ivCategoryImage);
        }

        holder.ivCategoryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onclick.onClick(pos,model);
                mSelectedItem = pos;
                notifyDataSetChanged();
            }
        });

        if(pos == mSelectedItem) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.mainColor));
            int newColor = context.getResources().getColor(R.color.white);
            holder.ivCategoryImage.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);
        }
        else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.white));
            int newColor = context.getResources().getColor(R.color.black);
            holder.ivCategoryImage.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void addData(int pos, recipeCategoryDetail model) {
        try {
            arrayList.add(pos, model);
            notifyItemInserted(pos);
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalClass.sendErrorLog(tag,"addData",error);
        }
    }

    public void updateData(int pos, recipeCategoryDetail model) {
        arrayList.set(pos, model);
        notifyItemChanged(pos);
    }

    public void deleteData(int pos) {
        arrayList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount());
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {

        ImageView ivCategoryImage;
        MaterialCardView cardView;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            ivCategoryImage = itemView.findViewById(R.id.ivCategoryImage);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
