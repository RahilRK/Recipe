package com.hksofttronix.khansama;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;


import com.hksofttronix.khansama.Log.sendLogService;
import com.hksofttronix.khansama.Models.addressDetail;
import com.hksofttronix.khansama.Models.allOrderItemDetail;
import com.hksofttronix.khansama.Models.cartDetail;
import com.hksofttronix.khansama.Models.checkStockExistDetail;
import com.hksofttronix.khansama.Models.cityDetail;
import com.hksofttronix.khansama.Models.favouriteDetail;
import com.hksofttronix.khansama.Models.ingredientsDetail;
import com.hksofttronix.khansama.Models.inventoryDetail;
import com.hksofttronix.khansama.Models.navMenuDetail;
import com.hksofttronix.khansama.Models.orderItemDetail;
import com.hksofttronix.khansama.Models.orderStatusDetail;
import com.hksofttronix.khansama.Models.orderSummaryDetail;
import com.hksofttronix.khansama.Models.placeOrderDetail;
import com.hksofttronix.khansama.Models.recipeCategoryDetail;
import com.hksofttronix.khansama.Models.recipeDetail;
import com.hksofttronix.khansama.Models.recipeInstructionDetail;
import com.hksofttronix.khansama.Models.recipePhotoDetail;
import com.hksofttronix.khansama.Models.stateDetail;
import com.hksofttronix.khansama.Models.unitDetail;
import com.hksofttronix.khansama.Models.vendorDetail;
import com.hksofttronix.khansama.Models.logModel;

import java.util.ArrayList;
import java.util.List;

public class Mydatabase extends SQLiteOpenHelper {
    String tag = this.getClass().getSimpleName();
    Context context;
    Globalclass globalclass;

    private static final String DATABASE_NAME = "Recipe";
    private static final int DATABASE_VERSION = 1;
    public static volatile Mydatabase mydatabase;

    //todo Add/Update TABLES
    public String uploadRecipe = "uploadRecipe";
    public String addRecipePhotos = "addRecipePhotos";
    public String addIngredients = "addIngredients";
    public String addRecipeInstructions = "addRecipeInstructions";

    //todo Show data TABLES
    public String recipe = "recipe";
    public String recipeIngredients = "recipeIngredients";
    public String recipeImages = "recipeImages";
    public String recipeInstruction = "recipeInstruction";
    public String inventory = "inventory";
    public String unit = "unit";
    public String vendor = "vendor";
    public String recipeCategory = "recipeCategory";
    public String cart = "cart";
    public String city = "city";
    public String state = "state";
    public String favourite = "favourite";
    public String address = "address";
    public String placeOrder = "placeOrder";
    public String placeOrderDetail = "placeOrderDetail";
    public String orderItemDetail = "orderItemDetail";
    public String log = "log";

    //todo to check cart recipe ingredient available
    public String cartRecipeIngredients = "cartRecipeIngredients";

    //todo Admin Tables
    public String allRecipe = "allRecipe";
    public String allIngredients = "allIngredients";
    public String allRecipeImages = "allRecipeImages";
    public String allRecipeInstructions = "allRecipeInstructions";
    public String allOrder = "allOrder";
    public String orderStatus = "orderStatus";
    public String navMenu = "navMenu";

