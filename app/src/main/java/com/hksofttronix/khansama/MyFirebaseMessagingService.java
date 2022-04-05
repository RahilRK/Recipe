package com.hksofttronix.khansama;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hksofttronix.khansama.Admin.AdminOrderDetail.AdminOrderDetail;
import com.hksofttronix.khansama.Models.allOrderItemDetail;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.orderItemDetail;
import com.hksofttronix.khansama.Models.orderSummaryDetail;
import com.hksofttronix.khansama.Models.placeOrderDetail;
import com.hksofttronix.khansama.Models.stockDetail;
import com.hksofttronix.khansama.OrderDetail.OrderDetail;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String tag = this.getClass().getSimpleName();
    Context context = MyFirebaseMessagingService.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification = null;

    String parameter = "";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        init();
        globalclass.log(tag, "onMessageReceived");

        if (remoteMessage.getFrom()!=null && remoteMessage.getFrom().contains("topics")) {
            String topicName = remoteMessage.getFrom().replace("/topics/", "");

            globalclass.log(tag,"From : - " + topicName);
        }
            if (remoteMessage.getData().size() > 0) {
            globalclass.log(tag, "Message data payload: " + remoteMessage.getData().toString());
            handleRemoteMessage(remoteMessage);
        }
    }

    void init() {
        globalclass = Globalclass.getInstance(context);
        mydatabase = Mydatabase.getInstance(context);
    }

    void handleRemoteMessage(RemoteMessage remoteMessage) {

        try {
            Map<String, String> map = remoteMessage.getData();

            if(map != null) {
                JSONObject jsonObject = new JSONObject(map);
                parameter = jsonObject.toString();
            }
            
            String action = map.get("action");
            String mfor = map.get("for");
            String userMobileNumber = map.get("userMobileNumber");
            String userId = map.get("userId");
            String orderId = map.get("orderId");
            String orderStep = map.get("orderStep");

            long sendTime = Long.parseLong(map.get("sendTime"));
            if(!DateUtils.isToday(sendTime)) {
                return;
            }

            if(action.equalsIgnoreCase("placeOrder")) {

                if(mfor.equalsIgnoreCase("user")) {

                    if(globalclass.checknull(globalclass.getStringData("id")).equalsIgnoreCase("") &&
                            !globalclass.checknull(userMobileNumber).equalsIgnoreCase(globalclass.getStringData("mobilenumber"))) {
                        return;
                    }

                    Intent intent = new Intent(context, OrderDetail.class);
                    intent.putExtra("orderId",map.get("orderId"));

                    sendNotification(map,intent);
                    getPlaceOrderDetail(orderId);
                }
                else if(mfor.equalsIgnoreCase("admin")) {

                    if(globalclass.checknull(globalclass.getStringData("adminId")).equalsIgnoreCase("")) {
                        return;
                    }

                    Intent intent = new Intent(context, AdminOrderDetail.class);
                    intent.putExtra("orderId",map.get("orderId"));

                    sendNotification(map, intent);
                    getPlaceOrderDetail(orderId);

                    if(Integer.parseInt(orderStep) == 1 ||
                            Integer.parseInt(orderStep) == 5) {
//                        getStockList(orderId);
                    }
                }
            }
            if(action.equalsIgnoreCase("orderUpdate")) {

                //todo silent notification just to update data locally

                if(mfor.equalsIgnoreCase("admin")) {

                    if(globalclass.checknull(globalclass.getStringData("adminId")).equalsIgnoreCase("")) {
                        return;
                    }

                    getPlaceOrderDetail(orderId);
                }
            }
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"handleRemoteMessage: "+error);
            globalclass.sendNotificationErrorLog(tag,"handleRemoteMessage: ",error, parameter);
        }
    }

    void getPlaceOrderDetail(String orderId) {

        try {
            CollectionReference placeOrderColl = globalclass.firebaseInstance().collection(Globalclass.placeOrderColl);
            Query query = placeOrderColl
                    .whereEqualTo("orderId",orderId);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {

                            mydatabase.deleteData(mydatabase.placeOrder,"orderId",orderId);
                            mydatabase.deleteData(mydatabase.allOrder, "orderId", orderId);

                            for(DocumentSnapshot documents : task.getResult()) {
                                orderSummaryDetail model = documents.toObject(orderSummaryDetail.class);
                                getOrderItemList(orderId,model);
                            }
                        }
                    }
                    else {
                        mydatabase.deleteData(mydatabase.placeOrder,"orderId",orderId);
                        mydatabase.deleteData(mydatabase.allOrder, "orderId", orderId);

                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getPlaceOrderDetail: "+error);
                        globalclass.sendNotificationErrorLog(tag,"getPlaceOrderDetail: ",error, parameter);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getPlaceOrderDetail: "+error);
            globalclass.sendNotificationErrorLog(tag,"getPlaceOrderDetail: ",error, parameter);
        }
    }

    void getOrderItemList(String orderId, orderSummaryDetail model) {

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

                                if(!globalclass.checknull(globalclass.getStringData("id")).equalsIgnoreCase("") &&
                                        globalclass.checknull(model.getMobileNumber()).equalsIgnoreCase(globalclass.getStringData("mobilenumber"))) {

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

                                if (!globalclass.checknull(globalclass.getStringData("adminId")).equalsIgnoreCase("")) {

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

                            Intent intent = new Intent(Globalclass.OrderReceiver);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    }
                    else {

                        mydatabase.deleteData(mydatabase.placeOrder,"orderId",orderId);
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getOrderItemList: "+error);
                        globalclass.sendNotificationErrorLog(tag,"getOrderItemList: ",error, parameter);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getOrderItemList: "+error);
            globalclass.sendNotificationErrorLog(tag,"getOrderItemList: ",error, parameter);
        }
    }

    void getStockList(String orderId) {

        try {
            ArrayList<String> inventoryIds = new ArrayList<>();
            inventoryIds.clear();

            CollectionReference stockColl = globalclass.firebaseInstance().collection(Globalclass.stockColl);
            Query query = stockColl
                    .whereEqualTo("inOutId",orderId);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                stockDetail model = documents.toObject(stockDetail.class);
                                inventoryIds.add(model.getInventoryId());
                            }

                            if(inventoryIds !=null && !inventoryIds.isEmpty()) {
                                updateInventoryList(inventoryIds);
                            }
                        }
                        else {
                            String error = "inOutId/orderId not available in stockColl!";
                            globalclass.log(tag,"getStockList: "+error);
                            globalclass.sendNotificationErrorLog(tag,"getStockList: ",error, parameter);
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getStockList: "+error);
                        globalclass.sendNotificationErrorLog(tag,"getStockList: ",error, parameter);
                    }
                }
            });
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getStockList: "+error);
            globalclass.sendNotificationErrorLog(tag,"getStockList: ",error, parameter);
        }
    }

    void updateInventoryList(ArrayList<String> inventoryIds) {

        try {
            CollectionReference inventoryColl = globalclass.firebaseInstance().collection(Globalclass.inventoryColl);
            Query query = inventoryColl.whereIn("inventoryId",inventoryIds);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {

                            for (DocumentSnapshot documentSnapshot : task.getResult()) {

                                inventoryDetail model = documentSnapshot.toObject(inventoryDetail.class);
                                model.setisSelected(false);
                                mydatabase.addInventory(model);
                            }

                            Intent intent = new Intent(Globalclass.OrderReceiver);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                    }
                    else {
                        String error = task.getException().toString();
                        globalclass.log(tag, "updateInventoryList: " + error);
                        globalclass.sendNotificationErrorLog(tag,"updateInventoryList: ",error, parameter);
                    }
                }
            });
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "updateInventoryList: " + error);
            globalclass.sendNotificationErrorLog(tag,"updateInventoryList: ",error, parameter);
        }
    }

    void sendNotification(Map<String, String> map, Intent intent) {

        try {
            if(map == null) {

                String error = "sendNotification map is null";
                globalclass.log(tag,"sendNotification: "+error);
                globalclass.sendNotificationErrorLog(tag,"sendNotification: ",error, parameter);
                return;
            }

            notificationManager = NotificationManagerCompat.from(context);
            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_appicon);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            notification = new NotificationCompat.Builder(context, Globalclass.appNotificationChannelId)
                    .setSmallIcon(R.drawable.ic_appicon)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(map.get("title"))
                    .setContentText(map.get("text"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                    .setLights(Color.BLUE, 3000, 3000)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(map.get("text"))
                            .setBigContentTitle(map.get("title"))
                            .setSummaryText(map.get("text")));

            long notifId = Long.parseLong(map.get("sendTime"));
            notificationManager.notify((int)notifId,notification.build());
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"sendNotification: "+error);
            globalclass.sendNotificationErrorLog(tag,"sendNotification: ",error, parameter);
        }
    }
}
