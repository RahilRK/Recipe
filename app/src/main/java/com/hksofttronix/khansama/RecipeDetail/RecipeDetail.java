package com.hksofttronix.khansama.RecipeDetail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.hksofttronix.khansama.CartActivity.CartActivity;
import com.hksofttronix.khansama.Models.cartDetail;
import com.hksofttronix.khansama.Models.favouriteDetail;
import com.hksofttronix.khansama.Models.ingredientsDetail;
import com.hksofttronix.khansama.Models.recipeDetail;
import com.hksofttronix.khansama.Models.recipeInstructionDetail;
import com.hksofttronix.khansama.Models.recipePhotoDetail;
import com.hksofttronix.khansama.Models.userDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Util.FadeTransformation;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;

public class RecipeDetail extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = RecipeDetail.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    ImageView ivBackArrow;
    LikeButton ivHeart;
    SwipeRefreshLayout swipeRefresh;
    ViewPager viewPager;
    DotsIndicator dotsIndicator;
    TextView name, price, recipeType, categoryName;
    ImageView ivRecipeType;
    MaterialButton addToCart;
    LinearLayout addQuantityLayout;
    TextView quantity;
    ImageView ivMinus, ivAdd;
    RelativeLayout cartDetaillayout;
    TextView total, viewCart;
    ChipGroup ingredientsChipGroup;
    LinearLayout addToCartLayout;
    TextView notAvailable;

    String recipeId = "";
    recipeDetail model;
    cartDetail cartDetailModel;
    favouriteDetail favouriteModel;

    ArrayList<recipePhotoDetail> recipeImagesList = new ArrayList<>();
    RecipeImagesSliderAdapter recipeImagesSliderAdapter;

    ArrayList<ingredientsDetail> ingredientsList = new ArrayList<>();

    ProgressDialog progressDialog;

    int favouriteCount = 0;
    int cartCount = 0;

    boolean removeFavourite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        init();
        binding();
        onClick();
        handleIntent();
    }

    void handleIntent() {
        recipeId = getIntent().getStringExtra("recipeId");
        model = mydatabase.getParticularRecipeDetail(recipeId);

        if (model != null) {
            setText();
        } else {
            globalclass.log(tag, "recipeDetails model is null!");
            globalclass.toast_long("Unable to get recipe detail, please refresh any try again!");
        }
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        ivBackArrow = findViewById(R.id.ivBackArrow);
        ivHeart = findViewById(R.id.ivHeart);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        name = findViewById(R.id.name);
        price = findViewById(R.id.price);
        recipeType = findViewById(R.id.recipeType);
        categoryName = findViewById(R.id.categoryName);
        ivRecipeType = findViewById(R.id.ivRecipeType);
        addToCart = findViewById(R.id.addToCart);
        addQuantityLayout = findViewById(R.id.addQuantityLayout);
        ivMinus = findViewById(R.id.ivMinus);
        quantity = findViewById(R.id.quantity);
        ivAdd = findViewById(R.id.ivAdd);
        ingredientsChipGroup = findViewById(R.id.ingredientsChipGroup);
        cartDetaillayout = findViewById(R.id.cartDetaillayout);
        total = findViewById(R.id.total);
        viewCart = findViewById(R.id.viewCart);
        addToCartLayout = findViewById(R.id.addToCartLayout);
        notAvailable = findViewById(R.id.notAvailable);
    }

    void setText() {

        getRecipeImagesData();

        name.setText(globalclass.firstLetterCapital(model.getRecipeName()));
        price.setText(Html.fromHtml(getResources().getString(R.string.typeprice, String.valueOf(model.getPrice()))));
        recipeType.setText(globalclass.firstLetterCapital(model.getRecipeType()));
        categoryName.setText("Category: " + globalclass.firstLetterCapital(model.getCategoryName()));

        if (model.getRecipeType().equalsIgnoreCase("Veg")) {
            ivRecipeType.setBackgroundResource(R.drawable.veg);
        } else {
            ivRecipeType.setBackgroundResource(R.drawable.nonveg);
        }

        changeFavButtonUI();
        changeCartButtonUI();
        getIngredientsData();
        showHideNotAvailable();
    }

    void onClick() {

        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();
            }
        });

        ivHeart.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {

                if (!globalclass.checkUserIsLoggedIn()) {
                    globalclass.showLoginDialogue(activity);
                    ivHeart.setLiked(false);
                    return;
                }

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getString(R.string.noInternetConnection));
                    ivHeart.setLiked(false);
                    return;
                }

                favouriteDetail favouriteDetailModel = new favouriteDetail();
                favouriteDetailModel.setUserId(globalclass.getStringData("id"));
                favouriteDetailModel.setUserName(globalclass.getStringData("name"));
                favouriteDetailModel.setRecipeId(model.getRecipeId());
                favouriteDetailModel.setRecipeName(model.getRecipeName());
                favouriteDetailModel.setRecipeImageId(mydatabase.getFirstRecipeImages(model.getRecipeId()).get(0).getRecipeImageId());
                favouriteDetailModel.setRecipeImageUrl(mydatabase.getFirstRecipeImages(model.getRecipeId()).get(0).getUrl());
                favouriteDetailModel.setPrice(model.getPrice());

