package com.hksofttronix.khansama.Instructions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Models.recipeDetail;
import com.hksofttronix.khansama.Models.recipeInstructionDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;

import java.util.ArrayList;

public class Instructions extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = Instructions.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    SwipeRefreshLayout swipeRefresh;
    ImageView ivRecipeImage;
    TextView recipeName, hint;
    RecyclerView recyclerView;

    String recipeId = "";
    recipeDetail model;

    ArrayList<recipeInstructionDetail> arrayList = new ArrayList<>();
    InstructionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        setToolbar();
        init();
        binding();
        onClick();
        handleIntent();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        toolbar.setTitle(R.string.instruction);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void binding() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        ivRecipeImage = findViewById(R.id.ivRecipeImage);
        recipeName = findViewById(R.id.recipeName);
        hint = findViewById(R.id.hint);
        recyclerView = findViewById(R.id.recyclerView);
    }

    void onClick() {

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                getRecipeDetails();
            }
        });
    }

    void handleIntent() {
        recipeId = getIntent().getStringExtra("recipeId");
        model = mydatabase.getParticularRecipeDetail(recipeId);

        if (model != null) {
            setText();
            getData();
        } else {
            globalclass.log(tag, "recipeDetails model is null!");
            globalclass.toast_long("Unable to get recipe instructions, please refresh any try again!");
        }
    }

    void setText() {
        recipeName.setText(globalclass.firstLetterCapital(model.getRecipeName()));
        hint.setText("Follow the step given below to prepare " + globalclass.firstLetterCapital(model.getRecipeName()));
        if (mydatabase.getFirstRecipeImages(model.getRecipeId()) != null &&
                !mydatabase.getFirstRecipeImages(model.getRecipeId()).isEmpty()) {
            String recipeImageUrl = mydatabase.getFirstRecipeImages(model.getRecipeId()).get(0).getUrl();
            if (!globalclass.checknull(recipeImageUrl).equalsIgnoreCase("")) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions
                        .transforms(new CenterCrop(), new RoundedCorners(16))
                        .placeholder(R.drawable.ic_appicon)
                        .error(R.drawable.ic_appicon);
                Glide
                        .with(activity)
                        .load(recipeImageUrl)
                        .apply(requestOptions)
                        .addListener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                String error = Log.getStackTraceString(e);
                                globalclass.log(tag, error);
                                globalclass.sendErrorLog(tag,"onLoadFailed",error);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                return false;
                            }
                        })
                        .into(ivRecipeImage);
            } else {
                Glide.with(activity)
                        .load(getResources().getIdentifier("ic_appicon", "drawable", getPackageName()))
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_appicon).centerCrop()
                                .fitCenter())
                        .into(ivRecipeImage);
            }
        } else {
            Glide.with(activity)
                    .load(getResources().getIdentifier("ic_appicon", "drawable", getPackageName()))
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_appicon).centerCrop()
                            .fitCenter())
                    .into(ivRecipeImage);
        }
    }

    void getData() {

        arrayList.clear();
        arrayList = mydatabase.getRecipeInstruction(recipeId);
        if (arrayList != null && !arrayList.isEmpty()) {
            setAdapter();
        } else {
            getRecipeDetails();
        }
    }

    void setAdapter() {

        adapter = new InstructionAdapter(activity, arrayList, new InstructionOnClick() {
            @Override
            public void onCheckBoxClick(int position, recipeInstructionDetail model) {

                if(!isAllUnChecked() && position > 0) {
                    return;
                }

                globalclass.log(tag,"getLastCheckedPos: "+getLastCheckedPos());
                if (position == getLastCheckedPos() ||
                    position == getLastCheckedPos()-1) {
                    if (model.getChecked()) {
                        model.setChecked(false);
                    } else {
                        model.setChecked(true);
                    }

                    adapter.updateData(position, model);
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
    }

    int getLastCheckedPos() {
        for(int i=0;i<arrayList.size();i++) {
            recipeInstructionDetail recipeInstructionModel = arrayList.get(i);
            if(!recipeInstructionModel.getChecked()) {
                return i;
            }
        }

        if(arrayList.get(arrayList.size()-1).getChecked()) {
            return arrayList.size()-1;
        }

        return 0;
    }

    boolean isAllUnChecked() {
        for(int i=0;i<arrayList.size();i++) {
            recipeInstructionDetail recipeInstructionModel = arrayList.get(i);
            if(recipeInstructionModel.getChecked()) {
                return true;
            }
        }
        return false;
    }

    void getRecipeDetails() {

        String parameter = "";

        try {
            CollectionReference recipeColl = globalclass.firebaseInstance().collection(Globalclass.recipeColl);
            DocumentReference documentReference = recipeColl.document(recipeId);

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            documentReference.get().addOnCompleteListener(activity, new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    swipeRefresh.setRefreshing(false);

                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            mydatabase.deleteData(mydatabase.recipe, "recipeId", model.getRecipeId());

                            model = document.toObject(recipeDetail.class);
                            mydatabase.addRecipe(model);

                            //todo adding Recipe Instructions
                            ArrayList<String> instructionList = model.getRecipeInstructions();
                            if (instructionList != null && !instructionList.isEmpty()) {
                                mydatabase.deleteData(mydatabase.recipeInstruction, "recipeId", recipeId);

                                for (int i = 0; i < instructionList.size(); i++) {
                                    recipeInstructionDetail recipeInstructionModel = new recipeInstructionDetail();
                                    recipeInstructionModel.setRecipeId(Instructions.this.model.getRecipeId());
                                    recipeInstructionModel.setInstruction(instructionList.get(i));
                                    recipeInstructionModel.setStepNumber(i + 1);
                                    mydatabase.addRecipeInstructions(recipeInstructionModel);
                                }
                                getData();
                            }

                        } else {
                            String error = "Recipe detail not found!";
                            globalclass.log(tag, "getRecipeDetails: " + error);
                            globalclass.toast_long(error);
                            mydatabase.deleteData(mydatabase.recipe, "recipeId", model.getRecipeId());
                            globalclass.sendResponseErrorLog(tag,"getRecipeDetails",error, finalParameter);
                        }
                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getRecipeDetails: " + error);
                        globalclass.toast_long("Unable to get recipe details, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"getRecipeDetails",error, finalParameter);
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeDetails: " + error);
            globalclass.toast_long("Unable to get recipe details, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"getRecipeDetails",error, parameter);
        }
    }
}