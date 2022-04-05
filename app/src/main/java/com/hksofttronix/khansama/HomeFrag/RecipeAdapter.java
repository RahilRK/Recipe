package com.hksofttronix.khansama.HomeFrag;

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
import com.hksofttronix.khansama.Models.recipeDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;

import java.util.ArrayList;


public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<recipeDetail> arrayList;
    Context context;
    RecipeOnClick onclick;

    Globalclass globalClass;
    Mydatabase mydatabase;

    public RecipeAdapter(Context context, ArrayList<recipeDetail> arrayList, RecipeOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onclick = onClick;
        globalClass = new Globalclass(context);
        mydatabase = new Mydatabase(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final recipeDetail model = arrayList.get(pos);

        holder.recipeName.setText(globalClass.firstLetterCapital(model.getRecipeName()));
        holder.recipePrice.setText(Html.fromHtml(context.getResources().getString(R.string.typeprice, String.valueOf(model.getPrice()))));
        holder.recipeType.setText(globalClass.firstLetterCapital(model.getRecipeType()));

        if(mydatabase.getFirstRecipeImages(model.getRecipeId()) != null &&
                !mydatabase.getFirstRecipeImages(model.getRecipeId()).isEmpty()) {
            String recipeImageUrl = mydatabase.getFirstRecipeImages(model.getRecipeId()).get(0).getUrl();
            if(!globalClass.checknull(recipeImageUrl).equalsIgnoreCase("")) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions
                        .transforms(new CenterCrop(), new RoundedCorners(16))
                        .placeholder(R.drawable.ic_appicon)
                        .error(R.drawable.ic_appicon);
                Glide
                        .with(context)
                        .load(recipeImageUrl)
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
        }
        else {
            Glide.with(context)
                    .load(context.getResources().getIdentifier("ic_appicon", "drawable", context.getPackageName()))
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_appicon).centerCrop()
                            .fitCenter())
                    .into(holder.ivRecipeImage);
        }

        if (model.getRecipeType().equalsIgnoreCase("Veg")) {
            holder.ivRecipeType.setBackgroundResource(R.drawable.veg);
        } else {
            holder.ivRecipeType.setBackgroundResource(R.drawable.nonveg);
        }

        holder.ivRecipeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                onclick.viewDetail(position,model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void addData(int pos, recipeDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, recipeDetail model) {
        arrayList.set(pos, model);
        notifyItemChanged(pos);
    }

    public void deleteData(int pos) {
        arrayList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount());
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {

        TextView recipeName, recipePrice,recipeType;
        ImageView ivRecipeImage,ivRecipeType;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            recipeName = itemView.findViewById(R.id.recipeName);
            recipePrice = itemView.findViewById(R.id.recipePrice);
            recipeType = itemView.findViewById(R.id.recipeType);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            ivRecipeType = itemView.findViewById(R.id.ivRecipeType);
        }
    }
}
