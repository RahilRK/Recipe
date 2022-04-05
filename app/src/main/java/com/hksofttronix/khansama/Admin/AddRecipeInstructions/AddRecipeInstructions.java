package com.hksofttronix.khansama.Admin.AddRecipeInstructions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;

import java.util.ArrayList;

public class AddRecipeInstructions extends AppCompatActivity {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity = AddRecipeInstructions.this;

    Globalclass globalclass;

    RecyclerView recyclerView;
    ExtendedFloatingActionButton extendedFloatingActionButton;

    LinearLayoutManager linearLayoutManager;

    ArrayList<String> arrayList;
    AddRecipeInstructionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addrecipe_instructions);

        init();
        binding();
        onClick();
        setToolbar();

        arrayList = getIntent().getStringArrayListExtra("arrayList");
        setAdapter();
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
    }

    void binding() {
        recyclerView = findViewById(R.id.recyclerView);
        extendedFloatingActionButton = findViewById(R.id.extendedFloatingActionButton);
    }

    void onClick() {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (dy >0) {
                    // Scroll Down
                    extendedFloatingActionButton.hide();
                }
                else if (dy <0) {

                    // Scroll Up
                    extendedFloatingActionButton.show();
                }
            }
        });

        extendedFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(arrayList.isEmpty()) {
                    showAddInstructionDialogue(0, "","add");
                }
                else {
                    showAddInstructionDialogue(arrayList.size(), "","add");
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
        toolbar.setTitle(getString(R.string.recipeinstructions));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
    }

    void setAdapter() {
        adapter = new AddRecipeInstructionAdapter(activity, arrayList, new AddRecipeInstructionOnClick() {
            @Override
            public void onClick(int position, String text) {
                showAddInstructionDialogue(position,text, "update");
            }

            @Override
            public void delete(int position) {

                new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                        .setTitle("Sure")
                        .setMessage("Are you sure you want to remove?")
                        .setCancelable(false)
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                adapter.deleteData(position);
                                dialog.dismiss();
                            }
                        })
                        .setNeutralButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                            }
                        })
                        .show();

            }
        });

        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    void showAddInstructionDialogue(int position, String text, String action) {

        String stepNumber = String.valueOf(position+1);
        AlertDialog dialog = new AlertDialog.Builder(activity,R.style.CustomAlertDialog)
                .setTitle("Step #"+stepNumber)
                .setPositiveButton("Add",null)
                .setNeutralButton("Cancel",null)
                .setView(R.layout.addrecipeinstructionbs)
                .setCancelable(false)
                .show();

        TextInputLayout instructiontf = dialog.findViewById(R.id.instructiontf);
        EditText instruction = dialog.findViewById(R.id.instruction);

        instruction.setText(text);

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globalclass.hidekeyboard_dialogue(dialog);
                if(instruction.getText().toString().length() < 10) {
                    instructiontf.setError("Should contain atleast 10 characters");
                }
                else if(!instruction.getText().toString().matches(globalclass.alphaNumericRegexOne())) {
                    instructiontf.setError("Invalid instruction");
                }
                else {
                    if(action.equalsIgnoreCase("add")) {
                        adapter.addData(position,
                                instruction.getText().toString().trim().replaceAll(System.lineSeparator(), " "));
                    }
                    else {
                        adapter.updateData(position,
                                instruction.getText().toString().trim().replaceAll(System.lineSeparator(), " "));
                    }

                    dialog.dismiss();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipeinstruction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_addrecipeinstruction:
                Intent intent = new Intent();
                intent.putStringArrayListExtra("arrayList",arrayList);
                setResult(RESULT_OK, intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        Intent intent = new Intent();
        intent.putStringArrayListExtra("arrayList",arrayList);
        setResult(RESULT_OK, intent);
        finish();
    }
}