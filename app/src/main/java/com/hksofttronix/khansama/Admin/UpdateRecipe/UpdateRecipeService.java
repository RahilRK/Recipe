package com.hksofttronix.khansama.Admin.UpdateRecipe;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hksofttronix.khansama.Models.recipeDetail;
import com.hksofttronix.khansama.Models.recipeInstructionDetail;
import com.hksofttronix.khansama.Models.recipePhotoDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Splash;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class UpdateRecipeService extends Service {

    String tag = this.getClass().getSimpleName();
    Context context = UpdateRecipeService.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;

    boolean getTotalRecipeImage = true;
    int totalRecipeImages = 0;
    int currentUploadingRecipeImages = 0;

    ArrayList<recipePhotoDetail> recipePhotoDetailsList = new ArrayList<>();

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

        if (globalclass.checknull(globalclass.getStringData("adminId")).equalsIgnoreCase("")) {
            globalclass.log(tag,"Stop due to admin not logged in...");
            stopSelf();
            return;
        }

        getData();
        globalclass.log(tag, "Started...");
    }

    void init() {
        globalclass = Globalclass.getInstance(context);
        mydatabase = Mydatabase.getInstance(context);
        notificationManager = NotificationManagerCompat.from(context);
    }

    void getData() {

        Cursor cursor = null;
        try {
//            String selectQuery = "select * from "+mydatabase.addRecipe +"\n" +
//                    "WHERE localid = (select MIN(localid) as localid from "+mydatabase.addRecipe+" WHERE actions ='Update')";

            String selectQuery = "select * from "+mydatabase.uploadRecipe +"\n" +
                    "WHERE localid = (select MIN(localid) as localid from "+mydatabase.uploadRecipe+" WHERE actions ='Update')";

            SQLiteDatabase db = mydatabase.getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.getCount() > 0) {

                if (!globalclass.isInternetPresent()) {
                    showFailedNotif("Some Recipe details are pending to make changes", "Please turn ON internet connection to change details in recipe!");
                    globalclass.log(tag,"No internet connection found, service stop!");
                    stopSelf();
                    return;
                }

                globalclass.log(tag, "Changing pending recipe details!");
                if (cursor.moveToFirst()) {
                    do {
                        String localid = cursor.getString(cursor.getColumnIndex("localid"));
                        String recipeName = cursor.getString(cursor.getColumnIndex("recipeName"));
                        globalclass.log(tag + ": getData", "localid: " + localid + ",recipeName: " + recipeName);
                        getRecipePhotos(recipeName);
                    }
                    while (cursor.moveToNext());
                }
            }
            else {
                globalclass.log(tag, "Every recipe details is changed!");
                stopSelf();
            }
        }
        catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getData: " + error);
            globalclass.sendErrorLog(tag,"getData: ",error);
            showFailedNotif("Recipe details uploading failed", "Failed to make changes in recipe details, tap here to try again!");
            stopSelf();
        }
        finally {
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    void getRecipePhotos(String recipename) {

        Cursor cursor = null;
        try {
            String query = "select * from "+mydatabase.addRecipePhotos +"\n" +
                    "WHERE localid = (select MIN(localid) as localid from "+mydatabase.addRecipePhotos+" WHERE\n" +
                    "url IS NULL or url = '' AND newImages = 'true')";

            cursor = mydatabase.getReadableDatabase().rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.getCount() > 0) {
                globalclass.log(tag, "Uploading pending recipe photo!");
                while (cursor.moveToNext()) {
                    String localid = cursor.getString(cursor.getColumnIndex("localid"));
                    String recipeName = cursor.getString(cursor.getColumnIndex("recipeName"));
                    String imageName = cursor.getString(cursor.getColumnIndex("imageName"));
                    String filepath = cursor.getString(cursor.getColumnIndex("filepath"));

                    globalclass.log(tag + ": getRecipePhotos", "localid: " + localid + ",recipeName: " + recipeName + ", filepath: " + filepath);

                    if (new File(filepath).exists()) {
                        recipePhotoDetail model = new recipePhotoDetail();
                        model.setLocalid(localid);
                        model.setRecipeName(recipeName);
                        model.setImageName(imageName);
                        model.setFilepath(filepath);

                        if (getTotalRecipeImage) {
                            totalRecipeImages = mydatabase.checkPerRecipePhotoUploaded(model.getRecipeName());
                        }

                        currentUploadingRecipeImages++;

                        uploadToFirebaseStorage(model);
                    } else {
                        mydatabase.deleteData(mydatabase.addRecipePhotos, "localid", localid);
                    }
                }
            } else {
//               getData();
                getRecipeDetails(recipename,false);
            }

        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipePhotos: " + error);
            globalclass.sendErrorLog(tag,"getRecipePhotos: ",error);
            showFailedNotif("Recipe details uploading failed", "Failed to make changes in recipe details, tap here to try again!");
            stopSelf();
        } finally {
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    void uploadToFirebaseStorage(recipePhotoDetail model) {

        String parameter = "";

        Gson gson = new Gson();
        if(model != null) {
            parameter = gson.toJson(model);
        }
        globalclass.log(tag,"parameter: "+parameter);

        String finalParameter = parameter;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("Recipe Images")
//                .child(model.getRecipeName())
                .child(model.getImageName());
        storageReference.putFile(Uri.fromFile(new File(model.getFilepath())))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                String url = uri.toString();
                                model.setUrl(url);
                                mydatabase.uploadRecipePhoto(model);
                                globalclass.log(tag, "url: " + url);

                                if (mydatabase.checkPerRecipePhotoUploaded(model.getRecipeName()) == 0) {

                                    currentUploadingRecipeImages = 0;
                                    getTotalRecipeImage = true;

                                    globalclass.log(tag, model.getRecipeName() + " photos uploaded!");
                                    getRecipeDetails(model.getRecipeName(), true);
                                } else {
                                    getTotalRecipeImage = false;
                                }

                                getRecipePhotos(model.getRecipeName());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                String error = Log.getStackTraceString(e);
                                globalclass.log(tag, "uploadToFirebaseStorage getDownloadUrl() onFailure" + error);
                                globalclass.sendResponseErrorLog(tag,"uploadToFirebaseStorage getDownloadUrl() onFailure",error, finalParameter);
                                showFailedNotif("Recipe details uploading failed","Failed to make changes in recipe details, tap here to try again!");
                                stopSelf();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        String error = Log.getStackTraceString(e);
                        globalclass.log(tag, "uploadToFirebaseStorage onFailure" + error);
                        globalclass.sendResponseErrorLog(tag,"uploadToFirebaseStorage onFailure",error, finalParameter);
                        showFailedNotif("Recipe details uploading failed","Failed to make changes in recipe details, tap here to try again!");
                        stopSelf();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress_percentage = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        globalclass.log(tag + ": uploadToFirebaseStorage_OnProgress", String.valueOf((int) progress_percentage) + "% ");

                        //todo update notification UI
                        globalclass.log(tag, "Uploading " + currentUploadingRecipeImages + "/" + totalRecipeImages);

                        notification.setProgress(100, (int) progress_percentage, false)
                                .setOngoing(true)
                                .setContentTitle("Uploading " + model.getRecipeName() + " images")
                                .setContentText("Image " + currentUploadingRecipeImages + "/" + totalRecipeImages + ": Progress " + String.valueOf((int) progress_percentage) + " %");
//                        .setStyle(new NotificationCompat.BigTextStyle()
//                                .bigText("Progress "+String.valueOf((int)progress_percentage)+" %")
//                                .setBigContentTitle("Uploading "+model.getRecipeName()+" images")
//                                .setSummaryText("Uploading "+model.getRecipeName()+" images"));

                        notificationManager.notify(Globalclass.updateRecipeNotificationId, notification.build());
                    }
                });
    }

    void getRecipeDetails(String recipeName, boolean recipeImageAvailable) {

        try {
            notification.setProgress(0, 0, true)
                    .setOngoing(true)
                    .setContentTitle("Changing " + recipeName + " details")
                    .setContentText("Changing " +recipeName+ " ingredients list");
            notificationManager.notify(Globalclass.updateRecipeNotificationId, notification.build());

            recipeDetail recipeDetailsModel = mydatabase.getUploadingRecipeDetail(recipeName);
            if(recipeDetailsModel == null) {
                String error = "recipeDetailsModel is null";
                globalclass.log(tag, "getRecipeDetails: " + error);
                globalclass.sendErrorLog(tag,"getRecipeDetails: ",error);
                showFailedNotif("Recipe details uploading failed","Failed to make changes in recipe details, tap here to try again!");
                stopSelf();
                return;
            }

            ArrayList<recipePhotoDetail> recipeImageList = mydatabase.getUploadingRecipeImages(recipeName);
            if(recipeImageList.isEmpty() && recipeImageAvailable) {

                String error = "recipeImageList is Empty";
                globalclass.log(tag, "getRecipeDetails: " + error);
                globalclass.sendErrorLog(tag,"getRecipeDetails: ",error);
                showFailedNotif("Recipe details uploading failed","Failed to make changes in recipe details, tap here to try again!");
                stopSelf();
                return;
            }

            ArrayList<String> recipeInstructionList = mydatabase.getUploadingRecipeInstructions(recipeName);
            if(recipeInstructionList.isEmpty()) {

                String error = "recipeInstructionList is Empty";
                globalclass.log(tag, "getRecipeDetails: " + error);
                globalclass.sendErrorLog(tag,"getRecipeDetails: ",error);
                showFailedNotif("Recipe details uploading failed","Failed to make changes in recipe details, tap here to try again!");
                stopSelf();
                return;
            }

            recipeDetailsModel.setAdminId(globalclass.getStringData("adminId"));
            recipeDetailsModel.setAdminName(globalclass.getStringData("name"));
            recipeDetailsModel.setRecipeInstructions(recipeInstructionList);

            updateToFireBaseDB(recipeDetailsModel,recipeImageList,recipeImageAvailable);
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeDetails: " + error);
            globalclass.sendErrorLog(tag,"getRecipeDetails: ",error);
            showFailedNotif("Recipe details uploading failed","Failed to make changes in recipe details, tap here to try again!");
            stopSelf();
        }
    }

    void updateToFireBaseDB(recipeDetail model,
                            ArrayList<recipePhotoDetail> recipeImageList,
                            boolean recipeImageAvailable) {

        String parameter = "";

        try {
            WriteBatch batch = globalclass.firebaseInstance().batch();

            CollectionReference recipeColl = globalclass.firebaseInstance().collection(Globalclass.recipeColl);
            String recipeId = model.getRecipeId();

            model.setRecipeId(recipeId);

            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(model);
            Map<String,Object> map = new Gson().fromJson(json, Map.class);

            DocumentReference recipeDocReference = recipeColl.document(recipeId);
            batch.update(recipeDocReference,map);

            if(recipeImageAvailable) {
                for(int i=0;i<recipeImageList.size();i++) {
                    CollectionReference recipeImagesColl = globalclass.firebaseInstance().collection(Globalclass.recipeImagesColl);
                    String recipeImageId = recipeImagesColl.document().getId();

                    recipeImageList.get(i).setRecipeImageId(recipeImageId);
                    recipeImageList.get(i).setRecipeId(recipeId);
                    recipePhotoDetail recipePhotoModel = recipeImageList.get(i);

                    recipePhotoDetailsList.add(recipePhotoModel);
                    DocumentReference recipeImagesDocReference = recipeImagesColl.document(recipeImageId);
                    batch.set(recipeImagesDocReference, recipePhotoModel);
                }
            }

            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        showSuccessNotif(Integer.parseInt(model.getLocalid()),
                                "Recipe detail changed successfully",
                                model.getRecipeName().toUpperCase() + " detail changed successfully!");

                        mydatabase.addAllRecipe(model);
                        for(int i=0;i<recipePhotoDetailsList.size();i++) {
                            recipePhotoDetail model = recipePhotoDetailsList.get(i);
                            mydatabase.addAllRecipeImages(model);
                        }

                        mydatabase.deleteData(mydatabase.allRecipeInstructions,"recipeId",model.getRecipeId());
                        ArrayList<String> instructionList = model.getRecipeInstructions();
                        for(int i=0;i<instructionList.size();i++) {
                            recipeInstructionDetail ingredientsDetailModel = new recipeInstructionDetail();
                            ingredientsDetailModel.setRecipeId(model.getRecipeId());
                            ingredientsDetailModel.setInstruction(instructionList.get(i));
                            ingredientsDetailModel.setStepNumber(i+1);
                            mydatabase.addAllRecipeInstructions(ingredientsDetailModel);
                        }

                        mydatabase.deleteData(mydatabase.uploadRecipe,"localid",model.getLocalid());
                        mydatabase.deleteData(mydatabase.addIngredients,"recipeName",model.getRecipeName());
                        mydatabase.deleteData(mydatabase.addRecipePhotos,"recipeName",model.getRecipeName());
                        mydatabase.deleteData(mydatabase.addRecipeInstructions,"recipeName",model.getRecipeName());

                        getData();

                        Intent intent = new Intent(Globalclass.AddRecipeReceiver);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                    } else {
                        String error = task.getException().toString();
                        globalclass.log(tag,"updateToFireBaseDB: "+error);
                        globalclass.sendResponseErrorLog(tag,"updateToFireBaseDB: ",error, finalParameter);
                        showFailedNotif("Recipe details uploading failed","Failed to make changes in recipe details, tap here to try again!");
                        stopSelf();
                    }
                }
            });
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"updateToFireBaseDB: "+error);
            globalclass.sendResponseErrorLog(tag,"updateToFireBaseDB: ",error, parameter);
            showFailedNotif("Recipe details uploading failed","Failed to make changes in recipe details, tap here to try again!");
            stopSelf();
        }
    }

    void createAndupdateNotification() {
        notification = new NotificationCompat.Builder(this, Globalclass.updateRecipeChannelId)
                .setSmallIcon(R.drawable.ic_appicon)
                .setContentTitle("Changing recipe details")
                .setContentText("0 %")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setProgress(100, 0, false);


        startForeground(Globalclass.updateRecipeNotificationId, notification.build());
        notificationManager.notify(Globalclass.updateRecipeNotificationId, notification.build());
    }

    void showSuccessNotif(int notifiId, String title, String text) {
        Intent myIntent = new Intent(this, Splash.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, myIntent, 0);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Notification notification = new NotificationCompat.Builder(this, Globalclass.updateRecipeChannelId)
                .setSmallIcon(R.drawable.ic_appicon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(uri)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setLights(Color.RED, 3000, 3000)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(notifiId, notification);
    }

    void showFailedNotif(String title, String text) {
        Intent myIntent = new Intent(this, Splash.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, myIntent, 0);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Notification notification = new NotificationCompat.Builder(this, Globalclass.updateRecipeChannelId)
                .setSmallIcon(R.drawable.ic_appicon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(uri)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setLights(Color.RED, 3000, 3000)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        notificationManager.notify(Globalclass.updateRecipeFailedNotifiID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        globalclass.setAlarm(context, Globalclass.updateRecipe_alarmMin, Globalclass.updateRecipe_ACTION, Globalclass.updateRecipe_requestID);
        globalclass.log(tag, "Destroy...");
    }
}