//                checkFavouriteCount(favouriteDetailModel);
                if (mydatabase.getFavouriteList() != null && !mydatabase.getFavouriteList().isEmpty()) {
                    if (mydatabase.getFavouriteList().size() < globalclass.maxFavouriteCount()) {
                        checkingRecipeIsFav(favouriteDetailModel, "Fav");
                    } else {
                        ivHeart.setLiked(false);
                        globalclass.showDialogue(activity, "Favourite limit alert",
                                "You can add maximum " + globalclass.maxFavouriteCount() + " recipe in your favourite list, please remove some recipe from favourite to add new!");
                    }
                } else {
                    checkingRecipeIsFav(favouriteDetailModel, "Fav");
                }
            }

            @Override
            public void unLiked(LikeButton likeButton) {

                if (!globalclass.checkUserIsLoggedIn()) {
                    globalclass.showLoginDialogue(activity);
                    ivHeart.setLiked(false);
                    return;
                }

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getString(R.string.noInternetConnection));
                    ivHeart.setLiked(true);
                    return;
                }

                checkingRecipeIsFav(favouriteModel, "Un-Fav");
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_long(getString(R.string.noInternetConnection));
                    return;
                }

                getRecipeDetails();
                getRecipeImageList();
                getRecipeIngredientsList();
                getFavouriteDetails();
                getCartDetails();
            }
        });

        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!globalclass.checkUserIsLoggedIn()) {
                    globalclass.showLoginDialogue(activity);
                    return;
                }

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getString(R.string.noInternetConnection));
                    return;
                }

                cartDetail cartModel = new cartDetail();
                cartModel.setUserId(globalclass.getStringData("id"));
                cartModel.setRecipeId(model.getRecipeId());
                cartModel.setRecipeName(model.getRecipeName());
                cartModel.setRecipeImageId(mydatabase.getFirstRecipeImages(model.getRecipeId()).get(0).getRecipeImageId());
                cartModel.setRecipeImageUrl(mydatabase.getFirstRecipeImages(model.getRecipeId()).get(0).getUrl());
                cartModel.setPrice(model.getPrice());
                cartModel.setQuantity(1);
                cartModel.setSum(model.getPrice());

                if (mydatabase.getCartList() != null && !mydatabase.getCartList().isEmpty()) {
                    if (mydatabase.getCartList().size() < globalclass.maxCartCount()) {
                        checkingRecipeInCart(cartModel);
                    } else {
                        changeCartButtonUI();
                        globalclass.showDialogue(activity, "Cart limit alert",
                                "You can add maximum " + globalclass.maxCartCount() + " recipe in your cart, please remove some recipe from cart to add new!");
                    }
                } else {
                    checkingRecipeInCart(cartModel);
                }
            }
        });

        ivMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getString(R.string.noInternetConnection));
                    return;
                }

                cartDetail cartDetailModel = mydatabase.getParticularCartDetail(recipeId);
                if (cartDetailModel.getQuantity() >= 2) {
                    int mquantity = cartDetailModel.getQuantity() - 1;
                    int sum = cartDetailModel.getPrice() * mquantity;
                    cartDetailModel.setQuantity(mquantity);
                    cartDetailModel.setSum(sum);
                    mydatabase.addToCart(cartDetailModel);
                    quantity.setText("" + quantity);
                    changeCartButtonUI();
                } else if (cartDetailModel.getQuantity() <= 1) {

                    if (mydatabase.checkRecipeExistInCart(model.getRecipeId()) == 1) {

                        removeFromCart();
                    } else {
                        changeCartButtonUI();
                    }
                }
            }
        });

        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cartDetail cartDetailModel = mydatabase.getParticularCartDetail(recipeId);
                int maxQuantity = 0;
                maxQuantity = globalclass.maxCartQuantity();

                if (cartDetailModel.getQuantity() <= maxQuantity - 1) {
                    int mquantity = cartDetailModel.getQuantity() + 1;
                    int sum = cartDetailModel.getPrice() * mquantity;
                    cartDetailModel.setQuantity(mquantity);
                    cartDetailModel.setSum(sum);
                    mydatabase.addToCart(cartDetailModel);
                    quantity.setText("" + quantity);
                    changeCartButtonUI();
                } else {
                    globalclass.toast_short("You can add maximum " + globalclass.maxCartQuantity() + " quantities for every recipe!");
                }
            }
        });

        viewCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                startActivity(new Intent(activity, CartActivity.class));
                startActivity(new Intent(activity, CartActivity.class));
            }
        });
    }

    //todo fetching ONLINE data
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
                                    recipeInstructionDetail model = new recipeInstructionDetail();
                                    model.setRecipeId(model.getRecipeId());
                                    model.setInstruction(instructionList.get(i));
                                    model.setStepNumber(i + 1);
                                    mydatabase.addRecipeInstructions(model);
                                }
                            }

                            setText();

                        } else {
                            globalclass.snackit(activity, "Recipe detail not found!");
                            mydatabase.deleteData(mydatabase.recipe, "recipeId", model.getRecipeId());
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

    void getRecipeIngredientsList() {

        String parameter = "";

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference ingredientsColl = globalclass.firebaseInstance().collection(Globalclass.ingredientsColl);
            Query query = ingredientsColl
                    .whereEqualTo("recipeId", recipeId);

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {

                            mydatabase.deleteData(mydatabase.recipeIngredients, "recipeId", model.getRecipeId());
                            for (DocumentSnapshot documents : task.getResult()) {

                                ingredientsDetail model = documents.toObject(ingredientsDetail.class);
                                mydatabase.addIngredients(model);
                            }

                            getIngredientsData();
                        } else {
                            mydatabase.deleteData(mydatabase.recipeIngredients, "recipeId", model.getRecipeId());
                            String error = "Recipe ingredients not found!";
                            globalclass.log(tag, "getRecipeIngredientsList: " + error);
                            globalclass.toast_long("Unable to get recipe ingredients list, please try after sometime!");
                            globalclass.sendResponseErrorLog(tag,"getRecipeIngredientsList: ",error, finalParameter);
                        }
                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getRecipeIngredientsList: " + error);
                        globalclass.toast_long("Unable to get recipe ingredients list, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"getRecipeIngredientsList: ",error, finalParameter);
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeIngredientsList: " + error);
            globalclass.toast_long("Unable to get recipe ingredients list, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"getRecipeIngredientsList: ",error, parameter);
        }
    }

    void getRecipeImageList() {

        String parameter = "";

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference recipeImagesColl = globalclass.firebaseInstance().collection(Globalclass.recipeImagesColl);
            Query query = recipeImagesColl
                    .whereEqualTo("recipeId", recipeId);

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);
                    if (task.isSuccessful()) {
                        mydatabase.deleteData(mydatabase.recipeImages, "recipeId", model.getRecipeId());
                        if (!task.getResult().isEmpty()) {
                            for (DocumentSnapshot documents : task.getResult()) {

                                recipePhotoDetail model = documents.toObject(recipePhotoDetail.class);
                                mydatabase.addRecipeImages(model);
                            }

                            getRecipeImagesData();
                        } else {
                            mydatabase.deleteData(mydatabase.recipeImages, "recipeId", model.getRecipeId());
                            String error = "Recipe images not found!";
                            globalclass.log(tag, "getRecipeImageList: " + error);
                            globalclass.toast_long("Unable to get recipe images, please try after sometime!");
                            globalclass.sendResponseErrorLog(tag,"getRecipeImageList: ",error, finalParameter);
                        }
                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getRecipeImageList: " + error);
                        globalclass.toast_long("Unable to get recipe images, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"getRecipeImageList: ",error, finalParameter);
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeImageList: " + error);
            globalclass.toast_long("Unable to get recipe images, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"getRecipeImageList: ",error, parameter);
        }
    }

    void getFavouriteDetails() {

        String parameter = "";

        try {
            if (!globalclass.checkUserIsLoggedIn()) {
                return;
            }

            swipeRefresh.setRefreshing(true);

            CollectionReference favouriteColl = globalclass.firebaseInstance().collection(Globalclass.favouriteColl);
            Query checkingAlreadyExist = favouriteColl
                    .whereEqualTo("userId", globalclass.getStringData("id"))
                    .whereEqualTo("recipeId", recipeId);

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            checkingAlreadyExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (DocumentSnapshot documents : task.getResult()) {
                                favouriteDetail model = documents.toObject(favouriteDetail.class);
                                mydatabase.addToFavourite(model);
                            }

                        } else {
                            mydatabase.deleteData(mydatabase.favourite, "recipeId", model.getRecipeId());
                        }

                        changeFavButtonUI();

                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getFavDetails: " + error);
                        globalclass.toast_long("Unable to refresh favourite details, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"getFavDetails: ",error, finalParameter);
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getFavDetails: " + error);
            globalclass.toast_long("Unable to refresh favourite details, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"getFavDetails: ",error, parameter);
        }
    }

    void getCartDetails() {

        String parameter = "";

        try {
            if (!globalclass.checkUserIsLoggedIn()) {
                return;
            }

            swipeRefresh.setRefreshing(true);

            CollectionReference cartColl = globalclass.firebaseInstance().collection(Globalclass.cartColl);
            Query checkingAlreadyExist = cartColl
                    .whereEqualTo("userId", globalclass.getStringData("id"))
                    .whereEqualTo("recipeId", model.getRecipeId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            checkingAlreadyExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (DocumentSnapshot documents : task.getResult()) {
                                cartDetail model = documents.toObject(cartDetail.class);
                                mydatabase.addToCart(model);
                            }

                        } else {
                            mydatabase.deleteData(mydatabase.cart, "recipeId", model.getRecipeId());
                        }

                        changeCartButtonUI();

                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getCartDetails: " + error);
                        globalclass.toast_long("Unable to refresh cart details, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"getCartDetails: ",error, finalParameter);
                    }
                }
            });
        } catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getCartDetails: " + error);
            globalclass.toast_long("Unable to refresh cart details, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"getCartDetails: ",error, parameter);
        }
    }

    //todo fetching OFFLINE data
    void getRecipeImagesData() {
        recipeImagesList.clear();
        recipeImagesList = mydatabase.getRecipeImagesList(model.getRecipeId());
        if (!recipeImagesList.isEmpty()) {
            setRecipeImagesSliderAdapter();
        } else {
            getRecipeImageList();
        }
    }

    void setRecipeImagesSliderAdapter() {
        recipeImagesSliderAdapter = new RecipeImagesSliderAdapter(activity, recipeImagesList);
        viewPager.setPageTransformer(true, new FadeTransformation());
        viewPager.setAdapter(recipeImagesSliderAdapter);

        if (recipeImagesList.size() > 1) {
            dotsIndicator.setVisibility(View.VISIBLE);
            dotsIndicator.setViewPager(viewPager);
        } else {
            dotsIndicator.setVisibility(View.GONE);
        }
    }

    void getIngredientsData() {
        ingredientsList.clear();
        ingredientsList = mydatabase.getIngredientsList(model.getRecipeId());
        if (ingredientsList != null && !ingredientsList.isEmpty()) {

            ingredientsChipGroup.removeAllViews();
            for (int i = 0; i < ingredientsList.size(); i++) {
                addChip(ingredientsList.get(i).getName(), ingredientsChipGroup);
            }
        } else {
            getRecipeIngredientsList();
        }
    }

    private void addChip(String text, ChipGroup chipGroup) {
        Chip chip = new Chip(this);
        chip.setText(text.toLowerCase());
        chip.setTextColor(getResources().getColor(R.color.black));
        chip.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/appfont.otf");
        chip.setTypeface(typeface);
//        chip.setChipBackgroundColor(getResources().getColorStateList(R.color.lightgraytoo));

        chipGroup.addView(chip, chipGroup.getChildCount() - 1);
    }

    void checkFavouriteCount(favouriteDetail favouriteDetailModel) {

        try {
            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);
            Query query = userColl.whereEqualTo("id", globalclass.getStringData("id"));
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (DocumentSnapshot documents : task.getResult()) {
                                userDetail model = documents.toObject(userDetail.class);
                                favouriteCount = model.getFavouriteCount();
                                globalclass.log(tag, "FavouriteCount: " + favouriteCount);
                            }

                            if (favouriteCount < globalclass.maxFavouriteCount()) {
                                checkingRecipeIsFav(favouriteDetailModel, "Fav");
                            } else {
                                ivHeart.setLiked(false);
                                globalclass.showDialogue(activity, "Favourite limit alert",
                                        "You can add maximum " + globalclass.maxFavouriteCount() + " recipe in your favourite list, please remove some recipe from favourite to add new!");
                            }
                        } else {
                            ivHeart.setLiked(false);
                            String error = "No user data found!";
                            globalclass.log(tag, "checkFavouriteCount: " + error);
                            globalclass.toast_long("Unable to add in favourite list, please try after sometime!");
                            globalclass.sendErrorLog(tag, "checkFavouriteCount", error);
                        }
                    } else {
                        ivHeart.setLiked(false);
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "checkAddressCount: " + error);
                        globalclass.toast_long("Unable to add in favourite list, please try after sometime!");
                        globalclass.sendErrorLog(tag, "checkFavouriteCount", error);
                    }
                }
            });
        } catch (Exception e) {

            ivHeart.setLiked(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkFavouriteCount: " + error);
            globalclass.toast_long("Unable to add in favourite list, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkFavouriteCount", error);
        }
    }

    void checkingRecipeIsFav(favouriteDetail favouriteModel, String action) {

        String parameter = "";

        try {
            CollectionReference favouriteColl = globalclass.firebaseInstance().collection(Globalclass.favouriteColl);
            Query checkingAlreadyExist = favouriteColl
                    .whereEqualTo("userId", favouriteModel.getUserId())
                    .whereEqualTo("recipeId", favouriteModel.getRecipeId());

            Gson gson = new Gson();
            if(favouriteModel != null) {
                parameter = gson.toJson(favouriteModel);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            checkingAlreadyExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {
                        if (action.equalsIgnoreCase("Fav")) {
                            if (!task.getResult().isEmpty()) {
                                for (DocumentSnapshot documents : task.getResult()) {
                                    favouriteDetail model = documents.toObject(favouriteDetail.class);
                                    mydatabase.addToFavourite(model);
                                }

                            } else {
                                addToFavourite(favouriteModel);
                            }
                        } else if (action.equalsIgnoreCase("Un-Fav")) {
                            if (!task.getResult().isEmpty()) {
                                removeFromFavourite();
                            } else {
                                mydatabase.deleteData(mydatabase.favourite, "recipeId", model.getRecipeId());
                            }
                        }
                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "checkingRecipeIsFav: " + error);
                        if (action.equalsIgnoreCase("Fav")) {
                            ivHeart.setLiked(false);
                            globalclass.toast_long("Unable to add in favourite list, please try after sometime!");
                        } else if (action.equalsIgnoreCase("Un-Fav")) {
                            ivHeart.setLiked(true);
                            globalclass.toast_long("Unable to remove from favourite list, please try after sometime!");
                        }
                        globalclass.sendResponseErrorLog(tag,"checkingRecipeIsFav: ",error, finalParameter);
                    }
                }
            });
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkingRecipeIsFav: " + error);
            if (action.equalsIgnoreCase("Fav")) {
                ivHeart.setLiked(false);
                globalclass.toast_long("Unable to add in favourite list, please try after sometime!");
            } else if (action.equalsIgnoreCase("Un-Fav")) {
                ivHeart.setLiked(true);
                globalclass.toast_long("Unable to remove from favourite list, please try after sometime!");
            }
            globalclass.sendResponseErrorLog(tag,"checkingRecipeIsFav: ",error, parameter);
        }
    }

    void addToFavourite(favouriteDetail favouriteDetailModel) {

        String parameter = "";

        try {
            showprogress("Adding to your favourite list", "Please wait...");

            WriteBatch batch = globalclass.firebaseInstance().batch();

            CollectionReference favouriteColl = globalclass.firebaseInstance().collection(Globalclass.favouriteColl);
//            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);

            String favouriteId = favouriteColl.document().getId();
            favouriteDetailModel.setFavouriteId(favouriteId);

            DocumentReference favouriteDocReference = favouriteColl.document(favouriteId);
            batch.set(favouriteDocReference, favouriteDetailModel);

//            Map<String,Object> map = new HashMap<>();
//            map.put("favouriteCount", FieldValue.increment(1));
//            DocumentReference userDocReference = userColl.document(favouriteDetailModel.getUserId());
//            batch.update(userDocReference,map);

            Gson gson = new Gson();
            if(favouriteDetailModel != null) {
                parameter = gson.toJson(favouriteDetailModel);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            batch.commit().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    hideprogress();
                    if (task.isSuccessful()) {
                        favouriteModel = favouriteDetailModel;
                        mydatabase.addToFavourite(favouriteDetailModel);
                        globalclass.snackit(activity, "Added to favourite list!");
                    } else {
                        ivHeart.setLiked(false);
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "addToFavourite: " + error);
                        globalclass.toast_long("Unable to add in favourite list, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"addToFavourite: ",error, finalParameter);
                    }
                }
            });

        } catch (Exception e) {

            hideprogress();
            ivHeart.setLiked(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addToFavourite: " + error);
            globalclass.toast_long("Unable to add in favourite list, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"addToFavourite: ",error, parameter);
        }
    }

    void removeFromFavourite() {

        String parameter = "";

        try {
            showprogress("Removing from your favourite list", "Please wait...");

            WriteBatch batch = globalclass.firebaseInstance().batch();

            CollectionReference favouriteColl = globalclass.firebaseInstance().collection(Globalclass.favouriteColl);
//            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);

            DocumentReference favouriteDocReference = favouriteColl.document(favouriteModel.getFavouriteId());
            batch.delete(favouriteDocReference);

//            Map<String,Object> map = new HashMap<>();
//            map.put("favouriteCount", FieldValue.increment(-1));
//            DocumentReference userDocReference = userColl.document(globalclass.getStringData("id"));
//            batch.update(userDocReference,map);

            Gson gson = new Gson();
            if(favouriteModel != null) {
                parameter = gson.toJson(favouriteModel);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            batch.commit().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    hideprogress();
                    if (task.isSuccessful()) {
                        removeFavourite = true;
                        mydatabase.deleteData(mydatabase.favourite, "favouriteId", favouriteModel.getFavouriteId());
                        globalclass.snackit(activity, "Removed from favourite list!");
                    } else {
                        ivHeart.setLiked(true);
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "removeFromFavourite: " + error);
                        globalclass.toast_long("Unable to remove from favourite list, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"removeFromFavourite: ",error, finalParameter);
                    }
                }
            });

        } catch (Exception e) {

            hideprogress();
            ivHeart.setLiked(true);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "removeFromFavourite: " + error);
            globalclass.toast_long("Unable to remove from favourite list, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"removeFromFavourite: ",error, parameter);
        }
    }

    void checkCartCount() {

        try {
            showprogress("Hold on", "Please wait...");

            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);
            Query query = userColl.whereEqualTo("id", globalclass.getStringData("id"));
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    hideprogress();

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (DocumentSnapshot documents : task.getResult()) {
                                userDetail model = documents.toObject(userDetail.class);
                                cartCount = model.getCartCount();
                                globalclass.log(tag, "CartCount: " + cartCount);
                            }

                            if (favouriteCount < globalclass.maxCartCount()) {

                                cartDetail cartModel = new cartDetail();
                                cartModel.setUserId(globalclass.getStringData("id"));
                                cartModel.setRecipeId(model.getRecipeId());
                                cartModel.setRecipeName(model.getRecipeName());
                                cartModel.setRecipeImageId(mydatabase.getFirstRecipeImages(model.getRecipeId()).get(0).getRecipeImageId());
                                cartModel.setRecipeImageUrl(mydatabase.getFirstRecipeImages(model.getRecipeId()).get(0).getUrl());
                                cartModel.setPrice(model.getPrice());
                                cartModel.setQuantity(1);
                                cartModel.setSum(model.getPrice());

                                if (mydatabase.checkRecipeExistInCart(model.getRecipeId()) == 0) {

                                    checkingRecipeInCart(cartModel);
                                } else {
                                    changeCartButtonUI();
                                }
                            } else {
                                globalclass.showDialogue(activity, "Cart limit alert",
                                        "You can add maximum " + globalclass.maxCartCount() + " recipe in your cart, please remove some recipe from cart to add new!");
                            }
                        } else {
                            String error = "No user data found!";
                            globalclass.log(tag, "checkCartCount: " + error);
                            globalclass.toast_long("Unable to add in cart, please try after sometime!");
                            globalclass.sendErrorLog(tag, "checkCartCount", error);
                        }
                    } else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "checkCartCount: " + error);
                        globalclass.toast_long("Unable to add in cart, please try after sometime!");
                        globalclass.sendErrorLog(tag, "checkCartCount", error);
                    }
                }
            });
        } catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkCartCount: " + error);
            globalclass.toast_long("Unable to add in cart, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkCartCount", error);
        }
    }

    void checkingRecipeInCart(cartDetail cartModel) {

        String parameter = "";

        try {
            CollectionReference cartColl = globalclass.firebaseInstance().collection(Globalclass.cartColl);
            Query checkingAlreadyExist = cartColl
                    .whereEqualTo("userId", cartModel.getUserId())
                    .whereEqualTo("recipeId", cartModel.getRecipeId());

            Gson gson = new Gson();
            if(cartModel != null) {
                parameter = gson.toJson(cartModel);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            checkingAlreadyExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (DocumentSnapshot documents : task.getResult()) {
                                cartDetail model = documents.toObject(cartDetail.class);
                                globalclass.log(tag, "Cart Id: " + model.getCartId());
                            }

                            changeCartButtonUI();

                        } else {
                            addToCart(cartModel);
                        }
                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "checkAlreadyExistInCartForAdd: " + error);
                        globalclass.toast_long("Unable to add in cart, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"checkAlreadyExistInCartForAdd: ",error, finalParameter);
                    }
                }
            });
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkAlreadyExistInCartForAdd: " + error);
            globalclass.toast_long("Unable to add in cart, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"checkAlreadyExistInCartForAdd: ",error, parameter);
        }
    }

    void addToCart(cartDetail cartModel) {

        String parameter = "";

        try {
            showprogress("Adding to cart", "Please wait...");

            WriteBatch batch = globalclass.firebaseInstance().batch();

            CollectionReference cartColl = globalclass.firebaseInstance().collection(Globalclass.cartColl);
//            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);

            String cartId = cartColl.document().getId();
            cartModel.setCartId(cartId);

            DocumentReference cartDocReference = cartColl.document(cartId);
            batch.set(cartDocReference, cartModel);

//            DocumentReference userDocReference = userColl.document(globalclass.getStringData("id"));
//            Map<String, Object> map = new HashMap<>();
//            map.put("cartCount", FieldValue.increment(1));
//            batch.update(userDocReference, map);

            Gson gson = new Gson();
            if(cartModel != null) {
                parameter = gson.toJson(cartModel);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            batch.commit().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    hideprogress();
                    if (task.isSuccessful()) {
                        mydatabase.addToCart(cartModel);
                        changeCartButtonUI();
                    } else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "addToCart: " + error);
                        globalclass.toast_long("Unable to add in cart, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"addToCart: ",error, finalParameter);
                    }
                }
            });
        } catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addToCart: " + error);
            globalclass.toast_long("Unable to add in cart, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"addToCart: ",error, parameter);
        }
    }

    void removeFromCart() {

        String parameter = "";

        try {
            showprogress("Removing from cart", "Please wait...");

            final DocumentReference cartDocRef = globalclass.firebaseInstance()
                    .collection(Globalclass.cartColl)
                    .document(cartDetailModel.getCartId());

            Gson gson = new Gson();
            if(cartDetailModel != null) {
                parameter = gson.toJson(cartDetailModel);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            globalclass.firebaseInstance().runTransaction(new Transaction.Function<Integer>() {

                @Nullable
                @Override
                public Integer apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                    DocumentSnapshot addressDoc = transaction.get(cartDocRef);
                    if(addressDoc.exists()) {

                        cartDetail cartModel = addressDoc.toObject(cartDetail.class);
                        if(cartModel.getCartId().equalsIgnoreCase(cartDetailModel.getCartId())) {

                            transaction.delete(cartDocRef);
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

                        mydatabase.deleteData(mydatabase.cart, "cartId", cartDetailModel.getCartId());
                        globalclass.snackit(activity, "Removed successfully!");
                        changeCartButtonUI();
                    }
                    else if(integer == -1) {

                        String error = "return -1";
                        globalclass.log(tag,"removeFromCart onSuccess: "+error);
                        globalclass.toast_long("Unable to remove recipe from cart, please try after sometime!");
                        globalclass.sendResponseErrorLog(tag,"removeFromCart: ",error, finalParameter);
                    }
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag, "removeFromCart onFailure: " + error);
                    globalclass.toast_long("Unable to remove recipe from cart, please try after sometime!");
                    globalclass.sendResponseErrorLog(tag,"removeFromCart: ",error, finalParameter);
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "removeFromCart: " + error);
            globalclass.toast_long("Unable to remove recipe from cart, please try after sometime!");
            globalclass.sendResponseErrorLog(tag,"removeFromCart: ",error, parameter);
        }
    }

    void changeFavButtonUI() {
        if (mydatabase.checkRecipeExistInFav(model.getRecipeId()) == 0) {
            ivHeart.setLiked(false);
        } else {
            ivHeart.setLiked(true);
            favouriteModel = mydatabase.getParticularFavDetail(model.getRecipeId());
        }
    }

    void changeCartButtonUI() {

        if (mydatabase.checkRecipeExistInCart(model.getRecipeId()) == 0) {

            addQuantityLayout.setVisibility(View.GONE);
            cartDetaillayout.setVisibility(View.GONE);
            addToCart.setVisibility(View.VISIBLE);
        } else {

            addToCart.setVisibility(View.GONE);
            addQuantityLayout.setVisibility(View.VISIBLE);
            cartDetaillayout.setVisibility(View.VISIBLE);

            cartDetailModel = mydatabase.getParticularCartDetail(model.getRecipeId());
            quantity.setText("" + mydatabase.getParticularCartDetail(recipeId).getQuantity());
            total.setText("" + mydatabase.getCartItemCount() + " Item | ₹ " + mydatabase.getCartSum() + "");
        }
    }

    void showHideNotAvailable() {
        if (!model.getStatus()) {
            addToCartLayout.setVisibility(View.GONE);
            notAvailable.setVisibility(View.VISIBLE);
        } else {
            notAvailable.setVisibility(View.GONE);
            addToCartLayout.setVisibility(View.VISIBLE);
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

    @Override
    protected void onResume() {
        super.onResume();

        changeCartButtonUI();
    }

    @Override
    public void onBackPressed() {
        if (removeFavourite && getIntent().getExtras() != null) {
            Intent intent = new Intent();
            intent.putExtra("position", getIntent().getStringExtra("position"));
            setResult(RESULT_OK, intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}