package com.hksofttronix.khansama;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hksofttronix.khansama.Admin.AdminHome;
import com.hksofttronix.khansama.Models.addressDetail;
import com.hksofttronix.khansama.Models.allOrderItemDetail;
import com.hksofttronix.khansama.Models.cartDetail;
import com.hksofttronix.khansama.Models.cityDetail;
import com.hksofttronix.khansama.Models.favouriteDetail;
import com.hksofttronix.khansama.Models.ingredientsDetail;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.navMenuDetail;
import com.hksofttronix.khansama.Models.orderItemDetail;
import com.hksofttronix.khansama.Models.orderStatusDetail;
import com.hksofttronix.khansama.Models.orderSummaryDetail;
import com.hksofttronix.khansama.Models.placeOrderDetail;
import com.hksofttronix.khansama.Models.recipeCategoryDetail;
import com.hksofttronix.khansama.Models.recipeDetail;
import com.hksofttronix.khansama.Models.recipeInstructionDetail;
import com.hksofttronix.khansama.Models.recipePhotoDetail;
import com.hksofttronix.khansama.Models.stateDetail;
import com.hksofttronix.khansama.Models.unitDetail;
import com.hksofttronix.khansama.Models.userDetail;
import com.hksofttronix.khansama.Models.vendorDetail;

import java.util.ArrayList;

