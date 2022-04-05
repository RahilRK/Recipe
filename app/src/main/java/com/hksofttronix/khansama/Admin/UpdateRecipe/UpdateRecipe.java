package com.hksofttronix.khansama.Admin.UpdateRecipe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hksofttronix.khansama.Admin.AddRecipe.ingredientsAdapter;
import com.hksofttronix.khansama.Admin.AddRecipe.ingredientsOnClick;
import com.hksofttronix.khansama.Admin.AddRecipeInstructions.AddRecipeInstructions;
import com.hksofttronix.khansama.Admin.SelectInventory.SelectInventory;
import com.hksofttronix.khansama.Models.ingredientsDetail;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.recipeCategoryDetail;
import com.hksofttronix.khansama.Models.recipeDetail;
import com.hksofttronix.khansama.Models.recipeInstructionDetail;
import com.hksofttronix.khansama.Models.recipePhotoDetail;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class UpdateRecipe extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = UpdateRecipe.this;

    Globalclass globalclass;
    Mydatabase mydatabase;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference storageReference;

    String recipeId = "";
    recipeDetail model;

    TextInputLayout recipenametf, additionalChargestf, amounttf;
    EditText recipename, additionalCharges, amount;
    LinearLayout addRecipePhotolo, addInventorylo, addInstructionslo, vegNonVeglo;
    RelativeLayout addCategoryNamelo;
    RecyclerView recipePhotosRecyclerview, inventoryRecyclerView;
    TextView addRecipeInstruction, categoryName,recipeType,recipeCostPrice;
    ImageView ivrefreshCategory;
    MaterialButton updateRecipebt;

    ArrayList<String> selectedimagesArrayList = new ArrayList<>();
    ArrayList<recipePhotoDetail> updateRecipeImagesArrayList = new ArrayList<>();
    UpdateRecipePhotos_adapter updateRecipePhotos_adapter;

    ArrayList<ingredientsDetail> ingredientsArrayList = new ArrayList<>();
    ingredientsAdapter ingredients_adapter;

    ArrayList<recipeInstructionDetail> recipeInstructionList;
    ArrayList<String> recipeInstructionsArrayList = new ArrayList<>();

    int INVENTORY_LIST = 111;
    int INSTRUCTION_LIST = 222;

    ArrayList<recipeCategoryDetail> categoryDetailArrayList = new ArrayList<>();
    ArrayList<String> categoryList = new ArrayList<>();
    int selectedCategoryPos = 0;

    ArrayList<String> recipeTypeList = new ArrayList<>();
    int selectedRecipeTypePos = 0;

    double recipeIngredientPrice = 0;

    String newFirstRecipeImageId = "";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_recipe);

        init();
        binding();
        setText();
        setToolbar();
        getCategoryData();
        fillRecipeTypeList();
        onClick();
        mydatabase.unCheckAllSelectedInventory();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding() {
        recipenametf = findViewById(R.id.recipenametf);
        additionalChargestf = findViewById(R.id.additionalChargestf);
        amounttf = findViewById(R.id.amounttf);
        recipename = findViewById(R.id.recipename);
        additionalCharges = findViewById(R.id.additionalCharges);
        amount = findViewById(R.id.amount);
        addRecipePhotolo = findViewById(R.id.addRecipePhotolo);
        addInventorylo = findViewById(R.id.addInventorylo);
        addInstructionslo = findViewById(R.id.addInstructionslo);
        vegNonVeglo = findViewById(R.id.vegNonVeglo);
        addCategoryNamelo = findViewById(R.id.addCategoryNamelo);
        recipePhotosRecyclerview = findViewById(R.id.recipePhotosRecyclerview);
        inventoryRecyclerView = findViewById(R.id.inventoryRecyclerView);
        addRecipeInstruction = findViewById(R.id.addRecipeInstruction);
        categoryName = findViewById(R.id.categoryName);
        recipeType = findViewById(R.id.recipeType);
        recipeCostPrice = findViewById(R.id.recipeCostPrice);
        ivrefreshCategory = findViewById(R.id.ivrefreshCategory);
        updateRecipebt = findViewById(R.id.updateRecipebt);
    }

    void onClick() {
        recipename.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    enableUpdateButton();
                }
            }
        });

        additionalCharges.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                enableUpdateButton();
            }
        });

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    enableUpdateButton();
                }
            }
        });

        addRecipePhotolo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestStoragePermission();
            }
        });

        addCategoryNamelo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showCategoryDialogue();
            }
        });

        ivrefreshCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                getRecipeCategoryList();
            }
        });

        addInventorylo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, SelectInventory.class);
                intent.putExtra("action", "AddRecipe");
                intent.putExtra("autoCheck", true);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("arrayList", ingredientsArrayList);
                intent.putExtras(bundle);
                startActivityForResult(intent, INVENTORY_LIST);
            }
        });

        addInstructionslo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, AddRecipeInstructions.class);
                intent.putStringArrayListExtra("arrayList", recipeInstructionsArrayList);
                startActivityForResult(intent, INSTRUCTION_LIST);
            }
        });

        vegNonVeglo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showRecipeTypeDialogue();
            }
        });

        amount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
                        updateRecipeDetails();
                    }
                }
                return false;
            }
        });

        updateRecipebt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globalclass.hideKeyboard(activity);

                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                    return;
                }

                if (validation()) {
//                    globalclass.toast_short("Done");
                    updateRecipeDetails();
                }
            }
        });
    }

    void setText() {

        try {
            recipeId = getIntent().getStringExtra("recipeId");
            model = mydatabase.getParticularRecipeDetailFromAllRecipe(recipeId);
            if (model != null) {
                globalclass.log(tag, model.getRecipeName());

                setRecipePhotosAdapter();

                recipename.setText(model.getRecipeName());

                ingredientsArrayList = mydatabase.getParticularRecipeAllIngredientsList(recipeId);
                if (ingredientsArrayList != null && !ingredientsArrayList.isEmpty()) {
                    setInventoryListAdapter();
                    calculateRecipeCostPrice();
                }

                fillRecipeInstructionsList();

                if(model.getAdditionalCharges() > 0) {
                    additionalCharges.setText(String.valueOf(model.getAdditionalCharges()));
                }

                amount.setText(String.valueOf(model.getPrice()));
            } else {
                String error = "recipeDetail model is null!";
                globalclass.log(tag, "setText: " + error);
                globalclass.sendErrorLog(tag, "setText: ", error);
                globalclass.toast_long("Unable to get recipe details, please try after sometime!");
            }
        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "setText: " + error);
            globalclass.sendErrorLog(tag, "setText: ", error);
            globalclass.toast_long("Unable to get recipe details, please try after sometime!");
        }
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

    void updateRecipeDetails() {

        try {
            storeRecipeDetails();
            storePhotosDetails();
            storeRecipeInstructions();

            globalclass.startService(activity, UpdateRecipeService.class);
            globalclass.toast_long("You will notify you when recipe details will be changed!");
            onBackPressed();

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "updateRecipeDetails: " + error);
            globalclass.sendErrorLog(tag, "updateRecipeDetails: ", error);
            globalclass.snackit(activity, "Unable to change recipe details, please try again later!");
        }
    }

    void storeRecipeDetails() {
        recipeDetail recipeDetailsmodel = new recipeDetail();
        recipeDetailsmodel.setRecipeId(model.getRecipeId());
        recipeDetailsmodel.setRecipeName(recipename.getText().toString().trim().toLowerCase());
        if (additionalCharges.getText().length() == 0) {
            recipeDetailsmodel.setAdditionalCharges(0);
        } else {
            recipeDetailsmodel.setAdditionalCharges(Integer.parseInt(additionalCharges.getText().toString().trim()));
        }
        recipeDetailsmodel.setPrice(Integer.parseInt(amount.getText().toString().trim().toLowerCase()));
        recipeDetailsmodel.setCategoryId(categoryDetailArrayList.get(selectedCategoryPos).getCategoryId());
        recipeDetailsmodel.setCategoryName(categoryDetailArrayList.get(selectedCategoryPos).getCategoryName().trim().toLowerCase());
        recipeDetailsmodel.setRecipeType(recipeTypeList.get(selectedRecipeTypePos));
        recipeDetailsmodel.setStatus(model.getStatus());
        recipeDetailsmodel.setActions("Update");
        mydatabase.uploadRecipeDetail(recipeDetailsmodel);
    }

    void storePhotosDetails() {
        for (int i = 0; i < updateRecipeImagesArrayList.size(); i++) {

            recipePhotoDetail model = updateRecipeImagesArrayList.get(i);
            if (model.getNewImages()) {
                model.setRecipeName(recipename.getText().toString().trim().toLowerCase());
                mydatabase.uploadRecipePhoto(model);
            }
        }
    }

    void storeRecipeInstructions() {
        for (int i = 0; i < recipeInstructionsArrayList.size(); i++) {
            mydatabase.addRecipeInstructions(recipename.getText().toString().trim().toLowerCase(),
                    recipeInstructionsArrayList.get(i));
        }
    }

    void enableUpdateButton() {
        recipeDetail recipeDetailModel = new recipeDetail();
        recipeDetailModel.setRecipeName(recipename.getText().toString().trim().toLowerCase());
        recipeDetailModel.setRecipeType(recipeTypeList.get(selectedRecipeTypePos));
        recipeDetailModel.setCategoryId(categoryDetailArrayList.get(selectedCategoryPos).getCategoryId());
        recipeDetailModel.setCategoryName(categoryDetailArrayList.get(selectedCategoryPos).getCategoryName().trim().toLowerCase());
        if (additionalCharges.getText().length() == 0) {
            recipeDetailModel.setAdditionalCharges(0);
        } else {
            recipeDetailModel.setAdditionalCharges(Integer.parseInt(additionalCharges.getText().toString().trim()));
        }
        recipeDetailModel.setPrice(Integer.parseInt(amount.getText().toString().trim().toLowerCase()));

        if (equals2(recipeDetailModel) || !checkInstructionsequal2() || checkNewRecipeImageAdded()) {
            updateRecipebt.setEnabled(true);
        } else {
            updateRecipebt.setEnabled(false);
        }
    }

    boolean equals2(Object object2) {  // equals2 method
        if (model.equals(object2)) { // if equals() method returns true
            return false; // return true
        } else return true; // if equals() method returns false, also return false
    }

    boolean checkInstructionsequal2() {
        ArrayList<String> oldRecipeInstructionList = new ArrayList<>();
        oldRecipeInstructionList.clear();

        for (int i = 0; i < recipeInstructionList.size(); i++) {
            String instructions = recipeInstructionList.get(i).getInstruction();
            oldRecipeInstructionList.add(instructions);
        }

        return oldRecipeInstructionList.equals(recipeInstructionsArrayList);
    }

    boolean checkNewRecipeImageAdded() {
        for (int i = 0; i < updateRecipeImagesArrayList.size(); i++) {
            recipePhotoDetail model = updateRecipeImagesArrayList.get(i);
            if (model.getNewImages()) {
                return true;
            }
        }

        return false;
    }

    boolean validation() {

        if (updateRecipeImagesArrayList.isEmpty()) {
            globalclass.snackit(activity, "Please add recipe images!");
            return false;
        } else if (recipename.getText().length() < 3) {
            recipenametf.setError("Should contain atleast 3 characters!");
            return false;
        } else if (!recipename.getText().toString().trim().matches(globalclass.alphaNumericRegexOne())) {
            recipenametf.setError("Invalid Recipe name!");
            return false;
        } else if (ingredientsArrayList.isEmpty()) {
            globalclass.snackit(activity, "Please add recipe items!");
            return false;
        } else if (!checkAllItemQuantityIsEntered()) {
            globalclass.snackit(activity, "Please enter the quantity for all items!");
            return false;
        } else if (recipeInstructionsArrayList.isEmpty()) {
            globalclass.snackit(activity, "Please enter recipe instructions!");
            return false;
        } else if (additionalCharges.getText().length() > 0 && Integer.parseInt(additionalCharges.getText().toString()) <= 0) {
            additionalCharges.setError("Invalid Additional Charges!");
            return false;
        } else if (amount.getText().length() == 0 || Integer.parseInt(amount.getText().toString()) == 0) {
            amounttf.setError("Invalid amount!");
            return false;
        } else if(Integer.parseInt(amount.getText().toString()) <= recipeIngredientPrice) {
            amounttf.setError("Invalid amount!");
            globalclass.showDialogue(activity,"Invalid amount","Recipe should be price more then it's ingredients purchase price, to avoid loss!");
            return false;
        } else if (mydatabase.recipePendingToUpload(recipename.getText().toString().trim().toLowerCase())) {
            globalclass.snackit(activity, recipename.getText().toString().trim().toUpperCase() + " is already been process to make changes!");
            return false;
        }

        recipenametf.setErrorEnabled(false);
        additionalChargestf.setErrorEnabled(false);
        amounttf.setErrorEnabled(false);
        return true;
    }

    boolean checkAllItemQuantityIsEntered() {
        for (int i = 0; i < ingredientsArrayList.size(); i++) {
            if (ingredientsArrayList.get(i).getQuantity() < 1) {
                return false;
            }
        }

        return true;
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
        int maxImage = globalclass.maxRecipeImages() - updateRecipeImagesArrayList.size();
        FilePickerBuilder.getInstance().setMaxCount(maxImage)
                .setSelectedFiles(selectedimagesArrayList)
                .setActivityTheme(R.style.ImagePickerTheme)
                .enableCameraSupport(false)
                .pickPhoto(activity);
    }

    void setRecipePhotosAdapter() {
        updateRecipeImagesArrayList = mydatabase.getParticularAllRecipeImages(recipeId);
        showhide_addRecipePhotolo();
        recipePhotosRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        updateRecipePhotos_adapter = new UpdateRecipePhotos_adapter(activity, updateRecipeImagesArrayList, new updateRecipePhotosOnClick() {
            @Override
            public void onDelete(int position, recipePhotoDetail model) {

                if (model.getNewImages()) {
                    removeFrom_selectedimagesArrayList(model);
                    updateRecipePhotos_adapter.deleteItem(position);
                    enableUpdateButton();
                } else {

                    if (!globalclass.isInternetPresent()) {
                        globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                        return;
                    }

                    if (mydatabase.getParticularAllRecipeImages(model.getRecipeId()).size() > 1) {
                        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                                .setTitle("Sure")
                                .setMessage("Are you sure you want to remove?")
                                .setCancelable(false)
                                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();
                                        removeRecipeImageOld(position, model);
//                                        removeRecipeImage(position, model);
                                    }
                                })
                                .setNeutralButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    } else {
                        globalclass.showDialogue(activity, "Alert", "Atleast one recipe image is required, please add and upload another image to remove current image!");
                    }
                }

                showhide_addRecipePhotolo();
            }
        });
        recipePhotosRecyclerview.setAdapter(updateRecipePhotos_adapter);
    }

    void removeFrom_selectedimagesArrayList(recipePhotoDetail model) {
        for (int i = 0; i < selectedimagesArrayList.size(); i++) {
            if (model.getFilepath().equalsIgnoreCase(selectedimagesArrayList.get(i))) {
                selectedimagesArrayList.remove(i);
                return;
            }
        }

    }

    void removeRecipeImage(int position, recipePhotoDetail model) {

        String parameter = "";

        try {
            showprogress("Hold on", "Please wait...");

            final DocumentReference recipeImagesDocRef = globalclass.firebaseInstance()
                    .collection(Globalclass.recipeImagesColl)
                    .document(model.getRecipeImageId());

            Gson gson = new Gson();
            if (model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag, "parameter: " + parameter);

            String finalParameter = parameter;

            globalclass.firebaseInstance().runTransaction(new Transaction.Function<Integer>() {

                @Nullable
                @Override
                public Integer apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                    DocumentSnapshot recipeImagesDoc = transaction.get(recipeImagesDocRef);
                    if (recipeImagesDoc.exists()) {

                        recipePhotoDetail recipePhotoModel = recipeImagesDoc.toObject(recipePhotoDetail.class);
                        transaction.delete(recipeImagesDocRef);

                        if (recipePhotoModel.getFirstImage()) {

                            //todo get first Image from list with 'firstImage = false' and update it to 'firstImage = true'
                            recipePhotoDetail tempRecipePhotoModel = null;
                            for (int i = 0; i < updateRecipeImagesArrayList.size(); i++) {
                                if (!updateRecipeImagesArrayList.get(i).getFirstImage()) {
                                    tempRecipePhotoModel = updateRecipeImagesArrayList.get(i);
                                    newFirstRecipeImageId = tempRecipePhotoModel.getRecipeImageId();
                                    break;
                                }
                            }

                            CollectionReference recipeImagesColl = globalclass.firebaseInstance().collection(Globalclass.recipeImagesColl);
                            DocumentReference updateRecipeImageDocReference = recipeImagesColl.document(tempRecipePhotoModel.getRecipeImageId());
                            Map<String, Object> map = new HashMap<>();
                            map.put("firstImage", true);

                            transaction.update(updateRecipeImageDocReference, map);
                        }
                        return 0;
                    } else {
                        return -1;
                    }
                }
            }).addOnSuccessListener(activity, new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {

                    hideprogress();
                    if (integer == 0) {

                        removeImageFromFirebaseStorage(position, model);
                    } else if (integer == -1) {

                        String error = "return -1";
                        globalclass.log(tag, "removeRecipeImage onSuccess: " + error);
                        globalclass.sendResponseErrorLog(tag, "removeRecipeImage onSuccess: ", error, finalParameter);
                        globalclass.toast_long("Unable to remove recipe image, please try again later!");
                    }
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag, "removeRecipeImage onFailure: " + error);
                    globalclass.sendResponseErrorLog(tag, "removeRecipeImage onFailure: ", error, finalParameter);
                    globalclass.toast_long("Unable to remove recipe image, please try again later!");
                }
            });
        } catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "removeRecipeImage: " + error);
            globalclass.sendResponseErrorLog(tag, "removeRecipeImage: ", error, parameter);
            globalclass.toast_long("Unable to remove recipe image, please try again later!");
        }
    }

    void removeRecipeImageOld(int position, recipePhotoDetail model) {

        String parameter = "";

        try {
            showprogress("Hold on", "Please wait...");

            WriteBatch batch = globalclass.firebaseInstance().batch();

            CollectionReference recipeImagesColl = globalclass.firebaseInstance().collection(Globalclass.recipeImagesColl);
            DocumentReference deleteRecipeImageDocReference = recipeImagesColl.document(model.getRecipeImageId());

            batch.delete(deleteRecipeImageDocReference);

            if (model.getFirstImage()) {

                //todo get first Image from list with 'firstImage = false' and update it to 'firstImage = true'
                recipePhotoDetail recipePhotoModel = null;
                for (int i = 0; i < updateRecipeImagesArrayList.size(); i++) {
                    if (!updateRecipeImagesArrayList.get(i).getFirstImage()) {
                        recipePhotoModel = updateRecipeImagesArrayList.get(i);
                        newFirstRecipeImageId = recipePhotoModel.getRecipeImageId();
                        break;
                    }
                }
                DocumentReference updateRecipeImageDocReference = recipeImagesColl.document(recipePhotoModel.getRecipeImageId());
                Map<String, Object> map = new HashMap<>();
                map.put("firstImage", true);
                batch.update(updateRecipeImageDocReference, map);
            }

            Gson gson = new Gson();
            if (model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag, "parameter: " + parameter);

            String finalParameter = parameter;

            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    hideprogress();
                    if (task.isSuccessful()) {
                        removeImageFromFirebaseStorage(position, model);
                    } else {
                        String error = task.getException().toString();
                        globalclass.log(tag, "removeRecipeImage: " + error);
                        globalclass.sendResponseErrorLog(tag, "removeRecipeImage: ", error, finalParameter);
                        globalclass.toast_long("Unable to remove recipe image, please try again later!");
                    }
                }
            });

        } catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "removeRecipeImage: " + error);
            globalclass.sendResponseErrorLog(tag, "removeRecipeImage: ", error, parameter);
            globalclass.toast_long("Unable to remove recipe image, please try again later!");
        }
    }

    void removeImageFromFirebaseStorage(int position, recipePhotoDetail model) {

        String parameter = "";

        try {
            showprogress("Removing image", "Please wait...");
            storageReference = firebaseStorage.getReference();

            storageReference = firebaseStorage.getReferenceFromUrl(model.getUrl());

            Gson gson = new Gson();
            if (model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag, "parameter: " + parameter);

            String finalParameter = parameter;

            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    hideprogress();
                    try {
                        if (model.getFirstImage()) {
                            if (!globalclass.checknull(newFirstRecipeImageId).equalsIgnoreCase("")) {
                                for (int i = 0; i < updateRecipeImagesArrayList.size(); i++) {
                                    if (updateRecipeImagesArrayList.get(i).getRecipeImageId().equalsIgnoreCase(newFirstRecipeImageId)) {
                                        recipePhotoDetail recipePhotoModel = updateRecipeImagesArrayList.get(i);
                                        recipePhotoModel.setFirstImage(true);
                                        mydatabase.addAllRecipeImages(recipePhotoModel);
                                        newFirstRecipeImageId = "";
                                        globalclass.log(tag, "first recipe image updated!");
                                        break;
                                    }
                                }
                            } else {
                                globalclass.log(tag, "Unable to update first recipe image!");
                            }
                        }

                        mydatabase.deleteData(mydatabase.allRecipeImages, "recipeImageId", model.getRecipeImageId());
                        updateRecipePhotos_adapter.deleteItem(position);
                        globalclass.snackit(activity, "Removed successfully!");

                    } catch (Exception e) {
                        String error = Log.getStackTraceString(e);
                        globalclass.log(tag, error);
                        globalclass.sendResponseErrorLog(tag, "removeImageFromFirebaseStorage onSuccess: ", error, finalParameter);
                        globalclass.toast_long("Error");
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

                    globalclass.sendResponseErrorLog(tag, "removeImageFromFirebaseStorage onFailure: ", error, finalParameter);
                    globalclass.snackit(activity, "Unable to remove recipe image, please try again later!");
                }
            });
        } catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "removeImageFromFirebaseStorage onFailure: " + error);
            globalclass.sendResponseErrorLog(tag, "removeImageFromFirebaseStorage onFailure: ", error, parameter);
            globalclass.snackit(activity, "Unable to remove recipe image, please try again later!");
        }
    }

    void addToList() {

        try {
            for (int i = 0; i < selectedimagesArrayList.size(); i++) {

                String imageName = String.valueOf(globalclass.getMilliSecond()) + ".png";
                recipePhotoDetail recipePhotoDetailsModel = new recipePhotoDetail();
                recipePhotoDetailsModel.setRecipeName(recipename.getText().toString().trim().toLowerCase());
                recipePhotoDetailsModel.setImageName(imageName);
                recipePhotoDetailsModel.setFilepath(selectedimagesArrayList.get(i));
                recipePhotoDetailsModel.setNewImages(true);
                updateRecipePhotos_adapter.addItem(recipePhotoDetailsModel);
                globalclass.log(tag, "imageName: " + imageName);
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, error);
            globalclass.sendErrorLog(tag, "addToList: ", error);
            globalclass.snackit(activity, "Unable to change recipe details, please try again later!");
        }
    }

    void setInventoryListAdapter() {
        showHideInventoryRV();
        ingredients_adapter = new ingredientsAdapter(activity, ingredientsArrayList, new ingredientsOnClick() {
            @Override
            public void onAddQuantity(int position, ingredientsDetail model) {

                showAddQuantityDialogue(position, model);
            }

            @Override
            public void onDelete(int position, ingredientsDetail model) {

                if (!globalclass.checknull(model.getIngredientId()).equalsIgnoreCase("")) {

                    if (!globalclass.isInternetPresent()) {
                        globalclass.toast_long(getResources().getString(R.string.noInternetConnection));
                        return;
                    }

                    if (mydatabase.getParticularRecipeAllIngredientsList(model.getRecipeId()).size() > 1) {

                        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                                .setTitle("Sure")
                                .setMessage("Are you sure you want to remove ?")
                                .setCancelable(false)
                                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();
                                        removeRecipeIngredient(position, model);
                                    }
                                })
                                .setNeutralButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    } else {
                        globalclass.showDialogue(activity, "Alert", "Recipe item/ingredients should not be empty!");
                    }
                } else {
                    ingredients_adapter.deleteData(position);
                    mydatabase.unCheckPerInventory(model.getInventoryId());
                }
            }
        });
        inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        inventoryRecyclerView.setAdapter(ingredients_adapter);
    }

    void removeRecipeIngredient(int position, ingredientsDetail model) {

        String parameter = "";

        try {
            showprogress("Removing recipe ingredient", "Please wait...");

            final DocumentReference ingredientDocRef = globalclass.firebaseInstance()
                    .collection(Globalclass.ingredientsColl)
                    .document(model.getIngredientId());

            Gson gson = new Gson();
            if (model != null) {
                parameter = gson.toJson(model);
            }
            globalclass.log(tag, "parameter: " + parameter);

            String finalParameter = parameter;

            globalclass.firebaseInstance().runTransaction(new Transaction.Function<Integer>() {

                @Nullable
                @Override
                public Integer apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                    DocumentSnapshot ingredientDoc = transaction.get(ingredientDocRef);
                    if (ingredientDoc.exists()) {
                        ingredientsDetail ingredientsModel = ingredientDoc.toObject(ingredientsDetail.class);
                        transaction.delete(ingredientDocRef);
                        return 0;
                    } else {
                        return -1;
                    }
                }
            }).addOnSuccessListener(activity, new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {

                    hideprogress();
                    if (integer == 0) {

                        ingredients_adapter.deleteData(position);
                        mydatabase.deleteData(mydatabase.allIngredients, "ingredientId", model.getIngredientId());
                        mydatabase.unCheckPerInventory(model.getInventoryId());
                        calculateRecipeCostPrice();
                        globalclass.snackit(activity, "Removed successfully!");
                    } else if (integer == -1) {

                        String error = "return -1";
                        globalclass.log(tag, "removeRecipeIngredient onSuccess: " + error);
                        globalclass.sendResponseErrorLog(tag, "removeRecipeIngredient onSuccess: ", error, finalParameter);
                        globalclass.toast_long("Unable to remove recipe ingredient, please try again later!");
                    }
                }
            }).addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    hideprogress();
                    String error = Log.getStackTraceString(e);
                    globalclass.log(tag, "removeRecipeIngredient onFailure: " + error);
                    globalclass.sendResponseErrorLog(tag, "removeRecipeIngredient onFailure: ", error, finalParameter);
                    globalclass.toast_long("Unable to remove recipe ingredient, please try again later!");
                }
            });

        } catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "removeRecipeIngredient: " + error);
            globalclass.sendResponseErrorLog(tag, "removeRecipeIngredient: ", error, parameter);
            globalclass.toast_long("Unable to remove recipe ingredient, please try again later!");
        }
    }

    void calculateRecipeCostPrice() {

        double sum = 0;

        if (ingredientsArrayList != null && !ingredientsArrayList.isEmpty()) {
            for (int i = 0; i < ingredientsArrayList.size(); i++) {
                ingredientsDetail ingredientsDetailModel = ingredientsArrayList.get(i);
                sum += ingredientsDetailModel.getPrice();
                recipeIngredientPrice = sum;
            }
        }

        recipeCostPrice.setText("Recipe's ingredient price "+ Html.fromHtml(getResources().getString(R.string.typeprice, String.valueOf(sum))));
    }

    void showAddQuantityDialogue(int position, ingredientsDetail ingredientsModel) {

        AlertDialog dialog = new AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                .setTitle("Add " + ingredientsModel.getName() + " " + ingredientsModel.getUnit())
                .setPositiveButton("Add", null)
                .setNeutralButton("Cancel", null)
                .setView(R.layout.addquatitybs)
                .setCancelable(false)
                .show();

        final TextInputLayout quantitytf = dialog.findViewById(R.id.quantitytf);
        final EditText quantity = dialog.findViewById(R.id.quantity);

        if (ingredientsModel.getUnit().equalsIgnoreCase("pieces")) {
            quantity.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        if (ingredientsModel.getQuantity() > 0) {
            if (ingredientsModel.getUnit().equalsIgnoreCase("pieces")) {
                quantity.setText(globalclass.checknull(String.valueOf((int) ingredientsModel.getQuantity())));
            } else {
                quantity.setText(globalclass.checknull(String.valueOf(ingredientsModel.getQuantity())));
            }
        }

        quantity.setSelection(quantity.getText().length());

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globalclass.hidekeyboard_dialogue(dialog);
                if (quantity.getText().toString().length() == 0 ||
                        Double.parseDouble(quantity.getText().toString()) == 0) {
                    quantitytf.setError("Invalid quantity");
                    quantity.requestFocus();
                } else {
                    ingredientsModel.setQuantity(Double.parseDouble(quantity.getText().toString().trim()));

                    if(ingredientsModel.getUnit().equalsIgnoreCase("ml") ||
                            ingredientsModel.getUnit().equalsIgnoreCase("grams")) {
                        inventoryDetail inventoryDetailModel = mydatabase.getParticularInventory(ingredientsModel.getInventoryId());
                        double perPrice = inventoryDetailModel.getCostPrice()/1000;
                        double price = perPrice * ingredientsModel.getQuantity();
                        ingredientsModel.setPrice(price);
                    }
                    else {
                        inventoryDetail inventoryDetailModel = mydatabase.getParticularInventory(ingredientsModel.getInventoryId());
                        ingredientsModel.setPrice(inventoryDetailModel.getCostPrice() * ingredientsModel.getQuantity());
                    }

                    ingredientsModel.setRecipeId(model.getRecipeId());
                    addIngredientQuantity(position, ingredientsModel, dialog);
                }
            }
        });

        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globalclass.hidekeyboard_dialogue(dialog);
                dialog.dismiss();
            }
        });

        quantity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    globalclass.hidekeyboard_dialogue(dialog);
                    if (quantity.getText().toString().length() == 0 ||
                            Double.parseDouble(quantity.getText().toString()) == 0) {
                        quantitytf.setError("Invalid quantity");
                        quantity.requestFocus();
                    } else {
                        ingredientsModel.setQuantity(Double.parseDouble(quantity.getText().toString().trim()));

                        if(ingredientsModel.getUnit().equalsIgnoreCase("ml") ||
                                ingredientsModel.getUnit().equalsIgnoreCase("grams")) {
                            inventoryDetail inventoryDetailModel = mydatabase.getParticularInventory(ingredientsModel.getInventoryId());
                            double perPrice = inventoryDetailModel.getCostPrice()/1000;
                            double price = perPrice * ingredientsModel.getQuantity();
                            ingredientsModel.setPrice(price);
                        }
                        else {
                            inventoryDetail inventoryDetailModel = mydatabase.getParticularInventory(ingredientsModel.getInventoryId());
                            ingredientsModel.setPrice(inventoryDetailModel.getCostPrice() * ingredientsModel.getQuantity());
                        }

                        ingredientsModel.setRecipeId(model.getRecipeId());
                        addIngredientQuantity(position, ingredientsModel, dialog);
                    }
                }

                return false;
            }
        });
    }

    void addIngredientQuantity(int position, ingredientsDetail model, AlertDialog dialog) {

        String parameter = "";

        try {
            showprogress("Adding Quantity", "Please wait...");

            CollectionReference ingredientsColl = globalclass.firebaseInstance().collection(Globalclass.ingredientsColl);

            if (!globalclass.checknull(model.getIngredientId()).equalsIgnoreCase("")) {

                DocumentReference ingredientsDocRef = ingredientsColl.document(model.getIngredientId());

                Gson gson = new Gson();
                if (model != null) {
                    parameter = gson.toJson(model);
                }
                globalclass.log(tag, "parameter: " + parameter);

                String finalParameter = parameter;

                globalclass.firebaseInstance().runTransaction(new Transaction.Function<Integer>() {

                    @Nullable
                    @Override
                    public Integer apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                        DocumentSnapshot ingredientsDoc = transaction.get(ingredientsDocRef);
                        if (ingredientsDoc.exists()) {

                            ingredientsDetail ingredientsModel = ingredientsDoc.toObject(ingredientsDetail.class);
                            Gson gson = new GsonBuilder().create();
                            String json = gson.toJson(model);
                            Map<String, Object> map = new Gson().fromJson(json, Map.class);

                            transaction.update(ingredientsDocRef, map);
                            return 0;
                        } else {

                            return -1;
                        }
                    }
                }).addOnSuccessListener(activity, new OnSuccessListener<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {

                        hideprogress();
                        if (integer == 0) {

                            mydatabase.addAllIngredients(model);
                            ingredients_adapter.updateData(position, model);
                            calculateRecipeCostPrice();
                            dialog.dismiss();
                            globalclass.snackit(activity, "Quantity changed successfully!");
                        } else if (integer == -1) {

                            String error = "return -1";
                            globalclass.log(tag, "addIngredientQuantity onSuccess: " + error);
                            globalclass.sendResponseErrorLog(tag, "addIngredientQuantity onSuccess: ", error, finalParameter);
                            globalclass.toast_long("Unable to add recipe quantity, please remove and add ingredient again!");
                        }
                    }
                }).addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        hideprogress();
                        String error = Log.getStackTraceString(e);
                        globalclass.log(tag, "addIngredientQuantity onFailure: " + error);
                        globalclass.sendResponseErrorLog(tag, "addIngredientQuantity onFailure: ", error, finalParameter);
                        globalclass.toast_long("Unable to add recipe quantity, please try after sometime!");
                    }
                });

