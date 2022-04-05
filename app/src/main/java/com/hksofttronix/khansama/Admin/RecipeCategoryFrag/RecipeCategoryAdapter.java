package com.hksofttronix.khansama.Admin.RecipeCategoryFrag;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Models.recipeCategoryDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;


public class RecipeCategoryAdapter extends RecyclerView.Adapter<RecipeCategoryAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<recipeCategoryDetail> arrayList;
    Context context;
    adminRecipeOnClick onClick;
    Globalclass globalClass;
    Mydatabase mydatabase;

    public RecipeCategoryAdapter(Context context, ArrayList<recipeCategoryDetail> arrayList, adminRecipeOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onClick = onClick;
        globalClass = new Globalclass(context);
        mydatabase = new Mydatabase(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.admincategory_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final recipeCategoryDetail model = arrayList.get(pos);

        holder.categoryName.setText(globalClass.firstLetterCapital(model.getCategoryName()));

        if(!globalClass.checknull(model.getCategoryIconUrl()).equalsIgnoreCase("")) {

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
                    .into(holder.ivCategoryIcon);
        }
        else {

            Glide.with(context)
                    .load(context.getResources().getIdentifier("ic_appicon", "drawable", context.getPackageName()))
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_appicon).centerCrop()
                            .fitCenter())
                    .into(holder.ivCategoryIcon);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClick.onClick(pos,model);
            }
        });

        holder.ivdelete.setOnClickListener(new View.OnClickListener() {
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

    public void addData(int pos, recipeCategoryDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
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
        MaterialCardView cardView;
        TextView categoryName;
        ImageView ivCategoryIcon, ivdelete;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            categoryName = itemView.findViewById(R.id.categoryName);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            ivdelete = itemView.findViewById(R.id.ivdelete);
        }
    }
}
