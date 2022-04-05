package com.hksofttronix.khansama.CartFrag;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hksofttronix.khansama.Models.cartDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;

import java.util.ArrayList;


public class CartAdapter extends RecyclerView.Adapter<CartAdapter.RecyclerViewHolders> {

    String tag = this.getClass().getSimpleName();

    ArrayList<cartDetail> arrayList;
    Context context;
    CartOnClick onclick;

    Globalclass globalClass;

    public CartAdapter(Context context, ArrayList<cartDetail> arrayList, CartOnClick onClick) {
        this.arrayList = arrayList;
        this.context = context;
        this.onclick = onClick;
        globalClass = new Globalclass(context);
    }

    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_rvitem, null);
        RecyclerViewHolders rcv = new RecyclerViewHolders(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolders holder, int position) {

        final int pos = holder.getAdapterPosition();
        final cartDetail model = arrayList.get(pos);

        holder.recipeName.setText(globalClass.firstLetterCapital(model.getRecipeName()));
        holder.recipePrice.setText(Html.fromHtml(context.getResources().getString(R.string.typeprice, String.valueOf(model.getPrice()))));
        holder.quantity.setText(""+model.getQuantity());

        if(!model.getRecipeStatus()) {
            holder.notAvailable.setVisibility(View.VISIBLE);
        }
        else {
            holder.notAvailable.setVisibility(View.GONE);
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

        holder.ivRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onclick.onRemove(pos,model);
            }
        });

        holder.fabMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onclick.minus(pos,model,holder);
            }
        });

        holder.fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onclick.add(pos,model,holder);
            }
        });

        holder.ivRecipeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onclick.viewDetail(pos,model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.arrayList.size();
    }

    public void addData(int pos, cartDetail model) {
        arrayList.add(pos, model);
        notifyItemInserted(pos);
    }

    public void updateData(int pos, cartDetail model) {
        arrayList.set(pos, model);
        notifyItemChanged(pos);
    }

    public void deleteData(int pos) {
        arrayList.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, getItemCount());
    }

    public class RecyclerViewHolders extends RecyclerView.ViewHolder {

        TextView recipeName, recipePrice, notAvailable;
        ImageView ivRecipeImage,ivRemove;
        public TextView quantity;
        FloatingActionButton fabMinus,fabAdd;

        public RecyclerViewHolders(View itemView) {
            super(itemView);

            recipeName = itemView.findViewById(R.id.recipeName);
            recipePrice = itemView.findViewById(R.id.recipePrice);
            notAvailable = itemView.findViewById(R.id.notAvailable);
            ivRecipeImage = itemView.findViewById(R.id.ivRecipeImage);
            ivRemove = itemView.findViewById(R.id.ivRemove);
            quantity = itemView.findViewById(R.id.quantity);
            fabMinus = itemView.findViewById(R.id.fabMinus);
            fabAdd = itemView.findViewById(R.id.fabAdd);
        }
    }
}
