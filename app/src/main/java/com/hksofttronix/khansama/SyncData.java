package com.hksofttronix.khansama;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hksofttronix.khansama.Models.addressDetail;
import com.hksofttronix.khansama.Models.allOrderItemDetail;
import com.hksofttronix.khansama.Models.cartDetail;
import com.hksofttronix.khansama.Models.cityDetail;
import com.hksofttronix.khansama.Models.favouriteDetail;
import com.hksofttronix.khansama.Models.ingredientsDetail;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.navMenuDetail;
import com.hksofttronix.khansama.Models.orderItemDetail;
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

public class SyncData extends Service {

    String tag = this.getClass().getSimpleName();
    Context context = SyncData.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;
    int stopServiceAfter = 10000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        createAndupdateNotification();

        if (!globalclass.isInternetPresent()) {
            globalclass.log(tag,"Stop due to no internet...");
            stopSelf();
            return;
        }

        globalclass.log(tag,"Started...");
        getData();
    }

    void init() {
        globalclass = Globalclass.getInstance(context);
        mydatabase = Mydatabase.getInstance(context);
        notificationManager = NotificationManagerCompat.from(context);
    }

    void getData() {

        if (globalclass.checknull(globalclass.getStringData("id")).equalsIgnoreCase("")) {
            getRecipeCategoryList();
//            getAllRecipeList();
//            getAllIngredientsList();
//            getAllRecipeImageList();
        }
        else {
            getUserDetail();
            getRecipeCategoryList();
//            getAllRecipeList();
//            getAllIngredientsList();
//            getAllRecipeImageList();
            getCartList();
            getFavouriteList();
            getCityList();
            getStateList();
            getAddressList();
            getPlaceOrderList();

            if(!globalclass.getStringData("adminId").equalsIgnoreCase("")) {
//                getInventoryList();
                getUnitList();
                getVendorList();
                getAllOrderList();
            }
        }

        globalclass.setLastSyncDataTime();
        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(Globalclass.OrderReceiver);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                stopSelf();
            }
        },stopServiceAfter);
    }

    void getUserDetail() {
        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing account detail")
                    .setContentText("Refreshing account detail, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference userColl = globalclass.firebaseInstance().collection(Globalclass.userColl);
            Query query = userColl
                    .whereEqualTo("mobilenumber",
                            globalclass.getStringData("mobilenumber"));
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {

                            for(DocumentSnapshot documents : task.getResult()) {
                                userDetail model = documents.toObject(userDetail.class);
                                globalclass.setStringData("id",model.getId());
                                globalclass.setStringData("name",model.getName());
                                globalclass.setStringData("emailid",model.getEmailid());
                                globalclass.setStringData("mobilenumber",model.getMobilenumber());

                                if(!globalclass.getStringData("adminId").equalsIgnoreCase("")) {

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
                            }
                        }
                        else {
                            String error = "No user detail found!";
                            globalclass.log(tag,"getUserDetail: "+error);
                            globalclass.sendErrorLog(tag, "getUserDetail", error);
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getUserDetail: "+error);
                        globalclass.sendErrorLog(tag, "getUserDetail", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getUserDetail: "+error);
            globalclass.sendErrorLog(tag, "getUserDetail", error);
        }
    }
    
    void getRecipeCategoryList() {

        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing recipe's category data")
                    .setContentText("Refreshing recipe's category list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference recipeCategoryColl = globalclass.firebaseInstance().collection(Globalclass.recipeCategoryColl);
            recipeCategoryColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                            mydatabase.truncateTable(mydatabase.recipeCategory);
                            globalclass.log(tag,"No Recipe Category exist!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getRecipeCategoryList: "+error);
                        globalclass.sendErrorLog(tag, "getRecipeCategoryList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getRecipeCategoryList: "+error);
            globalclass.sendErrorLog(tag, "getRecipeCategoryList", error);
        }
    }

    void getAllRecipeList() {

        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing recipe data")
                    .setContentText("Refreshing recipe list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference recipeColl = globalclass.firebaseInstance().collection(Globalclass.recipeColl);
            recipeColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                            mydatabase.truncateTable(mydatabase.recipe);
                            mydatabase.truncateTable(mydatabase.recipeInstruction);
                            globalclass.log(tag,"All Recipe is empty!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getAllRecipeList: "+error);
                        globalclass.sendErrorLog(tag, "getAllRecipeList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getAllRecipeList: "+error);
            globalclass.sendErrorLog(tag, "getAllRecipeList", error);
        }
    }

    void getAllIngredientsList() {

        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing recipe's ingredients data")
                    .setContentText("Refreshing recipe's ingredients list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference ingredientsColl = globalclass.firebaseInstance().collection(Globalclass.ingredientsColl);
            ingredientsColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.recipeIngredients);
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
                            mydatabase.truncateTable(mydatabase.recipeIngredients);
                            globalclass.log(tag,"All Ingredients is empty!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getAllIngredientsList: "+error);
                        globalclass.sendErrorLog(tag, "getAllIngredientsList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getAllIngredientsList: "+error);
            globalclass.sendErrorLog(tag, "getAllIngredientsList", error);
        }
    }

    void getAllRecipeImageList() {

        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing recipe's images data")
                    .setContentText("Refreshing recipe's images list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference recipeImagesColl = globalclass.firebaseInstance().collection(Globalclass.recipeImagesColl);
            recipeImagesColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                            mydatabase.truncateTable(mydatabase.recipeImages);
                            globalclass.log(tag,"All Recipe Image is empty!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getAllRecipeImageList: "+error);
                        globalclass.sendErrorLog(tag, "getAllRecipeImageList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getAllRecipeImageList: "+error);
            globalclass.sendErrorLog(tag, "getAllRecipeImageList", error);
        }
    }

    void getCartList() {

        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing cart data")
                    .setContentText("Refreshing cart list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference cartColl = globalclass.firebaseInstance().collection(Globalclass.cartColl);
            Query query = cartColl
                    .whereEqualTo("userId",globalclass.getStringData("id"));

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                            mydatabase.truncateTable(mydatabase.cart);
                            globalclass.log(tag,"Cart is empty!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getCartList: "+error);
                        globalclass.sendErrorLog(tag, "getCartList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getCartList: "+error);
            globalclass.sendErrorLog(tag, "getCartList", error);
        }
    }

    void getFavouriteList() {

        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing favourite data")
                    .setContentText("Refreshing favourite list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference favouriteColl = globalclass.firebaseInstance().collection(Globalclass.favouriteColl);
            Query query = favouriteColl
                    .whereEqualTo("userId",globalclass.getStringData("id"));

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                            mydatabase.truncateTable(mydatabase.favourite);
                            globalclass.log(tag,"Favourite is empty!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getFavouriteList: "+error);
                        globalclass.sendErrorLog(tag, "getFavouriteList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getFavouriteList: "+error);
            globalclass.sendErrorLog(tag, "getFavouriteList", error);
        }
    }

    void getCityList() {

        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing city data")
                    .setContentText("Refreshing city list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference cityColl = globalclass.firebaseInstance().collection(Globalclass.cityColl);
            cityColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                            mydatabase.truncateTable(mydatabase.city);
                            globalclass.log(tag,"City is empty!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getCityList: "+error);
                        globalclass.sendErrorLog(tag, "getCityList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getCityList: "+error);
            globalclass.sendErrorLog(tag, "getCityList", error);
        }
    }

    void getStateList() {

        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing state data")
                    .setContentText("Refreshing state list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference stateColl = globalclass.firebaseInstance().collection(Globalclass.stateColl);
            stateColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                            mydatabase.truncateTable(mydatabase.state);
                            globalclass.log(tag,"State is empty!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getStateList: "+error);
                        globalclass.sendErrorLog(tag, "getStateList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getStateList: "+error);
            globalclass.sendErrorLog(tag, "getStateList", error);
        }
    }

    void getAddressList() {

        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing address data")
                    .setContentText("Refreshing address list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference addressColl = globalclass.firebaseInstance().collection(Globalclass.addressColl);
            Query query = addressColl
                    .whereEqualTo("userId",globalclass.getStringData("id"));
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                            mydatabase.truncateTable(mydatabase.address);
                            globalclass.log(tag,"Address is empty!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getAddressList: "+error);
                        globalclass.sendErrorLog(tag, "getAddressList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getAddressList: "+error);
            globalclass.sendErrorLog(tag, "getAddressList", error);
        }
    }

    void getPlaceOrderList() {

        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing order data")
                    .setContentText("Refreshing order list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference placeOrderColl = globalclass.firebaseInstance().collection(Globalclass.placeOrderColl);
            Query query = placeOrderColl
                    .whereEqualTo("userId",globalclass.getStringData("id"));
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                            mydatabase.truncateTable(mydatabase.placeOrder);
                            globalclass.log(tag,"Place order is empty!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getPlaceOrderList: "+error);
                        globalclass.sendErrorLog(tag, "getPlaceOrderList", error);
                    }
                }
            });
        }
        catch (Exception e) {

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
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                        }
                        else {
                            globalclass.log(tag,"Order item is empty!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getOrderItemList: "+error);
                        globalclass.sendErrorLog(tag, "getOrderItemList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getOrderItemList: "+error);
            globalclass.sendErrorLog(tag, "getOrderItemList", error);
        }
    }

    //todo Admin Data
    void getInventoryList() {

        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing Inventory data")
                    .setContentText("Refreshing Inventory list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
            inventoryColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {

                            mydatabase.truncateTable(mydatabase.inventory);
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {

                                inventoryDetail model = documentSnapshot.toObject(inventoryDetail.class);
                                model.setisSelected(false);
                                mydatabase.addInventory(model);
                            }

                            globalclass.log(tag,"Inventory data added!");
                        }
                        else {
                            mydatabase.truncateTable(mydatabase.inventory);
                            globalclass.log(tag,"No inventory found!");
                        }
                    }
                    else {
                        String error = task.getException().toString();
                        globalclass.log(tag, "getInventoryList: " + error);
                        globalclass.sendErrorLog(tag, "getInventoryList", error);
                    }
                }
            });

        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getInventoryList: " + error);
            globalclass.sendErrorLog(tag, "getInventoryList", error);
        }
    }

    void getUnitList() {
        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing Unit data")
                    .setContentText("Refreshing Unit list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference collectionReference = globalclass.firebaseInstance().collection(Globalclass.unitColl);
            collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.unit);
                            for(DocumentSnapshot documents : task.getResult()) {
                                unitDetail model = documents.toObject(unitDetail.class);
                                mydatabase.addUnit(model);
                            }

                            globalclass.log(tag,"Unit data added!");
                        }
                        else {
                            mydatabase.truncateTable(mydatabase.unit);
                            globalclass.log(tag,"No unit details found!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getUnitList: "+error);
                        globalclass.sendErrorLog(tag, "getUnitList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getUnitList: "+error);
            globalclass.sendErrorLog(tag, "getUnitList", error);
        }
    }

    void getVendorList() {

        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing Vendor data")
                    .setContentText("Refreshing Vendor list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference vendorColl = globalclass.firebaseInstance().collection(Globalclass.vendorColl);
            vendorColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.vendor);
                            for(DocumentSnapshot documents : task.getResult()) {
                                vendorDetail model = documents.toObject(vendorDetail.class);
                                mydatabase.addVendor(model);
                            }

                            globalclass.log(tag,"Vendor data added!");
                        }
                        else {
                            mydatabase.truncateTable(mydatabase.vendor);
                            globalclass.log(tag,"No Vendor exist!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getVendorList: "+error);
                        globalclass.sendErrorLog(tag, "getVendorList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getVendorList: "+error);
            globalclass.sendErrorLog(tag, "getVendorList", error);
        }
    }

    void getAllOrderList() {

        try {
            notification
                    .setOngoing(true)
                    .setContentTitle("Syncing order data")
                    .setContentText("Refreshing order list, please wait...");
            notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());

            CollectionReference placeOrderColl = globalclass.firebaseInstance().collection(Globalclass.placeOrderColl);
            Query query = placeOrderColl
                    .whereEqualTo("userId",globalclass.getStringData("id"));
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            mydatabase.truncateTable(mydatabase.allOrder);
                            for(DocumentSnapshot documents : task.getResult()) {
                                orderSummaryDetail model = documents.toObject(orderSummaryDetail.class);
                                getAllOrderItemList(model);
                            }

                            globalclass.log(tag,"Place order data added!");
                        }
                        else {
                            mydatabase.truncateTable(mydatabase.allOrder);
                            globalclass.log(tag,"Place order is empty!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getPlaceOrderList: "+error);
                        globalclass.sendErrorLog(tag, "getPlaceOrderList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getPlaceOrderList: "+error);
            globalclass.sendErrorLog(tag, "getPlaceOrderList", error);
        }
    }

    void getAllOrderItemList(orderSummaryDetail model) {

        try {
            CollectionReference orderItemColl = globalclass.firebaseInstance().collection(Globalclass.orderItemColl);
            Query query = orderItemColl
                    .whereEqualTo("orderId",model.getOrderId());
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
                                allOrderItemModel.setOrderStatus(model.getOrderStatus());
                                allOrderItemModel.setOrderStep(model.getOrderStep());
                                allOrderItemModel.setRecipeCharges(model.getRecipeCharges());
                                allOrderItemModel.setPackagingCharges(model.getPackagingCharges());
                                allOrderItemModel.setTotal(model.getTotal());
                                allOrderItemModel.setItem(model.getItem());
                                allOrderItemModel.setOrderDateTime(model.getOrderDateTime());
                                allOrderItemModel.setDeliveryDateTime(model.getDeliveryDateTime());
                                allOrderItemModel.setAddressId(model.getAddressId());
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
                                allOrderItemModel.setRecipeImageId(orderItemModel.getRecipeImageId());
                                allOrderItemModel.setRecipeImageUrl(orderItemModel.getRecipeImageUrl());
                                allOrderItemModel.setPrice(orderItemModel.getPrice());
                                allOrderItemModel.setQuantity(orderItemModel.getQuantity());

                                mydatabase.addAllOrder(allOrderItemModel);
                            }
                        }
                        else {
                            globalclass.log(tag,"Order item is empty!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getOrderItemList: "+error);
                        globalclass.sendErrorLog(tag, "getOrderItemList", error);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getOrderItemList: "+error);
            globalclass.sendErrorLog(tag, "getOrderItemList", error);
        }
    }

    void createAndupdateNotification() {
        notification = new NotificationCompat.Builder(this, Globalclass.SyncDataChannelId)
                .setSmallIcon(R.drawable.ic_appicon)
                .setContentTitle("Syncing data")
                .setContentText("Please wait...")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setProgress(0,0,true);


        startForeground(Globalclass.SyncDataNotificationId, notification.build());
        notificationManager.notify(Globalclass.SyncDataNotificationId,notification.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        globalclass.setAlarm(context, Globalclass.SyncData_alarmMin, Globalclass.SyncData_ACTION, Globalclass.SyncData_requestID);
        globalclass.log(tag,"Destroy...");
    }
}
