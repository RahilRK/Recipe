package com.hksofttronix.khansama.AccountFrag;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hksofttronix.khansama.Address.Address;
import com.hksofttronix.khansama.BuildConfig;
import com.hksofttronix.khansama.ContactUs;
import com.hksofttronix.khansama.Favourite.Favourite;
import com.hksofttronix.khansama.Login;
import com.hksofttronix.khansama.MainActivity;
import com.hksofttronix.khansama.PlaceOrder.PlaceOrder;
import com.hksofttronix.khansama.R;
import com.hksofttronix.khansama.Globalclass;
import com.hksofttronix.khansama.Mydatabase;
import com.hksofttronix.khansama.UpdateAccountDetail.UpdateAccountDetail;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.util.List;

public class AccountFrag extends Fragment {

    String tag = this.getClass().getSimpleName();
    AppCompatActivity activity;

    Globalclass globalclass;
    Mydatabase mydatabase;

    RelativeLayout loginLayout;
    MaterialButton loginbt;
    LinearLayout mainLayout;
    TextView name, viewAccountDetails;
    RelativeLayout orderLayout, favouriteLayout, addressBookLayout, shareAppLayout, rateUsLayout, contactUsLayout, logoutLayout;

    ProgressDialog progressDialog;

    public AccountFrag() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof AppCompatActivity) {
            activity = (AppCompatActivity) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
        binding(view);
        onClick();

        if (globalclass.checkUserIsLoggedIn()) {
            loginLayout.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
            setText();
        } else {
            mainLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        }
    }

    void init() {
        globalclass = Globalclass.getInstance(activity);
        mydatabase = Mydatabase.getInstance(activity);
    }

    void binding(View view) {
        mainLayout = view.findViewById(R.id.mainLayout);
        loginLayout = view.findViewById(R.id.loginLayout);
        loginbt = view.findViewById(R.id.loginbt);
        name = view.findViewById(R.id.name);
        viewAccountDetails = view.findViewById(R.id.viewAccountDetails);
        orderLayout = view.findViewById(R.id.orderLayout);
        favouriteLayout = view.findViewById(R.id.favouriteLayout);
        addressBookLayout = view.findViewById(R.id.addressBookLayout);
        shareAppLayout = view.findViewById(R.id.shareAppLayout);
        rateUsLayout = view.findViewById(R.id.rateUsLayout);
        contactUsLayout = view.findViewById(R.id.contactUsLayout);
        logoutLayout = view.findViewById(R.id.logoutLayout);
    }

    void onClick() {

        viewAccountDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, UpdateAccountDetail.class));
            }
        });

        orderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, PlaceOrder.class));
            }
        });

        favouriteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, Favourite.class));
            }
        });

        addressBookLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, Address.class));
            }
        });

        shareAppLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestStoragePermission();
            }
        });

        rateUsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("market://details?id=" + activity.getPackageName()));
                startActivity(i);
            }
        });

        contactUsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(activity, ContactUs.class));
            }
        });

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                        .setTitle("Sure")
                        .setMessage("Are you sure you want to logout?")
                        .setCancelable(false)
                        .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                                globalclass.unsubscribeToTopic("allUser");
                                globalclass.unsubscribeToTopic(globalclass.getStringData("mobilenumber"));

                                globalclass.clearPref();
                                mydatabase.clearDatabase();

                                startActivity(new Intent(activity, MainActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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

        loginbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(activity, Login.class));
            }
        });
    }

    void setText() {
        if (globalclass.checknull(globalclass.getStringData("name")).equalsIgnoreCase("")) {
            name.setText(globalclass.firstLetterCapital("hello..."));
        } else {
            name.setText(globalclass.firstLetterCapital(globalclass.getStringData("name")));
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

                            shareApp();
                        }

                        if (report.getDeniedPermissionResponses().size() > 0) {

                            globalclass.snackit(activity, "Storage permission required!");
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

    void shareApp() {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/KhanSama.png");
            if (file.exists()) {
                Uri uri = FileProvider.getUriForFile(activity,
                        BuildConfig.APPLICATION_ID,
                        file);

                Intent intent = ShareCompat.IntentBuilder.from(activity)
                        .setStream(uri) // uri from FileProvider
                        .setType("image/*")
                        .getIntent()
                        .setAction(Intent.ACTION_SEND) //Change if needed
                        .setDataAndType(uri, "image/*")
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .putExtra(android.content.Intent.EXTRA_SUBJECT, "EXTRA_SUBJECT")
                        .putExtra(Intent.EXTRA_TEMPLATE, "EXTRA_TEMPLATE")
                        .putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.shareapptext));
                startActivity(Intent.createChooser(intent, "Share on"));
            } else {
                if (!globalclass.isInternetPresent()) {
                    globalclass.toast_long(getString(R.string.noInternetConnection));
                    return;
                }

                String imageUrl = globalclass.appShareImageUrl();

                downloadAppShareImage(imageUrl);

                //                if(downloadAppShareImage(imageUrl)) {
//                    shareApp();
//                }
//                else {
//                    String error = "downloadAppShareImage is false!";
//                    globalclass.log(tag, "shareApp: "+error);
//                    globalclass.toast_long("Something went wrong in share, please try again!");
//                    globalclass.sendErrorLog(tag,"shareApp",error);
//                }
            }
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "shareApp: " + error);
            globalclass.toast_long("Something went wrong in share, please try again!");
            globalclass.sendErrorLog(tag, "shareApp", error);
        }
    }

    public boolean downloadAppShareImage(String appShareImage) {
        try {

            showprogress("Hold on", "Please wait...");

            long result;

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(appShareImage));
            request.setTitle("KhanSama.png");
            request.setDescription("Please wait");
//        request.setAllowedNetworkTypes(3);
//        request.setAllowedOverRoaming(false);
//        request.allowScanningByMediaScanner();
//        request.setVisibleInDownloadsUi(true);
//        request.setMimeType("application/pdf");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "KhanSama.png");

            //todo get download status
            DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            result = manager.enqueue(request);

            DownloadManager.Query ImageDownloadQuery = new DownloadManager.Query();
            //set the query filter to our previously Enqueued download
            ImageDownloadQuery.setFilterById(result);

            //Query the download manager about downloads that have been requested.
            Cursor cursor = manager.query(ImageDownloadQuery);
            if (cursor.moveToFirst()) {
                //column for download  status
                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(columnIndex);

                String statusText = "";

                if (status == DownloadManager.STATUS_FAILED) {
                    statusText = "STATUS_FAILED";
                    globalclass.log(tag, statusText);
                    return false;
                } else {
                    statusText = "STATUS_SUCCESSFUL";
                    globalclass.log(tag, statusText);
                    return true;
                }
            }
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "downloadAppShareImage: " + error);
            globalclass.toast_long("Something went wrong in share, please try again!");
            globalclass.sendErrorLog(tag, "downloadAppShareImage", error);
            return false;
        }

        return false;
    }

    public BroadcastReceiver DownloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                globalclass.log(tag, "DownloadCompleteReceiver: onReceive");
                new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        hideprogress();
                        shareApp();
                    }
                },1000);

            } catch (Exception e) {

                String error = Log.getStackTraceString(e);
                globalclass.log(tag, "DownloadCompleteReceiver: " + error);
                globalclass.toast_long("Something went wrong in share, please try again!");
                globalclass.sendErrorLog(tag, "DownloadCompleteReceiver", error);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(activity).registerReceiver(
                DownloadCompleteReceiver, new IntentFilter(Globalclass.DownloadCompleteReceiver));
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(activity).unregisterReceiver(DownloadCompleteReceiver);
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