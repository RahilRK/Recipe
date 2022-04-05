package com.hksofttronix.khansama.Admin.UpdateRecipeCategory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hksofttronix.khansama.Models.recipeCategoryDetail;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class UpdateRecipeCategory extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = UpdateRecipeCategory.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    SwipeRefreshLayout swipeRefresh;
    ImageView ivCategoryIcon;
    TextInputLayout categoryNametf;
    EditText categoryName;
    MaterialButton updatebt;

    recipeCategoryDetail model;
    ArrayList<String> selectedimagesArrayList = new ArrayList<>();

    ProgressDialog progressDialogWithPercentage;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_recipe_category);

        setToolbar();
        init();
        binding();
        onClick();
        setText();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        swipeRefresh = findViewById(R.id.swipeRefresh);
        ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
        categoryNametf = findViewById(R.id.categoryNametf);
        categoryName = findViewById(R.id.categoryName);
        updatebt = findViewById(R.id.updatebt);
    }

    void onClick() {

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (!globalclass.isInternetPresent()) {
                    swipeRefresh.setRefreshing(false);
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                getCategoryDetail();
            }
        });

        ivCategoryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestStoragePermission();
            }
        });

        categoryName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.length() > 0) {
                    checkAnyChangesOccured();
                }
            }
        });

        categoryName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    globalclass.hideKeyboard(activity);

                    if (!globalclass.isInternetPresent()) {
                        globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                        return false;
                    }

                    if (validation()) {
                        showConfirmDialogue();
                    }
                }
                return false;
            }
        });

        updatebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globalclass.hideKeyboard(activity);

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                if (validation()) {
//                    globalclass.toast_short("Done");
                    showConfirmDialogue();
                }
            }
        });
    }

    void setText() {
        model = getIntent().getParcelableExtra("recipeCategoryDetail");
        categoryName.setText(model.getCategoryName());

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions
//                .transforms(new CenterCrop(), new RoundedCorners(16))
                .placeholder(R.drawable.ic_appicon)
                .error(R.drawable.ic_appicon);
        Glide
                .with(activity)
                .load(model.getCategoryIconUrl())
                .apply(requestOptions)
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object mmodel, Target<Drawable> target, boolean isFirstResource) {

                        globalclass.toast_long("Unable to load category image!");
                        String error = Log.getStackTraceString(e);
                        globalclass.log(tag,error);

                        String parameter = "";
                        Gson gson = new Gson();
                        if(model != null) {
                            parameter = gson.toJson(model);
                        }
                        globalclass.log(tag,"parameter: "+parameter);

                        String finalParameter = parameter;
                        globalclass.sendResponseErrorLog(tag,"onLoadFailed: ",error, finalParameter);

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                        return false;
                    }
                })
                .into(ivCategoryIcon);
    }

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        toolbar.setTitle(getString(R.string.change_detail));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void getCategoryDetail() {

        String parameter = "";

        try {
            swipeRefresh.setRefreshing(true);

            CollectionReference recipeCategoryColl = globalclass.firebaseInstance().collection(Globalclass.recipeCategoryColl);
            Query checkCategoryExist = recipeCategoryColl.whereEqualTo("categoryId", model.getCategoryId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            checkCategoryExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    swipeRefresh.setRefreshing(false);
                    globalclass.snackit(activity,"Category detail refresh successfully!");

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                model = documents.toObject(recipeCategoryDetail.class);
                                globalclass.log(tag,"Category name: "+ model.getCategoryName());
                            }

                            selectedimagesArrayList.clear();
                            setText();
                        }
                        else {
                            mydatabase.deleteData(mydatabase.recipeCategory,"categoryId",model.getCategoryId());
                            globalclass.snackit(activity,"Category not exist!");
                        }
                    }
                    else {
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getCategoryDetail: "+error);
                        globalclass.sendResponseErrorLog(tag,"getCategoryDetail: ",error, finalParameter);
                        globalclass.toast_long("Unable to get category detail, please try after sometime!");
                    }
                }
            });
        }
        catch (Exception e) {

            swipeRefresh.setRefreshing(false);
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getCategoryDetail: "+error);
            globalclass.sendResponseErrorLog(tag,"getCategoryDetail: ",error, parameter);
            globalclass.toast_long("Unable to get category detail, please try after sometime!");
        }
    }

    void requestStoragePermission() {
        Dexter.withActivity(activity)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {

                        if (report.areAllPermissionsGranted()) {

                            OpenImagePicker();
                        }

                        if (report.getDeniedPermissionResponses().size() > 0) {

                            globalclass.snackit(activity, "Storage permission required to choose image");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {

                    }
                })
                .onSameThread()
                .check();
    }

    void OpenImagePicker() {
        FilePickerBuilder.getInstance().setMaxCount(1)
                .setSelectedFiles(selectedimagesArrayList)
                .setActivityTheme(R.style.ImagePickerTheme)
                .enableCameraSupport(false)
                .pickPhoto(activity);
    }

    void showConfirmDialogue() {

        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure")
                .setMessage("Are you sure you want to change category detail ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        checkCategoryExist();
                    }
                })
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    void checkCategoryExist() {

        String parameter = "";

        try {
            showprogress("Hold on","Please wait...");

            CollectionReference recipeCategoryColl = globalclass.firebaseInstance().collection(Globalclass.recipeCategoryColl);
            Query query = recipeCategoryColl.whereEqualTo("categoryId",model.getCategoryId());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    String source = task.getResult().getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag,"checkCategoryExist - Data fetched from "+source);

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                recipeCategoryDetail model = documents.toObject(recipeCategoryDetail.class);
                                globalclass.log(tag,"Category name: "+model.getCategoryName());
                            }

                            hideprogress();
                            if(selectedimagesArrayList.isEmpty()) {
                                updateRecipeCategory("");
                            }
                            else {
                                removeImageFromFirebaseStorage();
                            }
                        }
                        else {
                            hideprogress();
                            globalclass.snackit(activity,"Category donot exist!");
                        }
                    }
                    else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"checkCategoryExist: "+error);
                        globalclass.sendResponseErrorLog(tag,"checkCategoryExist: ",error, finalParameter);
                        globalclass.toast_long("Unable to add recipe category, please try after sometime!");
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkCategoryExist: "+error);
            globalclass.sendResponseErrorLog(tag,"checkCategoryExist: ",error, parameter);
            globalclass.toast_long("Unable to add recipe category, please try after sometime!");
        }
    }

    void removeImageFromFirebaseStorage() {

        String parameter = "";

        try {
            if (!globalclass.isInternetPresent()) {
                globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                return;
            }

            showprogress("Removing existing category image","Please wait...");
            storageReference = firebaseStorage.getReference();

            storageReference = firebaseStorage.getReferenceFromUrl(model.getCategoryIconUrl());

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    hideprogress();
                    try {
                        uploadToFirebaseStorage();
                    } catch (Exception e) {

                        String error =  Log.getStackTraceString(e);
                        globalclass.log(tag,"removeImageFromFirebaseStorage onSuccess: "+error);
                        globalclass.sendResponseErrorLog(tag,"removeImageFromFirebaseStorage onSuccess: ",error, finalParameter);
                        globalclass.snackit(activity, "Unable to remove existing category image, please try again later!");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = e.getMessage();
                    if (error.equalsIgnoreCase("Object does not exist at location.")) {
                        globalclass.log(tag, "Object does not exist at location.");
                        globalclass.log(tag, Log.getStackTraceString(e));
                    } else {
                        globalclass.log(tag, Log.getStackTraceString(e));
                    }

                    globalclass.sendResponseErrorLog(tag,"removeImageFromFirebaseStorage onFailure: ",error, finalParameter);
                    globalclass.snackit(activity, "Unable to remove existing category image, please try again later!");
                }
            });
        } catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, error);
            globalclass.sendResponseErrorLog(tag,"removeImageFromFirebaseStorage: ",error, parameter);
            globalclass.snackit(activity, "Unable to remove existing category image, please try again later!");
        }
    }

    void uploadToFirebaseStorage() {

        String parameter = "";
        Gson gson = new Gson();
        if(model != null) {
            parameter = gson.toJson(model);
        }
        globalclass.log(tag,"parameter: "+parameter);

        String finalParameter = parameter;

        showprogressWithPercentage("Uploading category icon","Please wait...");

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("Recipe Category Images")
                .child(String.valueOf(globalclass.getMilliSecond()+".png"));
        storageReference.putFile(Uri.fromFile(new File(selectedimagesArrayList.get(0))))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                hideprogressWithPercentage();
                                String url = uri.toString();
                                globalclass.log(tag, "url: " + url);
                                updateRecipeCategory(url);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                hideprogressWithPercentage();
                                String error = Log.getStackTraceString(e);
                                globalclass.log(tag, "uploadToFirebaseStorage getDownloadUrl() onSuccess: " + error);
                                globalclass.sendResponseErrorLog(tag,"uploadToFirebaseStorage getDownloadUrl() onSuccess: ",error, finalParameter);
                                globalclass.toast_short("Error");
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        hideprogressWithPercentage();

                        String error = Log.getStackTraceString(e);
                        globalclass.log(tag, "uploadToFirebaseStorage onFailure: " + error);
                        globalclass.sendResponseErrorLog(tag,"uploadToFirebaseStorage onFailure: ",error, finalParameter);
                        globalclass.toast_short("Error");
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress_percentage = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        globalclass.log(tag + ": uploadToFirebaseStorage_OnProgress", String.valueOf((int) progress_percentage) + "% ");

                        if(progressDialogWithPercentage != null && progressDialogWithPercentage.isShowing()) {
                            progressDialogWithPercentage.setProgress((int)progress_percentage);
                        }
                    }
                });
    }


    void updateRecipeCategory(String url) {

        String parameter = "";

        try {
            showprogress("Updating Category details","Please wait...");

            model.setCategoryName(categoryName.getText().toString().trim().toLowerCase());
            if(url.equalsIgnoreCase("")) {
                model.setCategoryIconUrl(model.getCategoryIconUrl());
            }
            else {
                model.setCategoryIconUrl(url);
            }

            CollectionReference recipeCategoryColl = globalclass.firebaseInstance().collection(Globalclass.recipeCategoryColl);

            String categoryId = model.getCategoryId();

            model.setCategoryId(categoryId);

            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(model);
            Map<String,Object> map = new Gson().fromJson(json, Map.class);

            DocumentReference documentReference = recipeCategoryColl.document(categoryId);

            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            documentReference.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    hideprogress();
                    if(task.isSuccessful()) {

                        mydatabase.addRecipeCategory(model);
                        globalclass.toast_long("Category details changed successfully!");

                        Intent intent = new Intent();
                        intent.putExtra("position",getIntent().getStringExtra("position"));
                        intent.putExtra("recipeCategoryDetail",model);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    else {
                        String error = task.getException().toString();
                        globalclass.log(tag,"updateRecipeCategory: "+error);
                        globalclass.sendResponseErrorLog(tag,"updateRecipeCategory: ",error, finalParameter);
                        globalclass.toast_long("Unable to change category detail, please try after sometime!");
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"updateRecipeCategory: "+error);
            globalclass.sendResponseErrorLog(tag,"updateRecipeCategory: ",error, parameter);
            globalclass.toast_long("Unable to change category detail, please try after sometime!");
        }
    }

    void checkAnyChangesOccured() {
        if(!selectedimagesArrayList.isEmpty() ||
                !categoryName.getText().toString().trim().equalsIgnoreCase(model.getCategoryName())) {
            updatebt.setEnabled(true);
        }
        else {
            updatebt.setEnabled(false);
        }

//        if (!categoryName.getText().toString().trim().equalsIgnoreCase(model.getCategoryName())) {
//            updatebt.setEnabled(true);
//        }
//        else {
//            updatebt.setEnabled(false);
//        }
    }

    boolean validation() {

        if (categoryName.getText().length() < 3) {
            categoryNametf.setError("Should contain atleast 3 characters!");
            return false;
        } else if (!categoryName.getText().toString().trim().matches(globalclass.alphaRegexTwo())) {
            categoryNametf.setError("Invalid Category name!");
            return false;
        }

        categoryNametf.setErrorEnabled(false);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                selectedimagesArrayList.clear();
                selectedimagesArrayList.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));

                checkAnyChangesOccured();

                if (!selectedimagesArrayList.isEmpty()) {
                    Glide.with(activity)
                            .load(selectedimagesArrayList.get(0))
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_appicon).centerCrop()
                                    .fitCenter())
                            .into(ivCategoryIcon);
                }
            }

            globalclass.log(tag, String.valueOf(selectedimagesArrayList.size()) + " images selected");
        }
    }

    void showprogressWithPercentage(String title, String message) {

        progressDialogWithPercentage = new ProgressDialog(activity);
        progressDialogWithPercentage.setTitle(title);
        progressDialogWithPercentage.setMessage(message);
        progressDialogWithPercentage.setIndeterminate(false);
        progressDialogWithPercentage.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialogWithPercentage.setCancelable(false);
        progressDialogWithPercentage.setMax(100);
        progressDialogWithPercentage.show();
    }

    void hideprogressWithPercentage() {
        if (progressDialogWithPercentage.isShowing()) {
            progressDialogWithPercentage.dismiss();
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