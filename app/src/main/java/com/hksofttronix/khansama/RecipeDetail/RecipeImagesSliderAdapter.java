package com.hksofttronix.khansama.RecipeDetail;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hksofttronix.khansama.Models.recipePhotoDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;

import java.util.ArrayList;
import java.util.Objects;

class RecipeImagesSliderAdapter extends PagerAdapter {

    String tag = this.getClass().getSimpleName();
    // Context object
    Context context;
    Globalclass globalclass;

    // Array of images
    ArrayList<recipePhotoDetail> arrayList;

    // Layout Inflater
    LayoutInflater mLayoutInflater;


    // Viewpager Constructor
    public RecipeImagesSliderAdapter(Context context, ArrayList<recipePhotoDetail> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        globalclass = Globalclass.getInstance(context);
    }

    @Override
    public int getCount() {
        // return the number of images
//            return images.length;
        return arrayList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((LinearLayout) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        // inflating the item.xml
        View itemView = mLayoutInflater.inflate(R.layout.recipeimageslider, container, false);

        // referencing the image view from the item.xml file
        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageViewMain);

        // setting the image in the imageView
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions
                .placeholder(R.drawable.ic_appicon)
                .error(R.drawable.ic_appicon);
        Glide
                .with(context)
                .load(arrayList.get(position).getUrl())
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                        String error = Log.getStackTraceString(e);
                        globalclass.log(tag,error);
                        globalclass.sendErrorLog(tag, "onLoadFailed", error);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                        return false;
                    }
                })
                .into(imageView);


        // Adding the View
        Objects.requireNonNull(container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((LinearLayout) object);
    }
}

