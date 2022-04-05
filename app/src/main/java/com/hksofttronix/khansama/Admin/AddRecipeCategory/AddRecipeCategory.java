package com.hksofttronix.khansama.Admin.AddRecipeCategory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.bumptech.glide.request.RequestOptions;
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
import com.hksofttronix.khansama.Models.recipeCategoryDetail;
import com.hksofttronix.khansama.Models.vendorDetail;
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

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class AddRecipeCategory extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = AddRecipeCategory.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    ImageView ivCategoryIcon;
    TextInputLayout categoryNametf;
    EditText categoryName;
    MaterialButton addbt;

    ArrayList<String> selectedimagesArrayList = new ArrayList<>();

    boolean isCategoryAdded = false;

    ProgressDialog progressDialogWithPercentage;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe_category);

        setToolbar();
        init();
        binding();
        onClick();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        ivCategoryIcon = findViewById(R.id.ivCategoryIcon);
        categoryNametf = findViewById(R.id.categoryNametf);
        categoryName = findViewById(R.id.categoryName);
        addbt = findViewById(R.id.addbt);
    }

    void onClick() {
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
                    if(mydatabase.checkRecipeCategoryNameExist(categoryName.getText().toString().trim().toLowerCase()) > 0) {
                        categoryNametf.setError("Category already exist!");
                    }
                    else {
                        categoryNametf.setErrorEnabled(false);
                    }
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
//                        globalclass.toast_short("Done");
                        showConfirmDialogue();
                    }
                }
                return false;
            }
        });

        addbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        toolbar.setTitle(getString(R.string.add_category));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
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
                .setMessage("Are you sure you want to add new category ?")
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

        try {
            showprogress("Hold on","Please wait...");

            CollectionReference recipeCategoryColl = globalclass.firebaseInstance().collection(Globalclass.recipeCategoryColl);
            Query query = recipeCategoryColl.whereEqualTo("categoryName",categoryName.getText().toString().trim().toLowerCase());
            query.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    String source = task.getResult().getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag,"checkCategoryExist - Data fetched from "+source);

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                vendorDetail model = documents.toObject(vendorDetail.class);
                                globalclass.log(tag,"Category name: "+model.getVendorName());
                            }

                            hideprogress();
                            globalclass.snackit(activity,"Category already exist!");
                        }
                        else {
                            hideprogress();
                            uploadToFirebaseStorage();
                        }
                    }
                    else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"checkCategoryExist: "+error);
                        globalclass.sendErrorLog(tag,"checkCategoryExist: ",error);
                        globalclass.toast_long("Unable to add recipe category, please try after sometime!");
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkCategoryExist: "+error);
            globalclass.sendErrorLog(tag,"checkCategoryExist: ",error);
            globalclass.toast_long("Unable to add recipe category, please try after sometime!");
        }
    }

    void uploadToFirebaseStorage() {

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
                                addRecipeCategory(url);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                hideprogressWithPercentage();
                                String error = Log.getStackTraceString(e);
                                globalclass.log(tag, "uploadToFirebaseStorage getDownloadUrl() onFailure: " + error);
                                globalclass.sendErrorLog(tag,"uploadToFirebaseStorage getDownloadUrl() onFailure: ",error);
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
                        globalclass.log(tag, "uploadToFirebaseStorage: " + error);
                        globalclass.sendErrorLog(tag,"uploadToFirebaseStorage: ",error);
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

    void addRecipeCategory(String url) {

        String parameter = "";

        try {
            showprogress("Adding Category details","Please wait...");

            recipeCategoryDetail model = new recipeCategoryDetail();
            model.setCategoryName(categoryName.getText().toString().trim().toLowerCase());
            model.setCategoryIconUrl(url);

            CollectionReference recipeCategoryColl = globalclass.firebaseInstance().collection(Globalclass.recipeCategoryColl);

            String categoryId = recipeCategoryColl.document().getId();

            model.setCategoryId(categoryId);

            Gson gson = new Gson();
            if(model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag,"parameter: "+parameter);

            String finalParameter = parameter;

            DocumentReference documentReference = recipeCategoryColl.document(categoryId);
            documentReference.set(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    hideprogress();
                    if(task.isSuccessful()) {

                        isCategoryAdded = true;
                        mydatabase.addRecipeCategory(model);
                        clearAll();
                        globalclass.snackit(activity,"Added successfully!");
                    }
                    else {
                        String error = task.getException().toString();
                        globalclass.log(tag,"addRecipeCategory: "+error);
                        globalclass.sendResponseErrorLog(tag,"addRecipeCategory: ",error, finalParameter);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"addRecipeCategory: "+error);
            globalclass.sendResponseErrorLog(tag,"addRecipeCategory: ",error,parameter);
        }
    }

    void clearAll() {
        selectedimagesArrayList.clear();
        Glide.with(activity)
                .load(getResources().getIdentifier("ic_appicon", "drawable", getPackageName()))
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_appicon).centerCrop()
                        .fitCenter())
                .into(ivCategoryIcon);
        categoryName.setText("");
    }

    boolean validation() {

        if (selectedimagesArrayList.isEmpty()) {
            globalclass.snackit(activity, "Please add category icon!");
            return false;
        } else if (categoryName.getText().length() < 3) {
            categoryNametf.setError("Should contain atleast 3 characters!");
            return false;
        } else if (!categoryName.getText().toString().trim().matches(globalclass.alphaRegexTwo())) {
            categoryNametf.setError("Invalid Category name!");
            return false;
        }
        else if(mydatabase.checkRecipeCategoryNameExist(categoryName.getText().toString().trim().toLowerCase()) > 0) {
            categoryNametf.setError("Category already exist!");
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

    @Override
    public void onBackPressed() {

        if(isCategoryAdded) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
        else {
            super.onBackPressed();
        }
    }
}