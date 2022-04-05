package com.hksofttronix.khansama.Admin.AddRecipe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;

public class AddRecipePhotos_adapter extends RecyclerView.Adapter<AddRecipePhotos_adapter.ViewHolder> {

    Context context;
    private ArrayList<String> itemsData;
    addRecipePhotosOnClick onClick;

    public AddRecipePhotos_adapter(Context context, ArrayList<String> itemsData, addRecipePhotosOnClick onClick) {
        this.context = context;
        this.itemsData = itemsData;
        this.onClick = onClick;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AddRecipePhotos_adapter.ViewHolder onCreateViewHolder(ViewGroup parent,
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

        Glide.with(context)
                .load(itemsData.get(position))
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_appicon).centerCrop()
                        .fitCenter())
                .into(viewHolder.imageView);

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick.onDelete(position);
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

    public void deleteItem(int position)
    {
        itemsData.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, itemsData.size());
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return itemsData.size();
    }
}
