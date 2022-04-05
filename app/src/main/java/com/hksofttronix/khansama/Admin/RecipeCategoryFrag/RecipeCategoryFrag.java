package com.hksofttronix.khansama.Admin.RecipeCategoryFrag;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Admin.AddRecipeCategory.AddRecipeCategory;
import com.hksofttronix.khansama.Admin.AdminHome;
import com.hksofttronix.khansama.Admin.UpdateRecipeCategory.UpdateRecipeCategory;
import com.hksofttronix.khansama.Models.recipeCategoryDetail;
import com.hksofttronix.khansama.Models.recipeDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;

public class RecipeCategoryFrag extends Fragment {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity;

    Globalclass globalclass;
    Mydatabase mydatabase;

    Toolbar toolbar;
    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    ExtendedFloatingActionButton extendedFloatingActionButton;

    LinearLayoutManager linearLayoutManager;

    ArrayList<recipeCategoryDetail> arrayList = new ArrayList<>();
    RecipeCategoryAdapter adapter;

    int ADD_CATEGORY = 111;
    int UPDATE_CATEGORY = 222;


    ProgressDialog progressDialog;

    public RecipeCategoryFrag() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof AppCompatActivity) {
            activity = (AppCompatActivity) context;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            ((AdminHome) getActivity()).setToolbar(toolbar);
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"onActivityCreated: "+error);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipe_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(getResources().getString(R.string.recipe_category));

        init();
        binding(view);
        onClick();
        getData();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerView);
        extendedFloatingActionButton = view.findViewById(R.id.extendedFloatingActionButton);
    }

    void onClick() {

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }

                getCategoryList();
            }
        });

        extendedFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivityForResult(new Intent(activity, AddRecipeCategory.class),ADD_CATEGORY);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (dy > 0) {
                    // Scroll Down
                    extendedFloatingActionButton.hide();
                }
                else if (dy <0) {

                    // Scroll Up
                    extendedFloatingActionButton.show();
                }
            }
        });
    }

    void getData() {
        arrayList.clear();
        arrayList = mydatabase.getRecipeCategoryList();
        if(!arrayList.isEmpty()) {
            setAdapter();
        }
        else {
            getCategoryList();
        }
    }

    void getCategoryList() {
        try {
            swipeRefresh.setRefreshing(true);
            arrayList.clear();
            CollectionReference recipeCategoryColl = globalclass.firebaseInstance().collection(Globalclass.recipeCategoryColl);
            recipeCategoryColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {

                            mydatabase.truncateTable(mydatabase.recipeCategory);
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {

                                recipeCategoryDetail model = documentSnapshot.toObject(recipeCategoryDetail.class);
                                mydatabase.addRecipeCategory(model);
                            }

                            getData();
                        }
                        else {
                            globalclass.snackit(activity,"No category found!");
                        }
                    }
                    else {
                        String error = task.getException().toString();
                        globalclass.log(tag, "getCategoryList: " + error);
                        globalclass.sendErrorLog(tag,"getCategoryList: ",error);
                        globalclass.snackit(activity, "Unable to get category list, please try again after sometime!");
                    }
                }
            });

        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getCategoryList: " + error);
            globalclass.sendErrorLog(tag,"getCategoryList: ",error);
            globalclass.snackit(activity, "Unable to get category list, please try again after sometime!");
        }
    }

    void setAdapter() {
        adapter = new RecipeCategoryAdapter(activity, arrayList, new adminRecipeOnClick() {
            @Override
            public void onClick(int position, recipeCategoryDetail model) {

                Intent intent = new Intent(activity, UpdateRecipeCategory.class);
                intent.putExtra("position", String.valueOf(position));
                intent.putExtra("recipeCategoryDetail", model);
                startActivityForResult(intent, UPDATE_CATEGORY);
            }

            @Override
            public void onDelete(int position, recipeCategoryDetail model) {

                if(!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }

                new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                        .setTitle("Sure")
                        .setMessage("Are you sure you want to remove?")
                        .setCancelable(false)
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                try {
                                    checkCategoryExist(position,model);
                                }
                                catch (Exception e) {

//                                    hideprogress();
                                    String error = Log.getStackTraceString(e);
                                    globalclass.log(tag, "deleteInventory: " + error);
                                    globalclass.sendErrorLog(tag,"deleteInventory: ",error);
                                    globalclass.toast_long("Unable to remove category, please try after sometime!");
                                }
                            }
                        })
                        .setNeutralButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    void checkCategoryExist(int position, recipeCategoryDetail model) {

        try {
            showprogress("Removing category","Please wait...");

            CollectionReference recipeColl = globalclass.firebaseInstance().collection(Globalclass.recipeColl);
            Query checkInventoryExist = recipeColl.whereEqualTo("categoryId",model.getCategoryId()).limit(1);
            checkInventoryExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    String source = task.getResult().getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag,"checkCategoryExist - Data fetched from "+source);

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                recipeDetail model = documents.toObject(recipeDetail.class);
                                globalclass.log(tag,"Recipe name: "+model.getRecipeName());
                            }

                            hideprogress();
                            globalclass.showDialogue(activity,
                                    "Alert",
                                    "Recipe is still available under "+model.getCategoryName()+" category ,please remove all it's recipe first!");
                        }
                        else {
                            removeCategory(position,model);
                        }
                    }
                    else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"checkCategoryExist: "+error);
                        globalclass.sendErrorLog(tag,"checkCategoryExist: ",error);
                        globalclass.toast_long("Unable to remove category, please try after sometime!");
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkCategoryExist: "+error);
            globalclass.sendErrorLog(tag,"checkCategoryExist: ",error);
            globalclass.toast_long("Unable to remove category, please try after sometime!");
        }
    }

    void removeCategory(int position, recipeCategoryDetail model) {

        String parameter = "";

        try {
            CollectionReference recipeCategoryColl = globalclass.firebaseInstance().collection(Globalclass.recipeCategoryColl);
            DocumentReference documentReference = recipeCategoryColl.document(model.getCategoryId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            documentReference.delete().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    hideprogress();
                    if(task.isSuccessful()) {

                        mydatabase.deleteData(mydatabase.recipeCategory,"categoryId",model.getCategoryId());
                        adapter.deleteData(position);
                        globalclass.snackitForFab(extendedFloatingActionButton,"Removed successfully!");
                    }
                    else {
                        String error = task.getException().toString();
                        globalclass.log(tag,"removeCategory: "+error);
                        globalclass.sendResponseErrorLog(tag,"removeCategory: ",error, finalParameter);
                        globalclass.toast_long("Unable to remove category, please try after sometime!");
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"removeCategory: "+error);
            globalclass.sendResponseErrorLog(tag,"removeCategory: ",error, parameter);
            globalclass.toast_long("Unable to remove category, please try after sometime!");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CATEGORY) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                getData();
            }
        }
        else if (requestCode == UPDATE_CATEGORY) {
            if (resultCode == Activity.RESULT_OK && data != null) {

                int position = Integer.parseInt(data.getStringExtra("position"));
                recipeCategoryDetail model = data.getParcelableExtra("recipeCategoryDetail");
                adapter.updateData(position,model);
            }
        }
    }

    void showprogress(String title,String message) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    void hideprogress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}