package com.hksofttronix.khansama.Admin.RecipesFrag;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hksofttronix.khansama.Admin.AddRecipe.AddRecipe;
import com.hksofttronix.khansama.Admin.AdminHome;
import com.hksofttronix.khansama.Admin.UpdateRecipe.UpdateRecipe;
import com.hksofttronix.khansama.Models.ingredientsDetail;
import com.hksofttronix.khansama.Models.recipeDetail;
import com.hksofttronix.khansama.Models.recipeInstructionDetail;
import com.hksofttronix.khansama.Models.recipePhotoDetail;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;

import java.util.ArrayList;
import java.util.Map;

public class RecipesFrag extends Fragment {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity;

    Globalclass globalclass;
    Mydatabase mydatabase;
    ListenerRegistration recipeListListener;
    ListenerRegistration recipeImageListener;
    ListenerRegistration recipeIngredientsListener;

    Toolbar toolbar;
    SwipeRefreshLayout swipeRefresh;
    RecyclerView recyclerView;
    ExtendedFloatingActionButton extendedFloatingActionButton;

    ArrayList<recipeDetail> arrayList = new ArrayList<>();
    adminRecipeAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    ProgressDialog progressDialog;

    public RecipesFrag() {
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
            globalclass.log(tag, "onActivityCreated: " + error);
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
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getResources().getString(R.string.recipe));

        init();
        binding(view);
        onClick();
//        getData();
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