//                String ingredientId = model.getIngredientId();
//                model.setIngredientId(ingredientId);
//
//                Gson gson = new GsonBuilder().create();
//                String json = gson.toJson(model);
//                Map<String, Object> map = new Gson().fromJson(json, Map.class);
//
//                DocumentReference ingredientsDocReference = ingredientsColl.document(ingredientId);
//                ingredientsDocReference.update(map)
//                        .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//
//                                hideprogress();
//                                if (task.isSuccessful()) {
//                                    mydatabase.addAllIngredients(model);
//                                    ingredients_adapter.updateData(position, model);
//                                    calculateRecipePrice();
//                                    dialog.dismiss();
//                                    globalclass.snackit(activity,"Quantity changed successfully!");
//                                } else {
//                                    String error = Log.getStackTraceString(task.getException());
//                                    globalclass.log(tag, "addIngredientQuantity: " + error);
//                                    globalclass.toast_long("Unable to add recipe quantity, please try after sometime!");
//                                }
//                            }
//                        });
            } else {

                String ingredientId = ingredientsColl.document().getId();
                model.setIngredientId(ingredientId);

                DocumentReference ingredientsDocReference = ingredientsColl.document(ingredientId);

                String finalParameter1 = parameter;

                ingredientsDocReference.set(model)
                        .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                hideprogress();
                                if (task.isSuccessful()) {
                                    mydatabase.addAllIngredients(model);
                                    ingredients_adapter.updateData(position, model);
                                    calculateRecipeCostPrice();
                                    dialog.dismiss();
                                    globalclass.snackit(activity, "Quantity changed successfully!");
                                } else {
                                    String error = Log.getStackTraceString(task.getException());
                                    globalclass.log(tag, "addIngredientQuantity: " + error);
                                    globalclass.sendResponseErrorLog(tag, "addIngredientQuantity: ", error, finalParameter1);
                                    globalclass.toast_long("Unable to add recipe quantity, please try after sometime!");
                                }
                            }
                        });
            }
        } catch (Exception e) {
            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addIngredientQuantity: " + error);
            globalclass.sendResponseErrorLog(tag, "addIngredientQuantity: ", error, parameter);
            globalclass.toast_long("Unable to add recipe quantity, please try after sometime!");
        }
    }

    void getCategoryData() {
        categoryDetailArrayList.clear();
        categoryList.clear();

        categoryDetailArrayList = mydatabase.getRecipeCategoryList();
        if (!categoryDetailArrayList.isEmpty()) {

            fillCategoryList();
        } else {
            getRecipeCategoryList();
        }
    }

    void getRecipeCategoryList() {

        showprogress("Refreshing category list", "Please wait...");

        try {
            CollectionReference recipeCategoryColl = globalclass.firebaseInstance().collection(Globalclass.recipeCategoryColl);
            recipeCategoryColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    hideprogress();
                    globalclass.snackit(activity, "Refresh successfully!");

                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (DocumentSnapshot documents : task.getResult()) {
                                recipeCategoryDetail model = documents.toObject(recipeCategoryDetail.class);
                                globalclass.log(tag, "Category name: " + model.getCategoryName());
                                mydatabase.addRecipeCategory(model);
                            }
                        } else {
                            globalclass.snackit(activity, "No Recipe Category found!");
                        }
                    } else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag, "getRecipeCategoryList: " + error);
                        globalclass.sendErrorLog(tag, "getRecipeCategoryList: ", error);
                        globalclass.toast_long("Unable to get Recipe categories, please refresh & try again later!");
                    }
                }
            });
        } catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeCategoryList: " + error);
            globalclass.sendErrorLog(tag, "getRecipeCategoryList: ", error);
            globalclass.toast_long("Unable to get Recipe categories, please refresh & try again later!");
        }
    }

    void fillCategoryList() {

        if (!categoryDetailArrayList.isEmpty()) {
            for (int i = 0; i < categoryDetailArrayList.size(); i++) {
                categoryList.add(globalclass.firstLetterCapital(categoryDetailArrayList.get(i).getCategoryName()));
            }

            autoSelectCategory();
            categoryName.setText(categoryList.get(selectedCategoryPos));
        }
    }

    void autoSelectCategory() {
        if (!globalclass.checknull(model.getCategoryId()).equalsIgnoreCase("") &&
                !categoryDetailArrayList.isEmpty() &&
                !categoryList.isEmpty()) {
            for (int i = 0; i < categoryDetailArrayList.size(); i++) {
                if (model.getCategoryId().equalsIgnoreCase(categoryDetailArrayList.get(i).getCategoryId())) {
                    selectedCategoryPos = i;
                    return;
                }
            }
        }
    }

    void showCategoryDialogue() {

        if (categoryDetailArrayList.isEmpty()) {
            globalclass.snackit(activity, "No category found, please refresh and try again!");
            return;
        }

        CharSequence[] charSequence = categoryList.toArray(new CharSequence[categoryList.size()]);
        MaterialAlertDialogBuilder materialAlertDialogBuilder =
                new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                        .setTitle("Select Recipe Category")
                        .setCancelable(false)
                        .setSingleChoiceItems(charSequence, selectedCategoryPos, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                selectedCategoryPos = pos;
                                categoryName.setText(categoryList.get(selectedCategoryPos));
                                enableUpdateButton();
                                dialogInterface.dismiss();
                            }
                        });
        materialAlertDialogBuilder.show();
    }

    void fillRecipeTypeList() {

        recipeTypeList.add(getString(R.string.veg));
        recipeTypeList.add(getString(R.string.nonveg));

        autoSelectRecipeType();
        recipeType.setText(recipeTypeList.get(selectedRecipeTypePos));
    }

    void autoSelectRecipeType() {
        if (!globalclass.checknull(model.getRecipeType()).equalsIgnoreCase("") &&
                !recipeTypeList.isEmpty()) {
            for (int i = 0; i < recipeTypeList.size(); i++) {
                if (model.getRecipeType().equalsIgnoreCase(recipeTypeList.get(i))) {
                    selectedRecipeTypePos = i;
                    return;
                }
            }
        }
    }

    void showRecipeTypeDialogue() {

        CharSequence[] charSequence = recipeTypeList.toArray(new CharSequence[recipeTypeList.size()]);
        MaterialAlertDialogBuilder materialAlertDialogBuilder =
                new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                        .setTitle("Select Recipe Type")
                        .setCancelable(false)
                        .setSingleChoiceItems(charSequence, selectedRecipeTypePos, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                selectedRecipeTypePos = pos;
                                recipeType.setText(recipeTypeList.get(selectedRecipeTypePos));
                                enableUpdateButton();
                                dialogInterface.dismiss();
                            }
                        });
        materialAlertDialogBuilder.show();
    }

    public void showhide_addRecipePhotolo() {
        globalclass.log(tag, String.valueOf(selectedimagesArrayList.size()) + " images available");
        if (updateRecipeImagesArrayList.size() == 0) {
            recipePhotosRecyclerview.setVisibility(View.GONE);
        } else {
            recipePhotosRecyclerview.setVisibility(View.VISIBLE);
        }

        if (updateRecipeImagesArrayList.size() >= globalclass.maxRecipeImages()) {
            addRecipePhotolo.setVisibility(View.GONE);
        } else {
            addRecipePhotolo.setVisibility(View.VISIBLE);
        }
    }

    void showHideInventoryRV() {
        if (ingredientsArrayList != null && !ingredientsArrayList.isEmpty()) {
            inventoryRecyclerView.setVisibility(View.VISIBLE);
        } else {
            inventoryRecyclerView.setVisibility(View.GONE);
        }
    }

    void fillRecipeInstructionsList() {
        recipeInstructionList = mydatabase.getParticularRecipeInstructions(recipeId);
        if (recipeInstructionList != null && !recipeInstructionList.isEmpty()) {
            for (int i = 0; i < recipeInstructionList.size(); i++) {
                String instruction = recipeInstructionList.get(i).getInstruction();
                recipeInstructionsArrayList.add(instruction);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                selectedimagesArrayList.clear();
                selectedimagesArrayList.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                addToList();
                showhide_addRecipePhotolo();
                enableUpdateButton();
            }

            globalclass.log(tag, String.valueOf(selectedimagesArrayList.size()) + " images selected");
        } else if (requestCode == INVENTORY_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                ingredientsArrayList = data.getExtras().getParcelableArrayList("arrayList");
                showHideInventoryRV();
                if (ingredientsArrayList != null && !ingredientsArrayList.isEmpty()) {
                    setInventoryListAdapter();
                }
            }
        } else if (requestCode == INSTRUCTION_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                recipeInstructionsArrayList = data.getStringArrayListExtra("arrayList");
                enableUpdateButton();
            }
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
                setText();
            } catch (Exception e) {
                String error = Log.getStackTraceString(e);
                globalclass.log(tag, "AddRecipeReceiver: " + error);
                globalclass.sendErrorLog(tag, "AddRecipeReceiver: ", error);
                globalclass.toast_long("Error in refreshing data, please swipe down to refresh data!");
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(AddRecipeReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                AddRecipeReceiver, new IntentFilter(Globalclass.AddRecipeReceiver));
    }
}