    public Mydatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        globalclass = Globalclass.getInstance(context);

    }

    public static Mydatabase getInstance(Context context) {

        if (mydatabase == null) { //Check for the first time

            synchronized (Mydatabase.class) {   //Check for the second time.
                //if there is no instance available... create new one
                if (mydatabase == null) mydatabase = new Mydatabase(context);
            }
        }

        return mydatabase;
    }

    @Override
    public void onCreate(SQLiteDatabase sqldb) {

        globalclass.log(tag, "onCreate");

        String query = "CREATE TABLE IF NOT EXISTS " + uploadRecipe + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,recipeId TEXT DEFAULT '',recipeName TEXT DEFAULT '',additionalCharges INTEGER DEFAULT 0,price INTEGER DEFAULT 0,categoryId TEXT DEFAULT '',categoryName TEXT DEFAULT '',recipeType TEXT DEFAULT '',status TEXT DEFAULT '',actions TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + addRecipePhotos + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,recipeName TEXT DEFAULT '',imageName TEXT DEFAULT '',filepath TEXT DEFAULT '',url TEXT DEFAULT '',newImages TEXT DEFAULT '',firstImage TEXT DEFAULT 'false')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + inventory + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,inventoryId TEXT DEFAULT '',name TEXT DEFAULT '',unitId TEXT DEFAULT '',unit TEXT DEFAULT '',adminId TEXT DEFAULT '',adminName TEXT DEFAULT '',quantity REAL DEFAULT 0,minimumQuantity INTEGER DEFAULT 0,isSelected TEXT DEFAULT '',lastChangeTime TEXT DEFAULT '',costPrice REAL DEFAULT 0,sellingPrice REAL DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + addIngredients + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,inventoryId TEXT DEFAULT '',recipeName TEXT DEFAULT '',name TEXT DEFAULT '',unitId TEXT DEFAULT '',unit TEXT DEFAULT '',quantity REAL DEFAULT 0,price REAL DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + unit + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,unitId TEXT DEFAULT '',name TEXT DEFAULT '',unit TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + vendor + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,vendorId TEXT DEFAULT '',vendorName TEXT DEFAULT '',vendorMobileNumber TEXT DEFAULT '',vendorShopAddress TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + addRecipeInstructions + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,recipeName TEXT DEFAULT '',instruction TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + recipeCategory + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,categoryId TEXT DEFAULT '',categoryName TEXT DEFAULT '',categoryIconUrl TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + recipe + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,recipeId TEXT DEFAULT '',name TEXT DEFAULT '',additionalCharges INTEGER DEFAULT 0,price INTEGER DEFAULT 0,categoryId TEXT DEFAULT '',categoryName TEXT DEFAULT '',recipeType TEXT DEFAULT '',status TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + recipeImages + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,recipeImageId TEXT DEFAULT '',recipeId TEXT DEFAULT '',imageName TEXT DEFAULT '',url TEXT DEFAULT '',firstImage TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + recipeIngredients + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,ingredientId TEXT DEFAULT '',inventoryId TEXT DEFAULT '',recipeId TEXT DEFAULT '',name TEXT DEFAULT '',recipeName TEXT DEFAULT '',unitId TEXT DEFAULT '',unit TEXT DEFAULT '',quantity REAL DEFAULT 0,price REAL DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + cart + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,cartId TEXT DEFAULT '',userId TEXT DEFAULT '',recipeId TEXT DEFAULT '',recipeName TEXT DEFAULT '',recipeImageId TEXT DEFAULT '',recipeImageUrl TEXT DEFAULT '',price INTEGER DEFAULT 0,quantity INTEGER DEFAULT 0,sum INTEGER DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + city + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,cityId TEXT DEFAULT '',cityName TEXT DEFAULT '',stateId TEXT DEFAULT '',pincode TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + state + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,stateId TEXT DEFAULT '',stateName TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + favourite + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,favouriteId TEXT DEFAULT '',userId TEXT DEFAULT '',recipeId TEXT DEFAULT '',recipeName TEXT DEFAULT '',recipeImageId TEXT DEFAULT '',recipeImageUrl TEXT DEFAULT '',price INTEGER DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + address + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,userId TEXT DEFAULT '',name TEXT DEFAULT '',mobileNumber TEXT DEFAULT '',addressId TEXT DEFAULT '',address TEXT DEFAULT '',nearBy TEXT DEFAULT '',cityId TEXT DEFAULT '',city TEXT DEFAULT '',stateId TEXT DEFAULT '',state TEXT DEFAULT '',pincode TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + cartRecipeIngredients + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,inventoryId TEXT DEFAULT '',recipeId TEXT DEFAULT '',name TEXT DEFAULT '',recipeName TEXT DEFAULT '',unitId TEXT DEFAULT '',unit TEXT DEFAULT '',quantity REAL DEFAULT 0,price REAL DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + placeOrder + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,orderId TEXT DEFAULT '',userId TEXT DEFAULT '',userMobileNumber TEXT DEFAULT '',cancelReason TEXT DEFAULT '',orderStatus TEXT DEFAULT '',orderStep INTEGER DEFAULT 0,recipeCharges INTEGER DEFAULT 0,packagingCharges INTEGER DEFAULT 0,total REAL DEFAULT 0,item INTEGER DEFAULT 0,orderDateTime TEXT DEFAULT '',deliveryDateTime TEXT DEFAULT ''" +
                ",addressId TEXT DEFAULT '',name TEXT DEFAULT '',mobileNumber TEXT DEFAULT '',address TEXT DEFAULT '',nearBy TEXT DEFAULT '',cityId TEXT DEFAULT '',city TEXT DEFAULT '',stateId TEXT DEFAULT '',state TEXT DEFAULT '',pincode TEXT DEFAULT ''" +
                ",orderItemId TEXT DEFAULT '',recipeId TEXT DEFAULT '',recipeName TEXT DEFAULT '',recipeImageId TEXT DEFAULT '',recipeImageUrl TEXT DEFAULT '',price REAL DEFAULT 0,quantity INTEGER DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + placeOrderDetail + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,orderId TEXT DEFAULT '',userId TEXT DEFAULT '',userMobileNumber TEXT DEFAULT '',cancelReason TEXT DEFAULT '',orderStatus TEXT DEFAULT '',orderStep INTEGER DEFAULT 0,recipeCharges INTEGER DEFAULT 0,packagingCharges INTEGER DEFAULT 0,total REAL DEFAULT 0,item INTEGER DEFAULT 0,orderDateTime TEXT DEFAULT '',deliveryDateTime TEXT DEFAULT ''" +
                ",addressId TEXT DEFAULT '',name TEXT DEFAULT '',mobileNumber TEXT DEFAULT '',address TEXT DEFAULT '',nearBy TEXT DEFAULT '',cityId TEXT DEFAULT '',city TEXT DEFAULT '',stateId TEXT DEFAULT '',state TEXT DEFAULT '',pincode TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + orderItemDetail + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,orderItemId TEXT DEFAULT '',orderId TEXT DEFAULT '',userId TEXT DEFAULT '',recipeId TEXT DEFAULT '',recipeName TEXT DEFAULT '',recipeImageId TEXT DEFAULT '',recipeImageUrl TEXT DEFAULT '',price REAL DEFAULT 0,quantity INTEGER DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + allOrder + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,orderId TEXT DEFAULT '',userId TEXT DEFAULT '',userMobileNumber TEXT DEFAULT '',cancelReason TEXT DEFAULT '',orderStatus TEXT DEFAULT '',orderStep INTEGER DEFAULT 0,recipeCharges INTEGER DEFAULT 0,packagingCharges INTEGER DEFAULT 0,total REAL DEFAULT 0,item INTEGER DEFAULT 0,orderDateTime TEXT DEFAULT '',deliveryDateTime TEXT DEFAULT ''" +
                ",addressId TEXT DEFAULT '',name TEXT DEFAULT '',mobileNumber TEXT DEFAULT '',address TEXT DEFAULT '',nearBy TEXT DEFAULT '',cityId TEXT DEFAULT '',city TEXT DEFAULT '',stateId TEXT DEFAULT '',state TEXT DEFAULT '',pincode TEXT DEFAULT ''" +
                ",orderItemId TEXT DEFAULT '',recipeId TEXT DEFAULT '',recipeName TEXT DEFAULT '',recipeImageId TEXT DEFAULT '',recipeImageUrl TEXT DEFAULT '',price REAL DEFAULT 0,quantity INTEGER DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + allRecipe + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,adminId TEXT DEFAULT '',adminName TEXT DEFAULT '',recipeId TEXT DEFAULT '',recipeName TEXT DEFAULT '',additionalCharges INTEGER DEFAULT 0,price INTEGER DEFAULT 0,categoryId TEXT DEFAULT '',categoryName TEXT DEFAULT '',recipeType TEXT DEFAULT '',status TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + allIngredients + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,ingredientId TEXT DEFAULT '',inventoryId TEXT DEFAULT '',recipeId TEXT DEFAULT '',name TEXT DEFAULT '',recipeName TEXT DEFAULT '',unitId TEXT DEFAULT '',unit TEXT DEFAULT '',quantity REAL DEFAULT 0,price REAL DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + allRecipeImages + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,recipeImageId TEXT DEFAULT '',recipeId TEXT DEFAULT '',imageName TEXT DEFAULT '',url TEXT DEFAULT '',firstImage TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + allRecipeInstructions + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,recipeId TEXT DEFAULT '',instruction TEXT DEFAULT '',stepNumber INTEGER DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + recipeInstruction + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,recipeId TEXT DEFAULT '',instruction TEXT DEFAULT '',stepNumber INTEGER DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + orderStatus + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,orderStatus TEXT DEFAULT '',stepNumber INTEGER DEFAULT 0)";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + navMenu + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,navMenuId TEXT DEFAULT '',accessStatus TEXT DEFAULT '')";
        sqldb.execSQL(query);

        query = "CREATE TABLE IF NOT EXISTS " + log + " (localid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,logType TEXT DEFAULT '',mobileNumber TEXT DEFAULT '',userLogin TEXT DEFAULT '',detail TEXT DEFAULT '')";
        sqldb.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        globalclass.log(tag, "onUpgrade");
    }

    //todo Recipe
    public void uploadRecipeDetail(recipeDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("recipeId", model.getRecipeId());
            cv.put("recipeName", model.getRecipeName());
            cv.put("additionalCharges", model.getAdditionalCharges());
            cv.put("price", model.getPrice());
            cv.put("categoryId", model.getCategoryId());
            cv.put("categoryName", model.getCategoryName());
            cv.put("recipeType", model.getRecipeType());
            cv.put("status", String.valueOf(model.getStatus()));
            cv.put("actions", model.getActions());

            sqldb.insert(uploadRecipe, null, cv);
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "uploadRecipeDetail: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "uploadRecipeDetail", error);
        }
    }

    public void addRecipe(recipeDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("recipeId", model.getRecipeId());
            cv.put("name", model.getRecipeName());
            cv.put("additionalCharges", model.getAdditionalCharges());
            cv.put("price", model.getPrice());
            cv.put("categoryId", model.getCategoryId());
            cv.put("categoryName", model.getCategoryName());
            cv.put("recipeType", model.getRecipeType());
            cv.put("status", String.valueOf(model.getStatus()));

            if(checkParticularRecipeExist(model) == 0) {
                sqldb.insert(recipe, null, cv);
            }
            else {
                sqldb.update(recipe, cv, "recipeId = ?", new String[]{String.valueOf(model.getRecipeId())});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addRecipe: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addRecipe", error);
        }
    }

    public int checkParticularRecipeExist(recipeDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + recipe + " WHERE recipeId = '" + model.getRecipeId() + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkParticularRecipeExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkParticularRecipeExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public int checkRecipeExist() {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + recipe + "";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkRecipeExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkRecipeExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<recipeDetail> getHomeRecipeList(String selectQuery) {

        ArrayList<recipeDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
//            String selectQuery = "SELECT * FROM " + recipe + "";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    recipeDetail model = new recipeDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setCategoryId(cursor.getString(cursor.getColumnIndex("categoryId")));
                    model.setCategoryName(cursor.getString(cursor.getColumnIndex("categoryName")));
                    model.setRecipeType(cursor.getString(cursor.getColumnIndex("recipeType")));
                    model.setAdditionalCharges(cursor.getInt(cursor.getColumnIndex("additionalCharges")));
                    model.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
                    model.setStatus(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("status"))));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getHomeRecipeList: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getHomeRecipeList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public ArrayList<recipeDetail> getRecipeList() {

        ArrayList<recipeDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + recipe + "";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    recipeDetail model = new recipeDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setCategoryId(cursor.getString(cursor.getColumnIndex("categoryId")));
                    model.setCategoryName(cursor.getString(cursor.getColumnIndex("categoryName")));
                    model.setRecipeType(cursor.getString(cursor.getColumnIndex("recipeType")));
                    model.setAdditionalCharges(cursor.getInt(cursor.getColumnIndex("additionalCharges")));
                    model.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
                    model.setStatus(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("status"))));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeList: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getRecipeList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public ArrayList<recipeDetail> getSearchRecipeList(String keyword) {

        ArrayList<recipeDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + recipe + " WHERE name LIKE '%"+keyword+"%'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    recipeDetail model = new recipeDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setCategoryId(cursor.getString(cursor.getColumnIndex("categoryId")));
                    model.setCategoryName(cursor.getString(cursor.getColumnIndex("categoryName")));
                    model.setRecipeType(cursor.getString(cursor.getColumnIndex("recipeType")));
                    model.setAdditionalCharges(cursor.getInt(cursor.getColumnIndex("additionalCharges")));
                    model.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
                    model.setStatus(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("status"))));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getSearchRecipeList: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getSearchRecipeList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public recipeDetail getParticularRecipeDetail(String recipeId) {
        recipeDetail model = null;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + recipe + " WHERE recipeId = '" + recipeId + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.moveToFirst()) {
                do {
                    model = new recipeDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setCategoryId(cursor.getString(cursor.getColumnIndex("categoryId")));
                    model.setCategoryName(cursor.getString(cursor.getColumnIndex("categoryName")));
                    model.setRecipeType(cursor.getString(cursor.getColumnIndex("recipeType")));
                    model.setAdditionalCharges(cursor.getInt(cursor.getColumnIndex("additionalCharges")));
                    model.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
                    model.setStatus(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("status"))));

                }
                while (cursor.moveToNext());
            }


        } catch (Exception e) {

            model = null;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getParticularRecipeDetail: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getParticularRecipeDetail", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return model;
    }

    //todo Recipe Photos
    public int recipePhotoPendingToUpload() {

        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "select * from "+mydatabase.addRecipePhotos +"\n" +
                    "WHERE localid = (select MIN(localid) as localid from "+mydatabase.addRecipePhotos+" WHERE\n" +
                    "url IS NULL or url = '')";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "recipePhotoPendingToUpload: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "recipePhotoPendingToUpload", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public int recipeDetailPendingToUpdate() {

        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "select * from "+mydatabase.uploadRecipe +"\n" +
                    "WHERE localid = (select MIN(localid) as localid from "+mydatabase.uploadRecipe+" WHERE actions ='Update')";


            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "recipeDetailPendingToUpdate: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "recipeDetailPendingToUpdate", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public void uploadRecipePhoto(recipePhotoDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("recipeName", model.getRecipeName());
            cv.put("imageName", model.getImageName());
            cv.put("filepath", model.getFilepath());
            cv.put("url", model.getUrl());
            cv.put("newImages", String.valueOf(model.getNewImages()));
            cv.put("firstImage", String.valueOf(model.getFirstImage()));

            if (checkRecipePhotoExist(model) == 0) {
                sqldb.insert(addRecipePhotos, null, cv);
            } else {
                sqldb.update(addRecipePhotos, cv, "localid = ?", new String[]{String.valueOf(model.getLocalid())});
            }
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "uploadRecipePhoto: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "uploadRecipePhoto", error);
        }
    }

    public int checkRecipePhotoExist(recipePhotoDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + addRecipePhotos + " WHERE localid = " + model.getLocalid() + "";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkRecipePhotoExist" + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkRecipePhotoExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public int checkPerRecipePhotoUploaded(String recipeName) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "select * from "+addRecipePhotos+"\n" +
                    "WHERE recipeName = '" + recipeName + "'\n" +
                    "AND\n" +
                    "url ISNULL";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkPerRecipePhotoUploaded" + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkPerRecipePhotoUploaded", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    //todo Recipe upload details
    public recipeDetail getUploadingRecipeDetail(String recipeName) {

        recipeDetail model = null;
        Cursor cursor = null;

        try {
            String selectQuery = "SELECT * FROM " + uploadRecipe + " WHERE recipeName = '"+recipeName+"'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    model = new recipeDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setCategoryId(cursor.getString(cursor.getColumnIndex("categoryId")));
                    model.setCategoryName(cursor.getString(cursor.getColumnIndex("categoryName")));
                    model.setRecipeType(cursor.getString(cursor.getColumnIndex("recipeType")));
                    model.setStatus(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("status"))));
                    model.setAdditionalCharges(cursor.getInt(cursor.getColumnIndex("additionalCharges")));
                    model.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {

            model = null;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getUploadingRecipeDetail: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getUploadingRecipeDetail", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return model;
    }

    public ArrayList<ingredientsDetail> getUploadingIngredientsList(String recipeName) {

        ArrayList<ingredientsDetail> arrayList = new ArrayList<>();
        String query = "";
        Cursor cursor = null;
        try {
            query = "select * from "+ addIngredients +"\n" +
                    "WHERE recipeName = '" + recipeName + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.moveToFirst()) {
                do {
                    ingredientsDetail model = new ingredientsDetail();
                    model.setInventoryId(cursor.getString(cursor.getColumnIndex("inventoryId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setUnitId(cursor.getString(cursor.getColumnIndex("unitId")));
                    model.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
                    model.setQuantity(Double.parseDouble(cursor.getString(cursor.getColumnIndex("quantity"))));
                    model.setPrice(Double.parseDouble(cursor.getString(cursor.getColumnIndex("price"))));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getUploadingIngredientsList: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getUploadingIngredientsList", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public ArrayList<recipePhotoDetail> getUploadingRecipeImages(String recipeName) {

        ArrayList<recipePhotoDetail> arrayList = new ArrayList<>();
        String query = "";
        Cursor cursor = null;
        try {
            query = "select * from "+ addRecipePhotos +"\n" +
                    "WHERE recipeName = '" + recipeName + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.moveToFirst()) {
                do {
                    recipePhotoDetail model = new recipePhotoDetail();
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setImageName(cursor.getString(cursor.getColumnIndex("imageName")));
                    model.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    model.setFirstImage(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("firstImage"))));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getUploadingRecipeImages: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getUploadingRecipeImages", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public ArrayList<String> getUploadingRecipeInstructions(String recipeName) {

        ArrayList<String> arrayList = new ArrayList<>();
        String query = "";
        Cursor cursor = null;
        try {
            query = "select * from "+ addRecipeInstructions +"\n" +
                    "WHERE recipeName = '" + recipeName + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.moveToFirst()) {
                do {
                    arrayList.add(cursor.getString(cursor.getColumnIndex("instruction")));
                }
                while (cursor.moveToNext());
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getUploadingRecipeInstructions: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getUploadingRecipeInstructions", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public boolean recipePendingToUpload(String recipeName) {

        //todo checking recipeName already exist in uploadRecipe table and pending to upload data

        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + uploadRecipe + " WHERE recipeName = '" + recipeName + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if(cursor.getCount() > 0) {
                return true;
            }
            else {
                return false;
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "recipePendingToUpload" + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "recipePendingToUpload", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return false;
    }

    //todo Inventory
    public void addInventory(inventoryDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("inventoryId", model.getInventoryId());
            cv.put("name", model.getName());
            cv.put("unitId", model.getUnitId());
            cv.put("unit", model.getUnit());
            cv.put("quantity", model.getQuantity());
            cv.put("minimumQuantity", model.getMinimumQuantity());
            cv.put("isSelected", String.valueOf(model.getisSelected()));
            cv.put("adminId", model.getAdminId());
            cv.put("adminName", model.getAdminName());
            cv.put("costPrice", model.getCostPrice());
            cv.put("sellingPrice", model.getSellingPrice());

            if (checkInventoryIdExist(model) == 0) {
                sqldb.insert(inventory, null, cv);
            } else {
                sqldb.update(inventory, cv, "inventoryId = ?", new String[]{String.valueOf(model.getInventoryId())});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addInventory" + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addInventory", error);
        }
    }

    public int checkInventoryIdExist(inventoryDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + inventory + " WHERE inventoryId = '" + model.getInventoryId() + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkInventoryIdExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkInventoryIdExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public int checkInventoryExist(String inventoryName) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + inventory + " WHERE name = '" + inventoryName + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkInventoryExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkInventoryExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<inventoryDetail> getInventoryList() {
        ArrayList<inventoryDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + inventory + " order by isSelected DESC";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    inventoryDetail model = new inventoryDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setInventoryId(cursor.getString(cursor.getColumnIndex("inventoryId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setisSelected(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("isSelected"))));
                    model.setUnitId(cursor.getString(cursor.getColumnIndex("unitId")));
                    model.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
                    model.setAdminId(cursor.getString(cursor.getColumnIndex("adminId")));
                    model.setAdminName(cursor.getString(cursor.getColumnIndex("adminName")));
                    model.setQuantity(cursor.getDouble(cursor.getColumnIndex("quantity")));
                    model.setCostPrice(cursor.getDouble(cursor.getColumnIndex("costPrice")));
                    model.setSellingPrice(cursor.getDouble(cursor.getColumnIndex("sellingPrice")));
                    model.setMinimumQuantity(Integer.parseInt(cursor.getString(cursor.getColumnIndex("minimumQuantity"))));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getInventoryList" + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getInventoryList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public ArrayList<inventoryDetail> getHomeLimitedInventoryList() {
        ArrayList<inventoryDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * from "+inventory+"\n" +
                    "where quantity <= minimumQuantity ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    inventoryDetail model = new inventoryDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setInventoryId(cursor.getString(cursor.getColumnIndex("inventoryId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setisSelected(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("isSelected"))));
                    model.setUnitId(cursor.getString(cursor.getColumnIndex("unitId")));
                    model.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
                    model.setAdminId(cursor.getString(cursor.getColumnIndex("adminId")));
                    model.setAdminName(cursor.getString(cursor.getColumnIndex("adminName")));
                    model.setQuantity(cursor.getDouble(cursor.getColumnIndex("quantity")));
                    model.setCostPrice(cursor.getDouble(cursor.getColumnIndex("costPrice")));
                    model.setSellingPrice(cursor.getDouble(cursor.getColumnIndex("sellingPrice")));
                    model.setMinimumQuantity(Integer.parseInt(cursor.getString(cursor.getColumnIndex("minimumQuantity"))));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getHomeLimitedInventoryList: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getHomeLimitedInventoryList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public ArrayList<inventoryDetail> searchInventory(String keyword) {

        ArrayList<inventoryDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {

            String selectQuery = "SELECT * FROM " + inventory + " WHERE name LIKE '%"+keyword+"%'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    inventoryDetail model = new inventoryDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setInventoryId(cursor.getString(cursor.getColumnIndex("inventoryId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setisSelected(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("isSelected"))));
                    model.setUnitId(cursor.getString(cursor.getColumnIndex("unitId")));
                    model.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
                    model.setAdminId(cursor.getString(cursor.getColumnIndex("adminId")));
                    model.setAdminName(cursor.getString(cursor.getColumnIndex("adminName")));
                    model.setQuantity(cursor.getDouble(cursor.getColumnIndex("quantity")));
                    model.setCostPrice(cursor.getDouble(cursor.getColumnIndex("costPrice")));
                    model.setSellingPrice(cursor.getDouble(cursor.getColumnIndex("sellingPrice")));
                    model.setMinimumQuantity(Integer.parseInt(cursor.getString(cursor.getColumnIndex("minimumQuantity"))));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "searchInventory" + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "searchInventory", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public inventoryDetail getParticularInventory(String inventoryId) {
        inventoryDetail model = null;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + inventory + " WHERE inventoryId = '"+inventoryId+"'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    model = new inventoryDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setInventoryId(cursor.getString(cursor.getColumnIndex("inventoryId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setisSelected(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("isSelected"))));
                    model.setUnitId(cursor.getString(cursor.getColumnIndex("unitId")));
                    model.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
                    model.setAdminId(cursor.getString(cursor.getColumnIndex("adminId")));
                    model.setAdminName(cursor.getString(cursor.getColumnIndex("adminName")));
                    model.setQuantity(cursor.getDouble(cursor.getColumnIndex("quantity")));
                    model.setCostPrice(cursor.getDouble(cursor.getColumnIndex("costPrice")));
                    model.setSellingPrice(cursor.getDouble(cursor.getColumnIndex("sellingPrice")));
                    model.setMinimumQuantity(Integer.parseInt(cursor.getString(cursor.getColumnIndex("minimumQuantity"))));
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getParticularInventory: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getParticularInventory", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return model;
    }

    public void unCheckPerInventory(String id) {
        SQLiteDatabase sqldb = this.getWritableDatabase();
        String q = "UPDATE " + inventory + " SET isSelected = 'false' WHERE inventoryId = '" + id + "'";
        sqldb.execSQL(q);
    }

    public void unCheckAllSelectedInventory() {
        SQLiteDatabase sqldb = this.getWritableDatabase();
        String q = "UPDATE " + inventory + " SET isSelected = 'false' WHERE isSelected = 'true'";
        sqldb.execSQL(q);
    }

    public void uploadIngredients(ingredientsDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("inventoryId", model.getInventoryId());
            cv.put("recipeName", model.getRecipeName());
            cv.put("name", model.getName());
            cv.put("unitId", model.getUnitId());
            cv.put("unit", model.getUnit());
            cv.put("quantity", model.getQuantity());
            cv.put("price", model.getPrice());

            sqldb.insert(addIngredients, null, cv);

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "uploadIngredients: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "uploadIngredients", error);
        }
    }

    //todo Unit
    public void addUnit(unitDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("unitId", model.getunitId());
            cv.put("name", model.getName());
            cv.put("unit", model.getUnit());

            if (checkUnitExist(model) == 0) {
                sqldb.insert(unit, null, cv);
            } else {
                sqldb.update(unit, cv, "unitId = ?", new String[]{String.valueOf(model.getunitId())});
            }
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addUnit" + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addUnit", error);
        }
    }

    public int checkUnitExist(unitDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + unit + " WHERE unitId = '" + model.getunitId() + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkUnitExist" + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkUnitExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<unitDetail> getUnitList() {

        ArrayList<unitDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + unit + "";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    unitDetail model = new unitDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setunitId(cursor.getString(cursor.getColumnIndex("unitId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getUnitList" + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getUnitList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo Vendor
    public void addVendor(vendorDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("vendorId", model.getVendorId());
            cv.put("vendorName", model.getVendorName());
            cv.put("vendorMobileNumber", model.getVendorMobileNumber());
            cv.put("vendorShopAddress", model.getVendorShopAddress());

            if (checkVendorExist(model) == 0) {
                sqldb.insert(vendor, null, cv);
            } else {
                sqldb.update(vendor, cv, "vendorId = ?", new String[]{String.valueOf(model.getVendorId())});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addVendor" + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addVendor", error);
        }
    }

    public int checkVendorExist(vendorDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + vendor + " WHERE vendorId = '" + model.getVendorId() + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkVendorExist" + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkVendorExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public int checkVendorNumberExist(String mobilenumber) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + vendor + " WHERE vendorMobileNumber = '" + mobilenumber + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkVendorNumberExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkVendorNumberExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<vendorDetail> getVendorList() {

        ArrayList<vendorDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + vendor + "";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    vendorDetail model = new vendorDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setVendorId(cursor.getString(cursor.getColumnIndex("vendorId")));
                    model.setVendorName(cursor.getString(cursor.getColumnIndex("vendorName")));
                    model.setVendorMobileNumber(cursor.getString(cursor.getColumnIndex("vendorMobileNumber")));
                    model.setVendorShopAddress(cursor.getString(cursor.getColumnIndex("vendorShopAddress")));
                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getVendorList: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getVendorList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo recipe Instructions
    public void addRecipeInstructions(String recipeName,String instruction) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("recipeName", recipeName);
            cv.put("instruction", instruction);

            sqldb.insert(addRecipeInstructions, null, cv);

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addRecipeInstructions: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addRecipeInstructions", error);

        }
    }

    //todo RecipeCategory
    public void addRecipeCategory(recipeCategoryDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("categoryId", model.getCategoryId());
            cv.put("categoryName", model.getCategoryName());
            cv.put("categoryIconUrl", model.getCategoryIconUrl());

            if (checkRecipeCategoryExist(model) == 0) {
                sqldb.insert(recipeCategory, null, cv);
            } else {
                sqldb.update(recipeCategory, cv, "categoryId = ?", new String[]{String.valueOf(model.getCategoryId())});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addRecipeCategory: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addRecipeCategory", error);
        }
    }

    public int checkRecipeCategoryExist(recipeCategoryDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + recipeCategory + " WHERE categoryId = '" + model.getCategoryId() + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkRecipeCategoryExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkRecipeCategoryExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public int checkRecipeCategoryNameExist(String categoryName) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + recipeCategory + " WHERE categoryName = '" + categoryName + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkRecipeCategoryNameExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkRecipeCategoryNameExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<recipeCategoryDetail> getRecipeCategoryList() {

        ArrayList<recipeCategoryDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + recipeCategory + "";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    recipeCategoryDetail model = new recipeCategoryDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setCategoryId(cursor.getString(cursor.getColumnIndex("categoryId")));
                    model.setCategoryName(cursor.getString(cursor.getColumnIndex("categoryName")));
                    model.setCategoryIconUrl(cursor.getString(cursor.getColumnIndex("categoryIconUrl")));
                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeCategoryList: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getRecipeCategoryList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo ingredients
    public void addIngredients(ingredientsDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("ingredientId", model.getIngredientId());
            cv.put("inventoryId", model.getInventoryId());
            cv.put("recipeId", model.getRecipeId());
            cv.put("recipeName", model.getRecipeName());
            cv.put("name", model.getName());
            cv.put("unitId", model.getUnitId());
            cv.put("unit", model.getUnit());
            cv.put("quantity", model.getQuantity());
            cv.put("price", model.getPrice());

            if(checkIngredientsExist(model) == 0) {
                sqldb.insert(recipeIngredients, null, cv);
            }
            else {
                sqldb.update(recipeIngredients, cv,   "ingredientId = ? ",
                        new String[]{model.getIngredientId()});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addIngredients: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addIngredients", error);
        }
    }

    public int checkIngredientsExist(ingredientsDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + recipeIngredients + " WHERE ingredientId = '" + model.getIngredientId() + "' ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkIngredientsExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkIngredientsExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<ingredientsDetail> getIngredientsList(String recipeId) {

        ArrayList<ingredientsDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + recipeIngredients + " WHERE recipeId = '"+recipeId+"'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    ingredientsDetail model = new ingredientsDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setInventoryId(cursor.getString(cursor.getColumnIndex("inventoryId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
                    model.setQuantity(cursor.getDouble(cursor.getColumnIndex("quantity")));
                    model.setPrice(cursor.getDouble(cursor.getColumnIndex("price")));
                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getIngredientsList: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getIngredientsList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo recipeImages
    public void addRecipeImages(recipePhotoDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();

            cv.put("recipeImageId", model.getRecipeImageId());
            cv.put("recipeId", model.getRecipeId());
            cv.put("imageName", model.getImageName());
            cv.put("url", model.getUrl());
            cv.put("firstImage", String.valueOf(model.getFirstImage()));

            if (checkRecipeImageIdExist(model) == 0) {
                sqldb.insert(recipeImages, null, cv);
            } else {
                sqldb.update(recipeImages, cv, "recipeImageId = ?", new String[]{String.valueOf(model.getRecipeImageId())});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addRecipeImages: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addRecipeImages", error);
        }
    }

    public int checkRecipeImageIdExist(recipePhotoDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + recipeImages + " WHERE recipeImageId = '" + model.getRecipeImageId() + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkRecipeImageIdExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkRecipeImageIdExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<recipePhotoDetail> getRecipeImagesList(String recipeId) {

        ArrayList<recipePhotoDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + recipeImages + " WHERE recipeId = '"+recipeId+"'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    recipePhotoDetail model = new recipePhotoDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setImageName(cursor.getString(cursor.getColumnIndex("imageName")));
                    model.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    model.setFirstImage(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("firstImage"))));
                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeImagesList: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getRecipeImagesList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public ArrayList<recipePhotoDetail> getFirstRecipeImages(String recipeId) {

        ArrayList<recipePhotoDetail> arrayList = new ArrayList<>();
        String query = "";
        Cursor cursor = null;
        try {
            query = "select * from "+ recipeImages +"\n" +
                    "WHERE recipeId = '" + recipeId + "' AND firstImage = 'true'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.moveToFirst()) {
                do {
                    recipePhotoDetail model = new recipePhotoDetail();
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setImageName(cursor.getString(cursor.getColumnIndex("imageName")));
                    model.setUrl(cursor.getString(cursor.getColumnIndex("url")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getFirstRecipeImages: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getFirstRecipeImages", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo cart
    public void addToCart(cartDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("cartId", model.getCartId());
            cv.put("recipeId", model.getRecipeId());
            cv.put("recipeName", model.getRecipeName());
            cv.put("recipeImageId", model.getRecipeImageId());
            cv.put("recipeImageUrl", model.getRecipeImageUrl());
            cv.put("userId", model.getUserId());
            cv.put("price", model.getPrice());
            cv.put("quantity", model.getQuantity());
            cv.put("sum", model.getSum());

            if(checkCartIdExist(model) == 0) {
                sqldb.insert(cart, null, cv);
            }
            else {
                sqldb.update(cart, cv,   "cartId = ?", new String[]{model.getCartId()});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addToCart: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addToCart", error);
        }
    }

    public int checkCartIdExist(cartDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + cart + " WHERE cartId = '" + model.getCartId() + "' ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkExistInCart: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkExistInCart", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public int checkRecipeExistInCart(String recipeId) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + cart + " WHERE recipeId = '" + recipeId + "' ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkRecipeExistInCart: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkRecipeExistInCart", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public cartDetail getParticularCartDetail(String recipeId) {

        cartDetail model = null;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + cart + " WHERE recipeId = '"+recipeId+"'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    model = new cartDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setCartId(cursor.getString(cursor.getColumnIndex("cartId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeImageUrl(cursor.getString(cursor.getColumnIndex("recipeImageUrl")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
                    model.setQuantity(cursor.getInt(cursor.getColumnIndex("quantity")));
                    model.setSum(cursor.getInt(cursor.getColumnIndex("sum")));
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            model = null;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getParticularCartDetail: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getParticularCartDetail", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return model;
    }

    public ArrayList<cartDetail> getCartList() {

        ArrayList<cartDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + cart + " ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    cartDetail model = new cartDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setCartId(cursor.getString(cursor.getColumnIndex("cartId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeImageUrl(cursor.getString(cursor.getColumnIndex("recipeImageUrl")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
                    model.setQuantity(cursor.getInt(cursor.getColumnIndex("quantity")));
                    model.setSum(cursor.getInt(cursor.getColumnIndex("sum")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getCartList: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getCartList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public int getCartItemCount() {
        int quantity = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query =  "SELECT SUM(quantity) as quantity\n" +
                    "FROM cart";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.moveToFirst()) {
                do {
                    quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                }
                while (cursor.moveToNext());
            }


        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getCartItemCount: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getCartItemCount", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return quantity;
    }

    public int getCartSum() {

        int sum = 0;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT SUM(sum) as sum\n" +
                    "FROM cart";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    sum = cursor.getInt(cursor.getColumnIndex("sum"));
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            sum = 0;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getCartSum: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getCartSum", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return sum;
    }

    //todo city
    public void addCity(cityDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("cityId", model.getCityId());
            cv.put("cityName", model.getCityName());
            cv.put("stateId", model.getStateId());
            cv.put("pincode", model.getPincode());

            if(checkCityExist(model) == 0) {
                sqldb.insert(city, null, cv);
            }
            else {
                sqldb.update(city, cv,   "cityId = ?", new String[]{model.getCityId()});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addCity: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addCity", error);
        }
    }

    public int checkCityExist(cityDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + city + " WHERE cityId = '" + model.getCityId() + "' ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkCityExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkCityExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<cityDetail> getCityList() {

        ArrayList<cityDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + city + " ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    cityDetail model = new cityDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setCityId(cursor.getString(cursor.getColumnIndex("cityId")));
                    model.setCityName(cursor.getString(cursor.getColumnIndex("cityName")));
                    model.setStateId(cursor.getString(cursor.getColumnIndex("stateId")));
                    model.setPincode(cursor.getString(cursor.getColumnIndex("pincode")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getCityList: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getCityList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo state
    public void addState(stateDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("stateId", model.getStateId());
            cv.put("stateName", model.getStateName());

            if(checkStateExist(model) == 0) {
                sqldb.insert(state, null, cv);
            }
            else {
                sqldb.update(state, cv,   "stateId = ?", new String[]{model.getStateId()});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addState: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addState", error);
        }
    }

    public int checkStateExist(stateDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + state + " WHERE stateId = '" + model.getStateId() + "' ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkStateExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkStateExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public int checkRecipeExistInFav(String recipeId) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + favourite + " WHERE recipeId = '" + recipeId + "' ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkRecipeExistInFav: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkRecipeExistInFav", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<stateDetail> getStateList() {

        ArrayList<stateDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + state + " ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    stateDetail model = new stateDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setStateId(cursor.getString(cursor.getColumnIndex("stateId")));
                    model.setStateName(cursor.getString(cursor.getColumnIndex("stateName")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getStateList: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getStateList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo favourite
    public void addToFavourite(favouriteDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("favouriteId", model.getFavouriteId());
            cv.put("recipeId", model.getRecipeId());
            cv.put("recipeName", model.getRecipeName());
            cv.put("recipeImageId", model.getRecipeImageId());
            cv.put("recipeImageUrl", model.getRecipeImageUrl());
            cv.put("userId", model.getUserId());
            cv.put("price", model.getPrice());

            if(checkFavIdExist(model) == 0) {
                sqldb.insert(favourite, null, cv);
            }
            else {
                sqldb.update(favourite, cv,   "favouriteId = ?", new String[]{model.getFavouriteId()});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addToFavourite: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addToFavourite", error);
        }
    }

    public int checkFavIdExist(favouriteDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + favourite + " WHERE favouriteId = '" + model.getFavouriteId() + "' ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkFavIdExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkFavIdExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<favouriteDetail> getFavouriteList() {

        ArrayList<favouriteDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + favourite + " ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    favouriteDetail model = new favouriteDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setFavouriteId(cursor.getString(cursor.getColumnIndex("favouriteId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeImageUrl(cursor.getString(cursor.getColumnIndex("recipeImageUrl")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getFavouriteList: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getFavouriteList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public favouriteDetail getParticularFavDetail(String recipeId) {

        favouriteDetail model = null;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + favourite + " WHERE recipeId = '"+recipeId+"'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    model = new favouriteDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setFavouriteId(cursor.getString(cursor.getColumnIndex("favouriteId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeImageUrl(cursor.getString(cursor.getColumnIndex("recipeImageUrl")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            model = null;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getParticularFavDetail: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getParticularFavDetail", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return model;
    }

    //todo address
    public void addNewAddress(addressDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("userId", model.getUserId());
            cv.put("name", model.getName());
            cv.put("mobileNumber", model.getMobileNumber());
            cv.put("addressId", model.getAddressId());
            cv.put("address", model.getAddress());
            cv.put("nearBy", model.getNearBy());
            cv.put("cityId", model.getCityId());
            cv.put("city", model.getCity());
            cv.put("stateId", model.getStateId());
            cv.put("state", model.getState());
            cv.put("pincode", model.getPincode());

            if(checkAddressIdExist(model) == 0) {
                sqldb.insert(address, null, cv);
            }
            else {
                sqldb.update(address, cv,   "addressId = ?", new String[]{model.getAddressId()});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addNewAddress: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addNewAddress", error);
        }
    }

    public int checkAddressIdExist(addressDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + address + " WHERE addressId = '" + model.getAddressId() + "' ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkAddressIdExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkAddressIdExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<addressDetail> getAddressList() {

        ArrayList<addressDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + address + " ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    addressDetail model = new addressDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setMobileNumber(cursor.getString(cursor.getColumnIndex("mobileNumber")));
                    model.setAddressId(cursor.getString(cursor.getColumnIndex("addressId")));
                    model.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    model.setNearBy(cursor.getString(cursor.getColumnIndex("nearBy")));
                    model.setCityId(cursor.getString(cursor.getColumnIndex("cityId")));
                    model.setCity(cursor.getString(cursor.getColumnIndex("city")));
                    model.setStateId(cursor.getString(cursor.getColumnIndex("stateId")));
                    model.setState(cursor.getString(cursor.getColumnIndex("state")));
                    model.setPincode(cursor.getString(cursor.getColumnIndex("pincode")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAddressList: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getAddressList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public addressDetail getParticularAddressDetail(String addressId) {

        addressDetail model = null;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + address + " WHERE addressId = '"+addressId+"'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    model = new addressDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setMobileNumber(cursor.getString(cursor.getColumnIndex("mobileNumber")));
                    model.setAddressId(cursor.getString(cursor.getColumnIndex("addressId")));
                    model.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    model.setNearBy(cursor.getString(cursor.getColumnIndex("nearBy")));
                    model.setCityId(cursor.getString(cursor.getColumnIndex("cityId")));
                    model.setCity(cursor.getString(cursor.getColumnIndex("city")));
                    model.setStateId(cursor.getString(cursor.getColumnIndex("stateId")));
                    model.setState(cursor.getString(cursor.getColumnIndex("state")));
                    model.setPincode(cursor.getString(cursor.getColumnIndex("pincode")));
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {

            model= null;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getParticularAddressDetail: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getParticularAddressDetail", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return model;
    }

    //todo checkStockExist
    public ArrayList<ingredientsDetail> getIngredientsListWithAddedQuantity(String recipeId,int quantity) {

        ArrayList<ingredientsDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + recipeIngredients + " WHERE recipeId = '"+recipeId+"'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    ingredientsDetail ingredientsDetailModel = new ingredientsDetail();
                    ingredientsDetailModel.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    ingredientsDetailModel.setInventoryId(cursor.getString(cursor.getColumnIndex("inventoryId")));
                    ingredientsDetailModel.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    ingredientsDetailModel.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    ingredientsDetailModel.setName(cursor.getString(cursor.getColumnIndex("name")));
                    ingredientsDetailModel.setUnitId(cursor.getString(cursor.getColumnIndex("unitId")));
                    ingredientsDetailModel.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
                    ingredientsDetailModel.setQuantity(cursor.getInt(cursor.getColumnIndex("quantity")));
                    ingredientsDetailModel.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
                    arrayList.add(ingredientsDetailModel);

                    //todo do sum of quantity & price in ingredients
                    double sumOfQuantity = ingredientsDetailModel.getQuantity() * quantity;
                    ingredientsDetailModel.setQuantity(sumOfQuantity);

                    double sumOfPrice = ingredientsDetailModel.getPrice() * quantity;
                    ingredientsDetailModel.setPrice(sumOfPrice);

                    addCartRecipeIngredients(ingredientsDetailModel);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getIngredientsList: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getIngredientsList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public void addCartRecipeIngredients(ingredientsDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("inventoryId", model.getInventoryId());
            cv.put("recipeId", model.getRecipeId());
            cv.put("recipeName", getParticularRecipeDetail(model.getRecipeId()).getRecipeName());
            cv.put("name", model.getName());
            cv.put("unitId", model.getUnitId());
            cv.put("unit", model.getUnit());
            cv.put("quantity", model.getQuantity());
            cv.put("price", model.getPrice());

            sqldb.insert(cartRecipeIngredients, null, cv);

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addCartRecipeIngredients: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addCartRecipeIngredients", error);
        }
    }

    public ArrayList<checkStockExistDetail> getCartRecipeIngredients() {

        ArrayList<checkStockExistDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT *,SUM(quantity) as sumOfQuantity,SUM(price) as sumOfPrice\n" +
                    "from "+cartRecipeIngredients+"\n" +
                    "group by inventoryId\n" +
                    "order by sumOfQuantity";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    checkStockExistDetail model = new checkStockExistDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setInventoryId(cursor.getString(cursor.getColumnIndex("inventoryId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setUnitId(cursor.getString(cursor.getColumnIndex("unitId")));
                    model.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
                    model.setQuantity(cursor.getDouble(cursor.getColumnIndex("quantity")));
                    model.setSumOfQuantity(cursor.getDouble(cursor.getColumnIndex("sumOfQuantity")));
                    model.setSumOfPrice(cursor.getDouble(cursor.getColumnIndex("sumOfPrice")));
                    arrayList.add(model);

                    globalclass.log("getCartRecipeIngredients",
                            "recipeName: "+model.getRecipeName()
                                    +", Ingredient: "+model.getName()
                                    +", Quantity: "+model.getSumOfQuantity()
                    );
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getCartRecipeIngredients: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getCartRecipeIngredients", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo placeOrderDetail
    public void addPlaceOrderDetail(orderSummaryDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("orderId", model.getOrderId());
            cv.put("userId", model.getUserId());
            cv.put("userMobileNumber", model.getUserMobileNumber());
            cv.put("cancelReason", model.getCancelReason());
            cv.put("orderStatus", model.getOrderStatus());
            cv.put("orderStep", model.getOrderStep());
            cv.put("recipeCharges", model.getRecipeCharges());
            cv.put("packagingCharges", model.getPackagingCharges());
            cv.put("total", model.getTotal());
            cv.put("item", model.getItem());
            cv.put("orderDateTime", globalclass.dateToString(model.getOrderDateTime(),"dd MMM yyyy hh:mm aa"));
            cv.put("deliveryDateTime", globalclass.dateToString(model.getDeliveryDateTime(),"dd MMM yyyy hh:mm aa"));
            cv.put("addressId", model.getAddressId());
            cv.put("name", model.getName());
            cv.put("mobileNumber", model.getMobileNumber());
            cv.put("address", model.getAddress());
            cv.put("nearBy", model.getNearBy());
            cv.put("cityId", model.getCityId());
            cv.put("city", model.getCity());
            cv.put("stateId", model.getStateId());
            cv.put("state", model.getState());
            cv.put("pincode", model.getPincode());

            if(checkPlaceOrderDetail(model) == 0) {
                sqldb.insert(placeOrderDetail, null, cv);
            }
            else {
                sqldb.update(placeOrderDetail, cv,   "orderId = ?",
                        new String[]{model.getOrderId()});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addPlaceOrderDetail: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addPlaceOrderDetail", error);
        }
    }

    public int checkPlaceOrderDetail(orderSummaryDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + placeOrderDetail + " WHERE orderId = '" + model.getOrderId() + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkPlaceOrderDetail: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkPlaceOrderDetail", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    //todo orderItemDetail
    public void addOrderItemDetail(com.hksofttronix.khansama.Models.orderItemDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("orderItemId", model.getOrderItemId());
            cv.put("orderId", model.getOrderId());
            cv.put("userId", model.getUserId());
            cv.put("recipeId", model.getRecipeId());
            cv.put("recipeName", model.getRecipeName());
            cv.put("recipeImageId", model.getRecipeImageId());
            cv.put("recipeImageUrl", model.getRecipeImageUrl());
            cv.put("price", model.getPrice());
            cv.put("quantity", model.getQuantity());

            if(checkOrderItemDetail(model) == 0) {
                sqldb.insert(orderItemDetail, null, cv);
            }
            else {
                sqldb.update(orderItemDetail, cv,   "orderItemId = ?",
                        new String[]{model.getOrderItemId()});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addOrderItemDetail: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addOrderItemDetail", error);
        }
    }

    public int checkOrderItemDetail(orderItemDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + orderItemDetail + " WHERE orderItemId = '" + model.getOrderItemId() + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkOrderItemDetail: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkOrderItemDetail", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    //todo placeOrder
    public void addPlaceOrder(placeOrderDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("orderId", model.getOrderId());
            cv.put("userId", model.getUserId());
            cv.put("userMobileNumber", model.getUserMobileNumber());
            cv.put("cancelReason", model.getCancelReason());
            cv.put("orderStatus", model.getOrderStatus());
            cv.put("orderStep", model.getOrderStep());
            cv.put("recipeCharges", model.getRecipeCharges());
            cv.put("packagingCharges", model.getPackagingCharges());
            cv.put("total", model.getTotal());
            cv.put("item", model.getItem());
            cv.put("orderDateTime", globalclass.dateToString(model.getOrderDateTime(),"dd MMM yyyy hh:mm aa"));
            cv.put("deliveryDateTime", globalclass.dateToString(model.getDeliveryDateTime(),"dd MMM yyyy hh:mm aa"));
            cv.put("addressId", model.getAddressId());
            cv.put("name", model.getName());
            cv.put("mobileNumber", model.getMobileNumber());
            cv.put("address", model.getAddress());
            cv.put("nearBy", model.getNearBy());
            cv.put("cityId", model.getCityId());
            cv.put("city", model.getCity());
            cv.put("stateId", model.getStateId());
            cv.put("state", model.getState());
            cv.put("pincode", model.getPincode());
            cv.put("orderItemId", model.getOrderItemId());
            cv.put("recipeId", model.getRecipeId());
            cv.put("recipeName", model.getRecipeName());
            cv.put("recipeImageId", model.getRecipeImageId());
            cv.put("recipeImageUrl", model.getRecipeImageUrl());
            cv.put("price", model.getPrice());
            cv.put("quantity", model.getQuantity());

            if(checkAllOrder(model) == 0) {
                sqldb.insert(placeOrder, null, cv);
            }
            else {
                sqldb.update(placeOrder, cv,   "orderId = ? AND recipeId = ?",
                        new String[]{model.getOrderId(),model.getRecipeId()});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addPlaceOrder: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addPlaceOrder", error);
        }
    }

    public int checkAllOrder(placeOrderDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + placeOrder + " WHERE orderId = '" + model.getOrderId() + "' " +
                    "AND recipeId = '"+model.getRecipeId()+"'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkOrderIdExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkOrderIdExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<placeOrderDetail> getPlaceOrderList() {

        ArrayList<placeOrderDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * from "+placeOrder+"\n" +
                    "GROUP by\n" +
                    "orderId\n" +
                    "ORDER by orderDateTime DESC\n ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    placeOrderDetail model = new placeOrderDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setOrderId(cursor.getString(cursor.getColumnIndex("orderId")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setUserMobileNumber(cursor.getString(cursor.getColumnIndex("userMobileNumber")));
                    model.setCancelReason(cursor.getString(cursor.getColumnIndex("cancelReason")));
                    model.setOrderStatus(cursor.getString(cursor.getColumnIndex("orderStatus")));
                    model.setOrderStep(cursor.getInt(cursor.getColumnIndex("orderStep")));
                    model.setRecipeCharges(cursor.getInt(cursor.getColumnIndex("recipeCharges")));
                    model.setPackagingCharges(cursor.getInt(cursor.getColumnIndex("packagingCharges")));
                    model.setTotal(cursor.getDouble(cursor.getColumnIndex("total")));
                    model.setItem(cursor.getInt(cursor.getColumnIndex("item")));
                    model.setOrderDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("orderDateTime")),"dd MMM yyyy hh:mm aa"));
                    model.setDeliveryDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("deliveryDateTime")),"dd MMM yyyy hh:mm aa"));

                    model.setAddressId(cursor.getString(cursor.getColumnIndex("addressId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setMobileNumber(cursor.getString(cursor.getColumnIndex("mobileNumber")));
                    model.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    model.setNearBy(cursor.getString(cursor.getColumnIndex("nearBy")));
                    model.setCityId(cursor.getString(cursor.getColumnIndex("cityId")));
                    model.setCity(cursor.getString(cursor.getColumnIndex("city")));
                    model.setStateId(cursor.getString(cursor.getColumnIndex("stateId")));
                    model.setState(cursor.getString(cursor.getColumnIndex("state")));
                    model.setPincode(cursor.getString(cursor.getColumnIndex("pincode")));

                    model.setOrderItemId(cursor.getString(cursor.getColumnIndex("orderItemId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeImageUrl(cursor.getString(cursor.getColumnIndex("recipeImageUrl")));
                    model.setPrice(cursor.getDouble(cursor.getColumnIndex("price")));
                    model.setQuantity(cursor.getInt(cursor.getColumnIndex("quantity")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getPlaceOrderList: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getPlaceOrderList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public placeOrderDetail getParticularOrderDetail(String orderId) {

        placeOrderDetail model = null;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * from "+placeOrder+" WHERE orderId = '"+orderId+"' LIMIT 1";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    model = new placeOrderDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setOrderId(cursor.getString(cursor.getColumnIndex("orderId")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setUserMobileNumber(cursor.getString(cursor.getColumnIndex("userMobileNumber")));
                    model.setCancelReason(cursor.getString(cursor.getColumnIndex("cancelReason")));
                    model.setOrderStatus(cursor.getString(cursor.getColumnIndex("orderStatus")));
                    model.setOrderStep(cursor.getInt(cursor.getColumnIndex("orderStep")));
                    model.setRecipeCharges(cursor.getInt(cursor.getColumnIndex("recipeCharges")));
                    model.setPackagingCharges(cursor.getInt(cursor.getColumnIndex("packagingCharges")));
                    model.setTotal(cursor.getDouble(cursor.getColumnIndex("total")));
                    model.setItem(cursor.getInt(cursor.getColumnIndex("item")));
                    model.setOrderDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("orderDateTime")),"dd MMM yyyy hh:mm aa"));
                    model.setDeliveryDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("deliveryDateTime")),"dd MMM yyyy hh:mm aa"));

                    model.setAddressId(cursor.getString(cursor.getColumnIndex("addressId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setMobileNumber(cursor.getString(cursor.getColumnIndex("mobileNumber")));
                    model.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    model.setNearBy(cursor.getString(cursor.getColumnIndex("nearBy")));
                    model.setCityId(cursor.getString(cursor.getColumnIndex("cityId")));
                    model.setCity(cursor.getString(cursor.getColumnIndex("city")));
                    model.setStateId(cursor.getString(cursor.getColumnIndex("stateId")));
                    model.setState(cursor.getString(cursor.getColumnIndex("state")));
                    model.setPincode(cursor.getString(cursor.getColumnIndex("pincode")));

                    model.setOrderItemId(cursor.getString(cursor.getColumnIndex("orderItemId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeImageUrl(cursor.getString(cursor.getColumnIndex("recipeImageUrl")));
                    model.setPrice(cursor.getDouble(cursor.getColumnIndex("price")));
                    model.setQuantity(cursor.getInt(cursor.getColumnIndex("quantity")));

                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getParticularOrderDetail: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getParticularOrderDetail", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return model;
    }

    public ArrayList<placeOrderDetail> getOrderItemList(String orderId) {

        ArrayList<placeOrderDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * from "+placeOrder+" WHERE orderId = '"+orderId+"' ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    placeOrderDetail model = new placeOrderDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setOrderId(cursor.getString(cursor.getColumnIndex("orderId")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setUserMobileNumber(cursor.getString(cursor.getColumnIndex("userMobileNumber")));
                    model.setCancelReason(cursor.getString(cursor.getColumnIndex("cancelReason")));
                    model.setOrderStatus(cursor.getString(cursor.getColumnIndex("orderStatus")));
                    model.setOrderStep(cursor.getInt(cursor.getColumnIndex("orderStep")));
                    model.setRecipeCharges(cursor.getInt(cursor.getColumnIndex("recipeCharges")));
                    model.setPackagingCharges(cursor.getInt(cursor.getColumnIndex("packagingCharges")));
                    model.setTotal(cursor.getDouble(cursor.getColumnIndex("total")));
                    model.setItem(cursor.getInt(cursor.getColumnIndex("item")));
                    model.setOrderDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("orderDateTime")),"dd MMM yyyy hh:mm aa"));
                    model.setDeliveryDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("deliveryDateTime")),"dd MMM yyyy hh:mm aa"));

                    model.setAddressId(cursor.getString(cursor.getColumnIndex("addressId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setMobileNumber(cursor.getString(cursor.getColumnIndex("mobileNumber")));
                    model.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    model.setNearBy(cursor.getString(cursor.getColumnIndex("nearBy")));
                    model.setCityId(cursor.getString(cursor.getColumnIndex("cityId")));
                    model.setCity(cursor.getString(cursor.getColumnIndex("city")));
                    model.setStateId(cursor.getString(cursor.getColumnIndex("stateId")));
                    model.setState(cursor.getString(cursor.getColumnIndex("state")));
                    model.setPincode(cursor.getString(cursor.getColumnIndex("pincode")));

                    model.setOrderItemId(cursor.getString(cursor.getColumnIndex("orderItemId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeImageUrl(cursor.getString(cursor.getColumnIndex("recipeImageUrl")));
                    model.setPrice(cursor.getDouble(cursor.getColumnIndex("price")));
                    model.setQuantity(cursor.getInt(cursor.getColumnIndex("quantity")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getOrderItemList: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getOrderItemList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo allOrder
    public void addAllOrder(allOrderItemDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("orderId", model.getOrderId());
            cv.put("userId", model.getUserId());
            cv.put("userMobileNumber", model.getUserMobileNumber());
            cv.put("cancelReason", model.getCancelReason());
            cv.put("orderStatus", model.getOrderStatus());
            cv.put("orderStep", model.getOrderStep());
            cv.put("recipeCharges", model.getRecipeCharges());
            cv.put("packagingCharges", model.getPackagingCharges());
            cv.put("total", model.getTotal());
            cv.put("item", model.getItem());
            cv.put("orderDateTime", globalclass.dateToString(model.getOrderDateTime(),"dd MMM yyyy hh:mm aa"));
            cv.put("deliveryDateTime", globalclass.dateToString(model.getDeliveryDateTime(),"dd MMM yyyy hh:mm aa"));
            cv.put("addressId", model.getAddressId());
            cv.put("name", model.getName());
            cv.put("mobileNumber", model.getMobileNumber());
            cv.put("address", model.getAddress());
            cv.put("nearBy", model.getNearBy());
            cv.put("cityId", model.getCityId());
            cv.put("city", model.getCity());
            cv.put("stateId", model.getStateId());
            cv.put("state", model.getState());
            cv.put("pincode", model.getPincode());
            cv.put("orderItemId", model.getOrderItemId());
            cv.put("recipeId", model.getRecipeId());
            cv.put("recipeName", model.getRecipeName());
            cv.put("recipeImageId", model.getRecipeImageId());
            cv.put("recipeImageUrl", model.getRecipeImageUrl());
            cv.put("price", model.getPrice());
            cv.put("quantity", model.getQuantity());

            if(checkAllOrder(model) == 0) {
                sqldb.insert(allOrder, null, cv);
            }
            else {
                sqldb.update(allOrder, cv,   "orderId = ? AND recipeId = ?",
                        new String[]{model.getOrderId(),model.getRecipeId()});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addAllOrder: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addAllOrder", error);
        }
    }

    public int checkAllOrder(allOrderItemDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + allOrder + " WHERE orderId = '" + model.getOrderId() + "' " +
                    "AND recipeId = '"+model.getRecipeId()+"'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkAllOrder: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkAllOrder", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<allOrderItemDetail> getAllOrderList() {

        ArrayList<allOrderItemDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * from "+allOrder+"\n" +
                    "GROUP by\n" +
                    "orderId\n" +
                    "ORDER by orderDateTime DESC\n ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    allOrderItemDetail model = new allOrderItemDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setOrderId(cursor.getString(cursor.getColumnIndex("orderId")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setUserMobileNumber(cursor.getString(cursor.getColumnIndex("userMobileNumber")));
                    model.setCancelReason(cursor.getString(cursor.getColumnIndex("cancelReason")));
                    model.setOrderStatus(cursor.getString(cursor.getColumnIndex("orderStatus")));
                    model.setOrderStep(cursor.getInt(cursor.getColumnIndex("orderStep")));
                    model.setRecipeCharges(cursor.getInt(cursor.getColumnIndex("recipeCharges")));
                    model.setPackagingCharges(cursor.getInt(cursor.getColumnIndex("packagingCharges")));
                    model.setTotal(cursor.getDouble(cursor.getColumnIndex("total")));
                    model.setItem(cursor.getInt(cursor.getColumnIndex("item")));
                    model.setOrderDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("orderDateTime")),"dd MMM yyyy hh:mm aa"));
                    model.setDeliveryDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("deliveryDateTime")),"dd MMM yyyy hh:mm aa"));

                    model.setAddressId(cursor.getString(cursor.getColumnIndex("addressId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setMobileNumber(cursor.getString(cursor.getColumnIndex("mobileNumber")));
                    model.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    model.setNearBy(cursor.getString(cursor.getColumnIndex("nearBy")));
                    model.setCityId(cursor.getString(cursor.getColumnIndex("cityId")));
                    model.setCity(cursor.getString(cursor.getColumnIndex("city")));
                    model.setStateId(cursor.getString(cursor.getColumnIndex("stateId")));
                    model.setState(cursor.getString(cursor.getColumnIndex("state")));
                    model.setPincode(cursor.getString(cursor.getColumnIndex("pincode")));

                    model.setOrderItemId(cursor.getString(cursor.getColumnIndex("orderItemId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeImageUrl(cursor.getString(cursor.getColumnIndex("recipeImageUrl")));
                    model.setPrice(cursor.getDouble(cursor.getColumnIndex("price")));
                    model.setQuantity(cursor.getInt(cursor.getColumnIndex("quantity")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllOrderList: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getAllOrderList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public ArrayList<allOrderItemDetail> getAllPendingOrderListForHome() {

        ArrayList<allOrderItemDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * from "+allOrder+"\n" +
                    "where orderStep = 1\n" +
                    "GROUP by orderId\n" +
                    "ORDER by orderDateTime DESC\n" +
                    "LIMIT 10";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    allOrderItemDetail model = new allOrderItemDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setOrderId(cursor.getString(cursor.getColumnIndex("orderId")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setUserMobileNumber(cursor.getString(cursor.getColumnIndex("userMobileNumber")));
                    model.setCancelReason(cursor.getString(cursor.getColumnIndex("cancelReason")));
                    model.setOrderStatus(cursor.getString(cursor.getColumnIndex("orderStatus")));
                    model.setOrderStep(cursor.getInt(cursor.getColumnIndex("orderStep")));
                    model.setRecipeCharges(cursor.getInt(cursor.getColumnIndex("recipeCharges")));
                    model.setPackagingCharges(cursor.getInt(cursor.getColumnIndex("packagingCharges")));
                    model.setTotal(cursor.getDouble(cursor.getColumnIndex("total")));
                    model.setItem(cursor.getInt(cursor.getColumnIndex("item")));
                    model.setOrderDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("orderDateTime")),"dd MMM yyyy hh:mm aa"));
                    model.setDeliveryDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("deliveryDateTime")),"dd MMM yyyy hh:mm aa"));

                    model.setAddressId(cursor.getString(cursor.getColumnIndex("addressId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setMobileNumber(cursor.getString(cursor.getColumnIndex("mobileNumber")));
                    model.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    model.setNearBy(cursor.getString(cursor.getColumnIndex("nearBy")));
                    model.setCityId(cursor.getString(cursor.getColumnIndex("cityId")));
                    model.setCity(cursor.getString(cursor.getColumnIndex("city")));
                    model.setStateId(cursor.getString(cursor.getColumnIndex("stateId")));
                    model.setState(cursor.getString(cursor.getColumnIndex("state")));
                    model.setPincode(cursor.getString(cursor.getColumnIndex("pincode")));

                    model.setOrderItemId(cursor.getString(cursor.getColumnIndex("orderItemId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeImageUrl(cursor.getString(cursor.getColumnIndex("recipeImageUrl")));
                    model.setPrice(cursor.getDouble(cursor.getColumnIndex("price")));
                    model.setQuantity(cursor.getInt(cursor.getColumnIndex("quantity")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllPendingOrderListForHome: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getAllPendingOrderListForHome", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public ArrayList<allOrderItemDetail> getAllOrderListForHome() {

        ArrayList<allOrderItemDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * from "+allOrder+"\n" +
                    "GROUP by orderId\n" +
                    "ORDER by orderDateTime DESC\n" +
                    "LIMIT 10 ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    allOrderItemDetail model = new allOrderItemDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setOrderId(cursor.getString(cursor.getColumnIndex("orderId")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setUserMobileNumber(cursor.getString(cursor.getColumnIndex("userMobileNumber")));
                    model.setCancelReason(cursor.getString(cursor.getColumnIndex("cancelReason")));
                    model.setOrderStatus(cursor.getString(cursor.getColumnIndex("orderStatus")));
                    model.setOrderStep(cursor.getInt(cursor.getColumnIndex("orderStep")));
                    model.setRecipeCharges(cursor.getInt(cursor.getColumnIndex("recipeCharges")));
                    model.setPackagingCharges(cursor.getInt(cursor.getColumnIndex("packagingCharges")));
                    model.setTotal(cursor.getDouble(cursor.getColumnIndex("total")));
                    model.setItem(cursor.getInt(cursor.getColumnIndex("item")));
                    model.setOrderDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("orderDateTime")),"dd MMM yyyy hh:mm aa"));
                    model.setDeliveryDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("deliveryDateTime")),"dd MMM yyyy hh:mm aa"));

                    model.setAddressId(cursor.getString(cursor.getColumnIndex("addressId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setMobileNumber(cursor.getString(cursor.getColumnIndex("mobileNumber")));
                    model.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    model.setNearBy(cursor.getString(cursor.getColumnIndex("nearBy")));
                    model.setCityId(cursor.getString(cursor.getColumnIndex("cityId")));
                    model.setCity(cursor.getString(cursor.getColumnIndex("city")));
                    model.setStateId(cursor.getString(cursor.getColumnIndex("stateId")));
                    model.setState(cursor.getString(cursor.getColumnIndex("state")));
                    model.setPincode(cursor.getString(cursor.getColumnIndex("pincode")));

                    model.setOrderItemId(cursor.getString(cursor.getColumnIndex("orderItemId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeImageUrl(cursor.getString(cursor.getColumnIndex("recipeImageUrl")));
                    model.setPrice(cursor.getDouble(cursor.getColumnIndex("price")));
                    model.setQuantity(cursor.getInt(cursor.getColumnIndex("quantity")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllOrderListForHome: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getAllOrderListForHome", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public ArrayList<allOrderItemDetail> getAllOrderItemList(String orderId) {

        ArrayList<allOrderItemDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * from "+allOrder+" WHERE orderId = '"+orderId+"' ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    allOrderItemDetail model = new allOrderItemDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setOrderId(cursor.getString(cursor.getColumnIndex("orderId")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setUserMobileNumber(cursor.getString(cursor.getColumnIndex("userMobileNumber")));
                    model.setCancelReason(cursor.getString(cursor.getColumnIndex("cancelReason")));
                    model.setOrderStatus(cursor.getString(cursor.getColumnIndex("orderStatus")));
                    model.setOrderStep(cursor.getInt(cursor.getColumnIndex("orderStep")));
                    model.setRecipeCharges(cursor.getInt(cursor.getColumnIndex("recipeCharges")));
                    model.setPackagingCharges(cursor.getInt(cursor.getColumnIndex("packagingCharges")));
                    model.setTotal(cursor.getDouble(cursor.getColumnIndex("total")));
                    model.setItem(cursor.getInt(cursor.getColumnIndex("item")));
                    model.setOrderDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("orderDateTime")),"dd MMM yyyy hh:mm aa"));
                    model.setDeliveryDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("deliveryDateTime")),"dd MMM yyyy hh:mm aa"));

                    model.setAddressId(cursor.getString(cursor.getColumnIndex("addressId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setMobileNumber(cursor.getString(cursor.getColumnIndex("mobileNumber")));
                    model.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    model.setNearBy(cursor.getString(cursor.getColumnIndex("nearBy")));
                    model.setCityId(cursor.getString(cursor.getColumnIndex("cityId")));
                    model.setCity(cursor.getString(cursor.getColumnIndex("city")));
                    model.setStateId(cursor.getString(cursor.getColumnIndex("stateId")));
                    model.setState(cursor.getString(cursor.getColumnIndex("state")));
                    model.setPincode(cursor.getString(cursor.getColumnIndex("pincode")));

                    model.setOrderItemId(cursor.getString(cursor.getColumnIndex("orderItemId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeImageUrl(cursor.getString(cursor.getColumnIndex("recipeImageUrl")));
                    model.setPrice(cursor.getDouble(cursor.getColumnIndex("price")));
                    model.setQuantity(cursor.getInt(cursor.getColumnIndex("quantity")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllOrderItemList: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getAllOrderItemList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public allOrderItemDetail getAdminParticularOrderDetail(String orderId) {

        allOrderItemDetail model = null;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * from "+allOrder+" WHERE orderId = '"+orderId+"' LIMIT 1";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    model = new allOrderItemDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setOrderId(cursor.getString(cursor.getColumnIndex("orderId")));
                    model.setUserId(cursor.getString(cursor.getColumnIndex("userId")));
                    model.setUserMobileNumber(cursor.getString(cursor.getColumnIndex("userMobileNumber")));
                    model.setCancelReason(cursor.getString(cursor.getColumnIndex("cancelReason")));
                    model.setOrderStatus(cursor.getString(cursor.getColumnIndex("orderStatus")));
                    model.setOrderStep(cursor.getInt(cursor.getColumnIndex("orderStep")));
                    model.setRecipeCharges(cursor.getInt(cursor.getColumnIndex("recipeCharges")));
                    model.setPackagingCharges(cursor.getInt(cursor.getColumnIndex("packagingCharges")));
                    model.setTotal(cursor.getDouble(cursor.getColumnIndex("total")));
                    model.setItem(cursor.getInt(cursor.getColumnIndex("item")));
                    model.setOrderDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("orderDateTime")),"dd MMM yyyy hh:mm aa"));
                    model.setDeliveryDateTime(globalclass.stringToDate(cursor.getString(cursor.getColumnIndex("deliveryDateTime")),"dd MMM yyyy hh:mm aa"));

                    model.setAddressId(cursor.getString(cursor.getColumnIndex("addressId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setMobileNumber(cursor.getString(cursor.getColumnIndex("mobileNumber")));
                    model.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    model.setNearBy(cursor.getString(cursor.getColumnIndex("nearBy")));
                    model.setCityId(cursor.getString(cursor.getColumnIndex("cityId")));
                    model.setCity(cursor.getString(cursor.getColumnIndex("city")));
                    model.setStateId(cursor.getString(cursor.getColumnIndex("stateId")));
                    model.setState(cursor.getString(cursor.getColumnIndex("state")));
                    model.setPincode(cursor.getString(cursor.getColumnIndex("pincode")));

                    model.setOrderItemId(cursor.getString(cursor.getColumnIndex("orderItemId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeImageUrl(cursor.getString(cursor.getColumnIndex("recipeImageUrl")));
                    model.setPrice(cursor.getDouble(cursor.getColumnIndex("price")));
                    model.setQuantity(cursor.getInt(cursor.getColumnIndex("quantity")));

                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAdminParticularOrderDetail: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getAdminParticularOrderDetail", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return model;
    }

    //todo allRecipe
    public void addAllRecipe(recipeDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("adminId", globalclass.getStringData("adminId"));
            cv.put("adminName", globalclass.getStringData("adminName"));
            cv.put("recipeId", model.getRecipeId());
            cv.put("recipeName", model.getRecipeName());
            cv.put("additionalCharges", model.getAdditionalCharges());
            cv.put("price", model.getPrice());
            cv.put("categoryId", model.getCategoryId());
            cv.put("categoryName", model.getCategoryName());
            cv.put("recipeType", model.getRecipeType());
            cv.put("status", String.valueOf(model.getStatus()));

            if(checkRecipeIdExist(model) == 0) {
                sqldb.insert(allRecipe, null, cv);
            }
            else {
                sqldb.update(allRecipe, cv, "recipeId = ?", new String[]{String.valueOf(model.getRecipeId())});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addAllRecipe: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addAllRecipe", error);
        }
    }

    public int checkRecipeIdExist(recipeDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + allRecipe + " WHERE recipeId = '" + model.getRecipeId() + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkRecipeIdExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkRecipeIdExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public boolean checkRecipeExistInAllRecipe(String recipeName) {

        //todo checking recipeName already exist in recipe table

        Cursor cursor = null;
        try {
            String query = "SELECT * FROM " + allRecipe + " WHERE recipeName = '" + recipeName + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if(cursor.getCount() > 0) {
                return true;
            }
            else {
                return false;
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkRecipeExistInAllRecipe: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkRecipeExistInAllRecipe", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return false;
    }

    public ArrayList<recipeDetail> getAllRecipeList() {

        ArrayList<recipeDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + allRecipe + "";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    recipeDetail model = new recipeDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setCategoryId(cursor.getString(cursor.getColumnIndex("categoryId")));
                    model.setCategoryName(cursor.getString(cursor.getColumnIndex("categoryName")));
                    model.setRecipeType(cursor.getString(cursor.getColumnIndex("recipeType")));
                    model.setAdditionalCharges(cursor.getInt(cursor.getColumnIndex("additionalCharges")));
                    model.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
                    model.setStatus(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("status"))));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getAllRecipeList: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getAllRecipeList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public recipeDetail getParticularRecipeDetailFromAllRecipe(String recipeId) {
        recipeDetail model = null;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + allRecipe + " WHERE recipeId = '" + recipeId + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.moveToFirst()) {
                do {
                    model = new recipeDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setRecipeName(cursor.getString(cursor.getColumnIndex("recipeName")));
                    model.setCategoryId(cursor.getString(cursor.getColumnIndex("categoryId")));
                    model.setCategoryName(cursor.getString(cursor.getColumnIndex("categoryName")));
                    model.setRecipeType(cursor.getString(cursor.getColumnIndex("recipeType")));
                    model.setAdditionalCharges(cursor.getInt(cursor.getColumnIndex("additionalCharges")));
                    model.setPrice(cursor.getInt(cursor.getColumnIndex("price")));
                    model.setStatus(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("status"))));

                }
                while (cursor.moveToNext());
            }


        } catch (Exception e) {

            model = null;
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getParticularRecipeDetailFromAllRecipe: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getParticularRecipeDetailFromAllRecipe", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return model;
    }

    //todo allIngredients
    public void addAllIngredients(ingredientsDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("ingredientId", model.getIngredientId());
            cv.put("inventoryId", model.getInventoryId());
            cv.put("recipeId", model.getRecipeId());
            cv.put("recipeName", model.getRecipeName());
            cv.put("name", model.getName());
            cv.put("unitId", model.getUnitId());
            cv.put("unit", model.getUnit());
            cv.put("quantity", model.getQuantity());
            cv.put("price", model.getPrice());

            if(checkAllIngredientIdExist(model) == 0) {
                sqldb.insert(allIngredients, null, cv);
            }
            else {
                sqldb.update(allIngredients, cv,   "ingredientId = ?", new String[]{model.getIngredientId()});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addAllIngredients: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addAllIngredients", error);
        }
    }

    public int checkAllIngredientIdExist(ingredientsDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + allIngredients + " WHERE ingredientId = '" + model.getIngredientId() + "' ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkAllIngredientIdExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkAllIngredientIdExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<ingredientsDetail> getParticularRecipeAllIngredientsList(String recipeId) {

        ArrayList<ingredientsDetail> arrayList = new ArrayList<>();
        String query = "";
        Cursor cursor = null;
        try {
            query = "select * from "+ allIngredients +"\n" +
                    "WHERE recipeId = '" + recipeId + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.moveToFirst()) {
                do {
                    ingredientsDetail model = new ingredientsDetail();
                    model.setIngredientId(cursor.getString(cursor.getColumnIndex("ingredientId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setInventoryId(cursor.getString(cursor.getColumnIndex("inventoryId")));
                    model.setName(cursor.getString(cursor.getColumnIndex("name")));
                    model.setUnitId(cursor.getString(cursor.getColumnIndex("unitId")));
                    model.setUnit(cursor.getString(cursor.getColumnIndex("unit")));
                    model.setQuantity(Double.parseDouble(cursor.getString(cursor.getColumnIndex("quantity"))));
                    model.setPrice(Double.parseDouble(cursor.getString(cursor.getColumnIndex("price"))));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getParticularRecipeAllIngredientsList: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getParticularRecipeAllIngredientsList", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo allRecipeImages
    public void addAllRecipeImages(recipePhotoDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("recipeImageId", model.getRecipeImageId());
            cv.put("recipeId", model.getRecipeId());
            cv.put("imageName", model.getImageName());
            cv.put("url", model.getUrl());
            cv.put("firstImage", String.valueOf(model.getFirstImage()));

            if (checkAllRecipeImageIdExist(model) == 0) {
                sqldb.insert(allRecipeImages, null, cv);
            } else {
                sqldb.update(allRecipeImages, cv, "recipeImageId = ?", new String[]{String.valueOf(model.getRecipeImageId())});
            }
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addAllRecipeImages: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addAllRecipeImages", error);
        }
    }

    public int checkAllRecipeImageIdExist(recipePhotoDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + allRecipeImages + " WHERE recipeImageId = '" + model.getRecipeImageId() + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkAllRecipeImageIdExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkAllRecipeImageIdExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<recipePhotoDetail> getParticularAllRecipeImages(String recipeId) {

        ArrayList<recipePhotoDetail> arrayList = new ArrayList<>();
        String query = "";
        Cursor cursor = null;
        try {
            query = "select * from "+ allRecipeImages +"\n" +
                    "WHERE recipeId = '" + recipeId + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.moveToFirst()) {
                do {
                    recipePhotoDetail model = new recipePhotoDetail();
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setImageName(cursor.getString(cursor.getColumnIndex("imageName")));
                    model.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    model.setFirstImage(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("firstImage"))));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getParticularAllRecipeImages: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getParticularAllRecipeImages", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public ArrayList<recipePhotoDetail> getFirstAllRecipeImages(String recipeId) {

        ArrayList<recipePhotoDetail> arrayList = new ArrayList<>();
        String query = "";
        Cursor cursor = null;
        try {
            query = "select * from "+ allRecipeImages +"\n" +
                    "WHERE recipeId = '" + recipeId + "' AND firstImage = 'true'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.moveToFirst()) {
                do {
                    recipePhotoDetail model = new recipePhotoDetail();
                    model.setRecipeImageId(cursor.getString(cursor.getColumnIndex("recipeImageId")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setImageName(cursor.getString(cursor.getColumnIndex("imageName")));
                    model.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                    model.setFirstImage(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("firstImage"))));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getFirstAllRecipeImages: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getFirstAllRecipeImages", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo allRecipeInstructions
    public void addAllRecipeInstructions(recipeInstructionDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("recipeId", model.getRecipeId());
            cv.put("instruction", model.getInstruction());
            cv.put("stepNumber", model.getStepNumber());

            if (checkAllRecipeInstructionExist(model) == 0) {
                sqldb.insert(allRecipeInstructions, null, cv);
            } else {
                sqldb.update(allRecipeInstructions, cv, "localid = ? AND recipeImageId = ?", new String[]{model.getLocalid(),model.getRecipeId()});
            }
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addAllRecipeInstructions: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addAllRecipeInstructions", error);
        }
    }

    public int checkAllRecipeInstructionExist(recipeInstructionDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + allRecipeInstructions + " WHERE localid = '" + model.getLocalid() + "' AND " +
                    "recipeId = '"+model.getRecipeId()+"'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkAllRecipeInstructionExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkAllRecipeInstructionExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<recipeInstructionDetail> getParticularRecipeInstructions(String recipeId) {

        ArrayList<recipeInstructionDetail> arrayList = new ArrayList<>();
        String query = "";
        Cursor cursor = null;
        try {
            query = "select * from "+ allRecipeInstructions +"\n" +
                    "WHERE recipeId = '" + recipeId + "'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            if (cursor.moveToFirst()) {
                do {
                    recipeInstructionDetail model = new recipeInstructionDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setInstruction(cursor.getString(cursor.getColumnIndex("instruction")));
                    model.setStepNumber(cursor.getInt(cursor.getColumnIndex("stepNumber")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getParticularRecipeInstructions: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getParticularRecipeInstructions", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo recipeInstructions
    public void addRecipeInstructions(recipeInstructionDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("recipeId", model.getRecipeId());
            cv.put("instruction", model.getInstruction());
            cv.put("stepNumber", model.getStepNumber());

            if (checkRecipeInstructionExist(model) == 0) {
                sqldb.insert(recipeInstruction, null, cv);
            } else {
                sqldb.update(recipeInstruction, cv, "recipeId = ? AND stepNumber = ?",
                        new String[]{model.getRecipeId(), String.valueOf(model.getStepNumber())});
            }
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addRecipeInstructions: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addRecipeInstructions", error);
        }
    }

    public int checkRecipeInstructionExist(recipeInstructionDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + recipeInstruction + " WHERE " +
                    "recipeId = '"+model.getRecipeId()+"' AND stepNumber = "+model.getStepNumber()+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkRecipeInstructionExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkRecipeInstructionExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<recipeInstructionDetail> getRecipeInstruction(String recipeId) {

        ArrayList<recipeInstructionDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + recipeInstruction + " WHERE " +
            "recipeId = '"+recipeId+"'";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    recipeInstructionDetail model = new recipeInstructionDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setRecipeId(cursor.getString(cursor.getColumnIndex("recipeId")));
                    model.setStepNumber(cursor.getInt(cursor.getColumnIndex("stepNumber")));
                    model.setInstruction(cursor.getString(cursor.getColumnIndex("instruction")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getRecipeInstruction: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getRecipeInstruction", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo orderStatus
    public void addOrderStatus(orderStatusDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("orderStatus", model.getOrderStatus());
            cv.put("stepNumber", model.getStepNumber());

            sqldb.insert(orderStatus, null, cv);

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addOrderStatus: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addOrderStatus", error);
        }
    }

    public ArrayList<orderStatusDetail> getOrderStatusList() {

        ArrayList<orderStatusDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + orderStatus + " ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    orderStatusDetail model = new orderStatusDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setOrderStatus(cursor.getString(cursor.getColumnIndex("orderStatus")));
                    model.setStepNumber(cursor.getInt(cursor.getColumnIndex("stepNumber")));

                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getOrderStatusList: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getOrderStatusList", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    public orderStatusDetail getParticularOrderStatus(int stepNumber) {

        orderStatusDetail model = null;
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM " + orderStatus + " WHERE stepNumber = "+stepNumber+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    model = new orderStatusDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setOrderStatus(cursor.getString(cursor.getColumnIndex("orderStatus")));
                    model.setStepNumber(cursor.getInt(cursor.getColumnIndex("stepNumber")));
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getParticularOrderStatus: : " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getParticularOrderStatus", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return model;
    }

    //todo navItem
    public void addNavMenu(navMenuDetail model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("navMenuId", model.getNavMenuId());
            cv.put("accessStatus", String.valueOf(model.getAccessStatus()));

            if(checkNavMenuExist(model) == 0) {
                sqldb.insert(navMenu, null, cv);
            }
            else {
                sqldb.update(navMenu, cv,   "navMenuId = ?", new String[]{model.getNavMenuId()});
            }

        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addNavMenu: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "addNavMenu", error);
        }
    }

    public int checkNavMenuExist(navMenuDetail model) {
        int count = 0;
        String query = "";
        Cursor cursor = null;
        try {
            query = "SELECT * FROM " + navMenu + " WHERE navMenuId = '" + model.getNavMenuId() + "' COLLATE NOCASE ";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(query, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            count = cursor.getCount();
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "checkNavMenuExist: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "checkNavMenuExist", error);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public ArrayList<navMenuDetail> getNavMenu() {

        ArrayList<navMenuDetail> arrayList = new ArrayList<>();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * from "+navMenu+"";

            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);

            if (cursor instanceof SQLiteCursor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ((SQLiteCursor) cursor).setWindow(new CursorWindow(null, 1024 * 1024 * 10));
            }

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    navMenuDetail model = new navMenuDetail();
                    model.setLocalid(cursor.getString(cursor.getColumnIndex("localid")));
                    model.setNavMenuId(cursor.getString(cursor.getColumnIndex("navMenuId")));
                    model.setAccessStatus(Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex("accessStatus"))));

//                    globalclass.log(tag,"getNavMenu: " +
//                            "navMenuId:"+cursor.getString(cursor.getColumnIndex("navMenuId"))+
//                            ", accessStatus:"+cursor.getString(cursor.getColumnIndex("accessStatus"))
//                    );
                    arrayList.add(model);
                }
                while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "getNavMenu: " + error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "getNavMenu", error);
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return arrayList;
    }

    //todo log
    public void addLog(logModel model) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put("logType", model.getLogType());
            cv.put("userLogin", model.getUserLogin());
            cv.put("mobileNumber", model.getMobileNumber());
            cv.put("detail", model.getDetail());

            sqldb.insert(log, null, cv);

            globalclass.startService(context, sendLogService.class);

        } catch (Exception e) {

            String error = Log.getStackTraceString(e);
            globalclass.log(tag, "addLog: " + error);
            globalclass.sendErrorLog(tag, "addLog", error);
        }
    }

    //todo delete
    public void deleteData(String tablename, String columnname, String value) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();
            String query = "Delete from " + tablename + " where " + columnname + " = '" + value + "'";
            sqldb.execSQL(query);
            globalclass.log(tag + "_deleteData", query);
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag,"deleteData: "+ error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "deleteData", error);
        }
    }

    public void truncateTable(String tablename) {
        try {
            SQLiteDatabase sqldb = this.getWritableDatabase();
            String query = "Delete from " + tablename +"";
            sqldb.execSQL(query);
            globalclass.log(tag + "deleteTable", query);
        } catch (Exception e) {
            String error = Log.getStackTraceString(e);
            globalclass.log(tag + "truncateTable", error);
            globalclass.toast_long("Something went wrong, please try after sometime!");
            globalclass.sendErrorLog(tag, "truncateTable", error);
        }
    }

    public void clearDatabase() {
        SQLiteDatabase sqldb = this.getWritableDatabase();
        Cursor cursor = sqldb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        //noinspection TryFinallyCanBeTryWithResources not available with API < 19
        try {
            List<String> tables = new ArrayList<>(cursor.getCount());

            while (cursor.moveToNext()) {
                tables.add(cursor.getString(0));
            }

            for (String table : tables) {
                if (table.startsWith("sqlite_") || table.equalsIgnoreCase("notification")) {
                    continue;
                }
                sqldb.execSQL("DROP TABLE IF EXISTS " + table);
                globalclass.log(tag + "_cleardatabase", "Dropped table " + table);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            onCreate(sqldb);
        }
    }
}
