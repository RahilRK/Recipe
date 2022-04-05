package com.hksofttronix.khansama.Admin.UpdateRecipe;

import android.content.Context;
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
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Models.recipePhotoDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;

import java.util.ArrayList;

public class UpdateRecipePhotos_adapter extends RecyclerView.Adapter<UpdateRecipePhotos_adapter.ViewHolder> {

    String tag = this.getClass().getSimpleName();

    Context context;
    private ArrayList<recipePhotoDetail> arrayList;
    updateRecipePhotosOnClick onClick;
    Globalclass globalclass;

    public UpdateRecipePhotos_adapter(Context context, ArrayList<recipePhotoDetail> itemsData, updateRecipePhotosOnClick onClick) {
        this.context = context;
        this.arrayList = itemsData;
        this.onClick = onClick;
        globalclass = Globalclass.getInstance(context);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UpdateRecipePhotos_adapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                    int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.addrecipephotos_rvitem, null);

        // create ViewHolder

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        final int pos = viewHolder.getAdapterPosition();
        final recipePhotoDetail model = arrayList.get(pos);

        if(model.getNewImages()) {
            Glide.with(context)
                    .load(model.getFilepath())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.image_placeholder).centerCrop()
                            .fitCenter())
                    .into(viewHolder.imageView);
        }
        else {
            if(!globalclass.checknull(model.getUrl()).equalsIgnoreCase("")) {

                Glide
                        .with(context)
                        .load(model.getUrl())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_appicon)
                                .error(R.drawable.ic_appicon)
                                .fitCenter())
                        .addListener(new RequestListener<Drawable>() {

                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                String error = Log.getStackTraceString(e);
                                globalclass.log(tag, "onLoadFailed: "+error);

                                String parameter = "";
                                Gson gson = new Gson();
                                if(model != null) {
                                    parameter = gson.toJson(model);
                                }
                                globalclass.log(tag,"parameter: "+parameter);
                                globalclass.sendResponseErrorLog(tag,"onLoadFailed: ",error, parameter);

                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(viewHolder.imageView);

            }
        }

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onDelete(position,model);
            }
        });
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public ImageView delete;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            imageView = (ImageView) itemLayoutView.findViewById(R.id.imageView);
            delete = (ImageView) itemLayoutView.findViewById(R.id.delete);
        }
    }

    public void addItem(recipePhotoDetail model) {
        arrayList.add(getItemCount(), model);
        notifyItemInserted(getItemCount());
    }

    public void deleteItem(int position)
    {
        arrayList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, arrayList.size());
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}
