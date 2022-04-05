package com.hksofttronix.khansama.HomeFrag;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.hksofttronix.khansama.Models.ingredientsDetail;
import com.hksofttronix.khansama.Models.recipeCategoryDetail;
import com.hksofttronix.khansama.Models.recipeDetail;
import com.hksofttronix.khansama.Models.recipeInstructionDetail;
import com.hksofttronix.khansama.Models.recipePhotoDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.RecipeDetail.RecipeDetail;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.SearchRecipe.SearchRecipe;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFrag extends Fragment {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity;

    Globalclass globalclass;
    Mydatabase mydatabase;
    ListenerRegistration categoryListListener;
    ListenerRegistration recipeListListener;
    ListenerRegistration recipeImageListener;
    ListenerRegistration recipeIngredientsListener;

    SwitchCompat vegOnlySwitchCompat;
    ImageView ivSearchRecipe;
    LinearLayout helloUserLayout;
    SwipeRefreshLayout swipeRefresh;
    TextView helloUser, categoryName;
    RecyclerView categoryRecyclerView, recipeRecyclerView;

    ArrayList<recipeCategoryDetail> categoryList = new ArrayList<>();
    categoryAdapter categoryAdapter;

    ArrayList<recipeDetail> recipeList = new ArrayList<>();
    RecipeAdapter recipeAdapter;
    LinearLayoutManager linearLayoutManager;

    ArrayList<String> recipeTypeList = new ArrayList<>();
    int selectedRecipeTypePos = -1;

    String selectedCategoryId = "";
    String selectedRecipeType = "";
    String query = "";

    public HomeFrag() {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
        binding(view);
        setText();
        onClick();

//        getCategoryData();
//        getRecipeData();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding(View view) {
        vegOnlySwitchCompat = view.findViewById(R.id.vegOnlySwitchCompat);
        ivSearchRecipe = view.findViewById(R.id.ivSearchRecipe);
        helloUserLayout = view.findViewById(R.id.helloUserLayout);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        helloUser = view.findViewById(R.id.helloUser);
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        categoryName = view.findViewById(R.id.categoryName);
        recipeRecyclerView = view.findViewById(R.id.recipeRecyclerView);
    }

    void setText() {
        if (globalclass.checkUserIsLoggedIn()) {

            if(globalclass.checknull(globalclass.getStringData("name")).equalsIgnoreCase("")) {
                helloUser.setText("Hello...!");
            }
            else {
                helloUser.setText("Hello, " + globalclass.firstLetterCapital(globalclass.getStringData("name")) + "!");
            }
        } else {
            helloUser.setText("Hello...!");
        }

        if (globalclass.checknull(globalclass.getStringData("recipeInterested")).equalsIgnoreCase("")) {
            globalclass.setStringData("recipeInterested", "all");
        }

        selectedRecipeType = globalclass.getStringData("recipeInterested");
        if (globalclass.checknull(globalclass.getStringData("recipeInterested")).equalsIgnoreCase("all")) {
            vegOnlySwitchCompat.setChecked(false);
            vegOnlySwitchCompat.setTextColor(getResources().getColor(R.color.black));
        } else {
            vegOnlySwitchCompat.setChecked(true);
            vegOnlySwitchCompat.setTextColor(getResources().getColor(R.color.green));
        }
    }

    void onClick() {

        recipeRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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


                } else if (dy < 0) {

                    // Scroll Up
                }
            }
        });

        vegOnlySwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean result) {
                if (result) {
                    globalclass.setStringData("recipeInterested", "veg");
                    vegOnlySwitchCompat.setTextColor(getResources().getColor(R.color.green));
                } else {
                    globalclass.setStringData("recipeInterested", "all");
                    vegOnlySwitchCompat.setTextColor(getResources().getColor(R.color.black));
                }

                selectedRecipeType = globalclass.getStringData("recipeInterested");
                doFilter();
            }
        });

        ivSearchRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, SearchRecipe.class));
            }
        });

        swipeRefresh.setRefreshing(false);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_long(activity.getString(R.string.noInternetConnection));
                    return;
                }

                getRecipeCategoryListOld();
                getAllRecipeListOld();
                getAllIngredientsListOld();
                getAllRecipeImageListOld();
                globalclass.setLastSyncDataTime();

                new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        setCategoryAdapterOld();
                    }
                }, 2000);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        getLiveData();
    }

    void getLiveData() {

        getRecipeCategoryList();
        getAllRecipeList();
        getAllRecipeImageList();
        getAllIngredientsList();
    }

    void getRecipeCategoryList() {

        try {
            setCategoryAdapter();

            categoryList.clear();

            CollectionReference recipeCategoryColl = globalclass.firebaseInstance().collection(Globalclass.recipeCategoryColl);
            categoryListListener = recipeCategoryColl.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException firebaseFirestoreException) {

                    if (firebaseFirestoreException != null) {
                        String error = Log.getStackTraceString(firebaseFirestoreException);
                        globalclass.log(tag, "getRecipeCategoryList: " + error);
                        globalclass.toast_long("Unable to get Category date, please try after sometime!");
                        globalclass.sendErrorLog(tag,"getRecipeCategoryList",error);
                        return;
                    }

                    if (querySnapshot == null || querySnapshot.isEmpty()) {

                        mydatabase.truncateTable(mydatabase.recipeCategory);
                        globalclass.snackit(activity, "No Recipe Category exist!");
                        return;
                    }

                    String source = querySnapshot.getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag, "getRecipeCategoryList - Data fetched from " + source);

                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        DocumentSnapshot documentSnapshot = documentChange.getDocument();

                        int oldIndex = documentChange.getOldIndex();
                        int newIndex = documentChange.getNewIndex();
//                        int newIndex = documentChange.getNewIndex()-1; //todo for getting error & to check log detail

                        if (documentChange.getType() == DocumentChange.Type.ADDED) {

                            recipeCategoryDetail model = documentSnapshot.toObject(recipeCategoryDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Category ADDED from server: "+model.getCategoryName().toUpperCase());
                            }

                            categoryAdapter.addData(newIndex, model);
                            mydatabase.addRecipeCategory(model);

                        } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                            recipeCategoryDetail model = documentSnapshot.toObject(recipeCategoryDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Category MODIFIED from server: "+model.getCategoryName().toUpperCase());
                            }

                            categoryAdapter.addData(newIndex, model);
                            mydatabase.addRecipeCategory(model);
                        }
                        else if (documentChange.getType() == DocumentChange.Type.REMOVED) {

                            recipeCategoryDetail model = documentSnapshot.toObject(recipeCategoryDetail.class);                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Category REMOVED from server: "+model.getCategoryName().toUpperCase());
                            }

                            mydatabase.deleteData(mydatabase.recipeCategory, "categoryId", model.getCategoryId());
                            categoryAdapter.deleteData(oldIndex);
                        }
                    }

                    setCategoryAdapter();
                }
            });
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeCategoryList: " + error);
            globalclass.toast_long("Unable to get Category date, please tr after sometime!");
            globalclass.sendErrorLog(tag,"getRecipeCategoryList",error);
        }
    }

    void getAllRecipeList() {

        try {
            setRecipeAdapter();
            recipeList.clear();

            CollectionReference recipeColl = globalclass.firebaseInstance().collection(Globalclass.recipeColl);
            recipeListListener = recipeColl.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException firebaseFirestoreException) {

                    if (firebaseFirestoreException != null) {
                        String error = Log.getStackTraceString(firebaseFirestoreException);
                        globalclass.log(tag, "getAllRecipeList: " + error);
                        globalclass.toast_long("Unable to get recipes, please try again after sometime!");
                        globalclass.sendErrorLog(tag,"getAllRecipeList",error);
                        return;
                    }

                    if (querySnapshot == null || querySnapshot.isEmpty()) {

                        mydatabase.truncateTable(mydatabase.recipe);
                        globalclass.log(tag, "All Recipe is empty!");
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

                            mydatabase.addRecipe(model);

                            ArrayList<String> instructionList = model.getRecipeInstructions();
                            if (instructionList != null && !instructionList.isEmpty()) {

                                mydatabase.deleteData(mydatabase.recipeInstruction, "recipeId", model.getRecipeId());

                                for (int i = 0; i < instructionList.size(); i++) {
                                    recipeInstructionDetail recipeInstructionModel = new recipeInstructionDetail();
                                    recipeInstructionModel.setRecipeId(model.getRecipeId());
                                    recipeInstructionModel.setInstruction(instructionList.get(i));
                                    recipeInstructionModel.setStepNumber(i + 1);
                                    mydatabase.addRecipeInstructions(recipeInstructionModel);
                                }
                            } else {
                                mydatabase.deleteData(mydatabase.recipeInstruction, "recipeId", model.getRecipeId());
                            }

                        } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                            recipeDetail model = documentSnapshot.toObject(recipeDetail.class);
                            if(!source.equalsIgnoreCase("local cache")) {
                                globalclass.log(tag,"Recipe MODIFIED from server: "+model.getRecipeName().toUpperCase());
                            }

                            mydatabase.addRecipe(model);

                            ArrayList<String> instructionList = model.getRecipeInstructions();
                            if (instructionList != null && !instructionList.isEmpty()) {

                                mydatabase.deleteData(mydatabase.recipeInstruction, "recipeId", model.getRecipeId());

                                for (int i = 0; i < instructionList.size(); i++) {
                                    recipeInstructionDetail recipeInstructionModel = new recipeInstructionDetail();
                                    recipeInstructionModel.setRecipeId(model.getRecipeId());
                                    recipeInstructionModel.setInstruction(instructionList.get(i));
                                    recipeInstructionModel.setStepNumber(i + 1);
                                    mydatabase.addRecipeInstructions(recipeInstructionModel);
                                }
                            } else {
                                mydatabase.deleteData(mydatabase.recipeInstruction, "recipeId", model.getRecipeId());
                            }
                        }
                    }

                    doFilter();
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllRecipeList: " + error);
            globalclass.toast_long("Unable to get recipes, please try again after sometime!");
            globalclass.sendErrorLog(tag,"getAllRecipeList",error);
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
                        globalclass.toast_long("Unable to get recipes images, please try again after sometime!");
                        globalclass.sendErrorLog(tag,"getAllRecipeImageList",error);
                        return;
                    }

                    if (querySnapshot == null || querySnapshot.isEmpty()) {

                        globalclass.log(tag, "All Recipe Image is empty!");
                        mydatabase.truncateTable(mydatabase.recipeImages);
                        return;
                    }

                    String source = querySnapshot.getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag, "getAllRecipeImageList - Data fetched from " + source);

                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        DocumentSnapshot documentSnapshot = documentChange.getDocument();

                        int oldIndex = documentChange.getOldIndex();
                        int newIndex = documentChange.getNewIndex();

                        if (documentChange.getType() == DocumentChange.Type.ADDED) {

                            recipePhotoDetail model = documentSnapshot.toObject(recipePhotoDetail.class);
                            mydatabase.addRecipeImages(model);

                        } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                            recipePhotoDetail model = documentSnapshot.toObject(recipePhotoDetail.class);
                            mydatabase.addRecipeImages(model);

                        } else if (documentChange.getType() == DocumentChange.Type.REMOVED) {

                            recipePhotoDetail model = documentSnapshot.toObject(recipePhotoDetail.class);
                            mydatabase.deleteData(mydatabase.recipeImages, "recipeImageId", model.getRecipeImageId());
                        }
                    }
                }
            });
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllRecipeImageList: " + error);
            globalclass.toast_long("Unable to get recipes images, please try again after sometime!");
            globalclass.sendErrorLog(tag,"getAllRecipeImageList",error);
        }
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
                        globalclass.toast_long("Unable to get recipes ingredients, please try again after sometime!");
                        globalclass.sendErrorLog(tag,"getAllIngredientsList",error);
                        return;
                    }

                    if (querySnapshot == null || querySnapshot.isEmpty()) {

                        globalclass.log(tag, "All Ingredients is empty!");
                        mydatabase.truncateTable(mydatabase.recipeIngredients);
                        return;
                    }

                    String source = querySnapshot.getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag, "getAllIngredientsList - Data fetched from " + source);

                    for (DocumentChange documentChange : querySnapshot.getDocumentChanges()) {
                        DocumentSnapshot documentSnapshot = documentChange.getDocument();

                        int oldIndex = documentChange.getOldIndex();
                        int newIndex = documentChange.getNewIndex();

                        if (documentChange.getType() == DocumentChange.Type.ADDED) {

                            ingredientsDetail model = documentSnapshot.toObject(ingredientsDetail.class);
                            mydatabase.addIngredients(model);

                        } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                            ingredientsDetail model = documentSnapshot.toObject(ingredientsDetail.class);
                            mydatabase.addIngredients(model);

                        } else if (documentChange.getType() == DocumentChange.Type.REMOVED) {

                            ingredientsDetail model = documentSnapshot.toObject(ingredientsDetail.class);
                            mydatabase.deleteData(mydatabase.recipeIngredients, "ingredientId", model.getIngredientId());
                        }
                    }
                }
            });
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllIngredientsList: " + error);
            globalclass.toast_long("Unable to get recipes ingredients, please try again after sometime!");
            globalclass.sendErrorLog(tag,"getAllIngredientsList",error);
        }
    }

    void setCategoryAdapter() {
        categoryAdapter = new categoryAdapter(activity, categoryList, new categoryOnClick() {
            @Override
            public void onClick(int position, recipeCategoryDetail model) {

                selectedCategoryId = model.getCategoryId();
                categoryName.setText(globalclass.firstLetterCapital(model.getCategoryName()));
                doFilter();
            }
        });

        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        categoryRecyclerView.setAdapter(categoryAdapter);

        if (categoryList != null && !categoryList.isEmpty()) {
            selectedCategoryId = categoryList.get(0).getCategoryId();
            categoryName.setText(globalclass.firstLetterCapital(categoryList.get(0).getCategoryName()));
        } else {
//            String error = "categoryList is null";
//            globalclass.log(tag, error);
//            globalclass.toast_long("Unable to get recipe list, please try after sometime!");
        }
    }

    //todo Old Way
    void getCategoryData() {
        categoryList.clear();

        categoryList = mydatabase.getRecipeCategoryList();
        if (categoryList != null && !categoryList.isEmpty()) {
            setCategoryAdapterOld();
        } else {
            getRecipeCategoryListOld();
        }
    }

    void getRecipeCategoryListOld() {

        try {
            CollectionReference recipeCategoryColl = globalclass.firebaseInstance().collection(Globalclass.recipeCategoryColl);
            recipeCategoryColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.recipeCategory);
                            for (DocumentSnapshot documents : task.getResult()) {
                                recipeCategoryDetail model = documents.toObject(recipeCategoryDetail.class);
                                globalclass.log(tag, "Category name: " + model.getCategoryName());
                                mydatabase.addRecipeCategory(model);
                            }

                            getCategoryData();
                        } else {
                            mydatabase.truncateTable(mydatabase.recipeCategory);
                            globalclass.snackit(activity, "No Category exist!");
                        }
                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getRecipeCategoryList: " + error);
                        globalclass.toast_long("Unable to get Category date, please try after sometime!");
                        globalclass.sendErrorLog(tag,"getRecipeCategoryList",error);
                    }
                }
            });
        } catch (Exception e) {

            globalclass.toast_long("Unable to get Category date, please tr after sometime!");
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeCategoryList_" + error);
            globalclass.toast_long("Unable to get Category date, please try after sometime!");
            globalclass.sendErrorLog(tag,"getRecipeCategoryList",error);
        }
    }

    void getRecipeData() {
        if (mydatabase.checkRecipeExist() == 0) {
            getAllRecipeListOld();
            getAllIngredientsListOld();
            getAllRecipeImageListOld();
            globalclass.setLastSyncDataTime();

            new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {

                    setCategoryAdapterOld();
                }
            }, 3000);
        }
    }

    void getAllRecipeListOld() {

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference recipeColl = globalclass.firebaseInstance().collection(Globalclass.recipeColl);
            recipeColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    swipeRefresh.setRefreshing(false);

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.recipe);
                            mydatabase.truncateTable(mydatabase.recipeInstruction);
                            for (DocumentSnapshot documents : task.getResult()) {

                                recipeDetail model = documents.toObject(recipeDetail.class);
                                mydatabase.addRecipe(model);

                                ArrayList<String> instructionList = model.getRecipeInstructions();
                                if (instructionList != null && !instructionList.isEmpty()) {
                                    mydatabase.deleteData(mydatabase.recipeInstruction, "recipeId", model.getRecipeId());

                                    for (int i = 0; i < instructionList.size(); i++) {
                                        recipeInstructionDetail recipeInstructionModel = new recipeInstructionDetail();
                                        recipeInstructionModel.setRecipeId(model.getRecipeId());
                                        recipeInstructionModel.setInstruction(instructionList.get(i));
                                        recipeInstructionModel.setStepNumber(i + 1);
                                        mydatabase.addRecipeInstructions(recipeInstructionModel);
                                    }
                                }
                            }

                        } else {
                            mydatabase.truncateTable(mydatabase.recipe);
                            mydatabase.truncateTable(mydatabase.recipeInstruction);
                        }
                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getAllRecipeList: " + error);
                        globalclass.toast_long("Unable to get recipe, please try after sometime!");
                        globalclass.sendErrorLog(tag,"getAllRecipeList",error);
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllRecipeList: " + error);
            globalclass.toast_long("Unable to get recipe, please try after sometime!");
            globalclass.sendErrorLog(tag,"getAllRecipeList",error);
        }
    }

    void getAllIngredientsListOld() {

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference ingredientsColl = globalclass.firebaseInstance().collection(Globalclass.ingredientsColl);
            ingredientsColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.recipeIngredients);
                            for (DocumentSnapshot documents : task.getResult()) {

                                ingredientsDetail model = documents.toObject(ingredientsDetail.class);
                                mydatabase.addIngredients(model);
                            }

                            globalclass.log(tag, "All Ingredients data added!");
                        } else {
                            mydatabase.truncateTable(mydatabase.recipeIngredients);
                            globalclass.log(tag, "All Ingredients is empty!");
                        }
                    } else {
                        swipeRefresh.setRefreshing(false);
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getAllIngredientsList: " + error);
                        globalclass.toast_long("Unable to get recipe, please try after sometime!");
                        globalclass.sendErrorLog(tag,"getAllIngredientsList",error);
                    }
                }
            });
        } catch (Exception e) {
            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllIngredientsList: " + error);
            globalclass.toast_long("Unable to get recipe, please try after sometime!");
            globalclass.sendErrorLog(tag,"getAllIngredientsList",error);
        }
    }

    void getAllRecipeImageListOld() {

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference recipeImagesColl = globalclass.firebaseInstance().collection(Globalclass.recipeImagesColl);
            recipeImagesColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    swipeRefresh.setRefreshing(false);

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.recipeImages);
                            for (DocumentSnapshot documents : task.getResult()) {

                                recipePhotoDetail model = documents.toObject(recipePhotoDetail.class);
                                mydatabase.addRecipeImages(model);
                            }

                            globalclass.log(tag, "All Recipe Image data added!");
                        } else {
                            mydatabase.truncateTable(mydatabase.recipeImages);
                            globalclass.log(tag, "All Recipe Image is empty!");
                        }
                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getAllRecipeImageList: " + error);
                        globalclass.toast_long("Unable to get recipe, please try after sometime!");
                        globalclass.sendErrorLog(tag,"getAllRecipeImageList",error);
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllRecipeImageList: " + error);
            globalclass.toast_long("Unable to get recipe, please try after sometime!");
            globalclass.sendErrorLog(tag,"getAllRecipeImageList",error);
        }
    }

    void setCategoryAdapterOld() {
        if (categoryList != null && !categoryList.isEmpty()) {
            categoryAdapter = new categoryAdapter(activity, categoryList, new categoryOnClick() {
                @Override
                public void onClick(int position, recipeCategoryDetail model) {

                    selectedCategoryId = model.getCategoryId();
                    categoryName.setText(globalclass.firstLetterCapital(model.getCategoryName()));
                    doFilter();
                }
            });

            categoryRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
            categoryRecyclerView.setAdapter(categoryAdapter);

            selectedCategoryId = categoryList.get(0).getCategoryId();
            categoryName.setText(globalclass.firstLetterCapital(categoryList.get(0).getCategoryName()));
            doFilter();
        } else {
            String error = "categoryList is null";
            globalclass.log(tag, "setCategoryAdapter: "+error);
            globalclass.toast_long("Unable to get recipe list, please try after sometime!");
            globalclass.sendErrorLog(tag,"setCategoryAdapter",error);
        }
    }

    void getRecipeData(String query) {

        try {
//            globalclass.log(tag,query);
            recipeList.clear();

            recipeList = mydatabase.getHomeRecipeList(query);
            if (recipeList.isEmpty()) {
                globalclass.snackit(activity, "No recipe found!");
            }
            setRecipeAdapter();
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeData: " + error);
            globalclass.toast_long("Unable to get recipe list, please try after sometime!");
            globalclass.sendErrorLog(tag,"getRecipeData",error);
        }
    }

    void setRecipeAdapter() {
        recipeAdapter = new RecipeAdapter(activity, recipeList, new RecipeOnClick() {
            @Override
            public void viewDetail(int position, recipeDetail model) {

                startActivity(new Intent(activity, RecipeDetail.class)
                        .putExtra("recipeId", model.getRecipeId()));
            }
        });

        linearLayoutManager = new LinearLayoutManager(activity);
        recipeRecyclerView.setLayoutManager(linearLayoutManager);
        recipeRecyclerView.setAdapter(recipeAdapter);
    }

    void doFilter() {

        if (selectedRecipeType.equalsIgnoreCase(activity.getString(R.string.all))) {
            query = "SELECT * FROM " + mydatabase.recipe + " WHERE categoryId = '" + selectedCategoryId + "'";
            getRecipeData(query);
        } else {
            query = "SELECT * FROM " + mydatabase.recipe + " WHERE categoryId = '" + selectedCategoryId + "' " +
                    "AND recipeType = '" + selectedRecipeType + "' COLLATE NOCASE";
            getRecipeData(query);
        }
    }

    void removeListener() {

        if (categoryListListener != null) {
            categoryListListener.remove();
        }

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
    public void onPause() {
        super.onPause();

        removeListener();
    }
}