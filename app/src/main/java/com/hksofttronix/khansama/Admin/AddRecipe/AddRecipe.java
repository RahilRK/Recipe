package com.hksofttronix.khansama.Admin.AddRecipe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hksofttronix.khansama.Admin.AddRecipeInstructions.AddRecipeInstructions;
import com.hksofttronix.khansama.Admin.SelectInventory.SelectInventory;
import com.hksofttronix.khansama.Models.ingredientsDetail;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.recipeCategoryDetail;
import com.hksofttronix.khansama.Models.recipeDetail;
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
import java.util.List;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class AddRecipe extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = AddRecipe.this;

    Globalclass globalclass;
    Mydatabase mydatabase;

    TextInputLayout recipenametf,additionalChargestf,amounttf;
    EditText recipename,additionalCharges,amount;
    LinearLayout addRecipePhotolo, addInventorylo, addInstructionslo,vegNonVeglo;
    RelativeLayout addCategoryNamelo;
    RecyclerView recipePhotosRecyclerview, inventoryRecyclerView;
    TextView addRecipeInstruction,categoryName,recipeType,recipeCostPrice;
    ImageView ivrefreshCategory;
    MaterialButton addRecipebt;

    ArrayList<String> selectedimagesArrayList = new ArrayList<>();
    AddRecipePhotos_adapter addRecipePhotos_adapter;

    ArrayList<ingredientsDetail> ingredientsArrayList = new ArrayList<>();
    ingredientsAdapter adapter;

    ArrayList<String> recipeInstructionsArrayList = new ArrayList<>();

    int INVENTORY_LIST = 111;
    int INSTRUCTION_LIST = 222;

    ArrayList<recipeCategoryDetail> categoryDetailArrayList = new ArrayList<>();
    ArrayList<String> categoryList = new ArrayList<>();
    int selectedCategoryPos = -1;

    ArrayList<String> recipeTypeList = new ArrayList<>();
    int selectedRecipeTypePos = -1;

    double recipeIngredientPrice = 0;

    boolean checkDetailsAdded = false;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        setToolbar();
        init();
        binding();
        onClick();
        setRecipePhotosAdapter();
        getCategoryData();
        fillRecipeTypeList();
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
        addRecipebt = findViewById(R.id.addRecipebt);
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

                if(editable.length() > 0) {
                    if(mydatabase.checkRecipeExistInAllRecipe(recipename.getText().toString().trim().toLowerCase())) {
                        recipenametf.setError(recipename.getText().toString().trim().toUpperCase()+" already exist");
                    }
                    else {
                        recipenametf.setErrorEnabled(false);
                    }
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
                intent.putExtra("action", tag);
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
                        showConfirmDialogue();
                    }
                }
                return false;
            }
        });

        addRecipebt.setOnClickListener(new View.OnClickListener() {
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

    void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_backarrow);
        toolbar.setTitle(getString(R.string.add_recipe));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void showConfirmDialogue() {

        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure")
                .setMessage("Are you sure you want to add Recipe ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        checkRecipeExist();
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

    void checkRecipeExist() {

        try {
            showprogress("Hold on","Please wait...");

            CollectionReference recipeColl = globalclass.firebaseInstance().collection(Globalclass.recipeColl);
            Query checkInventoryExist = recipeColl
                    .whereEqualTo("recipeName",recipename.getText().toString().trim().toLowerCase());
            checkInventoryExist.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    String source = task.getResult().getMetadata().isFromCache() ?
                            "local cache" : "server";
                    globalclass.log(tag,"checkRecipeExist - Data fetched from "+source);

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {

                            hideprogress();
                            globalclass.snackit(activity,recipename.getText().toString().trim().toUpperCase()+" already exist!");
                        }
                        else {
                            addRecipeDetails();
                        }
                    }
                    else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"checkRecipeExist: "+error);
                        globalclass.snackit(activity, "Unable to add recipe, please try again later!");
                        globalclass.sendErrorLog(tag,"checkRecipeExist: ",error);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"checkRecipeExist: "+error);
            globalclass.snackit(activity, "Unable to add recipe, please try again later!");
            globalclass.sendErrorLog(tag,"checkRecipeExist: ",error);
        }

    }

    void addRecipeDetails() {

        try {
            storeRecipeDetails();
            storeIngredientsDetails();
            storePhotosDetails();
            storeRecipeInstructions();

            globalclass.toast_long("You will notify you when recipe details will be added!");
            globalclass.startService(activity, AddRecipeService.class);

            checkDetailsAdded = false;
            onBackPressed();

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addRecipeDetails: " + error);
            globalclass.snackit(activity, "Unable to add recipe, please try again later!");
            globalclass.sendErrorLog(tag,"addRecipeDetails: ",error);
        }
    }

    void storeRecipeDetails() {
        recipeDetail recipeDetailsmodel = new recipeDetail();
        recipeDetailsmodel.setRecipeName(recipename.getText().toString().trim().toLowerCase());
        if(additionalCharges.getText().length() == 0) {
            recipeDetailsmodel.setAdditionalCharges(0);
        }
        else {
            recipeDetailsmodel.setAdditionalCharges(Integer.parseInt(additionalCharges.getText().toString().trim()));
        }
        recipeDetailsmodel.setPrice(Integer.parseInt(amount.getText().toString().trim()));
        recipeDetailsmodel.setCategoryId(categoryDetailArrayList.get(selectedCategoryPos).getCategoryId());
        recipeDetailsmodel.setCategoryName(categoryDetailArrayList.get(selectedCategoryPos).getCategoryName().trim().toLowerCase());
        recipeDetailsmodel.setRecipeType(recipeTypeList.get(selectedRecipeTypePos));
        recipeDetailsmodel.setStatus(false);
        recipeDetailsmodel.setActions("Add");
        mydatabase.uploadRecipeDetail(recipeDetailsmodel);
    }

    void storeIngredientsDetails() {
        for (int i = 0; i < ingredientsArrayList.size(); i++) {
            ingredientsDetail model = ingredientsArrayList.get(i);
            model.setRecipeName(recipename.getText().toString().trim().toLowerCase());
            mydatabase.uploadIngredients(model);
        }
    }

    void storePhotosDetails() {
        try {
            for (int i = 0; i < selectedimagesArrayList.size(); i++) {

                String imageName = String.valueOf(globalclass.getMilliSecond()) + ".png";
                recipePhotoDetail model = new recipePhotoDetail();
                model.setRecipeName(recipename.getText().toString().trim().toLowerCase());
                model.setImageName(imageName);
                model.setFilepath(selectedimagesArrayList.get(i));
                if(i==0) {
                    model.setFirstImage(true);
                }
                mydatabase.uploadRecipePhoto(model);
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "storePhotosDetails: "+error);
            globalclass.toast_long( "Unable to add recipe, please try again later!");
            globalclass.sendErrorLog(tag,"storePhotosDetails: ",error);
        }
    }

    void storeRecipeInstructions() {
        for (int i = 0; i < recipeInstructionsArrayList.size(); i++) {
            mydatabase.addRecipeInstructions(recipename.getText().toString().trim().toLowerCase(),
                    recipeInstructionsArrayList.get(i));
        }
    }

    void setInventoryListAdapter() {

        adapter = new ingredientsAdapter(activity, ingredientsArrayList, new ingredientsOnClick() {
            @Override
            public void onAddQuantity(int position, ingredientsDetail model) {
                showAddQuantityDialogue(position,model);
            }

            @Override
            public void onDelete(int position, ingredientsDetail model) {
                adapter.deleteData(position);
                mydatabase.unCheckPerInventory(model.getInventoryId());
                calculateRecipeCostPrice();
            }
        });
        inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        inventoryRecyclerView.setAdapter(adapter);
    }

    void calculateRecipeCostPrice() {

        double sum = 0;

        if(ingredientsArrayList != null && !ingredientsArrayList.isEmpty()) {
            for(int i=0;i<ingredientsArrayList.size();i++) {
                ingredientsDetail ingredientsDetailModel = ingredientsArrayList.get(i);
                sum += ingredientsDetailModel.getPrice();
                recipeIngredientPrice = sum;
            }
        }

        recipeCostPrice.setText("Recipe's ingredient price "+Html.fromHtml(getResources().getString(R.string.typeprice, String.valueOf(sum))));
    }

    void showAddQuantityDialogue(int position, ingredientsDetail ingredientsModel) {

        AlertDialog dialog = new AlertDialog.Builder(activity,R.style.CustomAlertDialog)
                .setTitle("Add "+ ingredientsModel.getName()+" "+ ingredientsModel.getUnit())
                .setPositiveButton("Add",null)
                .setNeutralButton("Cancel",null)
                .setView(R.layout.addquatitybs)
                .setCancelable(false)
                .show();

        final TextInputLayout quantitytf = dialog.findViewById(R.id.quantitytf);
        final EditText quantity = dialog.findViewById(R.id.quantity);

        if(ingredientsModel.getUnit().equalsIgnoreCase("pieces")) {
            quantity.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        if(ingredientsModel.getQuantity() > 0) {
            if(ingredientsModel.getUnit().equalsIgnoreCase("pieces")) {
                quantity.setText(globalclass.checknull(String.valueOf((int)ingredientsModel.getQuantity())));
            }
            else {
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

                    adapter.updateData(position, ingredientsModel);
                    calculateRecipeCostPrice();
                    dialog.dismiss();
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

                        adapter.updateData(position, ingredientsModel);
                        calculateRecipeCostPrice();
                        dialog.dismiss();
                    }
                }

                return false;
            }
        });
    }

    void getCategoryData() {
        categoryDetailArrayList.clear();
        categoryList.clear();

        categoryDetailArrayList = mydatabase.getRecipeCategoryList();
        if(!categoryDetailArrayList.isEmpty()) {

            fillCategoryList();
        }
        else {
            getRecipeCategoryList();
        }
    }

    void getRecipeCategoryList() {

        showprogress("Refreshing category list","Please wait...");

        try {
            CollectionReference recipeCategoryColl = globalclass.firebaseInstance().collection(Globalclass.recipeCategoryColl);
            recipeCategoryColl.get().addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    hideprogress();
                    globalclass.snackit(activity,"Refresh successfully!");

                    if(task.isSuccessful()) {
                        if(!task.getResult().isEmpty()) {
                            for(DocumentSnapshot documents : task.getResult()) {
                                recipeCategoryDetail model = documents.toObject(recipeCategoryDetail.class);
                                globalclass.log(tag,"Category name: "+model.getCategoryName());
                                mydatabase.addRecipeCategory(model);
                            }
                        }
                        else {
                            globalclass.snackit(activity,"No Recipe Category found!");
                        }
                    }
                    else {
                        hideprogress();
                        String error = Log.getStackTraceString(task.getException());
                        globalclass.log(tag,"getRecipeCategoryList: "+error);
                        globalclass.sendErrorLog(tag,"getRecipeCategoryList: ",error);
                    }
                }
            });
        }
        catch (Exception e) {

            hideprogress();
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"getRecipeCategoryList: "+error);
            globalclass.sendErrorLog(tag,"getRecipeCategoryList: ",error);
        }

    }

    void fillCategoryList() {

        if(!categoryDetailArrayList.isEmpty()) {
            for(int i=0;i<categoryDetailArrayList.size();i++) {
                categoryList.add(globalclass.firstLetterCapital(categoryDetailArrayList.get(i).getCategoryName()));
            }
        }
    }

    void showCategoryDialogue() {

        if(categoryDetailArrayList.isEmpty()) {
            globalclass.snackit(activity,"No category found, please refresh and try again!");
            return;
        }

        CharSequence[] charSequence = categoryList.toArray(new CharSequence[categoryList.size()]);
        MaterialAlertDialogBuilder materialAlertDialogBuilder =
                new MaterialAlertDialogBuilder(activity,R.style.RoundShapeTheme)
                        .setTitle("Select Recipe category")
                        .setCancelable(false)
                        .setSingleChoiceItems(charSequence, selectedCategoryPos, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                selectedCategoryPos = pos;
                                categoryName.setText(categoryList.get(selectedCategoryPos));
                                dialogInterface.dismiss();
                            }
                        });
        materialAlertDialogBuilder.show();
    }

    void fillRecipeTypeList() {

        recipeTypeList.add(getString(R.string.veg));
        recipeTypeList.add(getString(R.string.nonveg));
    }

    void showRecipeTypeDialogue() {

        CharSequence[] charSequence = recipeTypeList.toArray(new CharSequence[recipeTypeList.size()]);
        MaterialAlertDialogBuilder materialAlertDialogBuilder =
                new MaterialAlertDialogBuilder(activity,R.style.RoundShapeTheme)
                        .setTitle("Select Recipe Type")
                        .setCancelable(false)
                        .setSingleChoiceItems(charSequence, selectedRecipeTypePos, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
                                selectedRecipeTypePos = pos;
                                recipeType.setText(recipeTypeList.get(selectedRecipeTypePos));
                                dialogInterface.dismiss();
                            }
                        });
        materialAlertDialogBuilder.show();
    }

    boolean validation() {

        if (selectedimagesArrayList.isEmpty()) {
            globalclass.snackit(activity, "Please add recipe images!");
            return false;
        }
        else if (recipename.getText().length() < 3) {
            recipenametf.setError("Should contain atleast 3 characters!");
            return false;
        }
        else if (!recipename.getText().toString().trim().matches(globalclass.alphaNumericRegexOne())) {
            recipenametf.setError("Invalid Recipe name!");
            return false;
        }
        else if (selectedCategoryPos < 0) {
            globalclass.snackit(activity, "Please select recipe category!");
            return false;
        }
        else if (ingredientsArrayList.isEmpty()) {
            globalclass.snackit(activity, "Please add recipe items!");
            return false;
        }
        else if (!checkAllItemQuantityIsEntered()) {
            globalclass.snackit(activity, "Please enter the quantity for all items!");
            return false;
        }
        else if (recipeInstructionsArrayList.isEmpty()) {
            globalclass.snackit(activity, "Please enter recipe instructions!");
            return false;
        }
        else if (selectedRecipeTypePos < 0) {
            globalclass.snackit(activity, "Please select recipe type!");
            return false;
        }
        else if (additionalCharges.getText().length() > 0 && Integer.parseInt(additionalCharges.getText().toString()) <= 0) {
            additionalCharges.setError("Invalid Additional Charges!");
            return false;
        }
        else if (amount.getText().length() == 0 || Integer.parseInt(amount.getText().toString()) == 0) {
            amounttf.setError("Invalid amount!");
            return false;
        }
        else if(Integer.parseInt(amount.getText().toString()) <= recipeIngredientPrice) {
            amounttf.setError("Invalid amount!");
            globalclass.showDialogue(activity,"Invalid amount","Recipe should be price more then it's ingredients purchase price, to avoid loss!");
            return false;
        }
        else if(mydatabase.checkRecipeExistInAllRecipe(recipename.getText().toString().trim().toLowerCase())) {
            recipenametf.setError(recipename.getText().toString().trim().toUpperCase()+" is already exist!");
            return false;
        }
        else if(mydatabase.recipePendingToUpload(recipename.getText().toString().trim().toLowerCase())) {
            globalclass.snackit(activity,recipename.getText().toString().trim().toUpperCase()+" is already been added, please wait till uploading finish!");
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
        FilePickerBuilder.getInstance().setMaxCount(globalclass.maxRecipeImages())
                .setSelectedFiles(selectedimagesArrayList)
                .setActivityTheme(R.style.ImagePickerTheme)
                .enableCameraSupport(false)
                .pickPhoto(activity);
    }

    void setRecipePhotosAdapter() {
        recipePhotosRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        addRecipePhotos_adapter = new AddRecipePhotos_adapter(activity, selectedimagesArrayList, new addRecipePhotosOnClick() {
            @Override
            public void onDelete(int position) {
                addRecipePhotos_adapter.deleteItem(position);
                showhide_addRecipePhotolo();
            }
        });
        recipePhotosRecyclerview.setAdapter(addRecipePhotos_adapter);
    }

    public void showhide_addRecipePhotolo() {
        globalclass.log(tag, String.valueOf(selectedimagesArrayList.size()) + " images available");
        if (selectedimagesArrayList.size() == 0) {
            recipePhotosRecyclerview.setVisibility(View.GONE);
        } else {
            recipePhotosRecyclerview.setVisibility(View.VISIBLE);
        }

        if (selectedimagesArrayList.size() >= globalclass.maxRecipeImages()) {
            addRecipePhotolo.setVisibility(View.GONE);
        } else {
            addRecipePhotolo.setVisibility(View.VISIBLE);
        }
    }

    void showHideInventoryRV() {
        if (!ingredientsArrayList.isEmpty()) {
            inventoryRecyclerView.setVisibility(View.VISIBLE);
        } else {
            inventoryRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                checkDetailsAdded = true;
                selectedimagesArrayList.clear();
                selectedimagesArrayList.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                addRecipePhotos_adapter.notifyDataSetChanged();
            }

            globalclass.log(tag, String.valueOf(selectedimagesArrayList.size()) + " images selected");
        } else if (requestCode == INVENTORY_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                checkDetailsAdded = true;
                ingredientsArrayList = data.getExtras().getParcelableArrayList("arrayList");
                showHideInventoryRV();
                setInventoryListAdapter();
            }
        } else if (requestCode == INSTRUCTION_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                checkDetailsAdded = true;
                recipeInstructionsArrayList = data.getStringArrayListExtra("arrayList");
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

    @Override
    protected void onResume() {
        super.onResume();

        showhide_addRecipePhotolo();
    }

    boolean checkDetailsAdded() {

        if(selectedimagesArrayList != null && !selectedimagesArrayList.isEmpty()) {
            return true;
        }

        if(ingredientsArrayList != null && !ingredientsArrayList.isEmpty()) {
            return true;
        }

        if(recipeInstructionsArrayList != null && !recipeInstructionsArrayList.isEmpty()) {
            return true;
        }

        return false;
    }

    void showCancelDialogue() {

        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle("Sure")
                .setMessage("Are you sure you want to cancel ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        checkDetailsAdded = false;
                        onBackPressed();
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

    @Override
    public void onBackPressed() {

        if(checkDetailsAdded() && checkDetailsAdded) {
            showCancelDialogue();
        }
        else {
            super.onBackPressed();
        }
    }
}