public class SetUpPage extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = SetUpPage.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    TextView tvholdOnText;
    ProgressBar progress_bar;
    MaterialButton retrybt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_page);

        init();
        binding();
        onClick();

        if(!globalclass.isInternetPresent()) {
            globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
            showRetryView();
            return;
        }

        handleIntent();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        tvholdOnText = findViewById(R.id.tvholdOnText);
        progress_bar = findViewById(R.id.progress_bar);
        retrybt = findViewById(R.id.retrybt);
    }

    void onClick() {
        retrybt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getData();
                showNormalView();
            }
        });
    }

    void getData() {

        getRecipeCategoryList();
//        getAllRecipeList();
//        getAllIngredientsList();
//        getAllRecipeImageList();
        getCartList();
        getFavouriteList();
        getCityList();
        getStateList();
        getAddressList();
        getPlaceOrderList();
        addOrderStatusList();

        if(globalclass.getStringData("loginType").equalsIgnoreCase("Admin")) {

//            getInventoryList();
            getUnitList();
            getVendorList();
            getAllOrderList();
        }

        globalclass.setLastSyncDataTime();

        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                globalclass.subscribeToTopic(globalclass.getStringData("mobilenumber"));

                if(globalclass.getStringData("loginType").equalsIgnoreCase("Admin")) {
                    globalclass.subscribeToTopic("allAdmin");
                    startActivity(new Intent(activity, AdminHome.class).putExtra("welcome","true")
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                }
                else {
                    globalclass.subscribeToTopic("allUser");
                    startActivity(new Intent(activity, MainActivity.class).putExtra("welcome","true")
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                }
            }
        },8000);
    }

    void getInventoryList() {

        try {
            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
            inventoryColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {

                                inventoryDetail model = documentSnapshot.toObject(inventoryDetail.class);
                                model.setisSelected(false);
                                mydatabase.addInventory(model);
                            }

                            globalclass.log(tag,"Inventory data added!");
                        }
                        else {
                            globalclass.log(tag,"No inventory found!");
                        }
                    }
                    else {
                        String error = task.getException().toString();
                        globalclass.log(tag, "getInventoryList:" + error);
                        globalclass.toast_long("Unable to get inventory, please try again after sometime!");
                        globalclass.sendErrorLog(tag, "getInventoryList", error);
                    }
                }
            });

        } catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getInventoryList_" + error);
            globalclass.toast_long("Unable to get inventory list, please try after sometime!");
            globalclass.sendErrorLog(tag, "getInventoryList", error);
        }
    }

    void getUnitList() {
        try {

            CollectionReference collectionReference = globalclass.firebaseInstance().collection(Globalclass.unitColl);
            collectionReference.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                unitDetail model = documents.toObject(unitDetail.class);
                                mydatabase.addUnit(model);
                            }

                            globalclass.log(tag,"Unit data added!");
                        }
                        else {
                            globalclass.log(tag,"No unit details found!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getUnitList: "+error);
                        globalclass.toast_long("Unable to get Unit data, please try after sometime!");
                        globalclass.sendErrorLog(tag, "getUnitList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getUnitList_"+error);
            globalclass.toast_long("Unable to get Unit data, please try after sometime!");
            globalclass.sendErrorLog(tag, "getUnitList", error);
        }
    }

    void getVendorList() {

        try {
            CollectionReference vendorColl = globalclass.firebaseInstance().collection(Globalclass.vendorColl);
            vendorColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                vendorDetail model = documents.toObject(vendorDetail.class);
                                mydatabase.addVendor(model);
                            }

                            globalclass.log(tag,"Vendor data added!");
                        }
                        else {
                            globalclass.log(tag,"No Vendor exist!");
                        }


                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getVendorList: "+error);
                        globalclass.sendErrorLog(tag, "getVendorList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getVendorList: "+error);
            globalclass.sendErrorLog(tag, "getVendorList", error);
        }

    }

    void getRecipeCategoryList() {

        try {
            CollectionReference recipeCategoryColl = globalclass.firebaseInstance().collection(Globalclass.recipeCategoryColl);
            recipeCategoryColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.recipeCategory);
                            for(DocumentSnapshot documents : task.getResult()) {
                                recipeCategoryDetail model = documents.toObject(recipeCategoryDetail.class);
                                mydatabase.addRecipeCategory(model);
                            }

                            globalclass.log(tag,"Recipe's category data added!");
                        }
                        else {
                            globalclass.log(tag,"No Recipe Category exist!");
                        }
                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getRecipeCategoryList: "+error);
                        globalclass.sendErrorLog(tag, "getRecipeCategoryList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getRecipeCategoryList: "+error);
            globalclass.sendErrorLog(tag, "getRecipeCategoryList", error);
        }
    }

    void getCartList() {

        try {
            CollectionReference cartColl = globalclass.firebaseInstance().collection(Globalclass.cartColl);
            Query query = cartColl
                    .whereEqualTo("userId",globalclass.getStringData("id"));

            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.cart);
                            for(DocumentSnapshot documents : task.getResult()) {
                                cartDetail model = documents.toObject(cartDetail.class);
                                model.setQuantity(1);
                                model.setSum(model.getPrice());
                                mydatabase.addToCart(model);
                            }

                            globalclass.log(tag,"Cart data added!");
                        }
                        else {
                            globalclass.log(tag,"Cart is empty!");
                        }
                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getCartList: "+error);
                        globalclass.sendErrorLog(tag, "getCartList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getCartList: "+error);
            globalclass.sendErrorLog(tag, "getCartList", error);
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

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.favourite);
                            for(DocumentSnapshot documents : task.getResult()) {
                                favouriteDetail model = documents.toObject(favouriteDetail.class);
                                mydatabase.addToFavourite(model);
                            }

                            globalclass.log(tag,"Favourite data added!");
                        }
                        else {
                            globalclass.log(tag,"Favourite is empty!");
                        }
                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getFavouriteList: "+error);
                        globalclass.sendErrorLog(tag, "getFavouriteList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getFavouriteList: "+error);
            globalclass.sendErrorLog(tag, "getFavouriteList", error);
        }
    }

    void getCityList() {

        try {
            CollectionReference cityColl = globalclass.firebaseInstance().collection(Globalclass.cityColl);
            cityColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.city);
                            for(DocumentSnapshot documents : task.getResult()) {
                                cityDetail model = documents.toObject(cityDetail.class);
                                mydatabase.addCity(model);
                            }

                            globalclass.log(tag,"City data added!");
                        }
                        else {
                            globalclass.log(tag,"City is empty!");
                        }
                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getCityList: "+error);
                        globalclass.sendErrorLog(tag, "getCityList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getCityList: "+error);
            globalclass.sendErrorLog(tag, "getCityList", error);
        }
    }

    void getStateList() {

        try {
            CollectionReference stateColl = globalclass.firebaseInstance().collection(Globalclass.stateColl);
            stateColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.state);
                            for(DocumentSnapshot documents : task.getResult()) {
                                stateDetail model = documents.toObject(stateDetail.class);
                                mydatabase.addState(model);
                            }

                            globalclass.log(tag,"State data added!");
                        }
                        else {
                            globalclass.log(tag,"State is empty!");
                        }
                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getStateList: "+error);
                        globalclass.sendErrorLog(tag, "getStateList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getStateList: "+error);
            globalclass.sendErrorLog(tag, "getStateList", error);
        }
    }

    void getAddressList() {

        try {
            CollectionReference addressColl = globalclass.firebaseInstance().collection(Globalclass.addressColl);
            Query query = addressColl
                    .whereEqualTo("userId",globalclass.getStringData("id"));
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.address);
                            for(DocumentSnapshot documents : task.getResult()) {
                                addressDetail model = documents.toObject(addressDetail.class);
                                mydatabase.addNewAddress(model);
                            }

                            globalclass.log(tag,"Address data added!");
                        }
                        else {
                            globalclass.log(tag,"Address is empty!");
                        }
                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getAddressList: "+error);
                        globalclass.sendErrorLog(tag, "getAddressList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getAddressList: "+error);
            globalclass.sendErrorLog(tag, "getAddressList", error);
        }
    }

    void getPlaceOrderList() {

        try {
            CollectionReference placeOrderColl = globalclass.firebaseInstance().collection(Globalclass.placeOrderColl);
            Query query = placeOrderColl
                    .whereEqualTo("userId",globalclass.getStringData("id"));
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.placeOrder);
                            for(DocumentSnapshot documents : task.getResult()) {
                                orderSummaryDetail model = documents.toObject(orderSummaryDetail.class);
                                getOrderItemList(model);
                            }

                            globalclass.log(tag,"Place order data added!");
                        }
                        else {
                            globalclass.log(tag,"Place order is empty!");
                        }
                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getPlaceOrderList: "+error);
                        globalclass.sendErrorLog(tag, "getPlaceOrderList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getPlaceOrderList: "+error);
            globalclass.sendErrorLog(tag, "getPlaceOrderList", error);
        }
    }

    void getOrderItemList(orderSummaryDetail model) {

        try {
            CollectionReference orderItemColl = globalclass.firebaseInstance().collection(Globalclass.orderItemColl);
            Query query = orderItemColl
                    .whereEqualTo("orderId",model.getOrderId());
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                orderItemDetail orderItemModel = documents.toObject(orderItemDetail.class);

                                placeOrderDetail placeOrderModel = new placeOrderDetail();
                                placeOrderModel.setOrderId(model.getOrderId());
                                placeOrderModel.setUserId(model.getUserId());
                                placeOrderModel.setUserMobileNumber(model.getUserMobileNumber());
                                placeOrderModel.setCancelReason(model.getCancelReason());
                                placeOrderModel.setOrderStatus(model.getOrderStatus());
                                placeOrderModel.setOrderStep(model.getOrderStep());
                                placeOrderModel.setRecipeCharges(model.getRecipeCharges());
                                placeOrderModel.setPackagingCharges(model.getPackagingCharges());
                                placeOrderModel.setTotal(model.getTotal());
                                placeOrderModel.setItem(model.getItem());
                                placeOrderModel.setOrderDateTime(model.getOrderDateTime());
                                placeOrderModel.setDeliveryDateTime(model.getDeliveryDateTime());
                                placeOrderModel.setAddressId(model.getAddressId());
                                placeOrderModel.setName(model.getName());
                                placeOrderModel.setMobileNumber(model.getMobileNumber());
                                placeOrderModel.setAddress(model.getAddress());
                                placeOrderModel.setNearBy(model.getNearBy());
                                placeOrderModel.setCityId(model.getCityId());
                                placeOrderModel.setCity(model.getCity());
                                placeOrderModel.setStateId(model.getStateId());
                                placeOrderModel.setState(model.getState());
                                placeOrderModel.setPincode(model.getPincode());
                                placeOrderModel.setOrderItemId(orderItemModel.getOrderItemId());
                                placeOrderModel.setRecipeId(orderItemModel.getRecipeId());
                                placeOrderModel.setRecipeName(orderItemModel.getRecipeName());
                                placeOrderModel.setRecipeImageId(orderItemModel.getRecipeImageId());
                                placeOrderModel.setRecipeImageUrl(orderItemModel.getRecipeImageUrl());
                                placeOrderModel.setPrice(orderItemModel.getPrice());
                                placeOrderModel.setQuantity(orderItemModel.getQuantity());

                                mydatabase.addPlaceOrder(placeOrderModel);
                            }
                            globalclass.log(tag,"Order item data added!");
                        }
                        else {
                            globalclass.log(tag,"Order item is empty!");
                        }
                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getOrderItemList: "+error);
                        globalclass.sendErrorLog(tag, "getOrderItemList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getOrderItemList: "+error);
            globalclass.sendErrorLog(tag, "getOrderItemList", error);
        }
    }

    void getAllOrderList() {

        try {
            CollectionReference placeOrderColl = globalclass.firebaseInstance().collection(Globalclass.placeOrderColl);
            placeOrderColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.allOrder);
                            for(DocumentSnapshot documents : task.getResult()) {
                                orderSummaryDetail model = documents.toObject(orderSummaryDetail.class);
                                getAllOrderItemList(model);
                            }

                            globalclass.log(tag,"All Order data added!");
                        }
                        else {
                            globalclass.log(tag,"All Order is empty!");
                        }
                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getAllOrderList: "+error);
                        globalclass.sendErrorLog(tag, "getAllOrderList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getAllOrderList: "+error);
            globalclass.sendErrorLog(tag, "getAllOrderList", error);
        }
    }

    void getAllOrderItemList(orderSummaryDetail model) {

        try {
            CollectionReference orderItemColl = globalclass.firebaseInstance().collection(Globalclass.orderItemColl);
            Query query = orderItemColl
                    .whereEqualTo("orderId",model.getOrderId());
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                orderItemDetail orderItemModel = documents.toObject(orderItemDetail.class);

                                allOrderItemDetail allOrderItemModel = new allOrderItemDetail();
                                allOrderItemModel.setOrderId(model.getOrderId());
                                allOrderItemModel.setUserId(model.getUserId());
                                allOrderItemModel.setUserMobileNumber(model.getUserMobileNumber());
                                allOrderItemModel.setCancelReason(model.getCancelReason());
                                allOrderItemModel.setOrderStatus(model.getOrderStatus());
                                allOrderItemModel.setOrderStep(model.getOrderStep());
                                allOrderItemModel.setRecipeCharges(model.getRecipeCharges());
                                allOrderItemModel.setPackagingCharges(model.getPackagingCharges());
                                allOrderItemModel.setTotal(model.getTotal());
                                allOrderItemModel.setItem(model.getItem());
                                allOrderItemModel.setOrderDateTime(model.getOrderDateTime());
                                allOrderItemModel.setDeliveryDateTime(model.getDeliveryDateTime());                                   allOrderItemModel.setAddressId(model.getAddressId());
                                allOrderItemModel.setName(model.getName());
                                allOrderItemModel.setMobileNumber(model.getMobileNumber());
                                allOrderItemModel.setAddress(model.getAddress());
                                allOrderItemModel.setNearBy(model.getNearBy());
                                allOrderItemModel.setCityId(model.getCityId());
                                allOrderItemModel.setCity(model.getCity());
                                allOrderItemModel.setStateId(model.getStateId());
                                allOrderItemModel.setState(model.getState());
                                allOrderItemModel.setPincode(model.getPincode());
                                allOrderItemModel.setOrderItemId(orderItemModel.getOrderItemId());
                                allOrderItemModel.setRecipeId(orderItemModel.getRecipeId());
                                allOrderItemModel.setRecipeName(orderItemModel.getRecipeName());
                                allOrderItemModel.setRecipeImageUrl(orderItemModel.getRecipeImageUrl());
                                allOrderItemModel.setPrice(orderItemModel.getPrice());
                                allOrderItemModel.setQuantity(orderItemModel.getQuantity());

                                mydatabase.addAllOrder(allOrderItemModel);
                            }
                        }
                        else {
                            globalclass.log(tag,"All Order item is empty!");
                        }
                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getAllOrderItemList: "+error);
                        globalclass.sendErrorLog(tag, "getAllOrderList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getAllOrderItemList: "+error);
            globalclass.sendErrorLog(tag, "getAllOrderList", error);
        }
    }

    void getAllRecipeList() {

        try {
            CollectionReference recipeColl = globalclass.firebaseInstance().collection(Globalclass.recipeColl);
            recipeColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {

                            mydatabase.truncateTable(mydatabase.recipe);
                            mydatabase.truncateTable(mydatabase.recipeInstruction);

                            if(globalclass.checknull(globalclass.getStringData("loginType"))
                                    .equalsIgnoreCase("Admin")) {
                                mydatabase.truncateTable(mydatabase.allRecipe);
                                mydatabase.truncateTable(mydatabase.allRecipeInstructions);
                            }

                            for(DocumentSnapshot documents : task.getResult()) {
                                recipeDetail model = documents.toObject(recipeDetail.class);
                                mydatabase.addRecipe(model);
                                if(globalclass.checknull(globalclass.getStringData("loginType"))
                                        .equalsIgnoreCase("Admin")) {
                                    mydatabase.addAllRecipe(model);
                                }

                                ArrayList<String> instructionList = model.getRecipeInstructions();
                                for(int i=0;i<instructionList.size();i++) {
                                    recipeInstructionDetail recipeInstructionModel = new recipeInstructionDetail();
                                    recipeInstructionModel.setRecipeId(model.getRecipeId());
                                    recipeInstructionModel.setInstruction(instructionList.get(i));
                                    recipeInstructionModel.setStepNumber(i+1);
                                    mydatabase.addRecipeInstructions(recipeInstructionModel);
                                    if(globalclass.checknull(globalclass.getStringData("loginType"))
                                            .equalsIgnoreCase("Admin")) {
                                        mydatabase.addAllRecipeInstructions(recipeInstructionModel);
                                    }
                                }
                            }

                            globalclass.log(tag,"All Recipe data added!");
                        }
                        else {
                            globalclass.log(tag,"All Recipe is empty!");
                        }
                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getAllRecipeList: "+error);
                        globalclass.sendErrorLog(tag, "getAllRecipeList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getAllRecipeList: "+error);
            globalclass.sendErrorLog(tag, "getAllRecipeList", error);
        }
    }

    void getAllIngredientsList() {

        try {
            CollectionReference ingredientsColl = globalclass.firebaseInstance().collection(Globalclass.ingredientsColl);
            ingredientsColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.recipeIngredients);
                            if(globalclass.checknull(globalclass.getStringData("loginType"))
                                    .equalsIgnoreCase("Admin")) {
                                mydatabase.truncateTable(mydatabase.allIngredients);
                            }

                            for(DocumentSnapshot documents : task.getResult()) {

                                ingredientsDetail model = documents.toObject(ingredientsDetail.class);
                                mydatabase.addIngredients(model);
                                if(globalclass.checknull(globalclass.getStringData("loginType"))
                                        .equalsIgnoreCase("Admin")) {
                                    mydatabase.addAllIngredients(model);
                                }
                            }

                            globalclass.log(tag,"All Ingredients data added!");
                        }
                        else {
                            globalclass.log(tag,"All Ingredients is empty!");
                        }
                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getAllIngredientsList: "+error);
                        globalclass.sendErrorLog(tag, "getAllIngredientsList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getAllIngredientsList: "+error);
            globalclass.sendErrorLog(tag, "getAllIngredientsList", error);
        }
    }

    void getAllRecipeImageList() {

        try {
            CollectionReference recipeImagesColl = globalclass.firebaseInstance().collection(Globalclass.recipeImagesColl);
            recipeImagesColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.recipeImages);
                            if(globalclass.checknull(globalclass.getStringData("loginType"))
                                    .equalsIgnoreCase("Admin")) {
                                mydatabase.truncateTable(mydatabase.allRecipeImages);
                            }
                            for(DocumentSnapshot documents : task.getResult()) {

                                recipePhotoDetail model = documents.toObject(recipePhotoDetail.class);
                                mydatabase.addRecipeImages(model);
                                if(globalclass.checknull(globalclass.getStringData("loginType"))
                                        .equalsIgnoreCase("Admin")) {
                                    mydatabase.addAllRecipeImages(model);
                                }
                            }

                            globalclass.log(tag,"All Recipe Image data added!");
                        }
                        else {
                            globalclass.log(tag,"All Recipe Image is empty!");
                        }
                    }
                    else {
                        ;
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getAllRecipeImageList: "+error);
                        globalclass.sendErrorLog(tag, "getAllRecipeImageList", error);

                    }
                }
            });
        }
        catch (Exception e) {

            ;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getAllRecipeImageList: "+error);
            globalclass.sendErrorLog(tag, "getAllRecipeImageList", error);
        }
    }

    void addOrderStatusList() {

        mydatabase.truncateTable(mydatabase.orderStatus);
        ArrayList<orderStatusDetail> arrayList = new ArrayList<>();

        orderStatusDetail model = new orderStatusDetail();
        model.setOrderStatus("processing");
        model.setStepNumber(1);
        arrayList.add(model);

        model = new orderStatusDetail();
        model.setOrderStatus("accepted");
        model.setStepNumber(2);
        arrayList.add(model);

        model = new orderStatusDetail();
        model.setOrderStatus("out for delivery");
        model.setStepNumber(3);
        arrayList.add(model);

        model = new orderStatusDetail();
        model.setOrderStatus("delivered");
        model.setStepNumber(4);
        arrayList.add(model);

        model = new orderStatusDetail();
        model.setOrderStatus("cancelled");
        model.setStepNumber(5);
        arrayList.add(model);

        for(int i=0;i<arrayList.size();i++) {
            orderStatusDetail orderStatusModel = arrayList.get(i);
            mydatabase.addOrderStatus(orderStatusModel);
        }

        globalclass.log(tag,"All Order Status added!");
    }

    void handleIntent() {

        try {

            Intent intent = getIntent();
            userDetail model = intent.getParcelableExtra("model");
            globalclass.setStringData("id",model.getId());
            globalclass.setStringData("name",model.getName());
            globalclass.setStringData("emailid",model.getEmailid());
            globalclass.setStringData("mobilenumber",model.getMobilenumber());
            globalclass.setBooleanData("admin",model.getAdmin());

            if(intent.getStringExtra("loginType").equalsIgnoreCase("Admin")) {
                globalclass.setStringData("adminId",model.getId());
                globalclass.setStringData("adminName",model.getName());
                globalclass.setStringData("adminMobileNumber",model.getMobilenumber());
                globalclass.setStringData("adminEmailId",model.getEmailid());
                if(model.getRightsList() !=null && !model.getRightsList().isEmpty()) {
                    if(model.getId().equalsIgnoreCase(globalclass.checknull(globalclass.getStringData("adminId")))) {
                        for(int i=0;i<model.getRightsList().size();i++) {
                            navMenuDetail navMenuModel = model.getRightsList().get(i);
                            mydatabase.addNavMenu(navMenuModel);
                        }
                    }
                }
            }

            getData();
        }
        catch (Exception e) {

            showRetryView();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"handleIntent: "+error);
            globalclass.sendErrorLog(tag, "handleIntent", error);
        }
    }

    void showRetryView() {
        if(!globalclass.isInternetPresent()) {
            tvholdOnText.setText("Internet connection not found!");
        }
        else {
            tvholdOnText.setText("Something went wrong in setting up account, please try again!");
        }
        progress_bar.setVisibility(View.GONE);
        retrybt.setVisibility(View.VISIBLE);
    }

    void showNormalView() {
        tvholdOnText.setText(R.string.account_setup);
        progress_bar.setVisibility(View.VISIBLE);
        retrybt.setVisibility(View.GONE);
    }
}