        swipeRefresh.setRefreshing(false);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }

                getDataOnline();
            }
        });

        extendedFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(activity, AddRecipe.class));
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
                } else if (dy < 0) {

                    // Scroll Up
                    extendedFloatingActionButton.show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        getLiveData();
    }

    void getLiveData() {
        getAllIngredientsList();
        getAllRecipeImageList();
        getAllRecipeList();
    }

    void getAllIngredientsList() {

        try {
            CollectionReference ingredientsColl = globalclass.firebaseInstance().collection(Globalclass.ingredientsColl);
            recipeIngredientsListener = ingredientsColl.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException firebaseFirestoreException) {

                    if (firebaseFirestoreException != null) {
                        String error = Log.getStackTraceString(firebaseFirestoreException);
                        globalclass.log(tag, "getAllIngredientsList: " + error);
                        globalclass.sendErrorLog(tag,"getAllIngredientsList: ",error);
                        globalclass.toast_long("Unable to get recipes ingredients, please try again after sometime!");                        return;
                    }

                    if (querySnapshot == null || querySnapshot.isEmpty()) {

                        mydatabase.truncateTable(mydatabase.allIngredients);
                        globalclass.snackitForFab(extendedFloatingActionButton, "No recipe ingredients found!");
                        return;
                    }

                    String source = querySnapshot.getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag, "getAllIngredientsList - Data fetched from " + source);

                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        DocumentSnapshot documentSnapshot = documentChange.getDocument();

                        int oldIndex = documentChange.getOldIndex();
                        int newIndex = documentChange.getNewIndex();
//                        globalclass.log(tag, "oldIndex: " + oldIndex);
//                        globalclass.log(tag, "newIndex: " + newIndex);

                        if (documentChange.getType() == DocumentChange.Type.ADDED) {

                            ingredientsDetail model = documentSnapshot.toObject(ingredientsDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Ingredients ADDED from server: "+model.getName());
                            }

                            mydatabase.addAllIngredients(model);

                        } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                            ingredientsDetail model = documentSnapshot.toObject(ingredientsDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Ingredients MODIFIED from server: "+model.getName());
                            }

                            mydatabase.addAllIngredients(model);

                        } else if (documentChange.getType() == DocumentChange.Type.REMOVED) {

                            ingredientsDetail model = documentSnapshot.toObject(ingredientsDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Ingredients REMOVED from server: "+model.getName());
                            }

                            mydatabase.deleteData(mydatabase.allIngredients, "ingredientId", model.getIngredientId());
                        }
                    }
                }
            });
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllIngredientsList: " + error);
            globalclass.sendErrorLog(tag,"getAllIngredientsList: ",error);
            globalclass.toast_long("Unable to get recipes ingredients, please try again after sometime!");
        }
    }

    void getAllRecipeImageList() {

        try {
            CollectionReference recipeImagesColl = globalclass.firebaseInstance().collection(Globalclass.recipeImagesColl);
            recipeImageListener = recipeImagesColl.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException firebaseFirestoreException) {

                    if (firebaseFirestoreException != null) {
                        String error = Log.getStackTraceString(firebaseFirestoreException);
                        globalclass.log(tag, "getAllRecipeImageList: " + error);
                        globalclass.sendErrorLog(tag,"getAllRecipeImageList: ",error);
                        globalclass.toast_long("Unable to get recipes images, please try again after sometime!");
                        return;
                    }

                    if (querySnapshot == null || querySnapshot.isEmpty()) {

                        mydatabase.truncateTable(mydatabase.allRecipeImages);
                        globalclass.snackitForFab(extendedFloatingActionButton, "No recipe images found!");
                        return;
                    }

                    String source = querySnapshot.getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag, "getAllRecipeImageList - Data fetched from " + source);

                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        DocumentSnapshot documentSnapshot = documentChange.getDocument();

                        int oldIndex = documentChange.getOldIndex();
                        int newIndex = documentChange.getNewIndex();
//                        globalclass.log(tag, "oldIndex: " + oldIndex);
//                        globalclass.log(tag, "newIndex: " + newIndex);

                        if (documentChange.getType() == DocumentChange.Type.ADDED) {

                            recipePhotoDetail model = documentSnapshot.toObject(recipePhotoDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Recipe Image ADDED from server: "+model.getRecipeImageId());
                            }

                            mydatabase.addAllRecipeImages(model);

                        } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                            recipePhotoDetail model = documentSnapshot.toObject(recipePhotoDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Recipe Image MODIFIED from server: "+model.getRecipeImageId());
                            }

                            mydatabase.addAllRecipeImages(model);

                        } else if (documentChange.getType() == DocumentChange.Type.REMOVED) {

                            recipePhotoDetail model = documentSnapshot.toObject(recipePhotoDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Recipe Image REMOVED from server: "+model.getRecipeImageId());
                            }

                            mydatabase.deleteData(mydatabase.allRecipeImages, "recipeImageId", model.getRecipeImageId());
                        }
                    }
                }
            });
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllRecipeImageList: " + error);
            globalclass.sendErrorLog(tag,"getAllRecipeImageList: ",error);
            globalclass.toast_long("Unable to get recipes images, please try again after sometime!");
        }
    }

    void getAllRecipeList() {

        try {
            setAdapter();
            arrayList.clear();

            CollectionReference recipeColl = globalclass.firebaseInstance().collection(Globalclass.recipeColl);
            recipeListListener = recipeColl.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException firebaseFirestoreException) {

                    if (firebaseFirestoreException != null) {
                        String error = Log.getStackTraceString(firebaseFirestoreException);
                        globalclass.log(tag, "getAllRecipeList: " + error);
                        globalclass.sendErrorLog(tag,"getAllRecipeList: ",error);
                        globalclass.toast_long("Unable to get recipes, please try again after sometime!");
                        return;
                    }

                    if (querySnapshot == null || querySnapshot.isEmpty()) {

                        mydatabase.truncateTable(mydatabase.allRecipe);
                        globalclass.snackitForFab(extendedFloatingActionButton, "No recipe found!");
                        return;
                    }

                    String source = querySnapshot.getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag, "getAllRecipeList - Data fetched from " + source);

                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        DocumentSnapshot documentSnapshot = documentChange.getDocument();

                        int oldIndex = documentChange.getOldIndex();
                        int newIndex = documentChange.getNewIndex();

                        if (documentChange.getType() == DocumentChange.Type.ADDED) {

                            recipeDetail model = documentSnapshot.toObject(recipeDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Recipe ADDED from server: "+model.getRecipeName().toUpperCase());
                            }

                            adapter.addData(newIndex, model);
                            mydatabase.addAllRecipe(model);

                            ArrayList<String> instructionList = model.getRecipeInstructions();
                            if (instructionList != null && !instructionList.isEmpty()) {

                                mydatabase.deleteData(mydatabase.allRecipeInstructions, "recipeId", model.getRecipeId());

                                for (int i = 0; i < instructionList.size(); i++) {
                                    recipeInstructionDetail recipeInstructionModel = new recipeInstructionDetail();
                                    recipeInstructionModel.setRecipeId(model.getRecipeId());
                                    recipeInstructionModel.setInstruction(instructionList.get(i));
                                    recipeInstructionModel.setStepNumber(i + 1);
                                    mydatabase.addAllRecipeInstructions(recipeInstructionModel);
                                }
                            } else {
                                mydatabase.deleteData(mydatabase.allRecipeInstructions, "recipeId", model.getRecipeId());
                            }

                        } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                            recipeDetail model = documentSnapshot.toObject(recipeDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Recipe MODIFIED from server: "+model.getRecipeName().toUpperCase());
                            }

                            adapter.updateData(newIndex, model);
                            mydatabase.addAllRecipe(model);

                            ArrayList<String> instructionList = model.getRecipeInstructions();
                            if (instructionList != null && !instructionList.isEmpty()) {

                                mydatabase.deleteData(mydatabase.allRecipeInstructions, "recipeId", model.getRecipeId());

                                for (int i = 0; i < instructionList.size(); i++) {
                                    recipeInstructionDetail recipeInstructionModel = new recipeInstructionDetail();
                                    recipeInstructionModel.setRecipeId(model.getRecipeId());
                                    recipeInstructionModel.setInstruction(instructionList.get(i));
                                    recipeInstructionModel.setStepNumber(i + 1);
                                    mydatabase.addAllRecipeInstructions(recipeInstructionModel);
                                }
                            } else {
                                mydatabase.deleteData(mydatabase.allRecipeInstructions, "recipeId", model.getRecipeId());
                            }
                        }
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllRecipeList: " + error);
            globalclass.sendErrorLog(tag,"getAllRecipeList: ",error);
            globalclass.toast_long("Unable to get recipes, please try again after sometime!");
        }
    }

    //todo Old Way
    void getData() {
        arrayList.clear();
        arrayList = mydatabase.getAllRecipeList();
        if (!arrayList.isEmpty()) {
            setAdapter();
        } else {
            getDataOnline();
        }
    }

    void getDataOnline() {
        getAllRecipeListOld();
        getAllIngredientsListOld();
        getAllRecipeImageListOld();
    }

    void getAllRecipeListOld() {

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference recipeColl = globalclass.firebaseInstance().collection(Globalclass.recipeColl);
            recipeColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {

                            arrayList.clear();
                            mydatabase.truncateTable(mydatabase.allRecipe);
                            mydatabase.truncateTable(mydatabase.allRecipeInstructions);

                            for (DocumentSnapshot documents : task.getResult()) {
                                recipeDetail model = documents.toObject(recipeDetail.class);
                                mydatabase.addAllRecipe(model);

                                ArrayList<String> instructionList = model.getRecipeInstructions();
                                for (int i = 0; i < instructionList.size(); i++) {
                                    recipeInstructionDetail recipeInstructionModel = new recipeInstructionDetail();
                                    recipeInstructionModel.setRecipeId(model.getRecipeId());
                                    recipeInstructionModel.setInstruction(instructionList.get(i));
                                    recipeInstructionModel.setStepNumber(i + 1);
                                    mydatabase.addAllRecipeInstructions(recipeInstructionModel);
                                }
                            }

                            getData();
                        } else {
                            mydatabase.truncateTable(mydatabase.allRecipe);
                            globalclass.snackitForFab(extendedFloatingActionButton, "No recipe found!");
                        }
                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getAllRecipeList: " + error);
                        globalclass.toast_long("Unable to get recipes, please try again after sometime!");
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllRecipeList: " + error);
            globalclass.toast_long("Unable to get recipes, please try again after sometime!");
        }
    }

    void getAllIngredientsListOld() {

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference ingredientsColl = globalclass.firebaseInstance().collection(Globalclass.ingredientsColl);
            ingredientsColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    swipeRefresh.setRefreshing(false);

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {

                            mydatabase.truncateTable(mydatabase.allIngredients);
                            for (DocumentSnapshot documents : task.getResult()) {
                                ingredientsDetail model = documents.toObject(ingredientsDetail.class);
                                mydatabase.addAllIngredients(model);
                            }

                            globalclass.log(tag, "All Ingredients data added!");
                        } else {
                            mydatabase.truncateTable(mydatabase.allIngredients);
                            globalclass.log(tag, "All Ingredients is empty!");
                        }
                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getAllIngredientsList: " + error);
                        globalclass.toast_long("Unable to get recipes, please try again after sometime!");
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllIngredientsList: " + error);
            globalclass.toast_long("Unable to get recipes, please try again after sometime!");
        }
    }

    void getAllRecipeImageListOld() {

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference recipeImagesColl = globalclass.firebaseInstance().collection(Globalclass.recipeImagesColl);
            recipeImagesColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {

                            mydatabase.truncateTable(mydatabase.allRecipeImages);
                            for (DocumentSnapshot documents : task.getResult()) {
                                recipePhotoDetail model = documents.toObject(recipePhotoDetail.class);
                                mydatabase.addAllRecipeImages(model);
                            }

                            globalclass.log(tag, "All Recipe Image data added!");
                        } else {
                            mydatabase.truncateTable(mydatabase.allRecipeImages);
                            globalclass.log(tag, "All Recipe Image is empty!");
                        }
                    } else {

                        swipeRefresh.setRefreshing(false);
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getAllRecipeImageList: " + error);
                        globalclass.toast_long("Unable to get recipes, please try again after sometime!");
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllRecipeImageList: " + error);
            globalclass.toast_long("Unable to get recipes, please try again after sometime!");
        }
    }

    void setAdapter() {
        adapter = new adminRecipeAdapter(activity, arrayList, new adminRecipeOnClick() {
            @Override
            public void onCheckStatus(int position, recipeDetail model, adminRecipeAdapter.RecyclerViewHolders holder) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_short(getString(R.string.noInternetConnection));
                    return;
                }

                if (model.getStatus()) {
                    model.setStatus(false);
                } else {
                    model.setStatus(true);
                }

                changeRecipeStatus(position, model, holder);
            }

            @Override
            public void viewDetail(int position, recipeDetail model) {

                Intent intent = new Intent(activity, UpdateRecipe.class);
                intent.putExtra("recipeId", model.getRecipeId());
                startActivity(intent);
            }
        });

        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    void changeRecipeStatus(int position, recipeDetail model, adminRecipeAdapter.RecyclerViewHolders holder) {

        String parameter = "";

        try {
            showprogress("Changing recipe status", "Please wait...");

            CollectionReference recipeColl = globalclass.firebaseInstance().collection(Globalclass.recipeColl);
            String recipeId = model.getRecipeId();

            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(model);
            Map<String, Object> map = new Gson().fromJson(json, Map.class);
            map.put("status", model.getStatus());

            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            DocumentReference inventoryReference = recipeColl.document(recipeId);
            inventoryReference.update(map)
                    .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            hideprogress();
                            if (task.isSuccessful()) {
                                mydatabase.addAllRecipe(model);
                                adapter.updateData(position, model);
                                holder.status.setChecked(model.getStatus());
                                globalclass.snackitForFab(extendedFloatingActionButton, "Recipe status changed successfully!");
                            } else {
                                String error = Log.getStackTraceString(task.getException());
                                globalclass.log(tag, "changeRecipeStatus: " + error);
                                globalclass.sendResponseErrorLog(tag,"changeRecipeStatus: ",error, finalParameter);
                                globalclass.toast_long("Unable to change recipe status, please try after sometime!");
                            }
                        }
                    });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "changeRecipeStatus: " + error);
            globalclass.sendResponseErrorLog(tag,"changeRecipeStatus: ",error,parameter);
            globalclass.toast_long("Unable to change recipe status, please try after sometime!");
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
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public BroadcastReceiver AddRecipeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                globalclass.log(tag, "AddRecipeReceiver: onReceive");
//                getData();
            } catch (Exception e) {
                String error = Log.getStackTraceString(e);
                globalclass.log(tag, "AddRecipeReceiver: " + error);
                globalclass.sendErrorLog(tag,"AddRecipeReceiver: ",error);
                globalclass.toast_long("Error in refreshing data, please swipe down to refresh data!");
            }
        }
    };

    void removeListener() {

        if (recipeListListener != null) {
            recipeListListener.remove();
        }

        if (recipeImageListener != null) {
            recipeImageListener.remove();
        }

        if (recipeIngredientsListener != null) {
            recipeIngredientsListener.remove();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                AddRecipeReceiver, new IntentFilter(Globalclass.AddRecipeReceiver));
    }

    @Override
    public void onPause() {
        super.onPause();

        removeListener();
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(AddRecipeReceiver);
    }
}