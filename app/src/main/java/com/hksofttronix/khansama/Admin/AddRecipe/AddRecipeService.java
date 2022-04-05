package com.hksofttronix.khansama.Admin.AddRecipe;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;
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
import com.hksofttronix.khansama.Models.ingredientsDetail;
import com.hksofttronix.khansama.Models.recipeDetail;
import com.hksofttronix.khansama.Models.recipeInstructionDetail;
import com.hksofttronix.khansama.Models.recipePhotoDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Splash;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;

import java.io.File;
import java.util.ArrayList;

public class AddRecipeService extends Service {

    String tag = this.getClass().getSimpleName();
    Context context = AddRecipeService.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder notification;

    boolean getTotalRecipeImage = true;
    int totalRecipeImages = 0;
    int currentUploadingRecipeImages = 0;

    ArrayList<ingredientsDetail> ingredientsDetailList = new ArrayList<>();
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

        getRecipePhotos();
        globalclass.log(tag, "Started...");
    }

    void init() {
        globalclass = Globalclass.getInstance(context);
        mydatabase = Mydatabase.getInstance(context);
        notificationManager = NotificationManagerCompat.from(context);
    }

    void getRecipePhotos() {

        Cursor cursor = null;
        try {
            String query = "select * from "+mydatabase.addRecipePhotos +"\n" +
                    "WHERE localid = (select MIN(localid) as localid from "+mydatabase.addRecipePhotos+" WHERE\n" +
                    "url IS NULL or url = '')";

            cursor = mydatabase.getReadableDatabase().rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.getCount() > 0) {

                if (!globalclass.isInternetPresent()) {
                    showFailedNotif("New Recipe is pending to upload", "Please turn ON internet connection to add new pending recipe!");
                    globalclass.log(tag,"No internet connection found, service stop!");
                    stopSelf();
                    return;
                }

                globalclass.log(tag, "Adding pending recipe details!");
                while (cursor.moveToNext()) {
                    String localid = cursor.getString(cursor.getColumnIndex("localid"));
                    String recipeName = cursor.getString(cursor.getColumnIndex("recipeName"));
                    String imageName = cursor.getString(cursor.getColumnIndex("imageName"));
                    String filepath = cursor.getString(cursor.getColumnIndex("filepath"));
                    boolean firstImage = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("firstImage")));

                    globalclass.log(tag + ": getData", "localid: " + localid + ",recipeName: " + recipeName + ", filepath: " + filepath);

                    if (new File(filepath).exists()) {
                        recipePhotoDetail model = new recipePhotoDetail();
                        model.setLocalid(localid);
                        model.setRecipeName(recipeName);
                        model.setImageName(imageName);
                        model.setFilepath(filepath);
                        model.setFirstImage(firstImage);

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
                globalclass.log(tag, "Every recipe is added!");
                stopSelf();
            }

        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipePhotos: " + error);
            globalclass.sendErrorLog(tag,"getRecipePhotos: ",error);
            showFailedNotif("Recipe details uploading failed", "Failed to add recipe details, tap here to try again!");
            stopSelf();
        } finally {
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    void uploadToFirebaseStorage(recipePhotoDetail model) {

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
                                    getRecipeDetails(model.getRecipeName());
                                } else {
                                    getTotalRecipeImage = false;
                                }

                                getRecipePhotos();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                String error = Log.getStackTraceString(e);
                                globalclass.log(tag, "uploadToFirebaseStorage getDownloadUrl() onFailure: "+error);
                                globalclass.sendErrorLog(tag,"uploadToFirebaseStorage getDownloadUrl() onFailure: ",error);
                                showFailedNotif("Recipe details uploading failed","Failed to add recipe details, tap here to try again!");
                                stopSelf();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        String error = Log.getStackTraceString(e);
                        globalclass.log(tag, "uploadToFirebaseStorage onFailure: " + error);
                        globalclass.sendErrorLog(tag,"uploadToFirebaseStorage onFailure: ",error);
                        showFailedNotif("Recipe details uploading failed","Failed to add recipe details, tap here to try again!");
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

                        notificationManager.notify(Globalclass.addRecipeNotificationId, notification.build());
                    }
                });
    }

    void getRecipeDetails(String recipeName) {

        try {
            notification.setProgress(0, 0, true)
                    .setOngoing(true)
                    .setContentTitle("Adding " + recipeName + " details")
                    .setContentText("Adding " +recipeName+ " ingredients list");
            notificationManager.notify(Globalclass.addRecipeNotificationId, notification.build());

            recipeDetail recipeDetailsModel = mydatabase.getUploadingRecipeDetail(recipeName);
            if(recipeDetailsModel == null) {
                String error = "recipeDetailsModel is null";
                globalclass.log(tag, "getRecipeDetails: " + error);
                globalclass.sendErrorLog(tag,"getRecipeDetails: ",error);
                showFailedNotif("Recipe details uploading failed","Failed to add recipe details, tap here to try again!");
                stopSelf();
                return;
            }

            ArrayList<ingredientsDetail> ingredientsList = mydatabase.getUploadingIngredientsList(recipeName);
            if(ingredientsList.isEmpty()) {

                String error = "ingredientsList is Empty";
                globalclass.log(tag, "getRecipeDetails: " + error);
                globalclass.sendErrorLog(tag,"getRecipeDetails: ",error);
                showFailedNotif("Recipe details uploading failed","Failed to add recipe details, tap here to try again!");
                stopSelf();
                return;
            }

            ArrayList<recipePhotoDetail> recipeImageList = mydatabase.getUploadingRecipeImages(recipeName);
            if(recipeImageList.isEmpty()) {

                String error = "recipeImageList is Empty";
                globalclass.log(tag, "getRecipeDetails: " + error);
                globalclass.sendErrorLog(tag,"getRecipeDetails: ",error);
                showFailedNotif("Recipe details uploading failed","Failed to add recipe details, tap here to try again!");
                stopSelf();
                return;
            }

            ArrayList<String> recipeInstructionList = mydatabase.getUploadingRecipeInstructions(recipeName);
            if(recipeInstructionList.isEmpty()) {

                String error = "recipeInstructionList is Empty";
                globalclass.log(tag, "getRecipeDetails: " + error);
                globalclass.sendErrorLog(tag,"getRecipeDetails: ",error);
                showFailedNotif("Recipe details uploading failed","Failed to add recipe details, tap here to try again!");
                stopSelf();
                return;
            }

            recipeDetailsModel.setStatus(false);
            recipeDetailsModel.setAdminId(globalclass.getStringData("adminId"));
            recipeDetailsModel.setAdminName(globalclass.getStringData("adminName"));
            recipeDetailsModel.setRecipeInstructions(recipeInstructionList);

//            addToFireBaseDB(recipeDetailsModel);
            addToFireBaseDB(recipeDetailsModel,ingredientsList,recipeImageList);
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeDetails: " + error);
            globalclass.sendErrorLog(tag,"getRecipeDetails: ",error);
            showFailedNotif("Recipe details uploading failed","Failed to add recipe details, tap here to try again!");
            stopSelf();
        }
    }

    void addToFireBaseDB(recipeDetail model,
                         ArrayList<ingredientsDetail> ingredientsList,
                         ArrayList<recipePhotoDetail> recipeImageList) {

        try {

            WriteBatch batch = globalclass.firebaseInstance().batch();

            CollectionReference recipeColl = globalclass.firebaseInstance().collection(Globalclass.recipeColl);
            String recipeId = recipeColl.document().getId();

            model.setRecipeId(recipeId);

            DocumentReference recipeDocReference = recipeColl.document(recipeId);
            batch.set(recipeDocReference, model);

            for(int i=0;i<ingredientsList.size();i++) {
                CollectionReference ingredientsColl = globalclass.firebaseInstance().collection(Globalclass.ingredientsColl);
                String ingredientId = ingredientsColl.document().getId();

                ingredientsList.get(i).setIngredientId(ingredientId);
                ingredientsList.get(i).setRecipeId(recipeId);
                ingredientsDetail ingredientsModel = ingredientsList.get(i);

                ingredientsDetailList.add(ingredientsModel);
                DocumentReference ingredientsDocReference = ingredientsColl.document(ingredientId);
                batch.set(ingredientsDocReference, ingredientsModel);
            }

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

            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        showSuccessNotif(Integer.parseInt(model.getLocalid()),
                                "Recipe detail added",
                                model.getRecipeName().toUpperCase() + " detail added successfully!");

                        mydatabase.addAllRecipe(model);

                        for(int i=0;i<ingredientsDetailList.size();i++) {
                            ingredientsDetail model = ingredientsDetailList.get(i);
                            mydatabase.addAllIngredients(model);
                        }

                        for(int i=0;i<recipePhotoDetailsList.size();i++) {
                            recipePhotoDetail model = recipePhotoDetailsList.get(i);
                            mydatabase.addAllRecipeImages(model);
                        }

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



                    } else {
                        String error = task.getException().toString();
                        globalclass.log(tag,"addToFireBaseDB: "+error);
                        globalclass.sendErrorLog(tag,"addToFireBaseDB: ",error);
                        showFailedNotif("Recipe details uploading failed","Failed to add recipe details, tap here to try again!");
                        stopSelf();
                    }
                }
            });
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"addToFireBaseDB: "+error);
            globalclass.sendErrorLog(tag,"addToFireBaseDB: ",error);
            showFailedNotif("Recipe details uploading failed","Failed to add recipe details, tap here to try again!");
            stopSelf();
        }
    }

    void createAndupdateNotification() {
        notification = new NotificationCompat.Builder(this, Globalclass.addRecipeChannelId)
                .setSmallIcon(R.drawable.ic_appicon)
                .setContentTitle("Uploading recipe data")
                .setContentText("0 %")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setProgress(100, 0, false);


        startForeground(Globalclass.addRecipeNotificationId, notification.build());
        notificationManager.notify(Globalclass.addRecipeNotificationId, notification.build());
    }

    void showSuccessNotif(int notifiId, String title, String text) {
        Intent myIntent = new Intent(this, Splash.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, myIntent, 0);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Notification notification = new NotificationCompat.Builder(this, Globalclass.addRecipeChannelId)
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
        Notification notification = new NotificationCompat.Builder(this, Globalclass.addRecipeChannelId)
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

        notificationManager.notify(Globalclass.addRecipeFailedNotifiID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        globalclass.setAlarm(context, Globalclass.addRecipe_alarmMin, Globalclass.addRecipe_ACTION, Globalclass.addRecipe_requestID);
        globalclass.log(tag, "Destroy...");
    }
}
