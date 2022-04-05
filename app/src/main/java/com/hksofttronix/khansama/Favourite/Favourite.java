package com.hksofttronix.khansama.Favourite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.hksofttronix.khansama.Models.addressDetail;
import com.hksofttronix.khansama.Models.favouriteDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.RecipeDetail.RecipeDetail;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.util.ArrayList;

public class Favourite extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = Favourite.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    RelativeLayout nodatafoundlo;

    ArrayList<favouriteDetail> arrayList = new ArrayList<>();
    FavouriteAdapter adapter;

    int REMOVE_FAVOURITE = 111;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        setToolbar();
        init();
        binding();
        onClick();
        getData();
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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        toolbar.setTitle(R.string.your_favourites);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void binding() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        recyclerView = findViewById(R.id.recyclerView);
        nodatafoundlo = findViewById(R.id.nodatafoundlo);
    }

    void onClick() {

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                getFavouriteList();
            }
        });
    }

    void getData() {
        arrayList.clear();
        arrayList = mydatabase.getFavouriteList();
        if(!arrayList.isEmpty()) {
            setAdapter();
        }
        else {
            getFavouriteList();
        }
    }

    void getFavouriteList() {

        try {
            CollectionReference favouriteColl = globalclass.firebaseInstance().collection(Globalclass.favouriteColl);
            Query query = favouriteColl
                    .whereEqualTo("userId",globalclass.getStringData("id"));

            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);
                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.favourite);
                            for(DocumentSnapshot documents : task.getResult()) {
                                favouriteDetail model = documents.toObject(favouriteDetail.class);
                                mydatabase.addToFavourite(model);
                            }

                            getData();
                        }
                        else {
                            mydatabase.truncateTable(mydatabase.favourite);
                            recyclerView.setVisibility(View.GONE);
                            nodatafoundlo.setVisibility(View.VISIBLE);
                        }
                    }
                    else {

                        recyclerView.setVisibility(View.GONE);
                        nodatafoundlo.setVisibility(View.VISIBLE);

                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getFavouriteList: "+error);
                        globalclass.toast_long("Unable to refresh favourite list, please try again later!");
                        globalclass.sendErrorLog(tag,"getFavouriteList",error);
                    }
                }
            });
        }
        catch (Exception e) {

            recyclerView.setVisibility(View.GONE);
            nodatafoundlo.setVisibility(View.VISIBLE);

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getFavouriteList: "+error);
            globalclass.toast_long("Unable to refresh favourite list, please try again later!");
            globalclass.sendErrorLog(tag,"getFavouriteList",error);
        }
    }

    void setAdapter() {

        nodatafoundlo.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        adapter = new FavouriteAdapter(activity, arrayList, new FavouriteOnClick() {
            @Override
            public void onRemove(int position, favouriteDetail model) {

                if(!globalclass.isInternetPresent()) {
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
                                removeFromFavourite(position,model);
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

            @Override
            public void viewDetail(int position, favouriteDetail model) {
                startActivityForResult(new Intent(activity, RecipeDetail.class)
                                .putExtra("position",""+position)
                                .putExtra("recipeId",model.getRecipeId())
                        ,REMOVE_FAVOURITE);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
    }

    void removeFromFavourite(int position, favouriteDetail model) {

        String parameter = "";

        try {
            showprogress("Removing from your favourite list", "Please wait...");

            final DocumentReference favouriteDocRef = globalclass.firebaseInstance()
                    .collection(Globalclass.favouriteColl)
                    .document(model.getFavouriteId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            globalclass.firebaseInstance().runTransaction(new Transaction.Function<Integer>() {

                @Nullable
                @Override
                public Integer apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                    DocumentSnapshot favouriteDoc = transaction.get(favouriteDocRef);
                    if(favouriteDoc.exists()) {

                        favouriteDetail favouriteModel = favouriteDoc.toObject(favouriteDetail.class);
                        if(favouriteModel.getFavouriteId().equalsIgnoreCase(model.getFavouriteId())) {

                            transaction.delete(favouriteDocRef);
                            return 0;
                        }
                    }
                    else {

                        return 0;
                    }

                    return -1;
                }
            }).addOnSuccessListener(activity, new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {

                    hideprogress();
                    if(integer == 0) {

                        adapter.deleteData(position);
                        mydatabase.deleteData(mydatabase.favourite,"favouriteId",model.getFavouriteId());
                        globalclass.snackit(activity, "Removed successfully!");

                        if(arrayList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            nodatafoundlo.setVisibility(View.VISIBLE);
                        }
                    }
                    else if(integer == -1) {

                        String error = "return -1";
                        globalclass.log(tag,"removeFromFavourite onSuccess: "+error);
                        globalclass.toast_long("Unable to remove from favourite list, please try after sometime!");
                        globalclass.sendErrorLog(tag,"removeFromFavourite",error);
                        globalclass.sendResponseErrorLog(tag,"removeFromFavourite",error, finalParameter);

                    }
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag, "removeFromFavourite onFailure: " + error);
                    globalclass.toast_long("Unable to remove from favourite list, please try after sometime!");
                    globalclass.sendResponseErrorLog(tag,"removeFromFavourite",error, finalParameter);
                }
            });

        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "removeFromFavourite: " + error);
            globalclass.toast_long("Unable to remove from favourite list, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"removeFromFavourite",error, parameter);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REMOVE_FAVOURITE) {
            if(resultCode == RESULT_OK) {

                if(data != null) {
                    int position = Integer.parseInt(data.getStringExtra("position"));
                    adapter.deleteData(position);
                }
            }
        }
    }

    void showprogress(String title, String message) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    void hideprogress() {
        if (progressDialog !=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}